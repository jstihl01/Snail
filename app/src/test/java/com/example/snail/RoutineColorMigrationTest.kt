package com.example.snail

import com.example.snail.ui.models.normalizedRoutineColorIndex
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineColorMigrationTest {
    @Test fun keepsCurrentPalettePositionsAndMigratesRetiredColors() {
        (0..7).forEach { index -> assertEquals(index, normalizedRoutineColorIndex(index)) }
        assertEquals(7, normalizedRoutineColorIndex(8))
        assertEquals(7, normalizedRoutineColorIndex(9))
        assertEquals(1, normalizedRoutineColorIndex(10))
        assertEquals(0, normalizedRoutineColorIndex(-1))
    }
}
