package org.coding.aggregates.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

public class LocalDateRange implements Iterable<LocalDate> {

    private final LocalDate startDate;
    private final LocalDate endDate;

    LocalDateRange(LocalDate startDate, LocalDate endDate) {
        Preconditions.checkArgument(!startDate.isAfter(endDate), "End date must after start date");
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static LocalDateRange of(LocalDate startDate, LocalDate endDate) {
        return new LocalDateRange(startDate, endDate);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public static LocalDateRange parse(String rangeStr) {
        Objects.requireNonNull(rangeStr);

        if (rangeStr.isEmpty()) {
            throw new IllegalArgumentException("The dates string is empty");
        }
        char charBegin = rangeStr.charAt(0);
        char charEnd = rangeStr.charAt(rangeStr.length() - 1);

        if ((charBegin == '(' || charBegin == '[') &&
                (charEnd == ')' || charEnd == ']')) {
            String subStr = rangeStr.substring(1, rangeStr.length() - 1);
            String[] splits = subStr.split(",");
            if (splits.length != 2) {
                throw new IllegalArgumentException("If range date is set, need both end date and begin date");
            }
            LocalDate begin = parseDate(splits[0]);
            LocalDate end = parseDate(splits[1]);
            if (begin.isAfter(end)) {
                throw new IllegalArgumentException("End date must after or equal begin date");
            }
            LocalDate rBegin;
            LocalDate rEnd;
            if (charBegin == '(') {
                rBegin = begin.plusDays(1);
            } else {
                rBegin = begin;
            }
            if (charEnd == ')') {
                rEnd = end.minusDays(1);
            } else {
                rEnd = end;
            }
            return new LocalDateRange(rBegin, rEnd);
        }
        else {
            try {
                LocalDate d = parseDate(rangeStr);
                return new LocalDateRange(d, d);
            } catch (Exception e) {
                throw new IllegalArgumentException("Time parameter must be in time range format");
            }
        }
    }

    public static LocalDate parseDate(String dateStr)  {
        String lowerCase = dateStr.toLowerCase(Locale.ROOT);
        if (lowerCase.startsWith("day-")) {
            return LocalDate.now().minusDays(Integer.parseInt(lowerCase.split("day-")[1]));
        }
        return switch (dateStr.toLowerCase(Locale.ROOT)) {
            case "now" -> LocalDate.now();
            case "yesterday" -> LocalDate.now().minusDays(1);
            default -> LocalDate.parse(dateStr);
        };
    }

    @Override
    public String toString() {
        return "[" + startDate.toString() + ", " + endDate.toString() + "]";
    }

    @NotNull
    @Override
    public Iterator<LocalDate> iterator() {
        return new Iterator<>() {
            private LocalDate theDate = endDate;

            @Override
            public boolean hasNext() {
                return !theDate.isBefore(startDate);
            }

            @Override
            public LocalDate next() {
                LocalDate ret = theDate;
                theDate = theDate.minusDays(1);
                return ret;
            }
        };
    }

    public int size() {
       return Iterators.size(iterator());
    }

}
