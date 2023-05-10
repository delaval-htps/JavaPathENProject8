package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

public class TestTourGuideService {

	@Test
	public void getUserLocation() throws InterruptedException {

		InternalTestHelper.setInternalUserNumber(0);

		System.setProperty("logFileName", "getUserLocation-" + InternalTestHelper.getInternalUserNumber());

		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();

		Logger logger = LogManager.getLogger("testPerformance");
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtil gpsUtil = new GpsUtil();
		GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());

		rootLogger.info("----------------------Test :  getUserLocation with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user);

		while (user.getLastVisitedLocation() == null) {
			TimeUnit.MILLISECONDS.sleep(100);
		}

		tourGuideService.tracker.stopTracking();
		assertTrue(tourGuideService.getUserLocation(user).userId.equals(user.getUserId()));

		System.clearProperty("logFileName");
	}

	@Test
	public void addUser() {

		InternalTestHelper.setInternalUserNumber(0);

		System.setProperty("logFileName", "addUser-" + InternalTestHelper.getInternalUserNumber());

		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();

		Logger logger = LogManager.getLogger("testPerformance");
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtil gpsUtil = new GpsUtil();
		GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);

		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());

		rootLogger.info("----------------------Test :  addUser with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();

		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);

		System.clearProperty("logFileName");

	}

	@Test
	public void getAllUsers() {
		
		InternalTestHelper.setInternalUserNumber(0);
		System.setProperty("logFileName", "getAllUsers-" + InternalTestHelper.getInternalUserNumber());

		
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();

		Logger logger = LogManager.getLogger("testPerformance");
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtil gpsUtil = new GpsUtil();
		GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		
		rootLogger.info("----------------------Test :  getAllUsers with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();

		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));

		System.clearProperty("logFileName");

	}

	@Test
	public void trackUser() {
		
		InternalTestHelper.setInternalUserNumber(0);
		System.setProperty("logFileName", "trackUser-" + InternalTestHelper.getInternalUserNumber());
		
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();

		Logger logger = LogManager.getLogger("testPerformance");
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtil gpsUtil = new GpsUtil();
		GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		

		rootLogger.info("----------------------Test :  trackUser with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user);
		// TODO change sleep by modification of return of
		// tourGuideService.tackUserLocation()

		try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		tourGuideService.tracker.stopTracking();

		assertEquals(user.getUserId(), tourGuideService.getUserLocation(user).userId);

		System.clearProperty("logFileName");

	}

	@Ignore // Not yet implemented
	@Test
	public void getNearbyAttractions() {

		InternalTestHelper.setInternalUserNumber(0);
		System.setProperty("logFileName", "getNearbyAttractions-"+InternalTestHelper.getInternalUserNumber());
		
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();

		Logger logger = LogManager.getLogger("testPerformance");
		Logger rootLogger = LogManager.getRootLogger();


		GpsUtil gpsUtil = new GpsUtil();
		GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
	

		rootLogger.info("----------------------Test :  getNearbyAttractions with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user);

		List<Attraction> attractions = tourGuideService.getNearByAttractions(tourGuideService.getUserLocation(user));

		tourGuideService.tracker.stopTracking();

		assertEquals(5, attractions.size());
		System.clearProperty("logFileName");

	}

	public void getTripDeals() {

		InternalTestHelper.setInternalUserNumber(0);
		System.setProperty("logFileName", "getTripDeals-"+InternalTestHelper.getInternalUserNumber());
		
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();

		Logger logger = LogManager.getLogger("testPerformance");
		Logger rootLogger = LogManager.getRootLogger();
		GpsUtil gpsUtil = new GpsUtil();
		GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		rootLogger.info("----------------------Test :  getTripDeals with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);

		tourGuideService.tracker.stopTracking();

		assertEquals(10, providers.size());
		System.clearProperty("logFileName");

	}
}
