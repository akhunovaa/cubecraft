package ru.mycubecraft.core.terrain.height;


import ru.mycubecraft.engine.math.noise.Noise2f;

public class NoiseHeightGenerator implements HeightGenerator {

    private final Noise2f noise;

    public NoiseHeightGenerator(Noise2f noise) {
        this.noise = noise;
    }

    @Override
    public float generateHeight(float x, float z) {
        return this.noise.noise(x, z);
    }
}
