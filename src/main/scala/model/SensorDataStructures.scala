package model

case class SensorMeasurement(id: String, humidity: Int)

case class SensorInfo(id: String, min: Int, sum: Long, max: Int, numOfElements: Long)

case class SensorStatistics(id: String, min: Int, avg: Int, max: Int)

case class FailedSensors(ids: Seq[String], numOfFails: Long)

case class Report(numOfFiles: Long, numOfMeasurements: Long, numOfFailedMeasurements: Long, sensors: Seq[SensorStatistics], failedMeasurementSensorsId: Seq[String])

object SensorStatistics {
  def toSensorStatistics(sensor: SensorInfo): SensorStatistics = {
    SensorStatistics(
      id = sensor.id,
      min = sensor.min,
      avg = (sensor.sum / sensor.numOfElements).toInt,
      max = sensor.max
    )
  }
}


