package com.hension.havenx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.hension.havenx.navigation.Screen

class ScreenTest {

    @Test
    fun `all screens have unique routes`() {
        val routes = Screen.entries.map { it.route }
        assertEquals(
            "All screen routes should be unique",
            routes.size, routes.toSet().size
        )
    }

    @Test
    fun `all screens provide localized label resources`() {
        Screen.entries.forEach { screen ->
            assertTrue("Screen ${screen.name} has no label resource", screen.labelRes != 0)
        }
    }

    @Test
    fun `there are exactly 6 screens`() {
        assertEquals(
            "Navigation should have 6 tabs",
            6, Screen.entries.size
        )
    }

    @Test
    fun `connections is first screen`() {
        assertEquals(
            "Connections should be the first tab",
            Screen.Connections, Screen.entries.first()
        )
    }
}
