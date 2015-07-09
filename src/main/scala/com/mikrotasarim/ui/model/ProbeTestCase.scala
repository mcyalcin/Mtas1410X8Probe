package com.mikrotasarim.ui.model

import scalafx.beans.property.{BooleanProperty, StringProperty}

class ProbeTestCase(val name: String, test: () => (Boolean, String)) {

  val testPassed = BooleanProperty(value = false)
  val testFailed = BooleanProperty(value = false)

  testPassed.onChange(if (testPassed.value) testFailed.set(false))
  testFailed.onChange(if (testFailed.value) testPassed.set(false))

  val message = StringProperty("")
  val shortMessage = StringProperty("")

  def runTest(): Unit = {
    val testResult = test()
    if (testResult._1) {
      testPassed.set(true)
    } else {
      testFailed.set(true)
    }
    message.set(testResult._2)
    shortMessage.set(testResult._2.takeWhile(_ != '\n'))
  }
}
