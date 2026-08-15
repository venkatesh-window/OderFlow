package com.exchange.matching.util;

import java.util.function.Supplier;

/**
 * A generic, lock-free (single-writer) Object Pool designed for Zero-GC latency requirements.
 * Used to avoid object allocation in the critical path (e.g., Trade objects).
 */
public class ObjectPool<T> {
    private final T[] pool;
    private final int mask;
    private int index = 0;

    @SuppressWarnings("unchecked")
    /**
     * Constructs a new ObjectPool with the specified capacity.
     *
     * @param factory  the supplier used to create new objects
     * @param capacity the maximum capacity of the pool
     */
    public ObjectPool(Supplier<T> factory, int capacity) {
        // Ensure capacity is a power of 2 for fast masking
        int powerOfTwoCapacity = Integer.highestOneBit(capacity - 1) << 1;
        if (capacity == 1) powerOfTwoCapacity = 1;
        
        this.pool = (T[]) new Object[powerOfTwoCapacity];
        this.mask = powerOfTwoCapacity - 1;

        for (int i = 0; i < powerOfTwoCapacity; i++) {
            this.pool[i] = factory.get();
        }
    }

    /**
     * Borrows an object from the pool. 
     * Since this is a circular buffer, objects borrowed earlier will eventually be overwritten 
     * if the caller holds onto them for too long (i.e. more than `capacity` elements ago).
     * For immediate processing and publishing, this is highly efficient.
     *
     * @return A reused object from the pool.
     */
    public T borrowObject() {
        return pool[(index++) & mask];
    }
}
