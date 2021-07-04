package ru.mycubecraft.world;

import ru.mycubecraft.core.GameItem;

import java.util.ArrayList;

public class World {
    public static final int WORLD_WIDTH = 16;
    public static final int WORLD_HEIGHT = 64;
    public Chunk[][] chunks = new Chunk[WORLD_WIDTH][WORLD_WIDTH];
    public Generator gen = new SuperFlatGen();

    public World(Generator gen2) {
        gen = gen2;
        genChunk(WORLD_WIDTH / 2, WORLD_WIDTH / 2);
        genChunk(WORLD_WIDTH / 2 - 1, WORLD_WIDTH / 2 - 1);
        genChunk(WORLD_WIDTH / 2, WORLD_WIDTH / 2 - 1);
        genChunk(WORLD_WIDTH / 2 - 1, WORLD_WIDTH / 2);
    }

    public Chunk getChunk(int cX, int cZ) {
        return chunks[cX][cZ];
    }

    private void genChunk(int cX, int cZ) {
        if (cX < WORLD_WIDTH && cZ < WORLD_WIDTH && cX > -1 && cZ > -1) {
            if (chunks[cX][cZ] == null) {
                chunks[cX][cZ] = new Chunk(cX - WORLD_WIDTH / 2, cZ - WORLD_WIDTH / 2, gen);
            }
        }
    }

    public void render() {
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int z = 0; z < WORLD_WIDTH; z++) {
                if (chunks[x][z] != null) {
                    chunks[x][z].render();
                }
            }
        }
    }

    public ArrayList<GameItem> renderItems() {
        ArrayList<GameItem> c = new ArrayList<>();
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int z = 0; z < WORLD_WIDTH; z++) {
                if (chunks[x][z] != null) {
                    c.addAll(chunks[x][z].renderItems());
                }
            }
        }
        return c;
    }

    public void generate() {
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int z = 0; z < WORLD_WIDTH; z++) {
                if (chunks[x][z] != null) {
                    chunks[x][z].continueGen();
                }
            }
        }
    }

    public void cleanup() {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (chunks[x][z] != null) {
                    chunks[x][z].cleanup();
                }
            }
        }

    }
}
