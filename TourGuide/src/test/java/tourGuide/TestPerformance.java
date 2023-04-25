package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class TestPerformance {

	private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(TestPerformance.class);
	private final static int INITIAL_NUMBER_OF_VISITED_LOCATIONS = 4;
	

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
	public void highVolumeTrackLocation()  {
		
		GpsUtil gpsUtil = new GpsUtil();
		GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		InternalTestHelper.setInternalUserNumber(1000);
		logger.info("\t----------------------HightVolumeTrackLocation with {} users-----------------------\t",InternalTestHelper.getInternalUserNumber());
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);
	
		try {
			tourGuideService.usersCountDownLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		List<User> allUsers = tourGuideService.getAllUsers();
		// allUsers.forEach(u -> assertTrue(u.getVisitedLocations().size() == 4));

		for (User user : allUsers) {
			logger.debug("\033[36m {}: \t\t tourGuideService.trackUserLocation({}) ", this.getClass().getCanonicalName(), user.getUserName());
			tourGuideService.trackUserLocation(user);
		}
		for (User user : allUsers) {
			while (user.getVisitedLocations().size() < INITIAL_NUMBER_OF_VISITED_LOCATIONS) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		stopWatch.stop();
		// tourGuideService.tracker.stopTracking();
		 tourGuideService.addShutDownHook();

		logger.info("{} - highVolumeTrackLocation: Time Elapsed: {} seconds", this.getClass().getCanonicalName(), TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	
		
	}


	@Test
	public void highVolumeGetRewards() {

		GpsUtil gpsUtil = new GpsUtil();
		GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes
		InternalTestHelper.setInternalUserNumber(100);
		logger.info("----------------------highVolumeGetRewards with {} users-----------------------", InternalTestHelper.getInternalUserNumber());
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		for (User user : allUsers) {
			while (user.getVisitedLocations().size() < (INITIAL_NUMBER_OF_VISITED_LOCATIONS +1)) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		allUsers.forEach(u -> rewardsService.calculateRewards(u));

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		// tourGuideService.tracker.stopTracking();
		tourGuideService.addShutDownHook();
		logger.info("{} - highVolumeGetRewards: Time Elapsed: {} seconds", this.getClass().getCanonicalName(), TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
		
	}

}
