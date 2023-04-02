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

  public CompletableFuture<Void> getLocation(User user) {
    return CompletableFuture.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()))
    .thenAccept(location -> {
     logger.info(" add location {} to user {} ", location,user.getUserId());
     user.addToVisitedLocations(location);
     logger.info("call calculateReward for user {} ", user.getUserId());
    //  rewardsService.calculateRewards(user);
        });
  }

  public CompletableFuture<List<Attraction>> getAttractions() {
    return CompletableFuture.supplyAsync(() -> {
			return  gpsUtil.getAttractions();
		},gpsExecutorService);
  }
 
}
