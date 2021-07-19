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

    private static final Font FONT = new Font("Arial", Font.PLAIN, 20);

    private static final String CHARSET = "UTF-8";

    private final GameItem[] gameItems;

    private TextItem statusTextItem;

    private final GameItem compassItem;

    public Hud(String statusText) throws Exception {
//        FontTexture fontTexture = new FontTexture(FONT, CHARSET);
//        fontTexture.buildTexture();
//
//        this.statusTextItem = new TextItem(statusText);
//        this.statusTextItem.setFontTexture(fontTexture);
//
//        Mesh statusItemMesh = this.statusTextItem.buildMesh();
//        statusItemMesh.getMaterial().setAmbientColour(new Vector4f(1, 1, 1, 1));
//        this.statusTextItem.setMesh(statusItemMesh);

        // Create compass
        Mesh mesh = OBJLoader.loadMesh("assets/models/compass.obj");

        Material material = new Material();
        material.setAmbientColour(new Vector4f(1, 0, 0, 1));

        mesh.setMaterial(material);

        compassItem = new GameItem(mesh);
        compassItem.setScale(40.0f);
        // Rotate to transform it to screen coordinates
        compassItem.setRotation(0f, 0f, 180f);
        // Create list that holds the items that compose the HUD
        gameItems = new GameItem[]{compassItem};
    }

    public void setStatusText(String statusText) {
        this.statusTextItem.setText(statusText);
    }
    
    public void rotateCompass(float angle) {
        this.compassItem.setRotation(0, 0, 180 + angle);
    }

    @Override
    public GameItem[] getGameItems() {
        return gameItems;
    }
   
    public void updateSize(Window window) {
        //this.statusTextItem.setPosition(10f, window.getHeight() - 50f, 0);
        this.compassItem.setPosition(window.getWidth() - 40f, 50f, 0);
    }
}
