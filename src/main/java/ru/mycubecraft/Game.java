package ru.mycubecraft;

import org.lwjgl.opengl.GL;
import ru.mycubecraft.window.Window;

public class Game {

    public static void main(String[] args) {
        /*
        Headless mode is mainly useful in those systems that don't have a graphical display, typically the servers.
        Many applications use graphical displays to do things that are not necessarily needed to be seen, for instance drawing an image and then saving it to disk.
        if you run such a program on a server (ssh connections only, no graphic endpoint) you get an exception in normal mode, you get the program ran in headless mode.
         */
        System.setProperty("java.awt.headless", "true");
        Window gameWindow = Window.getInstance();
        gameWindow.run();
    }

    /**
     * Determines if the OpenGL context supports version 3.2.
     *
     * @return true, if OpenGL context supports version 3.2, else false
     */
    public static boolean isDefaultContext() {
        return GL.getCapabilities().OpenGL32;
    }
}
