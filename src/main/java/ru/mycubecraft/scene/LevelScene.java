package ru.mycubecraft.scene;

import java.awt.event.KeyEvent;

public class LevelScene extends Scene {

    private boolean changingScene = false;

    public LevelScene() {
        System.out.println("Entered to a Level Scene");
        window.setBlue(1.0f);
    }

    @Override
    public void update(float dt) {
        //System.out.println((1.0f / dt) + " FPS");
        if (!changingScene && keyboardListener.isKeyPressed(KeyEvent.VK_SPACE)) {
            this.changingScene = true;
        }

        if (changingScene) {
            window.changeScene(0);
        }
    }
}
