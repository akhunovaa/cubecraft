package ru.mycubecraft.engine.math.noise;

import org.joml.Vector2f;

public interface Noise2f {

    float noise(float x, float y);

    default float noise(Vector2f v) {
        return noise(v.x, v.y);
    }

}
