package input

import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import com.typesafe.scalalogging.Logger
import service.{CalculationService, ValidationService}
import model.{FailedSensors, SensorInfo}

import java.io.{BufferedReader, File, FileReader}
import java.nio.file.Paths
import scala.collection.immutable.ArraySeq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait ReadInfo {
  def findAllFiles(dirPath: String, extensions: List[String]): List[File]

  def processFiles(files: Seq[File])(implicit ex: ExecutionContext, materializer: Materializer): Future[(Long, FailedSensors, Seq[SensorInfo])]
}

class ReadInfoImpl extends ReadInfo {
  override def findAllFiles(dirPath: String, extensions: List[String]): List[File] = {
    val dir = new File(dirPath)
    if (dir.exists && dir.isDirectory) {
      val files = Try {
        dir.listFiles().filter(_.isFile).toList
      }.toOption
      if (files != null) {
        logger.info(s"Files from the directory $dirPath: $files")
        files.getOrElse(List.empty).filter { file =>
          extensions.exists(file.getName.endsWith(_))
        }
      }
      else {
        logger.error(s"Can't get files from the directory $dirPath. Maybe, you do not have access to it")
        List.empty[File]
      }
    }
    else {
      logger.error(s"Can't get files from the directory $dirPath as it doesn't exist")
      List.empty[File]
    }
  }

  override def processFiles(files: Seq[File])(implicit ex: ExecutionContext, materializer: Materializer): Future[(Long, FailedSensors, Seq[SensorInfo])] = {
    val failedSensors: FailedSensors = FailedSensors(ids = Seq.empty[String], numOfFails = 0)
    val sensors: Map[String, SensorInfo] = Map.empty[String, SensorInfo]

    val data: Seq[Future[(FailedSensors, Map[String, SensorInfo])]] = files.map(file => {
      logger.info(s"File $file is processing")

      val filePath = Paths.get(file.getAbsolutePath)
      val source = FileIO.fromPath(filePath)

      val flow = Framing
        .delimiter(ByteString(System.lineSeparator()), maximumFrameLength = 512, allowTruncation = true)
        .map(_.utf8String)
        .map(_.split(","))
        .map(_.map(_.trim))
        .map(ArraySeq.unsafeWrapArray)

      val sink = Sink.fold((failedSensors, sensors))((acc, line) => processFileLine(acc, line))

      source
        .via(flow)
        .runWith(sink)
    })

    Future.sequence(data).map(dataSeq => {
      val (failedMeasurementsSensors, successSensors) = dataSeq.foldLeft((failedSensors, sensors))((acc, value) => {
        (FailedSensors(acc._1.ids :++ value._1.ids, acc._1.numOfFails + value._1.numOfFails), combineSensorInfo(acc._2, value._2))
      })

      (files.size, failedMeasurementsSensors, successSensors.values.toSeq)
    })
  }

  def processFilesBufferedReader(files: Seq[File]): (Long, FailedSensors, Seq[SensorInfo]) = {
    val nanSensors: FailedSensors = FailedSensors(ids = Seq.empty[String], numOfFails = 0)
    val sensors: Map[String, SensorInfo] = Map.empty[String, SensorInfo]
    val result: Seq[(FailedSensors, Map[String, SensorInfo])] = files.map(file => {
      val reader = new BufferedReader(new FileReader(file))
      val lines = Iterator.continually(reader.readLine()).takeWhile(_ != null)
        .foldLeft((nanSensors, sensors))((acc, line) => processFileLine((acc), line.split(',')))
      reader.close
      lines
    })
    val proceedInfo = result.foldLeft((nanSensors, sensors))((acc, value) => {
      (FailedSensors(acc._1.ids :++ value._1.ids, acc._1.numOfFails + value._1.numOfFails), combineSensorInfo(acc._2, value._2))
    })
    (files.size, proceedInfo._1, proceedInfo._2.values.toSeq)
  }

  private val logger = Logger[ReadInfoImpl]

  private def processFileLine(sensors: (FailedSensors, Map[String, SensorInfo]), line: Seq[String]): (FailedSensors, Map[String, SensorInfo]) = {
    val (failedSensors, successSensors) = sensors
    ValidationService.validateLine(line) match {
      case Left(value) =>
        if (value > 0)
          (FailedSensors(ids = failedSensors.ids :+ line.head, numOfFails = failedSensors.numOfFails + 1), successSensors)
        else
          sensors
      case Right(measurement) => (failedSensors, CalculationService.addInfo(successSensors, measurement))
    }
  }

  private def combineSensorInfo(sensorInfo1: Map[String, SensorInfo], sensorInfo2: Map[String, SensorInfo]): Map[String, SensorInfo] = {
    val (commonIds, diffIds): (Set[String], Set[String]) = sensorInfo2.keySet.partition(key => sensorInfo1.keySet.contains(key))
    val combinedSensors: Map[String, SensorInfo] = commonIds.foldLeft(sensorInfo1)((acc, id) => {
      acc + (id -> SensorInfo(
        id = id,
        min = if (acc(id).min < sensorInfo2(id).min) acc(id).min else sensorInfo2(id).min,
        sum = acc(id).sum + sensorInfo2(id).sum,
        max = if (acc(id).max > sensorInfo2(id).max) acc(id).max else sensorInfo2(id).max,
        numOfElements = acc(id).numOfElements + sensorInfo2(id).numOfElements))
    })
    combinedSensors ++ sensorInfo2.filter(sensorMap => diffIds.contains(sensorMap._1))
  }
}
