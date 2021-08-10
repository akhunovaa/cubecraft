package ru.mycubecraft.scene;

import lombok.Getter;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.engine.SkyBox;
import ru.mycubecraft.engine.graph.weather.Fog;
import ru.mycubecraft.listener.KeyboardListener;
import ru.mycubecraft.listener.MouseListener;
import ru.mycubecraft.renderer.Camera;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.window.Window;
import ru.mycubecraft.world.MouseBoxSelectionDetector;
import ru.mycubecraft.world.World;

import java.util.ArrayList;

@Getter
public abstract class Scene {

    public World world;
    protected Window window = Window.getInstance();
    protected SkyBox skyBox;
    protected Renderer renderer;
    protected Camera camera;
    protected ArrayList<GameItem> gameItems = new ArrayList<>();
    protected Fog fog;

    protected MouseBoxSelectionDetector mouseBoxSelectionDetector;

    protected KeyboardListener keyboardListener = KeyboardListener.getInstance();
    protected MouseListener mouseListener = MouseListener.getInstance();

    public abstract void update(float dt);

    public abstract void init() throws Exception;

    public abstract void render(float dt);

    public abstract void cleanup();

    public abstract void input();

    public SkyBox getSkyBox() {
        return skyBox;
    }

    public void setSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }


    /**
     * @return the fog
     */
    public Fog getFog() {
        return fog;
    }

    /**
     * @param fog the fog to set
     */
    public void setFog(Fog fog) {
        this.fog = fog;
    }

}
