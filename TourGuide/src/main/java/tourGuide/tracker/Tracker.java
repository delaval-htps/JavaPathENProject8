package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class Tracker extends Thread {
	private org.apache.logging.log4j.Logger logger = LogManager.getLogger("testPerformance");
	private static Logger rootLogger = LogManager.getRootLogger();

	private static final long TRACKINGPOLLINGINTERVAL = TimeUnit.MINUTES.toSeconds(5);

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
		while (true) {
			if (Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}

			List<User> users = tourGuideService.getAllUsers();

			rootLogger.info("Begin Tracker. Tracking {} users. ", users.size());

			stopWatch.start();

			// users.parallelStream().forEach(u -> {
			// logger.debug("\033[31m {}: tourGuideService.trackUserLocation ({})",
			// this.getClass().getCanonicalName(), u.getUserName());
			// tourGuideService.trackUserLocation(u);
			// tourGuideService.usersCountDownLatch.countDown();
			// });
			users.forEach(u -> {
				logger.debug("\033[31m - tourGuideService.trackUserLocation ({})", u.getUserName());
				tourGuideService.trackUserLocation(u);
				tourGuideService.usersCountDownLatch.countDown();
			} );

			stopWatch.stop();

			rootLogger.info("Tracker Time Elapsed: {} seconds", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

			stopWatch.reset();

			try {
				rootLogger.info("Tracker sleeping");
				TimeUnit.SECONDS.sleep(TRACKINGPOLLINGINTERVAL);
				// this.tourGuideService.usersCountDownLatch = new CountDownLatch(tourGuideService.getAllUsers().size());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

	}
}
