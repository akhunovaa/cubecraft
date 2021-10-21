package ru.mycubecraft.renderer.cube;

import lombok.Setter;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.util.AssetPool;

import java.io.FileNotFoundException;

@Setter
public class EmptyCube extends Cube {

    private final static String DEFAULT_MODEL_FILE_PATH = "assets/models/cube_empty.obj";
    private final Vector3f position;
    private Mesh mesh;
    private float scale;

    public EmptyCube() {
        this.position = new Vector3f(0, 0, 0);
    }

    @Override
    public void createCube() throws FileNotFoundException {
        Mesh mesh = AssetPool.getMesh(DEFAULT_MODEL_FILE_PATH);
        mesh.setBoundingRadius(1.0f);
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

    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public void render() {
        throw new UnsupportedOperationException("Not implemented here!");
    }

    @Override
    public int shouldRender() {
        return GL11.GL_FALSE;
    }
}




