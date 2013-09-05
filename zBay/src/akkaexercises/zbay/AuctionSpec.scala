package akkaexercises.zbay


import akka.actor.ActorSystem
import akka.pattern._
import akka.testkit.TestActorRef
import org.specs2.mutable.Specification
import akkaexercises.zbay.Auction.Protocol._
import scala.util.Success
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import org.specs2.time.NoTimeConversions
import scala.concurrent.Future

class AuctionSpec extends Specification with NoTimeConversions { isolated
  "An auction" should {
    "be able to describe itself after it has been started" in {
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(exampleStartTime, exampleEndTime, 0.00)
      }
    }
    "accept a bid" in {
      responseFrom(auction ? Bid(1.00)) must be equalTo BidSuccessful
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(exampleStartTime, exampleEndTime, 1.00)
      }
    }
  }

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(3, TimeUnit.SECONDS)

  val exampleStartTime = DateTime.now
  val exampleEndTime = exampleStartTime + 7.days

  lazy val auction = TestActorRef(new Auction(exampleStartTime, exampleEndTime))
  def responseFrom(future: Future[Any]) = future.value.get.get
}
