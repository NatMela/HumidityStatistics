package service

import model.Constants.FAILED_MEASUREMENT_VALUE
import model.SensorMeasurement
import org.scalatest.freespec.AnyFreeSpec

class ValidationServiceSpec extends AnyFreeSpec {

  "validateStructure " - {
    "should validate the structure of line: return true if line consists of two elements" in {
      assert(ValidationService.validateStructure(correctLine))
    }

    "should validate the structure of line: return false if line consists of more than two elements" in {
      assert(!ValidationService.validateStructure(longLine))
    }

    "should validate the structure of line: return false if line consists of one element" in {
      assert(!ValidationService.validateStructure(shortLine))
    }

    "should validate the structure of line: return false if line is empty" in {
      assert(!ValidationService.validateStructure(emptyLine))
    }
  }
  "validateId " - {
    "should validate the id element: return true if id is not empty" in {
      assert(ValidationService.validateId(correctLine.head))
    }

    "should validate the id element: return false if id is empty" in {
      assert(!ValidationService.validateId(longLine.head))
    }
  }
  "validateValue " - {
    "should validate the measurement value: return true if measurement is in interval [0, 100]" in {
      assert(ValidationService.validateValue(correctLine.last))
    }

    "should validate the measurement value: return true if measurement is failed" in {
      assert(ValidationService.validateValue(correctLineWithFailedMeasurement.last))
    }

    "should validate the measurement value: return false if measurement is not a number in interval [0, 100]" in {
      assert(!ValidationService.validateValue(lineWithIncorrectMeasurement.last))
    }

    "should validate the measurement value: return false if measurement is not a number and not a failed measurement" in {
      assert(!ValidationService.validateValue(lineWithNotANumberMeasurement.last))
    }
  }
  "validateLine " - {
    "should validate the line structure and values: return 0 if line is incorrect" in {
      assert(ValidationService.validateLine(lineWithIncorrectMeasurement) == Left(0))
      assert(ValidationService.validateLine(lineWithNotANumberMeasurement) == Left(0))
      assert(ValidationService.validateLine(longLine) == Left(0))
      assert(ValidationService.validateLine(emptyLine) == Left(0))
      assert(ValidationService.validateLine(shortLine) == Left(0))
    }

    "should validate the line structure and values: return 1 if line is correct and measurement is failed" in {
      assert(ValidationService.validateLine(correctLineWithFailedMeasurement) == Left(1))
    }

    "should validate the line structure and values: return a measurement if line is correct and measurement is successful" in {
      assert(ValidationService.validateLine(correctLine) == Right(sensorMeasurement))
    }
  }

  val id = "s1"
  val humidity = 45
  val correctLine: Seq[String] = Seq(id, humidity.toString)
  val correctLineWithFailedMeasurement: Seq[String] = Seq("s1", FAILED_MEASUREMENT_VALUE)
  val lineWithIncorrectMeasurement: Seq[String] = Seq("s1", "345")
  val lineWithNotANumberMeasurement: Seq[String] = Seq("s1", "a345")
  val longLine: Seq[String] = Seq("", "s2", "76")
  val shortLine: Seq[String] = Seq("s1")
  val emptyLine: Seq[String] = Seq.empty[String]
  val sensorMeasurement: SensorMeasurement = SensorMeasurement(id = id, humidity = humidity)

}
