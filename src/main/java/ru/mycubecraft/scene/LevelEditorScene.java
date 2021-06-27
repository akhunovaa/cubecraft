package ru.mycubecraft.scene;

public class LevelEditorScene extends Scene {

    private boolean changingScene = false;

    public LevelEditorScene() {
        System.out.println("Entered to a Level Editor Scene");
        window.setBlue(0.2f);
    }

    @Override
    public void update(float dt) {
        //System.out.println("Current FPS: " + (1.0f / dt) + " ");
    }

    @Override
    public void render() {

    }

    @Override
    public void cleanup() {

    }
}
