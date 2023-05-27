package tourGuide.configuration;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.location.VisitedLocation;
import tourGuide.dto.UserCurrentLocationDTO;
import tourGuide.dto.UserPreferencesDTO;
import tourGuide.user.UserPreferences;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();
        mm.addMappings(moneyMap);
        mm.addMappings(moneyMapDTO);
        mm.addMappings(userCurrentVisitedLocationMap);
        return mm;
    }

    /**
     * PropertyMap to convert a UserPreferencesDTO to UserPreferences
     */
    PropertyMap<UserPreferencesDTO, UserPreferences> moneyMap = new PropertyMap<UserPreferencesDTO, UserPreferences>() {
        @Override
        protected void configure() {
            using(convertStringIntoCurrencyUnit()).map(source.getCurrencyUnit(), destination.getCurrency());
            using(convertIntToMoney()).map(source.getLowerPricePoint(), destination.getLowerPricePoint());
            using(convertIntToMoney()).map(source.getHighPricePoint(), destination.getHighPricePoint());

        }
    };

    /**
     * PropertyMap to convert a UserPreferences to UserPreferencesDTO
     */
    PropertyMap<UserPreferences, UserPreferencesDTO> moneyMapDTO = new PropertyMap<UserPreferences, UserPreferencesDTO>() {
        @Override
        protected void configure() {
            using(convertCurrencyUnitIntoString()).map(source.getCurrency(), destination.getCurrencyUnit());
            using(convertMoneyIntoInteger()).map(source.getLowerPricePoint(), destination.getLowerPricePoint());
            using(convertMoneyIntoInteger()).map(source.getHighPricePoint(), destination.getHighPricePoint());

        }
    };

    /**
     * PropertyMap to convert a VisitedLocation to UserCurrentLocationDTO
     */
    PropertyMap<VisitedLocation, UserCurrentLocationDTO> userCurrentVisitedLocationMap = new PropertyMap<VisitedLocation, UserCurrentLocationDTO>() {

        @Override
        protected void configure() {

            map(source.location.longitude, destination.getLongitude());
            map(source.location.latitude, destination.getLatitude());
            map(source.userId, destination.getUserUuid());
        }

    };

    @Bean
    public Converter<String, CurrencyUnit> convertStringIntoCurrencyUnit() {
        return context -> Monetary.getCurrency("USD");
    }

    @Bean
    public Converter<Integer, Money> convertIntToMoney() {
        return context -> Money.of(context.getSource(), "USD");
    }

    @Bean
    public Converter<CurrencyUnit, String> convertCurrencyUnitIntoString() {
        return context -> context.getSource().getCurrencyCode();
    }

    @Bean
    public Converter<Money, Integer> convertMoneyIntoInteger() {
        return context -> context.getSource().getNumber().intValue();
    }

}