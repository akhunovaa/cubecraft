package ru.mycubecraft.scene;

import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.listener.KeyboardListener;
import ru.mycubecraft.listener.MouseListener;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.World;

import java.util.ArrayList;

public abstract class Scene {
    public static final float FOV = (float) Math.toRadians(60.0f);

    public World world;
    protected Window window = Window.getInstance();
    protected KeyboardListener keyboardListener = KeyboardListener.getInstance();
    protected MouseListener mouseListener = MouseListener.getInstance();
    protected Camera camera = new Camera();
    protected Renderer renderer;
    protected ArrayList<GameItem> gameItems = new ArrayList<>();

    public abstract void update(float dt);

    public abstract void render();

    public abstract void cleanup();

}
