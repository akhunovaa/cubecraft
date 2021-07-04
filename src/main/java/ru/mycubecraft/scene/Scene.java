package ru.mycubecraft.scene;

import ru.mycubecraft.Player;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.listener.KeyboardListener;
import ru.mycubecraft.listener.MouseListener;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.World;

import java.util.ArrayList;

public abstract class Scene {

    public World world;
    protected Window window = Window.getInstance();
    protected Renderer renderer;
    protected Player player;
    protected ArrayList<GameItem> gameItems = new ArrayList<>();

    protected KeyboardListener keyboardListener = KeyboardListener.getInstance();
    protected MouseListener mouseListener = MouseListener.getInstance();

    public abstract void update(float dt);

    public abstract void render();

    public abstract void cleanup();

}
