package akkaexercises.zbay.ex9_fsm_endingNotSold

import akka.actor.ActorSystem
import akka.pattern._
import akka.testkit.TestActorRef
import org.specs2.mutable.Specification
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.specs2.time.NoTimeConversions
import scala.concurrent.Future
import Auction.Protocol._
import Auction._
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import org.specs2.mock.Mockito
import User.Protocol._
import zBay.Protocol._
import com.typesafe.config.{ConfigFactory, Config}
import scala.collection.JavaConversions._

class AuctionSpec extends Specification
                     with NoTimeConversions
                     with Mockito { isolated
  "An auction" should {
    "be able to describe itself after it has been started" in {
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(0.00, Running)
      }
    }
    "accept a bid and update the current bid amount" in {
      auction ! Bid(1.00, user)
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(1.00, Running)
      }
    }
    "not accept a bid for lower than the current highest" in {
      auction ! Bid(1.00, user)
      auction ! Bid(0.99, user)
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(1.00, Running)
      }
    }
    "be able to tell if auction has finished" in {
      auction ! Bid(0.50, user)
      auction ! EndNotification
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(0.5, Sold)
      }
    }
    "ignore bids after auction finish" in {
      auction ! Bid(0.50, user)
      auction ! EndNotification
      auction ! Bid(1.00, user)
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(0.50, Sold)
      }
    }
    "tell the user actor that the bid was received" in {
      auction ! Bid(0.50, user)
      responseFrom(user ? ListAuctionsRequest) must be equalTo {
        ListAuctionsResponse(Seq(auction))
      }
    }
    "be not won if no bids placed" in {
      auction ! EndNotification
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(0, NotSold)
      }
    }
  }

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(3, TimeUnit.SECONDS)

  val exampleEndTime = DateTime.now + 7.days

  val auction = TestActorRef(new Auction(exampleEndTime), "auction1")
  val user = TestActorRef(new User, "user1")
  val api = TestActorRef(new API)
  def responseFrom(future: Future[Any]) = future.value.get.get
}
