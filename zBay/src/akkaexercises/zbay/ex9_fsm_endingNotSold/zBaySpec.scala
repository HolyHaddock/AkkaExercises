package akkaexercises.zbay.ex9_fsm_endingNotSold

import akka.actor.{Props, ActorSystem}
import akka.pattern._
import org.specs2.mutable.Specification
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.specs2.time.NoTimeConversions
import scala.concurrent.{Await, Future}
import Auction.Protocol._
import Auction._
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import org.specs2.mock.Mockito
import zBay.Protocol._
import com.typesafe.config.ConfigFactory

class zBaySpec extends Specification
                     with NoTimeConversions
                     with Mockito { isolated
  "The zBay" should {
    "be able to handle bids and return an auctions status" in {
      val auctionId = responseFrom(zBay ? AuctionCreateRequest(exampleEndTime)).asInstanceOf[Long]
      zBay ! AuctionBidRequest(auctionId = auctionId,
                               userId    = 1,
                               value     = 1.00)
      Thread.sleep(100)
      responseFrom(zBay ? AuctionStatusRequest(auctionId)) must be equalTo {
        StatusResponse(1.00, Running)
      }
    }
    "be able to query for auctions by date" in {
      val auctionId1 = responseFrom(zBay ? AuctionCreateRequest(twoDaysTime)).asInstanceOf[Long]
      val auctionId2 = responseFrom(zBay ? AuctionCreateRequest(tomorrow)).asInstanceOf[Long]
      val auctionId3 = responseFrom(zBay ? AuctionCreateRequest(tomorrow)).asInstanceOf[Long]
      responseFrom(zBay ? AuctionQueryRequest(tomorrow)) must be equalTo {
        AuctionQueryResponse(Set(auctionId2, auctionId3))
      }
    }
  }

  val config = """akka.actor.deployment {
                 |  /zBay/api {
                 |    router = round-robin
                 |    nr-of-instances = 5
                 |  }
                 |}""".stripMargin
  implicit val system = ActorSystem("zBay", ConfigFactory.parseString(config))
  implicit val timeout = Timeout(3, TimeUnit.SECONDS)

  val exampleEndTime = DateTime.now + 7.days

  val tomorrow = DateTime.now + 1.day
  val twoDaysTime = DateTime.now + 2.day

  val zBay = system.actorOf(Props[zBay], "zBay")
  def responseFrom(future: Future[Any]) = Await.result(future, timeout.duration)
}
