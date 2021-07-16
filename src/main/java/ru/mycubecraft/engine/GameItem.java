package ru.mycubecraft.engine;

import org.joml.Vector3f;
import ru.mycubecraft.engine.graph.Mesh;

public class GameItem {

    private Mesh mesh;
    
    private final Vector3f position;
    
    private float scale;

    private final Vector3f rotation;

    public GameItem() {
        position = new Vector3f();
        scale = 1;
        rotation = new Vector3f();
    }
    
    public GameItem(Mesh mesh) {
        this();
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
    
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }
}
