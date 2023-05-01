package tourGuide.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
	private static Logger rootLogger = LogManager.getRootLogger();

	// proximity in miles
	private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtilService gpsUtilService;
	private final RewardCentral rewardsCentral;
	private final org.apache.logging.log4j.Logger logger = LogManager.getLogger("testPerformance");

	public final ExecutorService rewardsExecutorService = Executors.newFixedThreadPool(1000);

	public RewardsService(GpsUtilService gpsUtilService, RewardCentral rewardCentral) {
		this.gpsUtilService = gpsUtilService;
		this.rewardsCentral = rewardCentral;
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public void calculateReward(User user) {
		rewardsExecutorService.submit(() -> calculateRewards(user));
	}

	public void calculateRewards(User user) {
		logger.debug("\033[35m - calculateRewards({}) ", user.getUserName());
		CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
		List<Attraction> attractions = gpsUtilService.getAttractions();

		for (VisitedLocation visitedLocation : userLocations) {
			for (Attraction attraction : attractions) {
				if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if (nearAttraction(visitedLocation, attraction)) {
						logger.debug("\033[35m - setUserRewards({},{},{}) ", attraction,visitedLocation,user.getUserName());
						setUserRewards(attraction, visitedLocation, user);
					}
				}
			}
		}
	}

	private void setUserRewards(Attraction attraction, VisitedLocation visitedLocation, User user) {
		CompletableFuture.supplyAsync(() -> {
			logger.debug("\033[35m - getRewardPoints({}, {}) ",  attraction, user.getUserName());
			return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
		}, rewardsExecutorService).thenAccept(rewardsPoint -> {
			logger.debug("\033[35m - addUserReward({}, {}, {}) ",  visitedLocation, attraction, rewardsPoint);
			user.addUserReward(new UserReward(visitedLocation, attraction, rewardsPoint));
		});
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return (getDistance(attraction, visitedLocation.location) >= proximityBuffer ? false : true);
	}

	// private int getRewardPoints(Attraction attraction, User user) {

	// 	return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	// }

	public double getDistance(Location loc1, Location loc2) {
		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);
		return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}

}
