package com.example.snail

import com.example.snail.ui.models.RoutineExercise
import com.example.snail.ui.models.splitRepetitionRange
import org.junit.Assert.assertEquals
import org.junit.Test

class RepetitionRangeTest {
    @Test fun preservesBothBoundsOfExistingRanges() {
        for (range in listOf("6-10", "8-10", "8-12", "12-20")) {
            val (minimum, maximum) = splitRepetitionRange(range)
            assertEquals(range, RoutineExercise(1, "Press", minRepetitions = minimum,
                maxRepetitions = maximum).repetitions)
        }
    }

    @Test fun sortsBoundsAndAcceptsSpacesAndDecimals() {
        assertEquals("6,5" to "10", splitRepetitionRange(" 10 – 6.5 "))
    }

    @Test fun retainsSingleAndUnrecognizedValuesWithoutTruncation() {
        assertEquals("8" to "", splitRepetitionRange("8"))
        assertEquals("" to "", splitRepetitionRange(""))
        assertEquals("6-10-12" to "", splitRepetitionRange("6-10-12"))
    }
}
