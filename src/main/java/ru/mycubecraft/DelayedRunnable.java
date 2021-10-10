package ru.mycubecraft;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Callable;

/**
 * An action that is optionally delayed a given number of frames.
 */
@Getter
@Setter
public class DelayedRunnable {

    private final Callable<Void> runnable;
    private final String name;
    private int delay;

    public DelayedRunnable(Callable<Void> runnable, String name, int delay) {
        this.runnable = runnable;
        this.name = name;
        this.delay = delay;
    }
}
