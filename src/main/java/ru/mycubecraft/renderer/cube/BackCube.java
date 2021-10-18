package ru.mycubecraft.renderer.cube;

import lombok.Setter;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.engine.Material;
import ru.mycubecraft.renderer.Texture;
import ru.mycubecraft.util.AssetPool;

import java.io.FileNotFoundException;

@Setter
public class BackCube extends Cube {

    private final static String DEFAULT_MODEL_FILE_PATH = "assets/models/cube_back.obj";
    private final Vector3f position;
    private final Vector3f rotation;
    private Mesh mesh;
    private float scale;
    private String texture;

    public BackCube() {
        this.position = new Vector3f(0, 0, 0);
        this.scale = 1;
        this.rotation = new Vector3f(0, 0, 0);
        this.texture = "assets/textures/white.png";
    }

    @Override
    public void createCube() throws FileNotFoundException {
        Texture texture = AssetPool.getTexture(this.texture);
        Mesh mesh = AssetPool.getMesh(DEFAULT_MODEL_FILE_PATH);
        mesh.setBoundingRadius(1.0f);
        mesh.setMaterial(new Material(texture, 0.0f));
        this.mesh = mesh;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public void render() {
        mesh.render();

    }

    @Override
    public int shouldRender() {
        // TODO Auto-generated method stub
        return GL11.GL_TRUE;
    }
}




