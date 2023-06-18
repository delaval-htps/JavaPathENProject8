package tourGuide.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.money.Monetary;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.awaitility.Awaitility;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.exception.UserPreferencesException;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;
import tripPricer.TripPricer;

public class TestTourGuideService {

	private Gson gson = new Gson();

	@BeforeAll
	public static void setup() {
		System.setProperty("path", "TestService/TestTourGuideService");
	}

	@AfterEach
	public void finishedTest() {
		System.clearProperty("logFileName");
	}

	@AfterAll
	public static void finalStep() {
		System.clearProperty("path");
	}

	@Test
	public void getUserLocation() throws InterruptedException {

		InternalTestHelper.setInternalUserNumber(0);

		System.setProperty("logFileName", "getUserLocation");
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		rootLogger.info("---------------------- Test :  getUserLocation -----------------------");

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user);

		// while (user.getLastVisitedLocation() == null) {
		// TimeUnit.MILLISECONDS.sleep(100);
		// }

		Awaitility.await().until(() -> user.getLastVisitedLocation() != null);

		tourGuideService.addShutDownHook();

		assertTrue(tourGuideService.getUserLocation(user).userId.equals(user.getUserId()));

	}

	@Test
	public void addUser() {

		InternalTestHelper.setInternalUserNumber(0);

		System.setProperty("logFileName", "addUser");
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		rootLogger.info("---------------------- Test :  addUser -----------------------");

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		// tourGuideService.tracker.stopTracking();
		tourGuideService.addShutDownHook();

		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);

	}


	@Test
	public void getAllUsers() {

		InternalTestHelper.setInternalUserNumber(0);

		System.setProperty("logFileName", "getAllUsers");
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		rootLogger.info("---------------------- Test :  getAllUsers -----------------------");

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		List<User> allUsers = tourGuideService.getAllUsers();

		// tourGuideService.tracker.stopTracking();
		tourGuideService.addShutDownHook();

		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));

	}

	@Test
	public void trackUser() {

		InternalTestHelper.setInternalUserNumber(0);

		System.setProperty("logFileName", "trackUser");
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		rootLogger.info("---------------------- Test :  trackUser -----------------------");

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user);

		Awaitility.await().until(() -> !user.getVisitedLocations().isEmpty());

		// tourGuideService.tracker.stopTracking();
		tourGuideService.addShutDownHook();

		assertEquals(user.getUserId(), tourGuideService.getUserLocation(user).userId);

	}

	@Test
	public void getNearbyAttractions() {

		InternalTestHelper.setInternalUserNumber(0);

		System.setProperty("logFileName", "getNearbyAttractions");
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		rootLogger.info("---------------------- Test :  getNearbyAttractions -----------------------");

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user);

		// we await that tracker is finish
		Awaitility.await().untilTrue(tourGuideService.tracker.SLEEPINGTRACKER);

		rootLogger.info("************* list of nearest attraction *************");
		rootLogger.info("waiting to collect data...");

		List<LinkedHashMap<String, String>> attractions = tourGuideService.getNearByAttractions(tourGuideService.getUserLocation(user));

		for (LinkedHashMap<String, String> attraction : attractions) {
			// rootLogger.info(" {} at {} miles", String.format("%-45s",attraction.attractionName),
			// String.format("%.2f", rewardsService.getDistance(attraction,
			// user.getLastVisitedLocation().location)));
			rootLogger.info("nearest attraction: {}", gson.toJson(attraction));
		}

		// tourGuideService.tracker.stopTracking();
		tourGuideService.addShutDownHook();

		assertEquals(5, attractions.size());

	}

	@Test
	public void getTripDeals() {

		InternalTestHelper.setInternalUserNumber(0);

		System.setProperty("logFileName", "getTripDeals-" + InternalTestHelper.getInternalUserNumber());
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		InternalTestHelper.setInternalUserNumber(0);
		rootLogger.info("----------------------Test :  getTripDeals with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = new ArrayList<>();

		providers = tourGuideService.getTripDeals(user);

		rootLogger.info("**********\ttripDeals with no preferences\t**********");
		for (Provider provider : providers) {
			rootLogger.info("providerName: {} - price: {}USD", String.format("%-50s", provider.name), provider.price);
		}

		UserPreferences jonPreference = new UserPreferences();
		jonPreference.setNumberOfAdults(10);
		jonPreference.setTripDuration(10);
		user.setUserPreferences(jonPreference);

		providers = tourGuideService.getTripDeals(user);
		rootLogger.info("**********\ttripDeals with preferences (1 adult and 10 nights\t**********");

		for (Provider provider : providers) {
			rootLogger.info("providerName: {} - price: {}USD", String.format("%-50s", provider.name), provider.price);
		}

		tourGuideService.addShutDownHook();

		assertEquals(5, providers.size());

	}

	@Test
	public void saveUserPreferences() {

		InternalTestHelper.setInternalUserNumber(0);

		System.setProperty("logFileName", "saveUserPreferences");
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		InternalTestHelper.setInternalUserNumber(0);
		rootLogger.info("----------------------Test : saveUserPreferences-----------------------");

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setAttractionProximity(1000);
		userPreferences.setCurrency(Monetary.getCurrency("USD"));
		userPreferences.setHighPricePoint(Money.of(100, Monetary.getCurrency("USD")));
		userPreferences.setLowerPricePoint(Money.of(0, Monetary.getCurrency("USD")));
		userPreferences.setNumberOfAdults(1);
		userPreferences.setNumberOfChildren(1);
		userPreferences.setTicketQuantity(1);
		userPreferences.setTripDuration(1);

		tourGuideService.saveUserPreferences(userPreferences, user);

		assertEquals(user.getUserPreferences(), userPreferences);

	}


	@Test
	public void saveUserPreferences_whenNullParams() {

		InternalTestHelper.setInternalUserNumber(1);

		System.setProperty("logFileName", "saveUserPreferences_whenNullParams");
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		rootLogger.info("----------------------Test : saveUserPreferences_whenNullParams-----------------------");

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService);

		User notNulluser = tourGuideService.getUser("internalUser0");
		UserPreferences nullUserPreferences = null;
		assertThrows(UserPreferencesException.class, () -> tourGuideService.saveUserPreferences(nullUserPreferences, notNulluser),
				"A problem occurs with setting user preferences");

		User nullUser = null;
		UserPreferences userPreferences = tourGuideService.getUser("internalUser0").getUserPreferences();
		assertThrows(UserPreferencesException.class, () -> tourGuideService.saveUserPreferences(userPreferences, nullUser), "A problem occurs with setting user preferences");
	}

}
