package com.theupnextapp.common.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DateUtilsTest {

    @Test
    fun getDisplayDate_iso8601_returnsCorrectDate_inPST() {
        val isoDate = "2015-02-09T02:00:00.000Z" // Better Call Saul S01E01 - 2 AM UTC
        
        // Set default timezone to PST (UTC-8)
        val originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        
        try {
            // getDisplayDate should return formatted string "MMM d, yyyy"
            // Feb 9 2AM UTC -> Feb 8 6PM PST
            
            val formatted = DateUtils.getDisplayDate(isoDate)
            assertNotNull("Formatted date should not be null", formatted)
            
            // "Feb 8, 2015"
            assertEquals("Feb 8, 2015", formatted)
        } finally {
            TimeZone.setDefault(originalTimeZone)
        }
    }

    @Test
    fun getDisplayDate_simpleDate_returnsCorrectDate() {
        val simpleDate = "2015-02-09"
        
        val formatted = DateUtils.getDisplayDate(simpleDate)
        
        // Simple date "2015-02-09" is treated as local midnight?
        // If parsed with yyyy-MM-dd, it's 00:00 local.
        // So formatting it back should be "Feb 9, 2015"
        
        assertEquals("Feb 9, 2015", formatted)
    }
}
