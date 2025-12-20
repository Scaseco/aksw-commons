package org.aksw.commons.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

public class IterableUtils {

    @Deprecated // Use guava Iterables.getOnlyElement(...)
    public static <T> T expectOneItem(Iterable<T> iterable) {
        return IteratorUtils.expectOneItem(iterable.iterator());
    }

    @Deprecated // Use guava Iterables.getOnlyElement(..., null)
    public static <T> T expectZeroOrOneItems(Iterable<T> iterable) {
        return IteratorUtils.expectZeroOrOneItems(iterable.iterator());
    }

    /**
     * Will only compare as many items as there are in the shorter iterable
     *
     * @param itemComparator
     * @return
     */
    public static <T> Comparator<? super Iterable<? extends T>> newComparatorForIterablesOfEqualLength(Comparator<? super T> itemComparator) {
        return (a, b) -> compareIterablesOfEqualLength(a, b, itemComparator);
    }

    /**
     * Will only compare as many items as there are in the shorter iterable
     *
     * @param a
     * @param b
     * @param itemComparator
     * @return
     */
    public static <T> int compareIterablesOfEqualLength(
            Iterable<? extends T> a,
            Iterable<? extends T> b,
            Comparator<? super T> itemComparator) {
        int result = Streams.zip(Streams.stream(a), Streams.stream(b), (x, y) -> itemComparator.compare(x, y))
            .mapToInt(x -> x)
            .filter(x -> x != 0)
            .findFirst().orElse(0);

        return result;
    }

    public static <T> int compareByLengthThenItems(Iterable<? extends T> a, Iterable<? extends T> b, Comparator<? super T> itemComparator) {
        int result = ComparisonChain.start()
                .compare(Iterables.size(a), Iterables.size(b))
                .compare(a, b, newComparatorForIterablesOfEqualLength(itemComparator))
                .result();
        return result;
    }

    public static boolean equalsByReference(Iterable<?> a, Iterable<?> b) {
        int val = compare(a, b, (x, y) -> x == y ? 0 : -1);
        return val == 0;
    }

    public static <X> int compare(Iterable<? extends X> a, Iterable<? extends X> b, Comparator<X> comparator) {
        if (a instanceof Collection aa && b instanceof Collection bb) {
            int d = bb.size() - aa.size();
            if (d != 0) {
                return d;
            }
        }
        Iterator<? extends X> it1 = a.iterator();
        Iterator<? extends X> it2 = b.iterator();
        int result = IteratorUtils.compare(it1, it2, comparator);
        return result;
    }
}
