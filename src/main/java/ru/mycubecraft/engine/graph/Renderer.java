package ru.mycubecraft.engine.graph;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.engine.*;
import ru.mycubecraft.renderer.Shader;
import ru.mycubecraft.util.AssetPool;

import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    /**
     * Field of View in Radians
     */
    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.f;

    private static final int MAX_POINT_LIGHTS = 5;

    private static final int MAX_SPOT_LIGHTS = 5;

    private final Transformation transformation;

    private Shader sceneShaderProgram;

    private Shader hudShaderProgram;

    private Shader skyBoxShaderProgram;

    private final float specularPower;

    public Renderer() {
        transformation = new Transformation();
        specularPower = 10f;
    }

    public void init(Window window) {
        // Create shader
        skyBoxShaderProgram = AssetPool.getShader("assets/shaders/skybox.glsl");
        hudShaderProgram = AssetPool.getShader("assets/shaders/hud.glsl");
        sceneShaderProgram = AssetPool.getShader("assets/shaders/scene.glsl");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Camera camera, Scene scene, IHud hud) {
        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        // Update projection and view atrices once per render cycle
        transformation.updateProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        transformation.updateViewMatrix(camera);

        renderScene(window, camera, scene);

        renderSkyBox(window, camera, scene);

        renderHud(window, hud);
    }

    private void renderSkyBox(Window window, Camera camera, Scene scene) {
        clear();
        skyBoxShaderProgram.use();
        skyBoxShaderProgram.uploadTexture("texture_sampler", 0);

        // Update projection Matrix
        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        skyBoxShaderProgram.uploadMat4f("projectionMatrix", projectionMatrix);
        SkyBox skyBox = scene.getSkyBox();

        Matrix4f viewMatrix = transformation.getViewMatrix();
//        viewMatrix.m30 = 0;
//        viewMatrix.m31 = 0;
//        viewMatrix.m32 = 0;
        skyBoxShaderProgram.uploadMat4f("viewMatrix", viewMatrix);

        Matrix4f modelMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
        skyBoxShaderProgram.uploadMat4f("modelMatrix", modelMatrix);
        skyBoxShaderProgram.uploadVec3f("ambientLight", scene.getSceneLight().getAmbientLight());

        scene.getSkyBox().getMesh().render();

        skyBoxShaderProgram.detach();
    }

    public void renderScene(Window window, Camera camera, Scene scene) {
        clear();
        skyBoxShaderProgram.use();

        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        sceneShaderProgram.uploadMat4f("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix();

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

    private void renderHud(Window window, IHud hud) {
        hudShaderProgram.use();

        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
        for (GameItem gameItem : hud.getGameItems()) {
            Mesh mesh = gameItem.getMesh();
            // Set ortohtaphic and model matrix for this HUD item
            Matrix4f projModelMatrix = transformation.buildOrthoProjModelMatrix(gameItem, ortho);
            hudShaderProgram.uploadMat4f("projModelMatrix", projModelMatrix);
            hudShaderProgram.uploadVec4f("colour", gameItem.getMesh().getMaterial().getAmbientColour());
            hudShaderProgram.uploadInt("hasTexture", gameItem.getMesh().getMaterial().isTextured() ? 1 : 0);

            // Render the mesh for this HUD item
            mesh.render();
        }

        hudShaderProgram.detach();
    }

    public void cleanup() {
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
}
