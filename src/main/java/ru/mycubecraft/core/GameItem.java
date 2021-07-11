package ru.mycubecraft.core;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class GameItem implements RenderObject {

    private Mesh mesh;
    private final Vector3f position;
    private final Vector3f rotation;
    private float scale;

    public GameItem() {
        position = new Vector3f();
        scale = 1;
        rotation = new Vector3f();
    }

    public GameItem(Mesh mesh) {
        this.mesh = mesh;
        position = new Vector3f(0, 0, 0);
        scale = 1;
        rotation = new Vector3f(0, 0, 0);
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

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
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
        return GL11.GL_TRUE;
    }

    @Override
    public void cleanup() {
        mesh.cleanUp();

    }
}
