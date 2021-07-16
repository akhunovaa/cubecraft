package ru.mycubecraft.engine;

import ru.mycubecraft.engine.graph.Material;
import ru.mycubecraft.engine.graph.Mesh;
import ru.mycubecraft.engine.graph.OBJLoader;
import ru.mycubecraft.engine.graph.Texture;

public class SkyBox extends GameItem {

    public SkyBox(String objModel, String textureFile) throws Exception {
        super();
        Mesh skyBoxMesh = OBJLoader.loadMesh(objModel);
        Texture skyBoxtexture = new Texture(textureFile);
        skyBoxMesh.setMaterial(new Material(skyBoxtexture, 0.0f));
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }
}
