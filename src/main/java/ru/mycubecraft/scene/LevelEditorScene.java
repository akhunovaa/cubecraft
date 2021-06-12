package ru.mycubecraft.scene;

import java.awt.event.KeyEvent;

public class LevelEditorScene extends Scene {

    private boolean changingScene = false;

    public LevelEditorScene() {
        System.out.println("Entered to a Level Editor Scene");
        window.setBlue(0.2f);
    }

    @Override
    public void update(float dt) {
        //System.out.println("Current FPS: " + (1.0f / dt) + " ");
        if (!changingScene && keyboardListener.isKeyPressed(KeyEvent.VK_SPACE)) {
            this.changingScene = true;
        }

        if (changingScene) {
            window.changeScene(1);
        }
    }
}
