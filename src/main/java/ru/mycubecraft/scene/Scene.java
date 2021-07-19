package ru.mycubecraft.scene;

import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.engine.SceneLight;
import ru.mycubecraft.engine.SkyBox;
import ru.mycubecraft.listener.KeyboardListener;
import ru.mycubecraft.listener.MouseListener;
import ru.mycubecraft.player.Player;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Scene {

    public World world;
    protected Window window = Window.getInstance();
    protected SkyBox skyBox;
    protected SceneLight sceneLight;
    protected Renderer renderer;
    protected Camera camera;
    protected Player player;
    protected ArrayList<GameItem> gameItems = new ArrayList<>();
    protected Map<Mesh, List<GameItem>> meshMap = new HashMap<>();

    protected KeyboardListener keyboardListener = KeyboardListener.getInstance();
    protected MouseListener mouseListener = MouseListener.getInstance();

    public abstract void update(float dt);

    public abstract void init() throws Exception;

    public abstract void render();

    public abstract void cleanup();

    public SkyBox getSkyBox() {
        return skyBox;
    }

    public void setSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }

    public SceneLight getSceneLight() {
        return sceneLight;
    }

    public void setSceneLight(SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }

    public Map<Mesh, List<GameItem>> getGameMeshes() {
        return meshMap;
    }
}
