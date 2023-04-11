package tourGuide.tracker;

import java.util.HashMap;
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
	private final ExecutorService trackerService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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
		trackerService.shutdown();

	}

	@Override
	public void run() {

		StopWatch stopWatch = new StopWatch();
		while (true) {
			if (Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}
			
				List<User> users = tourGuideService.getAllUsers();

				logger.debug("Begin Tracker. Tracking " + users.size() + " users.");

				stopWatch.start();

				// users.parallelStream().map(u -> {
				// logger.debug("\033[31m inside tracker: tourGuideService.trackUserLocation (){} in thread {}", u.getUserName(), Thread.currentThread().getName());
			
				// return tourGuideService.trackUserLocation(u);
				// }).collect(Collectors.toList());

				users.parallelStream().forEach(u -> {
					logger.debug("\033[31m inside tracker: tourGuideService.trackUserLocation () {} in thread {}", u.getUserName(), Thread.currentThread().getName());
					tourGuideService.trackUserLocation(u);
					tourGuideService.usersCountDownLatch.countDown();
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
