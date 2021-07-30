package ru.mycubecraft.data;

import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.engine.IHud;
import ru.mycubecraft.engine.Material;
import ru.mycubecraft.engine.TextItem;
import ru.mycubecraft.engine.graph.FontTexture;
import ru.mycubecraft.engine.graph.OBJLoader;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.World;

import java.awt.*;

public class Hud implements IHud {

    private static final Font FONT = new Font("Arial", Font.PLAIN, 16);

    private static final String CHARSET = "ISO-8859-1";

    private final GameItem[] gameItems;

    private final TextItem versionTextItem;
    private final TextItem fpsTextItem;
    private final TextItem coordinatesTextItem;
    private final TextItem rotationTextItem;
    private final TextItem chunkSizeTextItem;
    private final TextItem filteredBlockSizeTextItem;
    private final TextItem blockSizeTextItem;

    private final GameItem compassItem;


    public Hud() {
        FontTexture fontTexture = new FontTexture(FONT, CHARSET);

        String version = "ALPHA 0.1.0";
        String fpsText = "FPS: 0";
        String coordinatesText = "X: 0.00 Y: 0.00 Z: 0.00";
        String createdChunksSizeText = "CHUNKS: 0";
        String filteredBlockSizeText = "FILTERED BLOCKS: 0";
        String createdBlocksSizeText = "BLOCKS: 0";

        this.fpsTextItem = new TextItem(fpsText, fontTexture);
        this.fpsTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0.8f, 0.8f, 10f));

        this.coordinatesTextItem = new TextItem(coordinatesText, fontTexture);
        this.coordinatesTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0.8f, 0.8f, 10f));

        this.rotationTextItem = new TextItem(coordinatesText, fontTexture);
        this.rotationTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0.8f, 0.8f, 10f));

        this.chunkSizeTextItem = new TextItem(createdChunksSizeText, fontTexture);
        this.chunkSizeTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0.8f, 0.8f, 10f));

        this.filteredBlockSizeTextItem = new TextItem(filteredBlockSizeText, fontTexture);
        this.filteredBlockSizeTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0.8f, 0.8f, 10f));

        this.blockSizeTextItem = new TextItem(createdBlocksSizeText, fontTexture);
        this.blockSizeTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0.8f, 0.8f, 10f));

        this.versionTextItem = new TextItem(version, fontTexture);
        this.versionTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0.8f, 0.8f, 10f));

        // Create compass
        Mesh mesh = null;
        try {
            mesh = OBJLoader.loadMesh("assets/models/compass.obj");
            Material material = new Material();
            material.setAmbientColour(new Vector4f(1, 0, 0, 1));

            mesh.setMaterial(material);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        compassItem = new GameItem(mesh);
        compassItem.setScale(40.0f);
        // Rotate to transform it to screen coordinates
        compassItem.setRotation(0f, 0f, 180f);
        // Create list that holds the items that compose the HUD
        gameItems = new GameItem[]{versionTextItem, compassItem,
                coordinatesTextItem, fpsTextItem, rotationTextItem,
        chunkSizeTextItem, blockSizeTextItem, filteredBlockSizeTextItem};
    }

    public void rotateCompass(float angle) {
        this.compassItem.setRotation(0, 0, 180 + angle);
    }

    @Override
    public GameItem[] getGameItems() {
        return gameItems;
    }

    public void updateHud(Window window, Camera camera, World world, int filteredBlocksCount) {
        this.versionTextItem.setPosition(window.getWidth() - 100.0f, window.getHeight() - 20f, 0);

        Vector4f cameraPosition = camera.getPosition();
        Vector3f cameraRotation = camera.getRotation();

        this.coordinatesTextItem.setText(String.format("Position [X: %s Y: %s Z: %s]", cameraPosition.x, cameraPosition.y, cameraPosition.z));
        this.coordinatesTextItem.setPosition(20.0f, 40f, 0);

        this.rotationTextItem.setText(String.format("Rotation [X: %s Y: %s Z: %s]", cameraRotation.x, cameraRotation.y, cameraRotation.z));
        this.rotationTextItem.setPosition(20.0f, 70f, 0);

        this.chunkSizeTextItem.setText(String.format("CHUNKS: [%s]", world.getChunkMap().size()));
        this.chunkSizeTextItem.setPosition(20.0f, 100f, 0);

        this.filteredBlockSizeTextItem.setText(String.format("FILTERED BLOCKS: [%s]", filteredBlocksCount));
        this.filteredBlockSizeTextItem.setPosition(20.0f, 130f, 0);

        this.blockSizeTextItem.setText(String.format("TOTAL BLOCKS: [%s]", world.getChunkMap().size() * 8 * 16 * 8));
        this.blockSizeTextItem.setPosition(20.0f, 160f, 0);

        this.compassItem.setPosition(window.getWidth() - 40f, 50f, 0);
    }

    public void updateFps(float fps) {
        this.fpsTextItem.setPosition(20.0f, 10f, 0);
        this.fpsTextItem.setText(String.format("FPS: %s", (int) fps));
    }
}
