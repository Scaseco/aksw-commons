package org.aksw.commons.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Created by Claus Stadler
 * Date: Nov 1, 2010
 * Time: 10:22:25 PM
 */
public class IteratorUtils {
    public static <T> Iterable<T> makeIterable(Iterator<T> iterator) {
        return new IteratorIterable<T>(iterator);
    }

    public static <T> T expectOneItem(Iterator<T> iterator) {
        T result = null;
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("Exactly one item expected in stream; got none");
        }

        result = iterator.next();

        if (iterator.hasNext()) {
            throw new IllegalArgumentException("Exactly one item expected in stream; got multiple");
        }

        return result;

    }
    public static <T> T expectZeroOrOneItems(Iterator<T> iterator) {
        T result;
        if (!iterator.hasNext()) {
            result = null;
        } else {
            result = expectOneItem(iterator);
        }

        return result;
    }

    /** Similar to guava's Iterators.limit but with long argument*/
    public static <T> Iterator<T> limit(Iterator<T> iterator, long limitSize) {
        Objects.requireNonNull(iterator);

        return new Iterator<T>() {
            protected long count;

            @Override
            public boolean hasNext() {
                return count < limitSize && iterator.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                count++;
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    public static <X> int compare(Iterator<? extends X> it1, Iterator<? extends X> it2, Comparator<X> comparator) {
        while (it1.hasNext()) {
            if (!it2.hasNext()) {
                return 1;
            }
            X item1 = it1.next();
            X item2 = it2.next();

            int d = comparator.compare(item1, item2);
            if (d != 0) {
                return d;
            }
        }
        if (it2.hasNext()) {
            return -1;
        }
        return 0;
    }

    public static boolean equalsByReference(Iterator<?> it1, Iterator<?> it2) {
        int val = compare(it1, it2, (x, y) -> x == y ? 0 : -1);
        return val == 0;
    }
}
