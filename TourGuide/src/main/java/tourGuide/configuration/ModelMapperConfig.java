package tourGuide.configuration;

import java.util.Currency;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tourGuide.dto.UserPreferencesDTO;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();
        mm.addConverter(convertStringIntoCurrencyUnit());
        mm.addConverter(convertIntToMoney());
                return mm;
    }

    @Bean
    public Converter<String, CurrencyUnit> convertStringIntoCurrencyUnit() {
        return context -> Currency.getAvailableCurrencies().contains(Currency.getInstance(context.getSource())) 
                ? Monetary.getCurrency(context.getSource())
                : Monetary.getCurrency("USD");
    }

    @Bean
    public Converter<Integer, Money> convertIntToMoney() {
        return context -> Currency.getAvailableCurrencies().contains(Currency.getInstance(((UserPreferencesDTO) context).getCurrencyUnit()))
                ? Money.of(context.getSource(), Monetary.getCurrency(((UserPreferencesDTO) context).getCurrencyUnit()))
                : Money.of(context.getSource(), "USD");
    }

  

}