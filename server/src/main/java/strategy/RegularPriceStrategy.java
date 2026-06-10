package strategy;

public class RegularPriceStrategy implements PriceStrategy {
    @Override
    public double calculatePrice(double basePrice, int seatsCount) {
        return basePrice * seatsCount;
    }
}