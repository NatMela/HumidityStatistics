package input

import akka.actor.ActorSystem
import model.{FailedSensors, SensorInfo}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File

class ReadInfoSpec extends AnyFreeSpec with Matchers with ScalaFutures {
  implicit val actorSystem: ActorSystem = ActorSystem()

  import actorSystem.dispatcher

  "findAllFiles " - {
    "should return empty List if directory does not exist" in new TestWiring {
      assert(service.findAllFiles(incorrectDirPath, existsFileExtensions) == List.empty[File])
    }

    "should return List of files if directory exists and contains files with given extensions" in new TestWiring {
      assert(service.findAllFiles(dirPath, existsFileExtensions) == List(file))
    }

    "should return empty List if directory exists but does not contain files with given extensions" in new TestWiring {
      assert(service.findAllFiles(dirPath, notExistsFileExtensions) == List.empty[File])
    }
  }
  "processFiles " - {
    "should take data from the file and process it correctly" in new TestWiring {
      whenReady(service.processFiles(Seq(file))) { result =>
        result shouldBe processedData
      }
    }
  }
}

trait TestWiring {
  val service = new ReadInfoImpl

  val file: File = new File("C:\\Users\\Natallia_Melnikovich\\IdeaProjects\\statistics\\src\\test\\scala\\testFiles\\f1.csv")
  val dirPath = "C:\\Users\\Natallia_Melnikovich\\IdeaProjects\\statistics\\src\\test\\scala\\testFiles"
  val incorrectDirPath = "\\sample\\dir\\path"
  val existsFileExtensions = List(".csv")
  val notExistsFileExtensions = List(".sca")
  val sensorInfo3: SensorInfo = SensorInfo(
    id = "s3",
    min = 56,
    sum = 123,
    max = 67,
    numOfElements = 2
  )
  val sensorInfo2: SensorInfo = SensorInfo(
    id = "s2",
    min = 25,
    sum = 120,
    max = 95,
    numOfElements = 2
  )
  val sensorInfo1: SensorInfo = SensorInfo(
    id = "s1",
    min = 78,
    sum = 78,
    max = 78,
    numOfElements = 1
  )
  val numOfFiles = 1
  val failedSensors: FailedSensors = FailedSensors(ids = Seq("s4", "s2"), numOfFails = 2)
  val processedData: (Int, FailedSensors, Seq[SensorInfo]) = (numOfFiles, failedSensors, Seq(sensorInfo3, sensorInfo2, sensorInfo1))

}
