package tourGuide;

import java.util.List;
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
import tourGuide.dto.UserLocationHistoryDTO;
import tourGuide.dto.UserPreferencesDTO;
import tourGuide.dto.UserCurrentLocationDTO;
import tourGuide.exception.UserNotFoundException;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;

@RestController
public class TourGuideController {

    @Autowired
    private TourGuideService tourGuideService;

    @Autowired
    private ModelMapper modelMapper;

    private Gson gson = new Gson();

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return gson.toJson(visitedLocation.location);
    }

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
    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return gson.toJson(tourGuideService.getNearByAttractions(visitedLocation));
    }

    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String userName) {
        return gson.toJson(tourGuideService.getUserRewards(getUser(userName)));
    }

    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
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

        List<UserCurrentLocationDTO> allUserCurrentLocations = tourGuideService.getAllCurrentLocations().stream().map(v -> modelMapper.map(v, UserCurrentLocationDTO.class)).collect(Collectors.toList());
        return gson.toJson(allUserCurrentLocations);
    }

    // @RequestMapping("/getAllUserLocations")
    // public String getAllUserLocations() {
    // List<UserLocationHistoryDTO> userLocationsHistory =
    // tourGuideService.getAllUserLocationHistories().stream().map(v ->
    // modelMapper.map(v ->
    // UserLocationHistoryDTO.class)).collect(Collectors.toList());
    // return gson.toJson(userLocationsHistory);
    // }

    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return gson.toJson(providers);
    }

    /**
     * endPoint to retrieve userPreferences in json format
     * 
     * @param userName the userName of user
     * @return a serialization of userPreferences in json format
     */
    @GetMapping("/getUserPreferences")
    public String getUserPreferences(@RequestParam(value = "userName") String userName) {

        UserPreferences userPreferences = tourGuideService.getUser(userName).getUserPreferences();

        if (userPreferences != null) {
            return gson.toJson(modelMapper.map(userPreferences, UserPreferencesDTO.class));
        } else {
            throw new UserNotFoundException("UserName doesn't exist!");
        }

    }

    /**
     * post mapping to save or update user preferences
     * 
     * @param userName        the user that we want to modify preferences
     * @param userPreferences the requestbody with user preferences
     * @return responseEntity with httpsStatus.CREATED if user existing
     * @throws UserNotFoundException if user was not found
     */
    @PostMapping("/setUserPreferences")
    public ResponseEntity<String> setUserPreferences(@RequestParam(value = "userName") String userName, @Valid @RequestBody UserPreferencesDTO userPreferencesDTO) throws UserNotFoundException {

        User user = tourGuideService.getUser(userName);

        if (user != null) {

            UserPreferences userPreferences = modelMapper.map(userPreferencesDTO, UserPreferences.class);
            tourGuideService.saveUserPreferences(userPreferences, user);
            return new ResponseEntity<>("Your preferences are correctly saved!", HttpStatus.CREATED);

        } else {
            throw new UserNotFoundException("UserName doesnt exist!");
        }

    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }

}