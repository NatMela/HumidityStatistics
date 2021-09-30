package service

import model.{SensorInfo, SensorMeasurement}
import org.scalatest.freespec.AnyFreeSpec

class CalculationServiceSpec extends AnyFreeSpec {

  "addInfo " - {
    "should add a new sensor to the map if the was no information about the sensor before" in {
      assert(CalculationService.addInfo(sensors, measurement) == addedSensors)
    }

    "should combine sensor's data with previous data if the was information about the sensor before" in {
      assert(CalculationService.addInfo(addedSensors, measurement) == combinedSensorsInfo)
    }
  }

  val id = "s1"
  val id2 = "s2"
  val firstSensorInfo: SensorInfo = SensorInfo(
    id = id,
    min = 4,
    sum = 46,
    max = 32,
    numOfElements = 3
  )

  val secondSensorInfo: SensorInfo = SensorInfo(
    id = id2,
    min = 45,
    sum = 45,
    max = 45,
    numOfElements = 1
  )

  val combinedSecondSensorInfo: SensorInfo = SensorInfo(
    id = id2,
    min = 45,
    sum = 90,
    max = 45,
    numOfElements = 2
  )

  val sensors: Map[String, SensorInfo] = Map(id -> firstSensorInfo)
  val measurement: SensorMeasurement = SensorMeasurement(id = id2, humidity = 45)
  val addedSensors: Map[String, SensorInfo] = sensors + (id2 -> secondSensorInfo)
  val combinedSensorsInfo: Map[String, SensorInfo] = sensors + (id2 -> combinedSecondSensorInfo)
}
