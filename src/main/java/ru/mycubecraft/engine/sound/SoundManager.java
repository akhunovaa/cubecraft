package ru.mycubecraft.engine.sound;

import org.apache.commons.io.FileUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundManager {

    static public boolean isMac = System.getProperty("os.name").contains("Mac");
    private final List<SoundBuffer> soundBufferList;
    private final Map<String, SoundSource> soundSourceMap;
    private long device;
    private long context;
    private SoundListener listener;

    public SoundManager() {
        soundBufferList = new ArrayList<>();
        soundSourceMap = new HashMap<>();
    }

    public void init() throws IOException {
        if (isMac) {
            String libraryPath = System.getProperty("org.lwjgl.librarypath");
            File copied = new File("assets/sounds/drivers/macos/libopenal.dylib");
            FileUtils.copyFile(copied, new File(libraryPath + "/" + "libopenal.dylib"));
        }

        this.device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        this.context = ALC10.alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);
    }

    public void addSoundSource(String name, SoundSource soundSource) {
        this.soundSourceMap.put(name, soundSource);
    }

    public SoundSource getSoundSource(String name) {
        return this.soundSourceMap.get(name);
    }

    public void playSoundSource(String name) {
        SoundSource soundSource = this.soundSourceMap.get(name);
        if (soundSource != null && !soundSource.isPlaying()) {
            soundSource.play();
        }
    }

    public void pauseSoundSource(String name) {
        SoundSource soundSource = this.soundSourceMap.get(name);
        if (soundSource != null && soundSource.isPlaying()) {
            soundSource.pause();
        }
    }

    public void stopSoundSource(String name) {
        SoundSource soundSource = this.soundSourceMap.get(name);
        if (soundSource != null && soundSource.isPlaying()) {
            soundSource.stop();
        }
    }

    public void removeSoundSource(String name) {
        this.soundSourceMap.remove(name);
    }

    public void addSoundBuffer(SoundBuffer soundBuffer) {
        this.soundBufferList.add(soundBuffer);
    }

    public SoundListener getListener() {
        return this.listener;
    }

    public void setListener(SoundListener listener) {
        this.listener = listener;
    }

    public void setAttenuationModel(int model) {
        alDistanceModel(model);
    }

    public void cleanup() {
        for (SoundSource soundSource : soundSourceMap.values()) {
            soundSource.cleanup();
        }
        soundSourceMap.clear();
        for (SoundBuffer soundBuffer : soundBufferList) {
            soundBuffer.cleanup();
        }
        soundBufferList.clear();
        if (context != NULL) {
            ALC10.alcDestroyContext(context);
        }
        if (device != NULL) {
            ALC10.alcCloseDevice(device);
        }
    }
}
