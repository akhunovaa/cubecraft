package ru.mycubecraft;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.renderer.Camera;

public class Player {

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float LEVEL_CAMERA_SPEED = 3.00f;
    public Camera camera;

    public Player() {
        this.camera = new Camera();
        camera.transformation.addPosition(new Vector3f(2f, 15f, 0.0f));
        camera.transformation.addRotation(new Vector3f(0.0f, 0.0f, 0.0f));
    }

    public Player(Camera camera) {
        camera.transformation.addPosition(new Vector3f(2f, 15f, 0.0f));
        camera.transformation.addRotation(new Vector3f(0.0f, 0.0f, 0.0f));
        this.camera = camera;
    }


    public void moveForward(float delta) {
        Quaternionf viewDirection = new Quaternionf();
        viewDirection.setEulerAnglesXYZ(0.0f, -(float) Math.toRadians(camera.getRotation().y), 0.0f);

        Vector4f directionVector = new Vector4f(0f, 0f, -delta * LEVEL_CAMERA_SPEED, FOV);
        directionVector.rotate(viewDirection);
        camera.transformation.position.add(directionVector);
    }

    public void moveBackward(float delta) {
        Quaternionf viewDirection = new Quaternionf();
        viewDirection.setEulerAnglesXYZ(0.0f, -(float) Math.toRadians(camera.getRotation().y), 0.0f);

        Vector4f directionVector = new Vector4f(0f, 0f, delta * LEVEL_CAMERA_SPEED, FOV);
        directionVector.rotate(viewDirection);
        camera.transformation.position.add(directionVector);
    }

    public void moveLeft(float delta) {
        Quaternionf viewDirection = new Quaternionf();
        viewDirection.setEulerAnglesXYZ(0.0f, -(float) Math.toRadians(camera.getRotation().y), 0.0f);

        Vector4f directionVector = new Vector4f(delta * LEVEL_CAMERA_SPEED, 0.0f, 0.0f, FOV);
        directionVector.rotate(viewDirection);
        camera.transformation.position.add(directionVector);
    }

    public void moveRight(float delta) {
        Quaternionf viewDirection = new Quaternionf();
        viewDirection.setEulerAnglesXYZ(0.0f, -(float) Math.toRadians(camera.getRotation().y), 0.0f);

        Vector4f directionVector = new Vector4f(-delta * LEVEL_CAMERA_SPEED, 0.0f, 0.0f, FOV);
        directionVector.rotate(viewDirection);
        camera.transformation.position.add(directionVector);
    }

    public void jump(float delta) {
        Vector4f cameraUp = new Vector4f(0f, delta * LEVEL_CAMERA_SPEED, 0f, FOV);
        camera.transformation.position.add(cameraUp);
    }

    public void sitDown(float delta) {
        Vector4f cameraDown = new Vector4f(0f, -delta * LEVEL_CAMERA_SPEED, 0f, FOV);
        camera.transformation.position.add(cameraDown);
    }

    public void rotateCamera(float cursorPositionX, float cursorPositionY, float delta) {
        camera.rotateCamera(cursorPositionX, cursorPositionY, delta);
    }
}
