package tourGuide.service;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gpsUtil.location.VisitedLocation;
import tourGuide.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(UserService.class);

    public void addToUserLocationAndReward(User user, VisitedLocation visitedLocation, RewardsService rewardsService) {
       
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
    }
    public VisitedLocation getLastUserLocation(User user) {
        return !user.getVisitedLocations().isEmpty() ? user.getLastVisitedLocation():null;
      }
}
