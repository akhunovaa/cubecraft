package ru.mycubecraft.engine;

import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;

import static org.lwjgl.system.MemoryUtil.*;
import static ru.mycubecraft.Game.DEBUG;

/**
 * Dynamically growable ByteBuffer.
 */
@Slf4j
public class DynamicByteBuffer {

    private static final NumberFormat INT_FORMATTER = NumberFormat.getIntegerInstance();
    private static final NumberFormat PERCENT_FORMATTER = NumberFormat.getPercentInstance();

    public long addr;
    public int pos, cap;

    /**
     * Allocate a ByteBuffer with the given initial capacity.
     */
    public DynamicByteBuffer(int initialCapacity) {
        addr = nmemAlloc(initialCapacity);
        cap = initialCapacity;
        if (DEBUG) {
            log.info("Creating new DynamicByteBuffer with capacity [" + INT_FORMATTER.format(cap / 1024) + " KB]");
        }
    }

    private void grow() {
        int newCap = (int) (cap * 1.75f);
        if (DEBUG) {
            log.info(
                    "Growing DynamicByteBuffer from [" + INT_FORMATTER.format(cap / 1024) + " KB] to [" + INT_FORMATTER.format(newCap / 1024) + " KB]");
        }
        long newAddr = nmemRealloc(addr, newCap);
        cap = newCap;
        addr = newAddr;
    }

    public void free() {
        if (DEBUG) {
            log.info("Freeing DynamicByteBuffer (used " + PERCENT_FORMATTER.format((float) pos / cap) + " of capacity)");
        }
        nmemFree(addr);
    }

    public DynamicByteBuffer putInt(int v) {
        if (cap - pos < Integer.BYTES)
            grow();
        return putIntNoGrow(v);
    }

    private DynamicByteBuffer putIntNoGrow(int v) {
        memPutInt(addr + pos, v);
        pos += Integer.BYTES;
        return this;
    }

    public DynamicByteBuffer putShort(int v) {
        if (cap - pos < Short.BYTES)
            grow();
        return putShortNoGrow(v);
    }

    private DynamicByteBuffer putShortNoGrow(int v) {
        memPutShort(addr + pos, (short) v);
        pos += Short.BYTES;
        return this;
    }
}
