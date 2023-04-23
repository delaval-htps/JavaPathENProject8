package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;

@Configuration
public class TourGuideModule {

	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}

	@Bean
	GpsUtilService getGpsUtilService() {
		return new GpsUtilService(getGpsUtil());
	}

	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtilService(), getRewardCentral());
	}

	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}

}
