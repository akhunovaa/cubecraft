package ru.mycubecraft.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.engine.SceneLight;
import ru.mycubecraft.engine.SkyBox;
import ru.mycubecraft.engine.graph.DirectionalLight;
import ru.mycubecraft.engine.graph.PointLight;
import ru.mycubecraft.engine.graph.SpotLight;
import ru.mycubecraft.scene.Scene;
import ru.mycubecraft.util.AssetPool;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private final Transformation transformation;
    private final float specularPower;
    private Shader shaderProgram;
    private Shader skyBoxShaderProgram;
    private Shader sceneShaderProgram;

    public Renderer() {
        transformation = new Transformation();
        specularPower = 10f;
        init();
    }

    public void init() {
        // Create shader
        shaderProgram = AssetPool.getShader("assets/shaders/default.glsl");
        skyBoxShaderProgram = AssetPool.getShader("assets/shaders/skybox.glsl");
        sceneShaderProgram = AssetPool.getShader("assets/shaders/scene.glsl");
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

        Matrix4f viewMatrix = transformation.getViewMatrix(camera);
        shaderProgram.uploadMat4f("viewMatrix", viewMatrix);
        // Render each gameItem
        for (GameItem gameItem : allGameItems) {

            // Set world matrix for this item
            Matrix4f modelMatrix = transformation.getModelMatrix(gameItem.getPosition(), gameItem.getRotation(), gameItem.getScale());
            shaderProgram.uploadMat4f("modelMatrix", modelMatrix);
            // Render the mesh for this game item
            gameItem.render();
        }

        // Unbind shader
        shaderProgram.detach();
    }

    public void renderSkyBox(Window window, Camera camera, SkyBox skyBox, SceneLight sceneLight) {
        clear();
        skyBoxShaderProgram.use();
        skyBoxShaderProgram.uploadTexture("texture_sampler", 0);

        // Update projection Matrix
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(window.getWidth(), window.getHeight());
        skyBoxShaderProgram.uploadMat4f("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix(camera);
        viewMatrix.m30 = 0;
        viewMatrix.m31 = 0;
        viewMatrix.m32 = 0;
        skyBoxShaderProgram.uploadMat4f("viewMatrix", viewMatrix);

        Matrix4f modelMatrix = transformation.getModelMatrix(skyBox.getPosition(), skyBox.getRotation(), skyBox.getScale());

        skyBoxShaderProgram.uploadMat4f("modelMatrix", modelMatrix);
        skyBoxShaderProgram.uploadVec3f("ambientLight", sceneLight.getAmbientLight());

        skyBox.getMesh().render();

        skyBoxShaderProgram.detach();
    }

    public void renderScene(Window window, Camera camera, Scene scene) {
        clear();
        skyBoxShaderProgram.use();

        Matrix4f projectionMatrix = transformation.getProjectionMatrix(window.getWidth(), window.getHeight());
        sceneShaderProgram.uploadMat4f("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        SceneLight sceneLight = scene.getSceneLight();
        renderLights(viewMatrix, sceneLight);

        sceneShaderProgram.uploadTexture("texture_sampler", 0);
        // Render each mesh with the associated game Items
        Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            sceneShaderProgram.setUniform("material", mesh.getMaterial());
            mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
                        Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(gameItem, viewMatrix);
                        sceneShaderProgram.uploadMat4f("modelViewMatrix", modelViewMatrix);
                    }
            );
        }

        skyBoxShaderProgram.detach();
    }

    private void renderLights(Matrix4f viewMatrix, SceneLight sceneLight) {

        sceneShaderProgram.uploadVec3f("ambientLight", sceneLight.getAmbientLight());
        sceneShaderProgram.uploadFloat("specularPower", specularPower);

        // Process Point Lights
        PointLight[] pointLightList = sceneLight.getPointLightList();
        int numLights = pointLightList != null ? pointLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the point light object and transform its position to view coordinates
            PointLight currPointLight = new PointLight(pointLightList[i]);
            Vector3f lightPos = currPointLight.getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            sceneShaderProgram.setUniform("pointLights", currPointLight, i);
        }

        // Process Spot Ligths
        SpotLight[] spotLightList = sceneLight.getSpotLightList();
        numLights = spotLightList != null ? spotLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the spot light object and transform its position and cone direction to view coordinates
            SpotLight currSpotLight = new SpotLight(spotLightList[i]);
            Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
            dir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));

            Vector3f lightPos = currSpotLight.getPointLight().getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;

            sceneShaderProgram.setUniform("spotLights", currSpotLight, i);
        }

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        sceneShaderProgram.setUniform("directionalLight", currDirLight);
    }


    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.detach();
        }
        if (skyBoxShaderProgram != null) {
            skyBoxShaderProgram.detach();
        }
    }
}
