package ru.mycubecraft.engine.graph.particles;

import ru.mycubecraft.engine.items.GameItem;

import java.util.List;

public interface IParticleEmitter {

    void cleanup();
    
    Particle getBaseParticle();
    
    List<GameItem> getParticles();
}
