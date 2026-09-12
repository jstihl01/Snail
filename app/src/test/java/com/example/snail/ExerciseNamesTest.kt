package com.example.snail

import com.example.snail.ui.models.toExerciseTitle
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseNamesTest {
    @Test
    fun titlesKeepConnectorsLowercase() {
        assertEquals("Press de Pecho", "Press de pecho".toExerciseTitle())
        assertEquals("Jalón al Pecho", "Jalón al pecho".toExerciseTitle())
        assertEquals("Peso Muerto Rumano", "Peso muerto rumano".toExerciseTitle())
        assertEquals("Elevaciones Laterales", "Elevaciones laterales".toExerciseTitle())
    }

    @Test
    fun preservesAcronymsSpacingAndSingleWordNames() {
        assertEquals("Remo con TRX", "Remo con TRX".toExerciseTitle())
        assertEquals("Press  de Pecho", "Press  de pecho".toExerciseTitle())
        assertEquals("sentadilla", "sentadilla".toExerciseTitle())
        assertEquals("", "".toExerciseTitle())
    }

    @Test
    fun conversionIsIdempotent() {
        val name = "Press de pecho".toExerciseTitle()
        assertEquals(name, name.toExerciseTitle())
    }
}
