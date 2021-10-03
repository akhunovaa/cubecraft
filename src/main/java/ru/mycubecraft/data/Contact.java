package ru.mycubecraft.data;

/**
 * Describes a collision contact.
 */
public class Contact implements Comparable<Contact> {
    /* The collision time */
    public final float t;
    /* The collision normal */
    public int nx, ny, nz;
    /* The global position of the collided voxel */
    public int x, y, z;

    public Contact(float t, int x, int y, int z) {
        this.t = t;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int compareTo(Contact o) {
        /* Resolve first by Y contacts, then by distance */
        return ny != o.ny ? o.ny - ny : Float.compare(t, o.t);
    }

    public String toString() {
        return "{" + x + "|" + y + "|" + z + " " + nx + " " + ny + " " + nz + " @ " + t + "}";
    }
}
