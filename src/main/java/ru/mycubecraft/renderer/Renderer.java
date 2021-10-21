package ru.mycubecraft.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.block.Block;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.engine.IHud;
import ru.mycubecraft.engine.SkyBox;
import ru.mycubecraft.engine.graph.DirectionalLight;
import ru.mycubecraft.engine.graph.FrustumCullingFilter;
import ru.mycubecraft.engine.graph.weather.Fog;
import ru.mycubecraft.scene.Scene;
import ru.mycubecraft.util.AssetPool;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.BlockField;
import ru.mycubecraft.world.Chunk;
import ru.mycubecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        filteredItems = Collections.synchronizedList(new ArrayList<>());
    }

    public void init() {
        // Create shader
        shaderProgram = AssetPool.getShader("assets/shaders/default.glsl");
        skyBoxShaderProgram = AssetPool.getShader("assets/shaders/skybox.glsl");
        sceneShaderProgram = AssetPool.getShader("assets/shaders/scene.glsl");
        hudShaderProgram = AssetPool.getShader("assets/shaders/hud.glsl");
        //fogShaderProgram = AssetPool.getShader("assets/shaders/fog.glsl");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    public void render(World world, Camera camera, Scene scene, IHud hud, Vector3f ambientLight) {
        clear();
        filteredItems.clear();

        Window window = Window.getInstance();
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        // Update projection and view atrices once per render cycle
        transformation.updateProjectionMatrix(window.getWidth(), window.getHeight());
        transformation.updateViewMatrix(camera);

        renderScene(world, scene, ambientLight);
        renderHud(window, hud);
    }

    private void renderScene(World world, Scene scene, Vector3f ambientLight) {

        boolean frustumCulling = true;
        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        Matrix4f viewMatrix = transformation.getViewMatrix();

        if (frustumCulling) {
            frustumFilter.updateFrustum(projectionMatrix, viewMatrix);
        }

        if (world != null) {
            Map<String, Chunk> worldChunkMap = world.getChunkMap();
            for (Chunk chunk : worldChunkMap.values()) {
                BlockField blockField = chunk.getBlockField();
                if (blockField == null || blockField.getBlocks() == null) {
                    continue;
                }
                Map<String, Block> blocks = blockField.getBlocks();
                blocks.values()
                        .parallelStream()
                        .filter(block ->
                                block.getGameCubeItem() != null
                                        && block.isVisible()
                                        && !block.isDisableFrustumCulling())
                        .filter(frustumFilter::filter)
                        .forEach(block -> filteredItems.add(block.getGameCubeItem()));
            }
        }

        sceneShaderProgram.use();

        sceneShaderProgram.uploadMat4f("projectionMatrix", projectionMatrix);

        // Update Light Uniforms
        renderLights(ambientLight);

        sceneShaderProgram.uploadInt("texture_sampler", 0);
        sceneShaderProgram.setUniform("fog", scene.isFogLButtonPressed() ? scene.getFog() : Fog.NOFOG);
        if (!filteredItems.isEmpty()) {
            sceneShaderProgram.setUniform("material", filteredItems.get(0).getMesh().getMaterial());
        }
        // Render each filtered in frustum game item
        for (GameItem gameItem : filteredItems) {
            // Set world matrix for this item
            Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(gameItem, viewMatrix);
            sceneShaderProgram.uploadMat4f("modelViewMatrix", modelViewMatrix);

            // Render the mesh for this game item
            gameItem.render();
        }
        sceneShaderProgram.uploadFloat("selected", 1.0f);

        // Unbind shader
        sceneShaderProgram.detach();
    }

    private void renderLights(Vector3f ambientLight) {
        sceneShaderProgram.uploadVec3f("ambientLight", ambientLight);
        sceneShaderProgram.uploadFloat("specularPower", specularPower);

    }


    private void renderSkyBox(Scene scene, Vector3f ambientLight, DirectionalLight directionalLight) {
        SkyBox skyBox = scene.getSkyBox();

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
        skyBoxShaderProgram.setUniform("ambientLight", ambientLight);
        sceneShaderProgram.setUniform("fog", scene.isFogLButtonPressed() ? scene.getFog() : Fog.NOFOG);

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(directionalLight);
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        sceneShaderProgram.setUniform("directionalLight", currDirLight);

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

        if (filteredItems != null) {
            filteredItems.clear();
        }
    }

    public List<GameItem> getFilteredItems() {
        return filteredItems;
    }
}
