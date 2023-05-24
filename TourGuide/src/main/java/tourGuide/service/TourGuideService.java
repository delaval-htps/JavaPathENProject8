package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.exception.UserPreferencesException;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;

@Service
public class TourGuideService {

	private static Logger logger = LogManager.getLogger("testPerformance");
	private static Logger rootLogger = LogManager.getRootLogger();

	public final GpsUtilService gpsUtilService;
	private final RewardsService rewardsService;
	private final TripPricerService tripPricerService;
	public final Tracker tracker;

	public final ExecutorService tourGuideServiceExecutor = Executors.newFixedThreadPool(10000);

	boolean testMode = true;

	public TourGuideService(GpsUtilService gpsUtilService, RewardsService rewardsService, TripPricerService tripPricerService) {

		this.gpsUtilService = gpsUtilService;
		this.rewardsService = rewardsService;
		this.tripPricerService = tripPricerService;

		if (testMode) {
			rootLogger.info("TestMode enabled");
			rootLogger.info("Initializing users");
			initializeInternalUsers();
			rootLogger.info("Finished initializing users");
		}

		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
		return user.getLastVisitedLocation();
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	/**
	 * Save preferences for a user
	 * 
	 * @param userPreferences preferences of user
	 * @param user            user which we have to save preference
	 */
	public void saveUserPreferences(UserPreferences userPreferences, User user) {
		if (userPreferences != null && user != null) {
			user.setUserPreferences(userPreferences);
		} else {
			throw new UserPreferencesException("A problem occurs with setting user preferences");
		}
	}

	/**
	 * return the tripdeals for user
	 * @param user the user
	 * @return retunr a list of tripdeals for the user
	 */
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricerService.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(),
				cumulatativeRewardPoints);

		user.setTripDeals(providers);

		return user.getTripDeals();
	}

	/**
	 * Track the location of a user
	 * @param user user to be tracking
	 */
	public void trackUserLocation(User user) {
		try {
			tourGuideServiceExecutor.submit(() -> {
				logger.debug("\033[32m - trackUserLocation({})", user.getUserName());
				gpsUtilService.getLocation(user, this);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add the last Tracked location to user's list of visited Locations,
	 * Calulate the rewards for user visited location and add it ( point & informations) to his userRewards
	 * update the map UserTrackingProgress to informe the tracker of asynchronous progression of tracking 
	 * @param user the user to be tracking
	 * @param visitedLocation it's last visited location
	 */
	public void saveTrackedUserLocation(User user, VisitedLocation visitedLocation) {
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		tracker.updateUserTrackingProgress(user);
	}

	/**
	 * return the list of 5 nearest attractions from location of user
	 * @param visitedLocation the visited location of user
	 * @return the list of 5 nearest attractions from location of user
	 */
	public List<LinkedHashMap<String, String>> getNearByAttractions(VisitedLocation visitedLocation) {

		return gpsUtilService.getAttractions().stream()
				.map(attraction -> nearAttractionToMap(attraction, visitedLocation))
				.sorted(this::compareAttractionMapByDistance)
				.limit(5).collect(Collectors.toList());
	}

	/**
	 * custom method to return a hashMap representing an attraction near the last
	 * user's visitedLocation
	 * 
	 * @param attraction      the attraction to transform into a Map
	 * @param visitedLocation the last visited location of user
	 * @return a HashMap as asked for front end that collect information of
	 *         attraction : Name of Tourist attraction, Tourist attractions
	 *         lat/long, The user's location lat/long, The distance in miles between
	 *         attraction and the user's location, The reward points for visiting
	 *         this Attraction.
	 * 
	 */
	private LinkedHashMap<String, String> nearAttractionToMap(Attraction attraction, VisitedLocation visitedLocation) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("name", attraction.attractionName);
		map.put("touristAttraction lat/long", String.format("%f/%f", attraction.latitude, attraction.longitude));
		map.put("userLocation lat/long", String.format("%f/%f", visitedLocation.location.latitude, visitedLocation.location.longitude));
		map.put("distance", String.format("%f", rewardsService.getDistance(attraction, visitedLocation.location)));
		map.put("rewardPoints", String.format("%d", rewardsService.getNearestAttractionRewardPoints(attraction.attractionId, visitedLocation.userId)));

		return map;
	}

	/**
	 * custom comparator just to compare a list of attraction by distance from last
	 * user visitedLocation
	 * 
	 * @param h1 the hashmap that represents the first attraction
	 * @param h2 the hashmap that represents the second attraction
	 * @return a int to compare them By distance from user location
	 */
	private int compareAttractionMapByDistance(LinkedHashMap<String, String> h1, LinkedHashMap<String, String> h2) {
		if (h1.get("distance").equals(h2.get("distance"))) {
			return 0;
		} else {
			if (Double.valueOf(h1.get("distance")) > Double.valueOf(h2.get("distance"))) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	public List<VisitedLocation> getAllCurrentLocations() {
		return this.getAllUsers().parallelStream().map(u -> u.getLastVisitedLocation()).collect(Collectors.toList());
	}
	/**
	 * Method to shutdown all used executorService of application
	 */
	public void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				tracker.stopTracking();
				tourGuideServiceExecutor.shutdownNow();
				gpsUtilService.gpsExecutorService.shutdownNow();
				rewardsService.rewardsExecutorService.shutdownNow();

			}
		});
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final ConcurrentHashMap<String, User> internalUserMap = new ConcurrentHashMap<>();
	public static final int INITIAL_NUMBER_OF_VISITED_LOCATIONS = 4;

	private void initializeInternalUsers() {
		// Set the user locale to english to not have NumberFormatException with visited
		// locations
		Locale.setDefault(Locale.ENGLISH);
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		rootLogger.info("Created {}  internal test users.", InternalTestHelper.getInternalUserNumber());
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, (INITIAL_NUMBER_OF_VISITED_LOCATIONS - 1)).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
