package tourGuide.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

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
import tourGuide.user.UserReward;
import tripPricer.TripPricer;

public class TestRewardsService {
  
  @BeforeAll
	public static void setup() {
		System.setProperty("path", "TestService/TestRewardsService");
	}

	@AfterEach
	public void finishedTest() {
		System.clearProperty("logFileName");
	}

	@AfterAll
	public static void finalStep() {
		System.clearProperty("path");
	}

  private Gson gson = new Gson();

  @Test
  public void userGetRewards() {

    InternalTestHelper.setInternalUserNumber(0);

    System.setProperty("logFileName", "userGetRewards-" + InternalTestHelper.getInternalUserNumber());
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    ctx.reconfigure();
    Logger rootLogger = LogManager.getRootLogger();

    GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
    RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
    TripPricerService tripPricerService = new TripPricerService(new TripPricer());

    rootLogger.info("----------------------Test : userGetRewards with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

    TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService);

    User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

    Attraction attraction = gpsUtilService.getAttractions().get(0);

    user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

    tourGuideService.trackUserLocation(user);

    Awaitility.await().until(() -> !user.getUserRewards().isEmpty());

    List<UserReward> userRewards = user.getUserRewards();

    tourGuideService.addShutDownHook();

    assertTrue(userRewards.size() >= 1);

    rootLogger.info("****************** user Rewards *******************");
    for (UserReward userReward : userRewards) {
      rootLogger.info("{}", gson.toJson(userReward));
    }

    System.clearProperty("logFileName");
  }

  @Test
  public void isWithinAttractionProximity() {

    System.setProperty("logFileName", "isWithinAttractionProximity");
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    ctx.reconfigure();
    Logger rootLogger = LogManager.getRootLogger();


    GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
    RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());

    rootLogger.info("---------------------- Test : isWithinAttractionProximity -----------------------");

    Attraction attraction = gpsUtilService.getAttractions().get(0);

    assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));

    System.clearProperty("logFileName");

  }


  @Test
  public void nearAllAttractions() {

    InternalTestHelper.setInternalUserNumber(1);

    System.setProperty("logFileName", "nearAllAttractions-" + InternalTestHelper.getInternalUserNumber());
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    ctx.reconfigure();
    Logger rootLogger = LogManager.getRootLogger();

    GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
    RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
    TripPricerService tripPricerService = new TripPricerService(new TripPricer());

    rootLogger.info("----------------------Test :  nearAllAttractions with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

    // Allow to have all attraction near of user's location
    rewardsService.setProximityBuffer(Integer.MAX_VALUE);

    TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService);

    User uniqUser = tourGuideService.getAllUsers().get(0);

    // clear all visitedLocations defined by initialisation for just have one location
    uniqUser.clearVisitedLocations();

    // wait for tracker is finished and plus 1100 millisecond to be sur that all 26 rewards for all
    // attraction are added to user
    Awaitility.await().during(1100, TimeUnit.MILLISECONDS).untilTrue(tourGuideService.tracker.SLEEPINGTRACKER);

    List<UserReward> userRewards = tourGuideService.getUserRewards(uniqUser);

    tourGuideService.addShutDownHook();

    assertEquals(gpsUtilService.getAttractions().size(), userRewards.size());

    System.clearProperty("logFileName");

  }
}
