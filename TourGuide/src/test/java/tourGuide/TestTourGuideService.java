package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

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
import tourGuide.service.UserService;
import tourGuide.user.User;
import tripPricer.Provider;

public class TestTourGuideService {

	@Test
	public void getUserLocation() {
		UserService userService = new UserService();
		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user, userService, rewardsService);
		VisitedLocation visitedLocation = userService.getLastUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}

	@Test
	public void addUser() {
		UserService userService = new UserService();
		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();

		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}

	@Test
	public void getAllUsers() {
		UserService userService = new UserService();
		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();

		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}

	@Test
	public void trackUser() {
		UserService userService = new UserService();
		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user, userService, rewardsService);
		VisitedLocation visitedLocation = userService.getLastUserLocation(user);
		tourGuideService.tracker.stopTracking();

		assertEquals(user.getUserId(), visitedLocation.userId);
	}

	@Ignore // Not yet implemented
	@Test
	public void getNearbyAttractions() {
		UserService userService = new UserService();
		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user, userService, rewardsService);
		VisitedLocation visitedLocation = userService.getLastUserLocation(user);
		List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);

		tourGuideService.tracker.stopTracking();

		assertEquals(5, attractions.size());
	}

	public void getTripDeals() {
		UserService userService = new UserService();
		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);

		tourGuideService.tracker.stopTracking();

		assertEquals(10, providers.size());
	}

}
