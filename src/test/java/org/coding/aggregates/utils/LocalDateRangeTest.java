package org.coding.aggregates.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateRangeTest {

    @Test
    public void parseTest() {
        LocalDateRange drange = LocalDateRange.parse("yesterday");
        assertEquals(drange.size(), 1);
    }

    @Test
    public void parseDateTest() {
        String dateStr;
        LocalDate expectedDate;
        LocalDate parsedDate;


        dateStr = "day-7";
        expectedDate = LocalDate.now().minusDays(7);
        parsedDate = LocalDateRange.parseDate(dateStr);
        assertEquals(parsedDate, expectedDate);

        dateStr = "Yesterday";
        expectedDate = LocalDate.now().minusDays(1);
        parsedDate = LocalDateRange.parseDate(dateStr);
        assertEquals(parsedDate, expectedDate);

        dateStr = "now";
        expectedDate = LocalDate.now().minusDays(0);
        parsedDate = LocalDateRange.parseDate(dateStr);
        assertEquals(parsedDate, expectedDate);

    }


}