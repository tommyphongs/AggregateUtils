package org.coding.aggregates.aggregations;

import com.google.gson.JsonElement;

import java.util.Comparator;

/**
 * This is essentially a thin wrapper on top of a BufferedReader... which keeps
 * the last line in memory.
 */

public enum SupportKeyType {
    INTEGER((o1, o2) -> Integer.compare(o1.getAsInt(), o2.getAsInt())),
    DOUBLE((o1, o2) ->Double.compare(o1.getAsDouble(), o2.getAsDouble())),
    SHORT((o1, o2) ->Short.compare(o1.getAsShort(), o2.getAsShort())),
    FLOAT((o1, o2) ->Float.compare(o1.getAsFloat(), o2.getAsFloat())),
    LONG((o1, o2) -> Long.compare(o1.getAsLong(), o2.getAsLong()));

    Comparator<JsonElement> comparator;

    SupportKeyType(Comparator<JsonElement> comparator) {
        this.comparator = comparator;
    }
}
