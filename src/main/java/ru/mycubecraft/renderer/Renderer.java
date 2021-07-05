package ru.mycubecraft.renderer;

import org.joml.Matrix4f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.util.AssetPool;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.World;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private final Transformation transformation;
    private Shader shaderProgram;

    public Renderer() {
        transformation = new Transformation();
        init();
    }

    public void init() {
        // Create shader
        shaderProgram = AssetPool.getShader("assets/shaders/default.glsl");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, ArrayList<GameItem> gameItems, World world, Camera camera) {
        clear();
        ArrayList<GameItem> allGameItems = new ArrayList<>(gameItems);
        if (world != null) {
            allGameItems.addAll(world.renderItems());
        }
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.use();

        // Update projection Matrix
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(window.getWidth(), window.getHeight());
        shaderProgram.uploadMat4f("projectionMatrix", projectionMatrix);
        shaderProgram.uploadTexture("texture_sampler", 0);

        // Render each gameItem
        for (GameItem gameItem : allGameItems) {
            // Set world matrix for this item
            Matrix4f worldMatrix = transformation.getWorldMatrix(
                    gameItem.getPosition(),
                    gameItem.getRotation(),
                    gameItem.getScale(),
                    camera);
            shaderProgram.uploadMat4f("worldMatrix", worldMatrix);

            // Render the mesh for this game item
            gameItem.render();
        }

        // Unbind shader
        shaderProgram.detach();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.detach();
        }
    }
}
