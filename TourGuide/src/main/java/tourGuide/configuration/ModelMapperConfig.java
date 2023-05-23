package tourGuide.configuration;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tourGuide.dto.UserPreferencesDTO;
import tourGuide.user.UserPreferences;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();
        mm.addMappings(moneyMap);
        mm.addMappings(moneyMapDTO);
        return mm;
    }

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

    PropertyMap<UserPreferencesDTO, UserPreferences> moneyMap = new PropertyMap<UserPreferencesDTO, UserPreferences>() {
        @Override
        protected void configure() {
            using(convertStringIntoCurrencyUnit()).map(source.getCurrencyUnit(), destination.getCurrency());
            using(convertIntToMoney()).map(source.getLowerPricePoint(), destination.getLowerPricePoint());
            using(convertIntToMoney()).map(source.getHighPricePoint(), destination.getHighPricePoint());

        }
    };

    PropertyMap<UserPreferences, UserPreferencesDTO> moneyMapDTO = new PropertyMap<UserPreferences, UserPreferencesDTO>() {
        @Override
        protected void configure() {
            using(convertCurrencyUnitIntoString()).map(source.getCurrency(), destination.getCurrencyUnit());
            using(convertMoneyIntoInteger()).map(source.getLowerPricePoint(), destination.getLowerPricePoint());
            using(convertMoneyIntoInteger()).map(source.getHighPricePoint(), destination.getHighPricePoint());

        }
    };

}