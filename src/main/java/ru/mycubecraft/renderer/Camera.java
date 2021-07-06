package ru.mycubecraft.renderer;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mycubecraft.data.Settings;
import ru.mycubecraft.util.MathUtil;

public class Camera {

    public Vector4f position;
    public Vector3f rotation;

    public Camera() {
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

//    public void rotateCamera(float mouseDx, float mouseDy, float delta) {
//        System.out.println("===================================");
//
//        System.out.println("===================================");
//        float y = Math.max(Settings.MIN_LOOK, Math.min(Settings.MAX_LOOK, mouseDx * Settings.MOUSE_SENSITIVITY * delta));
//        this.rotation.x =+ (float) Math.max(Math.min(this.rotation.x - (mouseDy -Settings.HEIGHT / 2.0f) / 4.0f, 90.0f), -90.0f);
//        this.rotation.y =+ (float) (rotation.y - (mouseDx - Settings.WIDTH / 2.0f) / 4.0f);
//        //this.rotation.add(rotX * Settings.MOUSE_SENSITIVITY * delta, rotY, 0.0f);
//    }

    public void rotateCamera(float mouseDx, float mouseDy, float delta) {
        this.rotation.x = MathUtil.clamp(this.rotation.x, Settings.MIN_LOOK, Settings.MAX_LOOK);
        float xRotation = this.rotation.x;
        float yRotation = this.rotation.y;
        if (xRotation > Settings.MIN_LOOK) {
            rotation.x += mouseDy * delta;
            rotation.y += mouseDx * delta;
            rotation.z += 0.0f;
        } else {
            rotation.x -= mouseDy * delta;
            rotation.y -= mouseDx * delta;
            rotation.z -= 0.0f;
        }

    }

    public Vector4f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotation.x += offsetX;
        rotation.y += offsetY;
        rotation.z += offsetZ;
    }

}
