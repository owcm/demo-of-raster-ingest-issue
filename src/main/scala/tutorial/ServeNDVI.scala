package tutorial

import geotrellis.raster._
import geotrellis.raster.io.geotiff._
import geotrellis.raster.render._
import geotrellis.raster.resample._

import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.file._
import geotrellis.spark.io.avro.codecs._

import geotrellis.vector._

import akka.actor._
import akka.io.IO
import spray.can.Http
import spray.routing.{HttpService, RequestContext}
import spray.routing.directives.CachingDirectives
import spray.http.MediaTypes
import scala.concurrent._
import com.typesafe.config.ConfigFactory

object ServeNDVI {
  val catalogPath = new java.io.File("data/catalog").getAbsolutePath

  // Create a reader that will read in the indexed tiles we produced in IngestImage.
  val fileValueReader = FileValueReader(catalogPath)
  def reader(layerId: LayerId) = fileValueReader.reader[SpatialKey, Tile](layerId)

  def main(args: Array[String]): Unit = {
    implicit val system = akka.actor.ActorSystem("tutorial-system")

    // create and start our service actor
    val service =
      system.actorOf(Props(classOf[NDVIServiceActor]), "tutorial")

    // start a new HTTP server on port 8080 with our service actor as the handler
    IO(Http) ! Http.Bind(service, "localhost", 8090)
  }
}

class NDVIServiceActor extends Actor with HttpService {
  import scala.concurrent.ExecutionContext.Implicits.global

  def actorRefFactory = context
  def receive = runRoute(root)

  val colorMap =
    ColorMap.fromStringDouble(ConfigFactory.load().getString("aspect.colorbreaks")).get

  def root =
    pathPrefix(IntNumber / IntNumber / IntNumber) { (zoom, x, y) =>
      respondWithMediaType(MediaTypes.`image/png`) {
        complete {
          future {

            // Read in the tile at the given z/x/y coordinates.
            val tileOpt: Option[Tile] =
              try {
                Some(ServeNDVI.reader(LayerId("ftstory",zoom)).read(x, y))
              } catch {
                case _: TileNotFoundError =>
                  None
              }

            tileOpt.map { tile =>
              // Compute the NDVI
              /*
              val ndvi =
                tile.convert(DoubleConstantNoDataCellType).combineDouble(0, 1) { (r, ir) =>
                  if(isData(r) && isData(ir)) {
                    (ir - r) / (ir + r)
                  } else {
                    Double.NaN
                  }
                }
*/
              val cs = new CellSize(1.0, 1.0)
              val aspectRes =
                  tile.aspect( cs)
              // Render as a PNG
              aspectRes.renderPng(colorMap).bytes
            }
          }
        }
      }
    }
}
