package ru.mycubecraft.data;

import lombok.Getter;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.engine.IHud;
import ru.mycubecraft.engine.Material;
import ru.mycubecraft.engine.TextItem;
import ru.mycubecraft.engine.graph.FontTexture;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.util.AssetPool;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.World;

import java.awt.*;

@Getter
public class Hud implements IHud {

    private static final Font FONT = new Font("Arial", Font.PLAIN, 16);
    private static final String CHARSET = "ISO-8859-1";
    private final FontTexture fontTexture;
    private GameItem[] gameItems;

    private TextItem versionTextItem;
    private TextItem fpsTextItem;
    private TextItem coordinatesTextItem;
    private TextItem rotationTextItem;
    private TextItem chunkSizeTextItem;
    private TextItem filteredBlockSizeTextItem;
    private TextItem blockSizeTextItem;
    private TextItem crossHairTextItem;
    private TextItem targetObjectItem;

    private GameItem compassItem;


    public Hud() {
        this.fontTexture = new FontTexture(FONT, CHARSET);
    }

    public void buildHud() {
        fontTexture.buildTexture();

        String version = "ALPHA 0.1.2";
        String fpsText = "FPS: 0";
        String coordinatesText = "X: 0.00 Y: 0.00 Z: 0.00";
        String createdChunksSizeText = "CHUNKS: 0";
        String filteredBlockSizeText = "FILTERED BLOCKS: 0";
        String createdBlocksSizeText = "BLOCKS: 0";
        String crossHair = "+";
        String targetObjectInfo = "TARGET OBJECT [X: 0.00 Y: 0.00 Z: 0.00]";

        this.fpsTextItem = new TextItem(fpsText, fontTexture);
        this.fpsTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(1f, 0f, 0f, 10f));

        this.coordinatesTextItem = new TextItem(coordinatesText, fontTexture);
        this.coordinatesTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));

        this.rotationTextItem = new TextItem(coordinatesText, fontTexture);
        this.rotationTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));

        this.chunkSizeTextItem = new TextItem(createdChunksSizeText, fontTexture);
        this.chunkSizeTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));

        this.filteredBlockSizeTextItem = new TextItem(filteredBlockSizeText, fontTexture);
        this.filteredBlockSizeTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));

        this.blockSizeTextItem = new TextItem(createdBlocksSizeText, fontTexture);
        this.blockSizeTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));

        this.versionTextItem = new TextItem(version, fontTexture);
        this.versionTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));

        this.crossHairTextItem = new TextItem(crossHair, fontTexture);
        this.crossHairTextItem.setScale(2);

        this.targetObjectItem = new TextItem(targetObjectInfo, fontTexture);
        this.targetObjectItem.setPosition(20.0f, 190f, 0);

        // Create compass
        Mesh mesh = null;
        try {
            mesh = AssetPool.getMesh("assets/models/compass.obj");
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
                chunkSizeTextItem, blockSizeTextItem, filteredBlockSizeTextItem,
                crossHairTextItem, targetObjectItem};
    }


    public void rotateCompass(float angle) {
        this.compassItem.setRotation(0, 0, 180 + angle);
    }

    @Override
    public GameItem[] getGameItems() {
        return gameItems;
    }

    public void updateHud(Camera camera, World world, int filteredBlocksCount) {
        Window window = Window.getInstance();
        this.versionTextItem.setPosition(window.getWidth() - 100.0f, window.getHeight() - 20f, 0);

        Vector4f cameraPosition = camera.getPosition();
        Vector3f cameraRotation = camera.getRotation();

        this.coordinatesTextItem.setText(String.format("Position [X: %s Y: %s Z: %s]", cameraPosition.x, cameraPosition.y, cameraPosition.z));
        this.coordinatesTextItem.setPosition(20.0f, 40f, 0);

        this.rotationTextItem.setText(String.format("Rotation [X: %s Y: %s Z: %s]", cameraRotation.x, cameraRotation.y, cameraRotation.z));
        this.rotationTextItem.setPosition(20.0f, 70f, 0);

        this.chunkSizeTextItem.setText(String.format("CHUNKS: [%s]", world.getChunkMap().size()));
        this.chunkSizeTextItem.setPosition(20.0f, 100f, 0);

        this.filteredBlockSizeTextItem.setText(String.format("CULLED BLOCKS: [%s]", filteredBlocksCount));
        this.filteredBlockSizeTextItem.setPosition(20.0f, 130f, 0);

        this.blockSizeTextItem.setText(String.format("TOTAL BLOCKS: [%s]", world.getChunkMap().size() * 8 * 16 * 8));
        this.blockSizeTextItem.setPosition(20.0f, 160f, 0);

        int width = window.getWidth();
        int height = window.getHeight();

        // height/2f - 22.0f window height minus font texture height (19.0f) and element scale (3)
        this.crossHairTextItem.setPosition(width / 2f, height / 2f, 0);

        this.compassItem.setPosition(window.getWidth() - 40f, 50f, 0);
        setColor();
    }

    public void setColor() {
        this.fpsTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(1f, 0f, 0f, 10f));
        this.coordinatesTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));
        this.rotationTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));
        this.chunkSizeTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));
        this.filteredBlockSizeTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));
        this.blockSizeTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));
        this.versionTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));
    }

    public void updateFps(float fps) {
        this.fpsTextItem.setPosition(20.0f, 10f, 0);
        this.fpsTextItem.setText(String.format("FPS: %s", (int) fps));
        this.fpsTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));
    }

    public void updateTargetObjectInfo(Vector3f vector3f) {
        this.targetObjectItem.setPosition(20.0f, 190f, 0);
        this.targetObjectItem.setText(String.format("TARGET OBJECT [X: %s Y: %s Z: %s]", vector3f.x, vector3f.y, vector3f.z));
        this.targetObjectItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0f, 0f, 1f));
    }
}
