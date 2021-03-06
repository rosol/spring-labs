package savings;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.fest.assertions.Assertions.assertThat;
import static org.joda.money.CurrencyUnit.EUR;
import static org.joda.time.DateTime.now;

import org.joda.money.Money;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import savings.model.PaybackConfirmation;
import savings.model.Purchase;
import savings.service.PaybackBookKeeper;

// TODO #0 remove @Ignore to enable the test
// TODO #1 remove inheritance and annotate the test to be run using SpringJUnit4ClassRunner
// TODO #2 change context configuration to use internal configuration class instead of XML files
@Ignore
public class PaybackBookKeeperModuleTest extends AbstractJUnit4SpringContextTests {

    // TODO #3 use internal configuration class to import required production configs and set up test data source
    // TODO #4 enable property source based configuration to set up test data source utilizing META-INF/application.properties

    PaybackBookKeeper bookKeeper = null;

    String creditCardNumber = "1234123412341234";

    String merchantNumber = "1234567890";

    Purchase purchase = new Purchase(Money.of(EUR, 100L), creditCardNumber, merchantNumber, now());

    @Before
    public void setUp() {
        // TODO #5 use annotation based field injections to deliver this test dependency from test context
        bookKeeper = applicationContext.getBean("paybackBookKeeper", PaybackBookKeeper.class);
    }

    @Test
    public void shouldThrowWhenAccountNotFound() {
        purchase = new Purchase(Money.of(EUR, 100L), "4321432143214321", merchantNumber, now());

        catchException(bookKeeper, EmptyResultDataAccessException.class).registerPaybackFor(purchase);

        assertThat(caughtException()).isNotNull();
    }

    @Test
    public void shouldThrowWhenMerchantNotFound() {
        purchase = new Purchase(Money.of(EUR, 100L), creditCardNumber, "1111111111", now());

        catchException(bookKeeper, EmptyResultDataAccessException.class).registerPaybackFor(purchase);

        assertThat(caughtException()).isNotNull();
    }

    @Test
    public void shouldRegisterPayback() {
        PaybackConfirmation confirmation = bookKeeper.registerPaybackFor(purchase);

        assertThat(confirmation.getNumber()).isNotNull();
        assertThat(confirmation.getIncome().getAmount()).isEqualTo(Money.of(EUR, 6L));
        assertThat(confirmation.getIncome().getDistributions()).hasSize(2);
        assertThat(confirmation.getIncome().getDistribution("Glock").getAmount()).isEqualTo(Money.of(EUR, 3L));
        assertThat(confirmation.getIncome().getDistribution("M60").getAmount()).isEqualTo(Money.of(EUR, 3L));
    }
}
