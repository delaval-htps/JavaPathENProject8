package tourGuide.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;         
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import tourGuide.user.User;

@Service
public class GpsUtilService {

  private GpsUtil gpsUtil;
  
  private Logger logger = LogManager.getLogger("testPerformance");

  public final ExecutorService gpsExecutorService = Executors.newFixedThreadPool(6006);

  public GpsUtilService(GpsUtil gpsUtil) {
    this.gpsUtil = gpsUtil;
  }

  public void getLocation(User user, TourGuideService tourGuideService) {

    CompletableFuture.supplyAsync(() -> {
      logger.debug("\033[33m - gpstUtils.getUserLocation({})", user.getUserName());
      return gpsUtil.getUserLocation(user.getUserId());
    }, gpsExecutorService).thenAccept(location -> {
      logger.debug("\033[33m - tourGuideService.saveTrackedUserLocation({},{})", user.getUserName(), location);
      tourGuideService.saveTrackedUserLocation(user, location);
    });

  }

  public List<Attraction> getAttractions() {
    return gpsUtil.getAttractions();
  }

}
