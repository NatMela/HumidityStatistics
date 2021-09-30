import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import input.ReadInfoImpl
import report.PrintReport
import service.ReportCreationService
import model.Constants.EXTENSIONS

import scala.language.postfixOps
import scala.util.{Failure, Success}

object HumidityStatistics {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem()
    import actorSystem.dispatcher

    val readInfo = new ReadInfoImpl
    val reportCreationService = new ReportCreationService(readInfo)

    if (args.isEmpty || args.length > 1) {
      logger.error("Wrong number of arguments: exactly one argument with the path to the directory with required files is expected.")
      actorSystem.terminate()
    } else {
      val reportF = reportCreationService.createReport(dirPath = args.head, extensions = EXTENSIONS)
      reportF.onComplete({
        case Success(report) =>
          logger.info(s"The report was calculated successfully. Report: $report")
          PrintReport.printReport(report)
          actorSystem.terminate()
        case Failure(ex) =>
          logger.error(s"There is an error during the report calculation. Error Message: ${ex.getMessage}")
          actorSystem.terminate()
      })
    }
  }

  private val logger = Logger[HumidityStatistics.type]
}
