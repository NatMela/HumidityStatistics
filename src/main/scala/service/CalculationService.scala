package service

import model.{SensorInfo, SensorMeasurement}

object CalculationService {
  def addInfo(sensors: Map[String, SensorInfo], newMeasurement: SensorMeasurement): Map[String, SensorInfo] = {
    if (sensors.keySet.contains(newMeasurement.id)) {
      val sensor = sensors(newMeasurement.id)
      sensors +
        (newMeasurement.id -> SensorInfo(
          id = newMeasurement.id,
          min = if (sensor.min > newMeasurement.humidity) newMeasurement.humidity else sensor.min,
          sum = sensor.sum + newMeasurement.humidity,
          max = if (sensor.max < newMeasurement.humidity) newMeasurement.humidity else sensor.max,
          numOfElements = sensor.numOfElements + 1))
    } else {
      sensors +
        (newMeasurement.id -> SensorInfo(
          id = newMeasurement.id,
          min = newMeasurement.humidity,
          sum = newMeasurement.humidity,
          max = newMeasurement.humidity,
          numOfElements = 1))
    }
  }
}
