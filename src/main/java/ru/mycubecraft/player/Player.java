package ru.mycubecraft.player;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.data.Settings;

public class Player {

    public Vector4f position;
    public Vector3f rotation;

    public Player() {
        position = new Vector4f(2f, 15f, 0.0f, 1);
        rotation = new Vector3f(0, 0, 0);
    }

    public void moveForward(float delta) {
        Quaternionf viewDirection = new Quaternionf();
        viewDirection.setEulerAnglesXYZ(0.0f, -(float) Math.toRadians(rotation.y), 0.0f);

        Vector4f directionVector = new Vector4f(0f, 0f, -delta * Settings.MOVE_SPEED, Settings.FOV);
        directionVector.rotate(viewDirection);
        this.position.add(directionVector);
    }

    public void moveBackward(float delta) {
        Quaternionf viewDirection = new Quaternionf();
        viewDirection.setEulerAnglesXYZ(0.0f, -(float) Math.toRadians(rotation.y), 0.0f);

        Vector4f directionVector = new Vector4f(0f, 0f, delta * Settings.MOVE_SPEED, Settings.FOV);
        directionVector.rotate(viewDirection);
        this.position.add(directionVector);
    }

    public void moveLeft(float delta) {
        Quaternionf viewDirection = new Quaternionf();
        viewDirection.setEulerAnglesXYZ(0.0f, -(float) Math.toRadians(rotation.y), 0.0f);

        Vector4f directionVector = new Vector4f(delta * Settings.MOVE_SPEED, 0.0f, 0.0f, Settings.FOV);
        directionVector.rotate(viewDirection);
        this.position.add(directionVector);
    }

    public void moveRight(float delta) {
        Quaternionf viewDirection = new Quaternionf();
        viewDirection.setEulerAnglesXYZ(0.0f, -(float) Math.toRadians(rotation.y), 0.0f);

        Vector4f directionVector = new Vector4f(-delta * Settings.MOVE_SPEED, 0.0f, 0.0f, Settings.FOV);
        directionVector.rotate(viewDirection);
        this.position.add(directionVector);
    }

    public void jump(float delta) {
        Vector4f cameraUp = new Vector4f(0f, delta * Settings.MOVE_SPEED, 0f, Settings.FOV);
        this.position.add(cameraUp);
    }

    public void sitDown(float delta) {
        Vector4f cameraDown = new Vector4f(0f, -delta * Settings.MOVE_SPEED, 0f, Settings.FOV);
        this.position.add(cameraDown);
    }
}
