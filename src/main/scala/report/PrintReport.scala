package report

import model.Constants.FAILED_MEASUREMENT_VALUE
import model.Report

object PrintReport {
  def printReport(report: Report): Unit = {
    println(s"Num of processed files: ${report.numOfFiles}")
    println(s"Num of processed measurements: ${report.numOfMeasurements}")
    println(s"Num of failed measurements: ${report.numOfFailedMeasurements}")
    println()
    println("Sensors with highest avg humidity:")
    println()
    println("sensor-id,min,avg,max")
    report.sensors.foreach(line => println(s"${line.id},${line.min},${line.avg},${line.max}"))
    report.failedMeasurementSensorsId.foreach(id => println(s"${id},$FAILED_MEASUREMENT_VALUE,$FAILED_MEASUREMENT_VALUE,$FAILED_MEASUREMENT_VALUE"))
  }
}
