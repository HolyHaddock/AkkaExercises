package akkaexercises.zbay.ex5_hierarchy_zBayActor

import akka.actor.{Props, ActorSystem}
import akka.pattern._
import org.specs2.mutable.Specification
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.specs2.time.NoTimeConversions
import scala.concurrent.{Await, Future}
import Auction.Protocol._
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import org.specs2.mock.Mockito
import zBay.Protocol._

class zBaySpec extends Specification
                     with NoTimeConversions
                     with Mockito { isolated
  "The zBay" should {
    "be able to handle bids and return an auctions status" in {
      val auctionId = responseFrom(zBay ? AuctionCreateRequest(exampleEndTime)).asInstanceOf[Long]
      zBay ! AuctionBidRequest(auctionId = auctionId,
                               userId    = 1,
                               value     = 1.00)
      responseFrom(zBay ? AuctionStatusRequest(auctionId)) must be equalTo {
        StatusResponse(1.00, Running)
      }
    }
  }

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(3, TimeUnit.SECONDS)

  val exampleEndTime = DateTime.now + 7.days

  val zBay = system.actorOf(Props[zBay])
  def responseFrom(future: Future[Any]) = Await.result(future, timeout.duration)
}
