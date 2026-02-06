package org.aksw.commons.cache.synchronizer;

/**
 * A counter that can be accessed by multiple threads.
 * Synchronization must be ensured extrinsically, such as using synchronized blocks or within
 * ConcurrentHashMap.compute.
 */
class VolatileCounter {
    private volatile int value ;

    public VolatileCounter(int value) {
        this.value = value;
    }

    public VolatileCounter inc() { ++value; return this; }
    public VolatileCounter dec() { --value; return this; }
    public int get() { return value; }

    @Override
    public String toString() {
        return "Volatile counter " + System.identityHashCode(this) + " has value " + value;
    }
}
