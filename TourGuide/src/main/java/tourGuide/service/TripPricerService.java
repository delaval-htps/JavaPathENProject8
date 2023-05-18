package tourGuide.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TripPricerService {

    private final TripPricer tripPricer;

    public TripPricerService(TripPricer tripPricer) {
        this.tripPricer = tripPricer;
    }

    public List<Provider> getPrice(String trippricerapikey, UUID userId, int numberOfAdults, int numberOfChildren, int tripDuration, int cumulatativeRewardPoints) {
        return tripPricer.getPrice(trippricerapikey, userId, numberOfAdults, numberOfChildren, tripDuration, cumulatativeRewardPoints);
    }



}
