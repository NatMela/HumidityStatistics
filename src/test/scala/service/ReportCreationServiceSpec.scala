package service

import akka.actor.ActorSystem
import input.ReadInfo
import model.{FailedSensors, Report, SensorInfo, SensorStatistics}
import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.concurrent.Future

class ReportCreationServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures {
  implicit val system: ActorSystem = ActorSystem()

  import system.dispatcher

  "createReport " - {
    "should create a report if files were found and data was obtained" in new TestWiring {
      when(mockReadInfo.findAllFiles(dirPath, sampleFileExtensions)).thenReturn(List(file))
      when(mockReadInfo.processFiles(Seq(file))).thenReturn(Future.successful((1, failedSensors, Seq(sensorInfo))))

      whenReady(service.createReport(dirPath, sampleFileExtensions)) { result =>
        result shouldBe report
      }
    }

    "should create an empty report if data files were not found" in new TestWiring {
      when(mockReadInfo.findAllFiles(dirPath, sampleFileExtensions)).thenReturn(List.empty[File])
      when(mockReadInfo.processFiles(Seq.empty[File])).thenReturn(Future.successful((0, emptyFailedSensors, Seq.empty[SensorInfo])))

      whenReady(service.createReport(dirPath, sampleFileExtensions)) { result =>
        result shouldBe emptyReport
      }
    }

    "should fail if data processing was failed" in new TestWiring {
      when(mockReadInfo.findAllFiles(dirPath, sampleFileExtensions)).thenReturn(List.empty[File])
      when(mockReadInfo.processFiles(Seq.empty[File])).thenReturn(Future.failed(new RuntimeException))

      whenReady((service.createReport(dirPath, sampleFileExtensions)).failed) { ex =>
        ex shouldBe a[RuntimeException]
      }
    }
  }
}

trait TestWiring {
  val mockReadInfo: ReadInfo = mock(classOf[ReadInfo])
  val service = new ReportCreationService(mockReadInfo)

  val file: File = new File("sample.csv")
  val dirPath = "/sample/dir/path"
  val sampleFileExtensions = List(".csv")
  val sensorStatistics: SensorStatistics = SensorStatistics(
    id = "s1",
    min = 4,
    avg = 51,
    max = 98
  )
  val sensorInfo: SensorInfo = SensorInfo(
    id = "s1",
    min = 4,
    sum = 102,
    max = 98,
    numOfElements = 2
  )
  val failedSensors: FailedSensors = FailedSensors(ids = Seq("s1"), numOfFails = 1)
  val emptyFailedSensors: FailedSensors = FailedSensors(ids = Seq.empty[String], numOfFails = 0)
  val report: Report = Report(
    numOfFiles = 1,
    numOfMeasurements = 3,
    numOfFailedMeasurements = 1,
    sensors = Seq(sensorStatistics),
    failedMeasurementSensorsId = Seq.empty
  )
  val emptyReport: Report = Report(
    numOfFiles = 0,
    numOfMeasurements = 0,
    numOfFailedMeasurements = 0,
    sensors = Seq.empty[SensorStatistics],
    failedMeasurementSensorsId = Seq.empty[String]
  )
}
