package tourGuide;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import gpsUtil.location.VisitedLocation;
import tourGuide.dto.UserCurrentLocationDTO;
import tourGuide.dto.UserPreferencesDTO;
import tourGuide.dto.UserVisitedLocationDTO;
import tourGuide.exception.UserNotFoundException;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;

@RestController
public class TourGuideController {

    @Autowired
    private TourGuideService tourGuideService;

    @Autowired
    private ModelMapper modelMapper;

    private Gson gson = new Gson();

    @RequestMapping("/")
    public ResponseEntity<String> index() {
        return new ResponseEntity<>("Greetings from TourGuide!", HttpStatus.OK);
    }

    /**
     * endpoint to return the last location visited by a user
     * 
     * @param userName the name of user
     * @return the only location ( ie longitude and latitude) of last visitedLocation of user
     */
    @RequestMapping("/getLocation")
    public ResponseEntity<String> getLocation(@RequestParam String userName) {

        User user = tourGuideService.getUser(userName);

        if (user != null) {
            VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
            return new ResponseEntity<>(gson.toJson(visitedLocation.location), HttpStatus.OK);
        } else {
            throw new UserNotFoundException("User with name: " + userName + " doesnt exist!");
        }

    }

    /**
     * endpoint to return the closest five tourist attractions to the user
     * 
     * @param userName the name of user
     * @return the closest five tourist attractions to the user nearest of his current location sorted
     *         by distance
     */
    @RequestMapping("/getNearbyAttractions")
    public ResponseEntity<String> getNearbyAttractions(@RequestParam String userName) {
        // Instead: Get the closest five tourist attractions to the user - no matter how
        // far away they are.
        // Return a new JSON object that contains:
        // Name of Tourist attraction,
        // Tourist attractions lat/long,
        // The user's location lat/long,
        // The distance in miles between the user's location and each of the
        // attractions.
        // The reward points for visiting each Attraction.
        // Note: Attraction reward points can be gathered from RewardsCentral

        User user = tourGuideService.getUser(userName);

        if (user != null) {
            VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
            String result = gson.toJson(tourGuideService.getNearByAttractions(visitedLocation));
            return ResponseEntity.ok(result);
        } else {
            throw new UserNotFoundException("User with name: " + userName + " doesnt exist!");
        }
    }

    /**
     * endpoint to return the Rewards of a user
     * 
     * @param userName the name of user
     * @return the list of rewards of the selected user
     */
    @RequestMapping("/getRewards")
    public ResponseEntity<Object> getRewards(@RequestParam String userName) {

        User user = tourGuideService.getUser(userName);

        if (user != null) {
            List<UserReward> userRewards = tourGuideService.getUserRewards(getUser(userName));

            if (!userRewards.isEmpty()) {
                return new ResponseEntity<>(gson.toJson(userRewards), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("The user has not yet received any rewards!", HttpStatus.OK);
            }

        } else {
            throw new UserNotFoundException("User with name: " + userName + " doesnt exist!");
        }

    }

    /**
     * endpoint to retrieve all last visited location for all user
     * 
     * @return a list of UserCurrentLocationDTO that represente le last visited location of a user with
     *         only userid and location as field
     */
    @RequestMapping("/getAllCurrentLocations")
    public ResponseEntity<Object> getAllCurrentLocations() {
        // Get a list of every user's most recent location as JSON
        // - Note: does not use gpsUtil to query for their current location,
        // but rather gathers the user's current location from their stored location
        // history.

        // Return object should be the just a JSON mapping of userId to Locations
        // similar to:
        // {
        // "019b04a9-067a-4c76-8817-ee75088c3822":
        // {"longitude":-48.188821,"latitude":74.84371}
        // ...
        // }

        List<UserCurrentLocationDTO> allUserCurrentLocations =
                tourGuideService.getAllUserCurrentLocations().stream().map(v -> modelMapper.map(v, UserCurrentLocationDTO.class)).collect(Collectors.toList());
        return new ResponseEntity<>(gson.toJson(allUserCurrentLocations), HttpStatus.OK);

    }

    /**
     * endpoint to retrieve all visited locations of all users classified date
     * 
     * @return a list of map with userId for Key and List of UserVisitedLocationDTO(date + location) for
     *         value.
     */
    @RequestMapping("/getAllUserLocations")
    public ResponseEntity<Object> getAllUserLocations() {

        List<VisitedLocation> allUserVisitedLocations = tourGuideService.getAllUserLocations();

        Map<UUID, List<UserVisitedLocationDTO>> mapUserIdVisitedLocations =
                allUserVisitedLocations.parallelStream()
                        .collect(Collectors.groupingBy((visitedLocation -> visitedLocation.userId),
                                Collectors.mapping(v -> new UserVisitedLocationDTO(v.timeVisited, v.location), Collectors.toList())));

        List<LinkedHashMap<String, Object>> finalResult = new ArrayList<>();

        for (Map.Entry<UUID, List<UserVisitedLocationDTO>> entrySet : mapUserIdVisitedLocations.entrySet()) {
            LinkedHashMap<String, Object> mapConverter = new LinkedHashMap<>();
            mapConverter.put("userUuid", entrySet.getKey());
            mapConverter.put("visitedLocations", entrySet.getValue());
            finalResult.add(mapConverter);
        }

        return new ResponseEntity<>(gson.toJson(finalResult), HttpStatus.OK);

    }

    /**
     * endpoint to retrieve all proposition of provider's travel for a user
     * 
     * @param userName the username of user selected
     * @return list of providers with namme and price ( userpreferences are also taken into account )
     */
    @RequestMapping("/getTripDeals")
    public ResponseEntity<Object> getTripDeals(@RequestParam String userName) {

        User user = tourGuideService.getUser(userName);

        if (user != null) {
            List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
            return new ResponseEntity<>(gson.toJson(providers), HttpStatus.OK);
        } else {
            throw new UserNotFoundException("User with name: " + userName + " doesnt exist!");
        }

    }

    /**
     * endPoint to retrieve userPreferences in json format
     * 
     * @param userName the userName of user
     * @return a serialization of userPreferences in json format
     */
    @GetMapping("/getUserPreferences")
    public ResponseEntity<Object> getUserPreferences(@RequestParam(value = "userName") String userName) {

        User user = tourGuideService.getUser(userName);

        if (user != null) {

            UserPreferences userPreferences = tourGuideService.getUser(userName).getUserPreferences();

            return new ResponseEntity<>(gson.toJson(modelMapper.map(userPreferences, UserPreferencesDTO.class)), HttpStatus.OK);

        } else {
            throw new UserNotFoundException("User with name: " + userName + " doesnt exist!");
        }
    }

    /**
     * post mapping to save or update user preferences
     * 
     * @param userName the user that we want to modify preferences
     * @param userPreferences the requestbody with user preferences
     * @return responseEntity with httpsStatus.CREATED if user existing
     * @throws UserNotFoundException if user was not found
     */
    @PostMapping("/setUserPreferences")
    public ResponseEntity<String> setUserPreferences(@RequestParam(value = "userName") String userName, @Valid @RequestBody UserPreferencesDTO userPreferencesDTO)
            throws UserNotFoundException {

        User user = tourGuideService.getUser(userName);

        if (user != null) {

            UserPreferences userPreferences = modelMapper.map(userPreferencesDTO, UserPreferences.class);
            tourGuideService.saveUserPreferences(userPreferences, user);
            return new ResponseEntity<>("Your preferences are correctly saved!", HttpStatus.CREATED);

        } else {
            throw new UserNotFoundException("User with name: " + userName + " doesnt exist!");
        }

    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }

}