package ru.mycubecraft.util;

import ru.mycubecraft.core.Mesh;
import ru.mycubecraft.data.Sound;
import ru.mycubecraft.engine.graph.OBJLoader;
import ru.mycubecraft.renderer.Shader;
import ru.mycubecraft.renderer.Texture;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class AssetPool {

    private static final String DEFAULT_TEXTURE_LOCATION_FOLDER = "assets/textures";
    private static final String DEFAULT_MODELS_LOCATION_FOLDER = "assets/models";

    private static final Map<String, Mesh> meshes = new HashMap<>();
    private static final Map<String, Shader> shaders = new HashMap<>();
    private static final Map<String, Texture> textures = new HashMap<>();
    private static final Map<String, Sound> sounds = new HashMap<>();

    public static void loadAssets() {
        final File textureFolder = new File(DEFAULT_TEXTURE_LOCATION_FOLDER);
        for (String filePath : listFilesForFolder(textureFolder)) {
            Texture texture = new Texture();
            texture.createTexture(filePath);
            AssetPool.textures.put(filePath, texture);
        }
        final File modelsFolder = new File(DEFAULT_MODELS_LOCATION_FOLDER);
        for (String filePath : listFilesForFolder(modelsFolder)) {
            Mesh mesh = OBJLoader.loadMesh(filePath);
            AssetPool.meshes.put(filePath, mesh);
        }
    }

    public static Shader getShader(String resourceName) {
        File file = new File(resourceName);
        if (AssetPool.shaders.containsKey(file.getAbsolutePath())) {
            return AssetPool.shaders.get(file.getAbsolutePath());
        } else {
            Shader shader = new Shader(resourceName);
            shader.compile();
            AssetPool.shaders.put(file.getAbsolutePath(), shader);
            return shader;
        }
    }

    public static Texture getTexture(String resourceName) throws FileNotFoundException {
        File file = new File(resourceName);
        if (AssetPool.textures.containsKey(file.getAbsolutePath())) {
            return AssetPool.textures.get(file.getAbsolutePath());
        } else {
            throw new FileNotFoundException(String.format("Texture %s not loaded!", resourceName));
        }
    }

    public static Mesh getMesh(String resourceName) throws FileNotFoundException {
        File file = new File(resourceName);
        if (AssetPool.meshes.containsKey(file.getAbsolutePath())) {
            return AssetPool.meshes.get(file.getAbsolutePath());
        } else {
            throw new FileNotFoundException(String.format("Mesh %s not loaded!", resourceName));
        }
    }

    public static Collection<Sound> getAllSounds() {
        return sounds.values();
    }

    public static Sound getSound(String soundFile) {
        File file = new File(soundFile);
        if (sounds.containsKey(file.getAbsolutePath())) {
            return sounds.get(file.getAbsolutePath());
        } else {
            assert false : "Sound file not added '" + soundFile + "'";
        }

        return null;
    }

    public static Sound addSound(String soundFile, boolean loops) {
        File file = new File(soundFile);
        if (sounds.containsKey(file.getAbsolutePath())) {
            return sounds.get(file.getAbsolutePath());
        } else {
            Sound sound = new Sound(file.getAbsolutePath(), loops);
            AssetPool.sounds.put(file.getAbsolutePath(), sound);
            return sound;
        }
    }

    public static List<String> listFilesForFolder(final File folder) {
        List<String> pathList = new ArrayList<>();
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles != null ? listOfFiles : new File[0]) {
            if (file.isFile()) {
                String filePath = file.getAbsolutePath();
                pathList.add(filePath);
            }
        }
        return pathList;
    }
}
