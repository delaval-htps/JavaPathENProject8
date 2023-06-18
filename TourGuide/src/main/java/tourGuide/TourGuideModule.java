package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tripPricer.TripPricer;

@Configuration
public class TourGuideModule {

	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}

	@Bean
	public TripPricer getTripPricer() {
		return new TripPricer();
	}

	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}

}
