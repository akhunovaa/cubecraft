package ru.mycubecraft.util;

public class MathUtil {

    public static final float f_PI = (float) Math.PI;
    public static final float f_2PI = (float) (2.0d * Math.PI);
    public static float f_PI_div_2 = (float) (0.5d * Math.PI);

    /**
     * Simplifies an angle, given in radians
     *
     * @param rad the angle
     * @return same angle within the range <bb>]-PI, PI]</bb>
     */
    public static float simplifyRadians(float rad) {
        while (rad <= -f_PI) {
            rad += f_2PI;
        }
        while (rad > f_PI) {
            rad -= f_2PI;
        }
        return rad;
    }

    public static <T extends Comparable<T>> T clamp(T val, T min, T max) {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }

}
