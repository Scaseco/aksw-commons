package org.aksw.commons.util;

import java.util.Objects;

/**
 * Helper class to set and get a reference within a lambda.
 * A bit nicer than using an array with a single item.
 */
public class Holder<T> {
    private T value;

    public void set(T value) { this.value = value; }
    public T get() { return value; }

    private Holder(T value) {
        this.value = value;
    }

    public static <T> Holder<T> of(T value) {
        return new Holder<>(value);
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
