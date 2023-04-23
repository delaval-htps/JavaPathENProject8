package tourGuide.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtilService gpsUtilService;
	private final RewardCentral rewardsCentral;
	private final Logger logger = LoggerFactory.getLogger(RewardsService.class);

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

	public void calculateRewards(User user) {
		logger.info("\033[37m {}:  \t calculateRewards({}) ", this.getClass().getCanonicalName(), user.getUserName());
		CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
		List<Attraction> attractions = gpsUtilService.getAttractions();

		for (VisitedLocation visitedLocation : userLocations) {
			for (Attraction attraction : attractions) {
				if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if (nearAttraction(visitedLocation, attraction)) {
						logger.info("\033[35m {}:  \t setUserRewards({},{},{}) ", this.getClass().getCanonicalName(), attraction,visitedLocation,user.getUserName());
						setUserRewards(attraction, visitedLocation, user);
					}
				}
			}
		}
	}

	private void setUserRewards(Attraction attraction, VisitedLocation visitedLocation, User user) {
		CompletableFuture.supplyAsync(() -> {
			logger.info("\033[35m {}:  getRewardPoints({}, {}) ", this.getClass().getCanonicalName(), attraction, user.getUserName());
			return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
		}, rewardsExecutorService).thenAcceptAsync(rewardsPoint -> {
			logger.info("\033[35m {}:  {}.addUserReward({}, {}, {}) ", this.getClass().getCanonicalName(), user.getUserName(), visitedLocation, attraction, rewardsPoint);
			user.addUserReward(new UserReward(visitedLocation, attraction, rewardsPoint));
		}, rewardsExecutorService);
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
