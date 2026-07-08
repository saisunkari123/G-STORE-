package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testUnitWhitelistValidation() {
    val whitelist = setOf(
      "kg", "kgs", "g", "grams", "ltr", "ltrs", "liters", "ml", "pcs", "pieces", "packet", "packets", "pkt", "pkts", "box", "boxes"
    )
    
    assertTrue("kg".lowercase() in whitelist)
    assertTrue("Kg".lowercase() in whitelist)
    assertTrue("KG".lowercase() in whitelist)
    assertTrue("g".lowercase() in whitelist)
    assertTrue("Ltr".lowercase() in whitelist)
    assertTrue("ml".lowercase() in whitelist)
    assertTrue("Pcs".lowercase() in whitelist)
    assertTrue("Packet".lowercase() in whitelist)
    assertTrue("Box".lowercase() in whitelist)

    assertFalse("hvyv".lowercase() in whitelist)
    assertFalse("random".lowercase() in whitelist)
    assertFalse("123".lowercase() in whitelist)
    assertFalse("".lowercase() in whitelist)
  }
}
