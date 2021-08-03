package ru.mycubecraft.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.engine.IHud;
import ru.mycubecraft.engine.SkyBox;
import ru.mycubecraft.engine.graph.FrustumCullingFilter;
import ru.mycubecraft.engine.graph.PointLight;
import ru.mycubecraft.scene.Scene;
import ru.mycubecraft.util.AssetPool;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private final Transformation transformation;
    private final float specularPower;
    private final FrustumCullingFilter frustumFilter;
    private final List<GameItem> filteredItems;
    private Shader shaderProgram;
    private Shader skyBoxShaderProgram;
    private Shader sceneShaderProgram;
    private Shader hudShaderProgram;

    public Renderer() {
        transformation = new Transformation();
        specularPower = 10f;
        frustumFilter = new FrustumCullingFilter();
        filteredItems = new ArrayList<>();
        init();
    }

    public void init() {
        // Create shader
        shaderProgram = AssetPool.getShader("assets/shaders/default.glsl");
        skyBoxShaderProgram = AssetPool.getShader("assets/shaders/skybox.glsl");
        sceneShaderProgram = AssetPool.getShader("assets/shaders/scene.glsl");
        hudShaderProgram = AssetPool.getShader("assets/shaders/hud.glsl");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    public void render(Window window, ArrayList<GameItem> gameItems, World world, Camera camera,
                       SkyBox skyBox, Scene scene, IHud hud, Vector3f ambientLight, PointLight pointLight) {
        clear();
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        // Update projection and view atrices once per render cycle
        transformation.updateProjectionMatrix(window.getWidth(), window.getHeight());
        transformation.updateViewMatrix(camera);

        renderScene(gameItems, world, scene, ambientLight, pointLight);
        //renderSkyBox(skyBox);

        renderHud(window, hud);
    }

    private void renderScene(ArrayList<GameItem> gameItems, World world, Scene scene, Vector3f ambientLight, PointLight pointLight) {
        // clearing for the frustum filter game item list
        filteredItems.clear();
        ArrayList<GameItem> allGameItems = new ArrayList<>(gameItems);
        if (world != null) {
            List<GameItem> gameItemList = world.getChunksBlockItems();
            allGameItems.addAll(gameItemList);
        }
        boolean frustumCulling = true;
        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        Matrix4f viewMatrix = transformation.getViewMatrix();

        if (frustumCulling) {
            frustumFilter.updateFrustum(projectionMatrix, viewMatrix);
            frustumFilter.filter(allGameItems, scene.getCamera());
        }

        sceneShaderProgram.use();

        sceneShaderProgram.uploadMat4f("projectionMatrix", projectionMatrix);

        // Update Light Uniforms
        sceneShaderProgram.uploadVec3f("ambientLight", ambientLight);
        sceneShaderProgram.uploadFloat("specularPower", specularPower);
        sceneShaderProgram.uploadTexture("texture_sampler", 0);
        // Get a copy of the light object and transform its position to view coordinates
        PointLight currPointLight = new PointLight(pointLight);
        Vector3f lightPos = currPointLight.getPosition();
        Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        sceneShaderProgram.setUniform("material", allGameItems.get(0).getMesh().getMaterial());
        sceneShaderProgram.setUniform("pointLight", currPointLight);

        for (GameItem gameItem : allGameItems) {
            if (gameItem.isInsideFrustum()) {
                filteredItems.add(gameItem);
            }
        }

        // Render each filtered in frustum game item
        for (GameItem gameItem : filteredItems) {
            // Set world matrix for this item
            Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(gameItem, viewMatrix);
            sceneShaderProgram.uploadMat4f("modelViewMatrix", modelViewMatrix);
            // Render the mesh for this game item
            gameItem.render();
        }



        // Unbind shader
        sceneShaderProgram.detach();
        //allGameItems.clear();
    }

    private void renderSkyBox(SkyBox skyBox) {
        skyBoxShaderProgram.use();
        skyBoxShaderProgram.uploadTexture("texture_sampler", 0);

        // Update projection Matrix
        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        skyBoxShaderProgram.uploadMat4f("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix();
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);

        Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
        skyBoxShaderProgram.uploadMat4f("modelViewMatrix", modelViewMatrix);

        skyBox.getMesh().render();

        skyBoxShaderProgram.detach();
    }

    private void renderHud(Window window, IHud hud) {
        hudShaderProgram.use();

        // GL_BLEND - the technique to implement transparency within objects
        glEnable(GL_BLEND);
        // GL_SRC_ALPHA - alpha component of the source color vector
        // GL_ONE_MINUS_SRC_ALPHA - 1 = alpha of the source color vector
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);

        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
        for (GameItem gameItem : hud.getGameItems()) {
            Mesh mesh = gameItem.getMesh();
            // Set ortohtaphic and model matrix for this HUD item
            Matrix4f projModelMatrix = transformation.buildOrthoProjModelMatrix(gameItem, ortho);
            hudShaderProgram.uploadMat4f("projModelMatrix", projModelMatrix);
            hudShaderProgram.uploadVec4f("colour", mesh.getMaterial().getAmbientColour());
            hudShaderProgram.uploadInt("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);

            gameItem.render();
        }

        glDisable(GL_BLEND);
        glDepthMask(true);

        hudShaderProgram.detach();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.detach();
        }
        if (skyBoxShaderProgram != null) {
            skyBoxShaderProgram.detach();
        }
        if (sceneShaderProgram != null) {
            sceneShaderProgram.detach();
        }
        if (hudShaderProgram != null) {
            hudShaderProgram.detach();
        }
    }

    public List<GameItem> getFilteredItems() {
        return filteredItems;
    }
}
