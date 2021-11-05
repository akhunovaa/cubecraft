package ru.mycubecraft.world.player.impl;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import ru.mycubecraft.Settings;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.block.GrassBlock;
import ru.mycubecraft.util.MathUtil;
import ru.mycubecraft.world.BlockField;
import ru.mycubecraft.world.Chunk;
import ru.mycubecraft.world.MouseBoxSelectionDetector;
import ru.mycubecraft.world.player.Player;

import static java.lang.Math.*;

@Getter
@Setter
public class DefaultPlayer extends Player {

    private final Vector3i sideOffset = new Vector3i();

    public DefaultPlayer() {
        this.viewMatrix = new Matrix4f();
        this.position = new Vector4f(47.0f, 130f, -179f, 1);
        this.rotation = new Vector3f(32.0f, 9.0f, 0.0f);
        this.mouseBoxSelectionDetector = new MouseBoxSelectionDetector();
    }

    /**
     * Determine the block pointed to by a ray <code>(rayOrigin.x, rayOrigin.y, rayOrigin.z) + t * (rayDirection.x, rayDirection.y, rayDirection.z)</code> and store
     * the block and side offset of that block (if any) into {@link #selectedBlock} and
     * {@link #sideOffset}, respectively.
     *
     * @param blockField the chunks block field
     */
    public Block findAndSelectBlock(BlockField blockField) {
        if (blockField == null) {
            return null;
        }
        if (this.selectedBlock != null) {
            this.selectedBlock.setSelected(false);
        }
        float xStart = (float) Math.ceil(this.position.x);
        float yStart = (float) Math.floor(this.position.y);
        float zStart = (float) Math.ceil(this.position.z);

        Vector3f rayOrigin = new Vector3f(xStart, yStart, zStart);
        Vector3f rayDirection = mouseBoxSelectionDetector.rayDirection();

        /* "A Fast Block Traversal Algorithm for Ray Tracing" by John Amanatides, Andrew Woo */
        float big = 1E30f;

        int px = (int) floor(rayOrigin.x),
                py = (int) floor(rayOrigin.y),
                pz = (int) floor(rayOrigin.z);

        float dxi = 1f / rayDirection.x,
                dyi = 1f / rayDirection.y,
                dzi = 1f / rayDirection.z;

        float sx = rayDirection.x > 0 ? 1 : -1,
                sy = rayDirection.y > 0 ? 1 : -1,
                sz = rayDirection.z > 0 ? 1 : -1;

        float dtx = min(dxi * sx, big),
                dty = min(dyi * sy, big),
                dtz = min(dzi * sz, big);

        float tx = abs((px + max(sx, 0) - rayOrigin.x) * dxi),
                ty = abs((py + max(sy, 0) - rayOrigin.y) * dyi),
                tz = abs((pz + max(sz, 0) - rayOrigin.z) * dzi);

        int maxSteps = 8;
        for (int i = 0; i < maxSteps && py >= 0; i++) {
            if (i > 0 && py < Chunk.CHUNK_HEIGHT) {
                Block block = blockField.load(px, py, pz);
                if (block != null) {
                    MathUtil.enterSide(rayOrigin.x, rayOrigin.y, rayOrigin.z, rayDirection.x,
                            rayDirection.y, rayDirection.z, px, py, pz, sideOffset);
                    if (this.selectedBlock != null) {
                        this.selectedBlock.setSelected(false);
                    }
                    block.setSelected(true);
                    this.selectedBlock = block;
                    return block;
                }
            }
            /* Advance to next block */
            int cmpx = MathUtil.step(tx, tz) * MathUtil.step(tx, ty);
            int cmpy = MathUtil.step(ty, tx) * MathUtil.step(ty, tz);
            int cmpz = MathUtil.step(tz, ty) * MathUtil.step(tz, tx);
            tx += dtx * cmpx;
            ty += dty * cmpy;
            tz += dtz * cmpz;
            px += sx * cmpx;
            py += sy * cmpy;
            pz += sz * cmpz;
        }
        return null;
    }


    @Override
    public void moveRotation(double offsetX, double offsetY, double offsetZ) {
        double xRotation = rotation.x;
        double yRotation = rotation.y;
        yRotation += offsetY;
        if (yRotation > 360.0f || yRotation < -360.0f) {
            yRotation = 0.0f;
        }
        xRotation += offsetX;
        rotation.y = (float) yRotation;
        rotation.z += offsetZ;
        rotation.x = MathUtil.clamp((float) xRotation, (float) Settings.MIN_LOOK, (float) Settings.MAX_LOOK);
    }

    @Override
    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if (offsetZ != 0) {
            position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }
        if (offsetX != 0) {
            position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        position.y += offsetY;
    }

    @Override
    public void removeSelectedBlock(BlockField blockField) {
        if (this.selectedBlock == null) {
            return;
        }
        int xPosition = (int) this.selectedBlock.getPosition().x;
        int yPosition = (int) this.selectedBlock.getPosition().y;
        int zPosition = (int) this.selectedBlock.getPosition().z;

        blockField.store(xPosition, yPosition, zPosition, null);
    }

    @Override
    public void placeAtSelectedBlock(BlockField blockField) {
        if (this.selectedBlock == null || this.selectedBlock.getPosition().y + this.sideOffset.y < 0 ||
                this.selectedBlock.getPosition().y + this.sideOffset.y >= Chunk.CHUNK_HEIGHT) {
            return;
        }

        int xPosition = (int) (this.selectedBlock.getPosition().x + this.sideOffset.x);
        int yPosition = (int) (this.selectedBlock.getPosition().y + this.sideOffset.y);
        int zPosition = (int) (this.selectedBlock.getPosition().z + this.sideOffset.z);

        Block block = new GrassBlock(xPosition, yPosition, zPosition);
        blockField.store(xPosition, yPosition, zPosition, block);
    }
}
