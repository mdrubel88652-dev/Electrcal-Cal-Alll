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
  fun printAllCalculators() {
    val all = com.example.data.calculator.CalculatorRegistry.allCalculators
    println("=== ALL 167 CALCULATORS ===")
    all.forEach { calc ->
      val inputNames = calc.inputs.map { it.id }.joinToString(", ")
      println("ID=${calc.id} | NAME=${calc.name} | CAT=${calc.category} | FORMULA=${calc.formula} | INPUTS=[$inputNames]")
    }
  }
}
