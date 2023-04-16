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

  public final ExecutorService gpsExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  public GpsUtilService(GpsUtil gpsUtil) {
    this.gpsUtil = gpsUtil;
  }

  public CompletableFuture<VisitedLocation> getLocation(User user, TourGuideService tourGuideService) {

    CompletableFuture<VisitedLocation> visitedLocationFuture =CompletableFuture.supplyAsync(() -> {
      logger.info("\033[37m {}: gpstUtils.getUserLocation({}) ", this.getClass().getCanonicalName(), user.getUserName());
      return gpsUtil.getUserLocation(user.getUserId());
    }, gpsExecutorService);

    visitedLocationFuture.thenAcceptAsync((location) -> {
      logger.info("\033[33m {}:  {}.addVisitedLocation({}})", this.getClass().getCanonicalName(), user.getUserName(), location);
      tourGuideService.saveTrackedUserLocation(user,location);
    }, gpsExecutorService);

    return visitedLocationFuture;
  }

  public CompletableFuture<List<Attraction>> getAttractions() {
    return CompletableFuture.supplyAsync(() -> {
      return gpsUtil.getAttractions();
    }, gpsExecutorService);
  }

  public void stopGpsExecutorService() {
    gpsExecutorService.shutdown();
  }

}
