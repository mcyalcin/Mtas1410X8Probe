package com.mikrotasarim.ui.controller

import java.io.{FileWriter, File}
import java.text.SimpleDateFormat

import scala.collection.mutable
import scalax.file.Path
import scalafx.beans.property.StringProperty

object OutputController {
  val comment = StringProperty("")
  val dieNumber = StringProperty("1")
  val waferId = StringProperty("Wafer")
  val outputPath = StringProperty("./")

  def saveAndProceed(): Unit = {
    val folder = outputPath.value + "/" + waferId.value + "/Die" + dieNumber.value

    val path: Path = Path.fromString(folder)
    if (!path.exists) path.createDirectory()

    val output = new mutable.StringBuilder()

    output ++= "Project Name: MTAS1410X2\n"
    output ++= "Wafer ID: " + waferId.value + "\n"
    output ++= "Die #: " + dieNumber.value + "\n"
    val format = new SimpleDateFormat("HH:mm dd/MM/yyyy")
    output ++= "Date: " + format.format(new java.util.Date()) + "\n"
    var allTestsPassed = true

    for (testCase <- ProbeTestController.testCases) {
      if (!testCase.message.value.isEmpty) {
        writeStringToFile(new File(folder + "/" + testCase.name.filterNot(_==' ') + ".txt"), testCase.message.value)
      }
      if (!testCase.testPassed.value) {
        allTestsPassed = false
      }
    }

    output ++= "Status: "

    if (allTestsPassed) output ++= "Pass\n\n" else output ++= "Fail\n\n"

    if (!allTestsPassed) {
      output ++= "Results: \n"
      for (testCase <- ProbeTestController.testCases) {
        output ++= "Test " + testCase.name + " -> "
        if (!testCase.testPassed.value) output ++= "Fail\n" else output ++= "Pass\n"
      }
    }

    if (!comment.value.isEmpty) {
      output ++= "\n Comment: " + comment.value
      comment.value = ""
    }

    writeStringToFile(new File(folder + "/dieSummary.txt"), output.toString())

    for (testCase <- ProbeTestController.testCases) {
      testCase.testPassed.set(false)
      testCase.testFailed.set(false)
    }

    PowerSourceController.outputOff()

    dieNumber.value = (dieNumber.value.toInt + 1).toString
  }

  def using[A <: {def close() : Unit}, B](resource: A)(f: A => B): B =
    try f(resource) finally resource.close()

  def writeStringToFile(file: File, data: String, appending: Boolean = false) =
    using(new FileWriter(file, appending))(_.write(data))
}
