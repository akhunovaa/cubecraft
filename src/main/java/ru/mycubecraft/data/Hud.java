package ru.mycubecraft.data;

import org.joml.Vector4f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.engine.IHud;
import ru.mycubecraft.engine.Material;
import ru.mycubecraft.engine.TextItem;
import ru.mycubecraft.engine.graph.FontTexture;
import ru.mycubecraft.engine.graph.OBJLoader;
import ru.mycubecraft.window.Window;

import java.awt.*;

public class Hud implements IHud {

    private static final Font FONT = new Font("Arial", Font.PLAIN, 16);

    private static final String CHARSET = "ISO-8859-1";

    private final GameItem[] gameItems;

    private final TextItem versionTextItem;

    private final GameItem compassItem;


    public Hud(String statusText) {

        FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.versionTextItem = new TextItem(statusText, fontTexture);
        this.versionTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.8f, 0.8f, 0.8f, 10f));;

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
        gameItems = new GameItem[]{versionTextItem, compassItem};
    }

    public void rotateCompass(float angle) {
        this.compassItem.setRotation(0, 0, 180 + angle);
    }

    @Override
    public GameItem[] getGameItems() {
        return gameItems;
    }

    public void updateSize(Window window) {
        this.versionTextItem.setPosition(window.getWidth() - 100.0f, window.getHeight() - 20f, 0);
        this.compassItem.setPosition(window.getWidth() - 40f, 50f, 0);
    }
}
