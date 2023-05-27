package tourGuide.tracker;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private HashMap<User, Boolean> trackingUsersProgress = new HashMap<>();

	private final TourGuideService tourGuideService;
	private boolean stop = false;
	private boolean finishedTrackingProgress;
	public AtomicBoolean SLEEPINGTRACKER = new AtomicBoolean();

	/**
	 * @return the finishedTrackingProgress
	 */
	public boolean isFinishedTrackingProgress() {
		return finishedTrackingProgress;
	}

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

		SLEEPINGTRACKER.set(false);

		StopWatch stopWatch = new StopWatch();
		while (true) {
			if (Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}

			List<User> users = tourGuideService.getAllUsers();

			for (User user : users) {
				trackingUsersProgress.put(user, false);
			}

			rootLogger.info("Begin Tracker. Tracking {} users. ", users.size());

			stopWatch.start();

			users.forEach(u -> {
				logger.debug("\033[31m - tourGuideService.trackUserLocation ({})", u.getUserName());
				tourGuideService.trackUserLocation(u);
			});

			finishedTrackingProgress = false;

			while (!finishedTrackingProgress) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException e) {
					logger.debug("\033[31m -tracker sleeping InterruptedException");
				}

				if (!trackingUsersProgress.containsValue(false)) {
					finishedTrackingProgress = true;
				}
			}
			stopWatch.stop();
			trackingUsersProgress.clear();
			rootLogger.info("Tracker Time Elapsed: {} seconds", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

			stopWatch.reset();

			try {
				SLEEPINGTRACKER.set(true);
				rootLogger.info("Tracker sleeping");
				TimeUnit.SECONDS.sleep(TRACKINGPOLLINGINTERVAL);
			
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

	}
	
	public synchronized void updateUserTrackingProgress(User user) {
		this.trackingUsersProgress.put(user, true);
	}

	
}
