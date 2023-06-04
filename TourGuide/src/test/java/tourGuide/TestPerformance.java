package tourGuide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;
import tourGuide.user.User;
import tripPricer.TripPricer;

public class TestPerformance {

	@BeforeAll
	public static void init() {
			System.setProperty("path", "TestPerformance");
	}

	@AfterEach
	public void finishedTest() {
			System.clearProperty("logFileName");
	}

	@AfterAll
	public static void finalStep() {
			System.clearProperty("path");
	}


	/*
	 * A note on performance improvements:
	 * 
	 * The number of users generated for the high volume tests can be easily
	 * adjusted via this method:
	 * 
	 * InternalTestHelper.setInternalUserNumber(100000);
	 * 
	 * 
	 * These tests can be modified to suit new solutions, just as long as the
	 * performance metrics at the end of the tests remains consistent.
	 * 
	 * These are performance metrics that we are trying to hit:
	 * 
	 * highVolumeTrackLocation: 100,000 users within 15 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 * highVolumeGetRewards: 100,000 users within 20 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	@Test
	public void highVolumeTrackLocation() {

		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		InternalTestHelper.setInternalUserNumber(1000);
		
		System.setProperty("logFileName", "highVolumeTrackLocation-" + InternalTestHelper.getInternalUserNumber());
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger logger = LogManager.getLogger("testPerformance");
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		rootLogger.info("----------------------HightVolumeTrackLocation with {} users-----------------------\t", InternalTestHelper.getInternalUserNumber());
		
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService,tripPricerService);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		List<User> allUsers = tourGuideService.getAllUsers();

		for (User user : allUsers) {
			logger.debug("\033[36m - tourGuideService.trackUserLocation({}) ", user.getUserName());
			tourGuideService.trackUserLocation(user);
		}
		for (User user : allUsers) {
			while (user.getVisitedLocations().size() < TourGuideService.INITIAL_NUMBER_OF_VISITED_LOCATIONS +1) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		stopWatch.stop();
		
		tourGuideService.addShutDownHook();

		rootLogger.info("highVolumeTrackLocation: Time Elapsed: {} seconds", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

	}

	@Test
	public void highVolumeGetRewards() {

		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes
		InternalTestHelper.setInternalUserNumber(1000);
		
		System.setProperty("logFileName", "highVolumeGetRewards-" + InternalTestHelper.getInternalUserNumber());
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger logger = LogManager.getLogger("testPerformance");
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		rootLogger.info("----------------------highVolumeGetRewards with {} users-----------------------", InternalTestHelper.getInternalUserNumber());
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService,tripPricerService);

		Attraction attraction = gpsUtilService.getAttractions().get(0);
		List<User> allUsers = tourGuideService.getAllUsers();

		allUsers.forEach(u -> {
			VisitedLocation firstAttraction = new VisitedLocation(u.getUserId(), attraction, new Date());
			logger.debug("\033[36m - addToVisitedLocations({}) to user: {} ",  attraction.attractionName, u.getUserName());
			u.getVisitedLocations().clear();
			u.addToVisitedLocations(firstAttraction);
		});
		
		for (User user : allUsers) {
			while (user.getUserRewards().isEmpty()) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}

		stopWatch.stop();

		tourGuideService.addShutDownHook();

		rootLogger.info("highVolumeGetRewards: Time Elapsed: {} seconds", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

	}

}
