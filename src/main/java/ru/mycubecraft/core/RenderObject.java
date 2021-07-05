package ru.mycubecraft.core;

public interface RenderObject {

    void render();

    int shouldRender();

    void cleanup();

}
