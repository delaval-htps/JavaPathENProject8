package tourGuide.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.google.gson.Gson;

import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.dto.UserPreferencesDTO;
import tourGuide.exception.UserNotFoundException;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

/**
 * Integration test for RestController TourGuideController
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTourGuideController {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private GpsUtil gpsUtil;

        @Autowired
        private RewardsService rewardsService;

        @Autowired
        private TourGuideService tourGuideService;


        private static Logger rootLogger;
        private Gson gson = new Gson();
        private User user;

        @BeforeAll
        public static void init() {
                System.setProperty("path", "TestTourGuideController");
                InternalTestHelper.setInternalUserNumber(100);
        }

        @AfterEach
        public void finishedTest() {
                System.clearProperty("logFileName");
                tourGuideService.addShutDownHook();
        }

        @AfterAll
        public static void finalStep() {
                System.clearProperty("path");
        }

        @Test
        @Order(1)
        public void indexTest_thenReturn200() throws Exception {

                System.setProperty("logFileName", "getIndex");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /index -----------------------");
                MvcResult result = mockMvc.perform(get("/"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", is("Greetings from TourGuide!")))
                                .andDo(print()).andReturn();
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }

        private static final Stream<Arguments> get_endpoint_methods() {
                return Stream.of(Arguments.of("/getLocation"),
                                Arguments.of("/getNearbyAttractions"),
                                Arguments.of("/getRewards"),
                                Arguments.of("/getTripDeals"),
                                Arguments.of("/getUserPreferences"));
        }

        @ParameterizedTest
        @Order(2)
        @MethodSource("get_endpoint_methods")
        public void getEndpoints_whenNotValidUserName_thenReturn404(String endpoint) throws Exception {

                System.setProperty("logFileName", "NoValidUserName");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  {} - when NotValidUSerName -----------------------", endpoint);

                MvcResult result = mockMvc.perform(get(endpoint).param("userName", "badUserName"))
                                .andExpect(status().isNotFound())
                                .andDo(print()).andReturn();
                assertThat(result.getResolvedException()).isInstanceOf(UserNotFoundException.class);
                assertThat(result.getResolvedException().getMessage()).isEqualTo("User with name: badUserName doesnt exist!");

                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());

        }

        @Test
        @Order(3)
        public void getLocation_thenReturn200() throws Exception {

                System.setProperty("logFileName", "getLocation");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /getLocation -----------------------");

                // Given
                // create a user and launch tourGuideService on it
                user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
                tourGuideService.addUser(user);
                tourGuideService.trackUserLocation(user);

                // await tracker give a visited Location to this user
                Awaitility.await().until(() -> user.getLastVisitedLocation() != null);

                // add manually a new last visited location to check if getLocation() return it
                user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(100, 200), new Date()));

                Awaitility.await().untilAsserted(() -> assertEquals(user.getVisitedLocations().size(), 2));

                // when & then
                MvcResult result = mockMvc.perform(get("/getLocation")

                                .param("userName", "jon"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(2)))
                                .andExpect(jsonPath("$.longitude", is(200.0)))
                                .andExpect(jsonPath("$.latitude", is(100.0)))
                                .andDo(print()).andReturn();
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }

        @Test
        @Order(4)
        public void getAllCurrentLocations_thenReturn200() throws Exception {

                System.setProperty("logFileName", "getAllCurrentLocations");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /getAllCurrentLocations -----------------------");

                MvcResult result = mockMvc.perform(get("/getAllCurrentLocations"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(101)))
                                .andExpect(jsonPath("$[0].keys()", Matchers.is(Set.<String> of("userUuid", "longitude", "latitude"))))
                                .andDo(print()).andReturn();
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }

        @Test
        @Order(5)
        public void getAllUserLocations_thenReturn200() throws Exception {

                System.setProperty("logFileName", "getAllUserLocations");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /getAllUserLocations -----------------------");

                MvcResult result = mockMvc.perform(get("/getAllUserLocations"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(101)))
                                .andExpect(jsonPath("$[0].keys()", Matchers.is(Set.<String> of("userUuid", "visitedLocations"))))
                                .andExpect(jsonPath("$[0].length()", is(2)))
                                .andExpect(jsonPath("$[0].visitedLocations[0].keys()", Matchers.is(Set.<String> of("timeVisited", "location"))))
                                .andExpect(jsonPath("$[0].visitedLocations[0].location.keys()", Matchers.is(Set.<String> of("longitude", "latitude"))))
                                .andDo(print()).andReturn();
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }

        @Test
        @Order(6)
        public void getNearByAttractions_thenReturn200() throws Exception {

                System.setProperty("logFileName", "getNearByAttraction");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /getNearByAttraction -----------------------");

                // Given
                // create a user and launch tourGuideService on it
                user = new User(UUID.randomUUID(), "jonNearByAttraction", "000", "jon@tourGuide.com");
                tourGuideService.addUser(user);
                tourGuideService.trackUserLocation(user);

                // await tracker give a visited Location to this user
                Awaitility.await().until(() -> user.getLastVisitedLocation() != null);


                // when & then
                MvcResult result = mockMvc.perform((get("/getNearbyAttractions")).param("userName", "jonNearByAttraction"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(5)))
                                .andExpect(jsonPath("$[0].length()", is(5)))

                                .andExpect(jsonPath("$[0].keys()",
                                                Matchers.containsInAnyOrder("name", "touristAttraction lat/long", "userLocation lat/long", "distance", "rewardPoints")))
                                .andExpect(jsonPath("$[1].keys()",
                                                Matchers.containsInAnyOrder("name", "touristAttraction lat/long", "userLocation lat/long", "distance", "rewardPoints")))
                                .andExpect(jsonPath("$[2].keys()",
                                                Matchers.containsInAnyOrder("name", "touristAttraction lat/long", "userLocation lat/long", "distance", "rewardPoints")))
                                .andExpect(jsonPath("$[3].keys()",
                                                Matchers.containsInAnyOrder("name", "touristAttraction lat/long", "userLocation lat/long", "distance", "rewardPoints")))
                                .andExpect(jsonPath("$[4].keys()",
                                                Matchers.containsInAnyOrder("name", "touristAttraction lat/long", "userLocation lat/long", "distance", "rewardPoints")))

                                .andDo(print()).andReturn();
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }



        @Test
        @Order(7)
        public void getRewards_whenNoRewardsForUser__thenReturn200() throws Exception {

                System.setProperty("logFileName", "getRewards-noRewards");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /getRewards - noRewards -----------------------");

                // Given
                // create a user and launch tourGuideService on it but not track him
                user = new User(UUID.randomUUID(), "jonNoRewards", "000", "jon@tourGuide.com");
                tourGuideService.addUser(user);
                rewardsService.calculateRewards(user);

                // Then and When
                MvcResult result = mockMvc.perform(get("/getRewards").param("userName", "jonNoRewards"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", Matchers.equalTo("The user has not yet received any rewards!")))
                                .andDo(print()).andReturn();
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }

        @Test
        @Order(8)
        public void getRewards_whenUserVisitedAttraction_thenReturn200() throws Exception {

                System.setProperty("logFileName", "getRewards-withRewards");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /getRewards - withRewards -----------------------");

                // Given
                // create a user and launch tourGuideService on it
                user = new User(UUID.randomUUID(), "jonRewards", "000", "jon@tourGuide.com");
                tourGuideService.addUser(user);
                tourGuideService.trackUserLocation(user);

                // await tracker give a visited Location to this user
                Awaitility.await().until(() -> user.getLastVisitedLocation() != null);

                // add manually a attraction for a new visitedLocation to be sure to get rewards for user
                VisitedLocation visitedAttraction = new VisitedLocation(user.getUserId(), gpsUtil.getAttractions().get(0), new Date());
                user.addToVisitedLocations(visitedAttraction);

                rewardsService.calculateRewards(user);

                Awaitility.waitAtMost(1200, TimeUnit.MILLISECONDS).until(() -> user.getUserRewards().size() > 0);

                assertEquals(user.getVisitedLocations().size(), 2);

                // when & then
                MvcResult result = mockMvc.perform(get("/getRewards")
                                .param("userName", "jonRewards"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", Matchers.greaterThanOrEqualTo(1)))
                                .andExpect(jsonPath("$[-1].keys()",
                                                Matchers.containsInAnyOrder("visitedLocation", "attraction", "rewardPoints")))
                                .andDo(print()).andReturn();
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }



        @Test
        @Order(9)
        public void getTripDeals_thenReturn200() throws Exception {

                System.setProperty("logFileName", "geTripDeals");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /geTripDeals -----------------------");

                MvcResult result = mockMvc.perform(get("/getTripDeals").param("userName", "internalUser0"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(5)))
                                .andExpect(jsonPath("$[0].keys()", Matchers.is(Set.<String> of("name", "price", "tripId"))))
                                .andExpect(jsonPath("$[1].keys()", Matchers.is(Set.<String> of("name", "price", "tripId"))))
                                .andExpect(jsonPath("$[2].keys()", Matchers.is(Set.<String> of("name", "price", "tripId"))))
                                .andExpect(jsonPath("$[3].keys()", Matchers.is(Set.<String> of("name", "price", "tripId"))))
                                .andExpect(jsonPath("$[4].keys()", Matchers.is(Set.<String> of("name", "price", "tripId"))))
                                .andDo(print()).andReturn();
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }



        @Test
        @Order(10)
        public void getUserPreferences_thenReturn200() throws Exception {

                System.setProperty("logFileName", "getUserPreferences");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /getUserPreferences -----------------------");
                MvcResult result = mockMvc.perform(get("/getUserPreferences").param("userName", "internalUser0"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(8)))
                                .andExpect(jsonPath("$.keys()", Matchers.is(Set.<String> of(
                                                "attractionProximity",
                                                "currencyUnit",
                                                "lowerPricePoint",
                                                "highPricePoint",
                                                "tripDuration",
                                                "ticketQuantity",
                                                "numberOfAdults", "numberOfChildren"))))
                                .andDo(print()).andReturn();
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }

        @Test
        @Order(11)
        public void setUserPreferences_whenNotValidUserName_thenReturn404() throws Exception {

                System.setProperty("logFileName", "setUserPreferences-NoValidUsername");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /setUserPreferences - No Valid Username -----------------------");
                MvcResult result = mockMvc.perform(post("/setUserPreferences")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(new UserPreferencesDTO(
                                                1, "USD",
                                                0, 100,
                                                1, 10,
                                                1, 1)))
                                .param("userName", "badUserName"))
                                .andExpect(status().isNotFound()).andReturn();

                assertThat(result.getResolvedException()).isInstanceOf(UserNotFoundException.class);
                assertThat(result.getResolvedException().getMessage()).isEqualTo("User with name: badUserName doesnt exist!");
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }


        private static Stream<Arguments> invalidUserPreferences() {
                return Stream.of(
                                Arguments.of(new UserPreferencesDTO(-1, "USD", 0, 100, 1, 10, 1, 1)),
                                Arguments.of(new UserPreferencesDTO(12345, "USD", 0, 100, 1, 10, 1, 1)),
                                Arguments.of(new UserPreferencesDTO(1, null, 0, 100, 1, 10, 1, 1)),
                                Arguments.of(new UserPreferencesDTO(1, "", 0, 100, 1, 10, 1, 1)),
                                Arguments.of(new UserPreferencesDTO(1, "USD", -1, 100, 1, 10, 1, 1)), Arguments.of(new UserPreferencesDTO(1, "USD", 12345, 100, 1, 10, 1, 1)),
                                Arguments.of(new UserPreferencesDTO(1, "USD", 12, -1, 1, 10, 1, 1)), Arguments.of(new UserPreferencesDTO(1, "USD", 1234, 10000, 1, 10, 1, 1)),
                                Arguments.of(new UserPreferencesDTO(1, "USD", 1234, 100, -1, 10, 1, 1)), Arguments.of(new UserPreferencesDTO(1, "USD", 0, 1, 10000, 10, 1, 1)),
                                Arguments.of(new UserPreferencesDTO(1, "USD", 0, 100, 1, -1, 1, 1)), Arguments.of(new UserPreferencesDTO(1, "USD", 0, 100, 1, 10000, 1, 1)),
                                Arguments.of(new UserPreferencesDTO(1, "USD", 0, 100, 1, 1, -1, 1)), Arguments.of(new UserPreferencesDTO(1, "USD", 0, 100, 1, 1, 10000, 1)),
                                Arguments.of(new UserPreferencesDTO(-1, "USD", 0, 100, 1, 1, 1, -1)), Arguments.of(new UserPreferencesDTO(1, "USD", 0, 100, 1, 1, 1, 10000))

                );
        }


        @ParameterizedTest
        @Order(12)
        @MethodSource("invalidUserPreferences")
        public void setUserPreferences_whenNotValidInput_thenReturn400(UserPreferencesDTO userPreferences) throws Exception {

                System.setProperty("logFileName", "setUserPreferences-InvalidBody");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /setUserPreferences - invalid Body : \n{} -----------------------", userPreferences.toString());

                MvcResult result = mockMvc.perform(post("/setUserPreferences")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(userPreferences))
                                .param("userName", "internalUser0"))
                                .andExpect(status().isBadRequest()).andDo(print()).andReturn();

                assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
                assertThat(result.getResponse().getContentAsString()).contains("invalid data");
                rootLogger.info("\033[32m - Response error message: {}", result.getResponse().getContentAsString());
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());

        }

        @Test
        @Order(13)
        public void setUserPreferences_thenReturn201() throws Exception {

                System.setProperty("logFileName", "setUserPreferences");
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.reconfigure();
                rootLogger = LogManager.getRootLogger();

                rootLogger.info("---------------------- endPoint :  /setUserPreferences -----------------------");
                MvcResult result = mockMvc.perform(post("/setUserPreferences")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(new UserPreferencesDTO(
                                                1, "USD",
                                                0, 100,
                                                1, 10,
                                                1, 1)))
                                .param("userName", "internalUser0"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$", is("Your preferences are correctly saved!")))
                                .andDo(print())
                                .andReturn();
                rootLogger.info("\033[32m - Response status: {} \n", result.getResponse().getStatus());
        }


}
