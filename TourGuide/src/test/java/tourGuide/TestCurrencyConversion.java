package tourGuide;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.MonetaryConversions;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestCurrencyConversion {
  
  @Disabled("just to see for fun, how we can retrieve from moneyAPI a conversion....")
  @Test
  public void givenAmount_whenConversion_thenNotNull() {
    MonetaryAmount oneDollar = Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create();
    System.out.println("providers : " + MonetaryConversions.getConversionProviderNames());
    CurrencyConversion conversionEUR = MonetaryConversions.getConversion("EUR", "ECB");

    MonetaryAmount convertedAmountUSDtoEUR = oneDollar.with(conversionEUR);
    System.out.println(convertedAmountUSDtoEUR);
    // assertEquals("USD 1", oneDollar.toString());
    assertNotNull(convertedAmountUSDtoEUR);
  }
}
