package ru.mycubecraft.core.terrain.height;

public class FlatHeightGenerator implements HeightGenerator {

    private final float height;

    public FlatHeightGenerator(float height) {
        this.height = height;
    }

    @Override
    public float generateHeight(float x, float z) {
        return this.height;
    }
}
