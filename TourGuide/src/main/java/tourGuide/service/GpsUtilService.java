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

  public final ExecutorService gpsExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*(1+110));

  public GpsUtilService(GpsUtil gpsUtil) {
    this.gpsUtil = gpsUtil;
  }

  public CompletableFuture<VisitedLocation> getLocation(User user) {
    return CompletableFuture.supplyAsync(() -> {
      logger.info("\u001B[35m thread GPS.getLocation():{}", Thread.currentThread().getName());
      return gpsUtil.getUserLocation(user.getUserId());
    }, gpsExecutorService).thenApplyAsync(result -> {
      user.addToVisitedLocations(result);
      return result;
    }, gpsExecutorService);
  }

  public CompletableFuture<List<Attraction>> getAttractions() {
    return CompletableFuture.supplyAsync(() -> {
			return  gpsUtil.getAttractions();
		},gpsExecutorService);
  }
 
}
