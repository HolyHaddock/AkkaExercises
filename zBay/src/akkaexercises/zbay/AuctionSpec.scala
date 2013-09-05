package akkaexercises.zbay


import akka.actor.ActorSystem
import akka.pattern._
import akka.testkit.TestActorRef
import org.specs2.mutable.Specification
import akkaexercises.zbay.Auction.Protocol._
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.specs2.time.NoTimeConversions
import scala.concurrent.Future

class AuctionSpec extends Specification with NoTimeConversions { isolated
  "An auction" should {
    "be able to describe itself after it has been started" in {
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(0.00)
      }
    }
    "accept a bid and update the current bid amount" in {
      auction ! Bid(1.00)
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(1.00)
      }
    }
    "not accept a bid for lower than the current highest" in {
      auction ! Bid(1.00)
      auction ! Bid(0.99)
      responseFrom(auction ? StatusRequest) must be equalTo {
        StatusResponse(1.00)
      }
    }
  }

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(3, TimeUnit.SECONDS)

  lazy val auction = TestActorRef(new Auction())
  def responseFrom(future: Future[Any]) = future.value.get.get
}
