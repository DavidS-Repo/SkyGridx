package main;

public class MinMaxSettings {
    private final int minY;
    private final int maxY;

    public MinMaxSettings(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }
}