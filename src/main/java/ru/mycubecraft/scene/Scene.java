package ru.mycubecraft.scene;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.mycubecraft.DelayedRunnable;
import ru.mycubecraft.Settings;
import ru.mycubecraft.core.GameItem;
import ru.mycubecraft.engine.SkyBox;
import ru.mycubecraft.engine.graph.weather.Fog;
import ru.mycubecraft.engine.sound.SoundManager;
import ru.mycubecraft.listener.KeyboardListener;
import ru.mycubecraft.listener.MouseListener;
import ru.mycubecraft.renderer.Renderer;
import ru.mycubecraft.world.World;
import ru.mycubecraft.world.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static ru.mycubecraft.Game.DEBUG;

@Slf4j
@Getter
public abstract class Scene {

    /**
     * Tasks that must be run on the update/render thread, usually because they involve calling OpenGL
     * functions.
     */
    protected final Queue<DelayedRunnable> updateAndRenderRunnables = new ConcurrentLinkedQueue<>();

    public World world;
    //    protected Window window = Window.getInstance();
    protected SkyBox skyBox;
    protected Renderer renderer;
    protected Player player;
    protected ArrayList<GameItem> gameItems = new ArrayList<>();
    protected Fog fog;
    protected SoundManager soundMgr;
    protected boolean fogLButtonPressed = Settings.SHOW_FOG;
    protected KeyboardListener keyboardListener = KeyboardListener.getInstance();
    protected MouseListener mouseListener = MouseListener.getInstance();

    public abstract void update(float dt);

    public abstract void runUpdateAndRenderLoop();

    public abstract void render();

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

    /**
     * Process all update/render thread tasks in the {@link #updateAndRenderRunnables} queue.
     */
    void drainRunnables() {
        Iterator<DelayedRunnable> it = updateAndRenderRunnables.iterator();
        while (it.hasNext()) {
            DelayedRunnable dr = it.next();
            int delay = dr.getDelay();
            /* Check if we want to delay this runnable */
            if (delay > 0) {
                log.debug("Delaying runnable [" + dr.getName() + "] for " + dr.getDelay() + " frames");
                delay--;
                dr.setDelay(delay);
                continue;
            }
            try {
                /* Remove from queue and execute */
                it.remove();
                dr.getRunnable().call();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
