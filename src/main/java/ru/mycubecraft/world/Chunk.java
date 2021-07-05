package ru.mycubecraft.world;

import ru.mycubecraft.block.Block;
import ru.mycubecraft.core.GameItem;

import java.util.ArrayList;

public class Chunk {
    private final int genHeight = 0;
    public Block[][][] blocks;
    int cx = 0;
    int cz = 0;
    Generator g;
    private boolean genFin = false;

    public Chunk(int cx, int cz, Generator g) {
        this.g = g;
        blocks = new Block[16][World.WORLD_HEIGHT][16];
        this.cx = cx;
        this.cz = cz;
        int wX = cx * 16;
        int wZ = cz * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                boolean dirt = false;
                int mHeight = g.maxHeight(wX + x, wZ + z, 0);
                //System.out.println(mHeight);
                for (int y = 0; y < World.WORLD_HEIGHT; y++) {
                    if (y < mHeight) {
                        Block block = g.genBlock(wX + x, y, wZ + z, 0);

                        //block.model.setPosition(x+8.0f, (float) y, z+8.0f);
                        blocks[x][y][z] = block;
                        //block.setPosition(x+8.0f, (float) y, z+8.0f);
                    } else {

                        blocks[x][y][z] = null;
                    }

                }
            }
        }
    }

    public void render() {
        int blocksMade = 0;
        int wX = cx * 16;
        int wZ = cz * 16;
        for (int y = 0; y < World.WORLD_HEIGHT; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (blocks[x][y][z] != null) {
                        blocks[x][y][z].render();
                    }

                }
            }
        }
    }

    public void continueGen() {
        genFin = true;
        if (!genFin) {
			
			/*int wX = cx * 16;
			int wZ = cz * 16;
	
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {

						

							
								
									Block block = g.genBlock(wX + x, genHeight, wZ + z, 0);
									// block.setPosition(x+8.0f, y, z+8.0f);

									blocks[x][genHeight][z] = block;
								
							
						

					}
				}
				genHeight++;
			if(World.WORLD_HEIGHT==genHeight){
			genFin =true;
			}*/
        }
    }

    public ArrayList<GameItem> renderItems() {
        ArrayList<GameItem> c = new ArrayList<>();
        //if (genFin) {
        for (int y = 0; y < World.WORLD_HEIGHT; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (blocks[x][y][z] != null) {
                        c.add(blocks[x][y][z].model);
                    }
                }
            }
        }
        //}
        return c;
    }

    public void cleanup() {
        for (int y = 0; y < World.WORLD_HEIGHT; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (blocks[x][y][z] != null) {
                        blocks[x][y][z].model.cleanup();
                    }
                }
            }
        }

    }
}
