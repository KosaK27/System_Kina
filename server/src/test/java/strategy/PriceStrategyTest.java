package strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PriceStrategyTest {

    @Test
    public void testRegularPriceStrategy() {
        PriceStrategy strategy = new RegularPriceStrategy();
        double result = strategy.calculatePrice(20.0, 3);
        assertEquals(60.0, result);
    }

    @Test
    public void testStudentPriceStrategy() {
        PriceStrategy strategy = new StudentPriceStrategy();
        double result = strategy.calculatePrice(20.0, 4);
        assertEquals(40.0, result);
    }
}