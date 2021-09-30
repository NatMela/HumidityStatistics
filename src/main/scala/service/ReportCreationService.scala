package service

import akka.stream.Materializer
import input.ReadInfo
import model.Report
import model.SensorStatistics.toSensorStatistics

import scala.concurrent.{ExecutionContext, Future}

class ReportCreationService(readInfo: ReadInfo) {
  def createReport(dirPath: String, extensions: List[String])(implicit ex: ExecutionContext, materializer: Materializer): Future[Report] = {
    val files = readInfo.findAllFiles(dirPath, extensions)
    val processedData = readInfo.processFiles(files)

    processedData.map(data => {
      val (numOfFiles, failedSensors, successSensors) = data
      val emptySensors: Seq[String] = failedSensors.ids.filter(id => !successSensors.map(_.id).contains(id))

      Report(
        numOfFiles = numOfFiles,
        numOfMeasurements = successSensors.map(_.numOfElements).sum + failedSensors.numOfFails,
        numOfFailedMeasurements = failedSensors.numOfFails,
        sensors = successSensors.map(toSensorStatistics).sortBy(_.avg)(Ordering[Int].reverse),
        failedMeasurementSensorsId = emptySensors
      )
    })
  }
}
