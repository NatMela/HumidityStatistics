package service

import model.Constants.{FAILED_MEASUREMENT_VALUE, MAX_MEASUREMENT, MIN_MEASUREMENT}
import model.SensorMeasurement

import scala.util.Try

object ValidationService {
  def validateLine(line: Seq[String]): Either[Int, SensorMeasurement] = {
    if (validateStructure(line))
      if (validateId(line.headOption.getOrElse("")))
        if (validateValue(line(1)))
          if (line(1) == FAILED_MEASUREMENT_VALUE)
            Left(failedMeasurement)
          else
            Right(SensorMeasurement(line.head, line(1).toInt))
        else Left(notValidLine)
      else Left(notValidLine)
    else Left(notValidLine)
  }

  def validateStructure(line: Seq[String]): Boolean = line.size == 2

  def validateId(id: String): Boolean = id.nonEmpty

  def validateValue(humidity: String): Boolean = {
    val convertedHumidity: Int = safeConvertToInt(humidity).getOrElse(-1)
    if (humidity == FAILED_MEASUREMENT_VALUE)
      true
    else if (convertedHumidity >= MIN_MEASUREMENT && convertedHumidity <= MAX_MEASUREMENT)
      true
    else
      false
  }

  private def safeConvertToInt(input: String): Option[Int] = {
    Try {
      input.toInt
    }.toOption
  }

  private val failedMeasurement = 1
  private val notValidLine = 0
}
