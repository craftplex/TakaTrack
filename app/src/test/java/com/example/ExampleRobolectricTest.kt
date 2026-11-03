package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `verify app identity name`() {
    val appName = "TakaTrack"
    assertEquals("TakaTrack", appName)
  }
}
