package ru.mycubecraft.util;

import org.joml.Vector3i;

import static java.lang.Math.max;
import static java.lang.Math.min;

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

    /**
     * GLSL's clamp function.
     */
    public static <T extends Comparable<T>> T clamp(T val, T min, T max) {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }

    /**
     * GLSL's step function.
     */
    public static int step(float edge, float x) {
        return x < edge ? 0 : 1;
    }


    /**
     * Determine the side (as a normal vector) at which the ray
     * <code>(ox, oy, oz) + t * (dx, dy, dz)</code> enters the unit box with min coordinates
     * <code>(x, y, z)</code>, and store the normal of that face into <code>off</code>.
     */
    public static void enterSide(float ox, float oy, float oz, float dx, float dy, float dz, int x, int y, int z, Vector3i off) {
        float tMinx = (x - ox) / dx, tMiny = (y - oy) / dy, tMinz = (z - oz) / dz;
        float tMaxx = (x + 1 - ox) / dx, tMaxy = (y + 1 - oy) / dy, tMaxz = (z + 1 - oz) / dz;
        float t1x = min(tMinx, tMaxx), t1y = min(tMiny, tMaxy), t1z = min(tMinz, tMaxz);
        float tNear = max(max(t1x, t1y), t1z);
        off.set(tNear == t1x ? dx > 0 ? -1 : 1 : 0, tNear == t1y ? dy > 0 ? -1 : 1 : 0, tNear == t1z ? dz > 0 ? -1 : 1 : 0);
    }


}
