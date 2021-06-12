package ru.mycubecraft;

import ru.mycubecraft.window.Window;

public class Game {

    public static void main(String[] args) {
        Window gameWindow = Window.getInstance();
        gameWindow.run();
    }
}
