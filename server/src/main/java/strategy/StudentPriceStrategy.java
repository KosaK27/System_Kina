package strategy;

public class StudentPriceStrategy implements PriceStrategy {
    @Override
    public double calculatePrice(double basePrice, int seatsCount) {
        return (basePrice * 0.5) * seatsCount;
    }
}