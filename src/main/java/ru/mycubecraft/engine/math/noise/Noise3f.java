package ru.mycubecraft.engine.math.noise;

import org.joml.Vector3f;

public interface Noise3f {

    float noise(float x, float y, float z);

    default float noise(Vector3f v) {
        return noise(v.x, v.y, v.z);
    }

}
