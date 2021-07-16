package ru.mycubecraft;

import ru.mycubecraft.engine.GameEngine;
import ru.mycubecraft.engine.IGameLogic;

public class Game {

    public static void main(String[] args) {
        try {
            boolean vSync = true;
            IGameLogic gameLogic = new DummyGame();
            GameEngine gameEng = new GameEngine(Settings.WINDOW_TITLE, Settings.WIDTH, Settings.HEIGHT, vSync, gameLogic);
            gameEng.run();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}
