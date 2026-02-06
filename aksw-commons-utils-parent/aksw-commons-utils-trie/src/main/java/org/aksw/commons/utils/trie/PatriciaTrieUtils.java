package org.aksw.commons.utils.trie;

import java.util.NoSuchElementException;
import java.util.SortedMap;

import org.apache.commons.collections4.trie.PatriciaTrie;

public class PatriciaTrieUtils {
    /** Returns the longest key in the trie that is a prefix of `value`, or null if none. */
    public static String longestPrefixOf(PatriciaTrie<?> prefixMap, String value) {
        if (prefixMap.isEmpty()) return null;

        String candidate;
        try {
            // approximate floorKey(value)
            candidate = prefixMap.headMap(value + Character.MAX_VALUE).lastKey();
        } catch (NoSuchElementException e) {
            return null;
        }

        while (candidate != null && !value.startsWith(candidate)) {
            SortedMap<String, ?> head = prefixMap.headMap(candidate); // strictly less than candidate
            if (head.isEmpty()) return null;
            candidate = head.lastKey();
        }
        return candidate;
    }
}
