package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gpsUtil.location.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class Tracker extends Thread {
	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(15);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final ExecutorService executorServiceTourGuide = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private final TourGuideService tourGuideService;
	private boolean stop = false;

	public Tracker(TourGuideService tourGuideService) {
		this.tourGuideService = tourGuideService;
		
		executorService.submit(this);
	}
	
	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	
	}
	
	@Override
	public void run() {
		
		StopWatch stopWatch = new StopWatch();
		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}
			
			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			
			// List<VisitedLocation> trackedLocationOfUsers = users.parallelStream().map(u -> {
			// 	logger.info("tourguideService .tarckeruser {} in thread {}", u.getUserId(),Thread.currentThread().getName());tourGuideService.trackUserLocation(u);				
			// 	return tourGuideService.trackUserLocation(u);
			// }).collect(Collectors.toList());
			
			users.forEach(u ->{
			CompletableFuture.runAsync(() -> {
			logger.info("\033[31m inside tracker: tourGuideService.trackUserLocation () {} in thread {}", u.getUserId(),
					Thread.currentThread().getName());
			tourGuideService.trackUserLocation(u);
				}, executorServiceTourGuide);
		});

			

			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}
		}
		
	}
}
