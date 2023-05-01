package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
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
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

public class TestRewardsService {
 

  @Test
  public void userGetRewards() {

    InternalTestHelper.setInternalUserNumber(0);

    System.setProperty("logFileName", "userGetRewards-" + InternalTestHelper.getInternalUserNumber());

    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    ctx.reconfigure();

    Logger logger = LogManager.getLogger("testPerformance");
    Logger rootLogger = LogManager.getRootLogger();

    GpsUtil gpsUtil = new GpsUtil();
    GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
    RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());

    rootLogger.info("----------------------Test : userGetRewards with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

    TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

    User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
    Attraction attraction = gpsUtil.getAttractions().get(0);
    user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
    tourGuideService.trackUserLocation(user);

    // TODO change sleep by modification of return of
    try {
      TimeUnit.MILLISECONDS.sleep(2000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    List<UserReward> userRewards = user.getUserRewards();
    tourGuideService.tracker.stopTracking();
    assertTrue(userRewards.size() >= 1);
    System.clearProperty("logFileName");
  }

  @Test
  public void isWithinAttractionProximity() {

    System.setProperty("logFileName", "isWithinAttractionProximity");

    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    ctx.reconfigure();

    Logger logger = LogManager.getLogger("testPerformance");
    Logger rootLogger = LogManager.getRootLogger();

    GpsUtil gpsUtil = new GpsUtil();
    GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
    RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
    rootLogger.info("---------------------- Test : isWithinAttractionProximity -----------------------");
    Attraction attraction = gpsUtil.getAttractions().get(0);
    assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    System.clearProperty("logFileName");

  }

  @Ignore // Needs fixed - can throw ConcurrentModificationException
  @Test
  public void nearAllAttractions() {

    InternalTestHelper.setInternalUserNumber(1);
    System.setProperty("logFileName", "nearAllAttractions-"+InternalTestHelper.getInternalUserNumber());

    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    ctx.reconfigure();

    Logger logger = LogManager.getLogger("testPerformance");
    Logger rootLogger = LogManager.getRootLogger();

    GpsUtil gpsUtil = new GpsUtil();
    GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
    RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
    rewardsService.setProximityBuffer(Integer.MAX_VALUE);

  
    rootLogger.info("----------------------Test :  nearAllAttractions with {} users-----------------------", InternalTestHelper.getInternalUserNumber());

    TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

    rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
    List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
    tourGuideService.tracker.stopTracking();

    assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
    System.clearProperty("logFileName");

  }
}
