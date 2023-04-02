package tourGuide.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import tourGuide.user.User;

@Service
public class GpsUtilService {

  private GpsUtil gpsUtil;

  private Logger logger = LoggerFactory.getLogger(GpsUtilService.class);

  // public final ExecutorService gpsExecutorService =
  // Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * (1
  // + 10110));
  public final ExecutorService gpsExecutorService = Executors.newFixedThreadPool(10000);

  public GpsUtilService(GpsUtil gpsUtil) {
    this.gpsUtil = gpsUtil;
  }

  public GpsUtilService() {
  }

  public void getLocation(User user, UserService userService, RewardsService rewardsService) {

    // CompletableFuture.supplyAsync(() -> {

    // logger.debug("\u001B[35m thread gpsUtilService.getLocation():{}",
    // Thread.currentThread().getName());
    // return gpsUtil.getUserLocation(user.getUserId());

    // }, gpsExecutorService).thenAccept(visitedLocation -> {
    // logger.debug("\u001B[32m userService.addToUSerLocation():{}",
    // Thread.currentThread().getName());
    // userService.addToUserLocationAndReward(user, visitedLocation,
    // rewardsService);

    // }).join();

    Runnable runnable = () -> {
      logger.debug("\u001B[35m thread gpsUtilService.getLocation():{}", Thread.currentThread().getName());
      VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
      logger.debug("\u001B[32m userService.addToUSerLocation():{}", Thread.currentThread().getName());
      userService.addToUserLocationAndReward(user, visitedLocation, rewardsService);
    };
    gpsExecutorService.execute(runnable);

  }

  public List<Attraction> getAttractions() {
    return gpsUtil.getAttractions();
  }

}
