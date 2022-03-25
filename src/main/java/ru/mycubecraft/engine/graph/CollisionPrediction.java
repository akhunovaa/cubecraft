package ru.mycubecraft.engine.graph;

import ru.mycubecraft.data.Contact;
import ru.mycubecraft.world.BlockField;

import java.util.List;

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Math.*;
import static java.lang.Math.min;
import static ru.mycubecraft.world.player.Player.*;
import static ru.mycubecraft.world.player.Player.PLAYER_WIDTH;

/**
 * author chanpreet singh
 * This class is built to predict the collison between two players and restrict them at a given coordinate
 */

public class CollisionPrediction {



    /**
     * Compute the exact collision point between the player and the block at <code>(x_coord, y_coord, z_coord)</code>.
     * https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-collision-detection-and-response-r3084/
     */
    public static void intersectSweptAabbAabb(int x_coord, int y_coord, int z_coord, float px, float py, float pz, float dx, float dy, float dz,
                                        List<Contact> contacts, BlockField blockField) {
        float pxmax = px + PLAYER_WIDTH, pxmin = px - PLAYER_WIDTH, pymax = py + PLAYER_HEIGHT - PLAYER_EYE_HEIGHT, pymin = py - PLAYER_EYE_HEIGHT,
                pzmax = pz + PLAYER_WIDTH, pzmin = pz - PLAYER_WIDTH;

        float xInvEntry = dx > 0f ? -pxmax : 1 - pxmin,
                xInvExit = dx > 0f ? 1 - pxmin : -pxmax;

        //boolean xNotValid = dx == 0 || blockField.load(x_coord + (dx > 0 ? -1 : 1), y_coord, z_coord) != null;
        boolean xNotValid = dx == 0;

        float xEntry = xNotValid ? NEGATIVE_INFINITY : xInvEntry / dx,
                xExit = xNotValid ? POSITIVE_INFINITY : xInvExit / dx;

        float yInvEntry = dy > 0f ? -pymax : 1 - pymin,
                yInvExit = dy > 0f ? 1 - pymin : -pymax;

        //boolean yNotValid = dy == 0 || blockField.load(x_coord, y_coord + (dy > 0 ? -1 : 1), z_coord) != null;
        boolean yNotValid = dy == 0f;

        float yEntry = yNotValid ? NEGATIVE_INFINITY : yInvEntry / dy,
                yExit = yNotValid ? POSITIVE_INFINITY : yInvExit / dy;

        float zInvEntry = dz > 0f ? -pzmax : 1 - pzmin,
                zInvExit = dz > 0f ? 1 - pzmin : -pzmax;

        //boolean zNotValid = dz == 0 || blockField.load(x_coord, y_coord, z_coord + (dz > 0 ? -1 : 1)) != null;
        boolean zNotValid = dz == 0;

        float zEntry = zNotValid ? NEGATIVE_INFINITY : zInvEntry / dz,
                zExit = zNotValid ? POSITIVE_INFINITY : zInvExit / dz;

        float tEntry = max(max(xEntry, yEntry), zEntry),
                tExit = min(min(xExit, yExit), zExit);

        if (tEntry < -.5f || tEntry > tExit) {
            return;
        }

        Contact contact = new Contact(tEntry, x_coord, y_coord, z_coord);

        if (xEntry == tEntry) {
            contact.nx = dx > 0 ? -1 : 1;
        } else if (yEntry == tEntry) {
            contact.ny = dy > 0 ? -1 : 1;
        } else {
            contact.nz = dz > 0 ? -1 : 1;
        }
        contacts.add(contact);
    }
}
