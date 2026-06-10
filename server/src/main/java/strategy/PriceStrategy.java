package strategy;

public interface PriceStrategy {
    double calculatePrice(double basePrice, int seatsCount);
}