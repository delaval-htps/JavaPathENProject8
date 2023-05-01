package tourGuide.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import tourGuide.user.User;

@Service
public class GpsUtilService {

  private GpsUtil gpsUtil;
  private org.apache.logging.log4j.Logger logger = LogManager.getLogger("testPerformance");
	private static Logger rootLogger = LogManager.getRootLogger();

  public final ExecutorService gpsExecutorService = Executors.newFixedThreadPool(6006);

  public GpsUtilService(GpsUtil gpsUtil) {
    this.gpsUtil = gpsUtil;
  }

  public void getLocation(User user, TourGuideService tourGuideService) {

     CompletableFuture.supplyAsync(() -> {
      logger.debug("\033[33m - gpstUtils.getUserLocation({})", user.getUserName());
      return gpsUtil.getUserLocation(user.getUserId());
    }, gpsExecutorService).thenAccept(location ->{
      logger.debug("\033[33m - tourGuideService.saveTrackedUserLocation({},{})",  user.getUserName(), location);
       tourGuideService.saveTrackedUserLocation(user, location);
     });

    // getUserLocationSupplyAsync.whenComplete((result, ex) -> {
    //   if (ex != null) {
    //     logger.error("getUSerLocation failed", ex);
    //   } else {
      
    //   }
    // });

    // getUserLocationthenAcceptAsync.whenComplete((result, ex) -> {
    //   if (ex != null) {
    //     logger.error("getUSerLocation failed", ex);
    //   } else {
    //     logger.debug("\033[35m {}: tourGuideService.saveTrackedUserLocation({},{})", this.getClass().getCanonicalName(), user.getUserName(), result);
    //   }
    // });
  }

  public List<Attraction> getAttractions() {
    // return CompletableFuture.supplyAsync(() -> {
    //   return gpsUtil.getAttractions();
    // }, gpsExecutorService).join();
    return gpsUtil.getAttractions();
  }

  public void stopGpsExecutorService() {
    gpsExecutorService.shutdown();
    try {
      gpsExecutorService.awaitTermination(10, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    
    }
  }

}
