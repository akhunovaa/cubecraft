package ru.mycubecraft.util;

public class MathUtil {

    public static float clamp(float x, float y, float z) {
        if (x < y) {
            return y;
        } else {
            return Math.min(x, z);
        }
    }

}
