package akkaexercises.zbay

import akka.actor.Actor
import org.joda.time.DateTime

class Auction(startTime: DateTime,
              endTime: DateTime) extends Actor {
  import Auction.Protocol._

  var currentHighestBid = BigDecimal(0)

  def receive = {
    case StatusRequest =>
      sender ! StatusResponse(startTime, endTime, currentHighestBid)
    case Bid(value) => {
      currentHighestBid = value
      sender ! BidSuccessful
    }
  }
}
object Auction {
  object Protocol {
    case object StatusRequest
    case class StatusResponse(startTime: DateTime,  endTime: DateTime, currentHighestBid: BigDecimal)
    case class Bid(value: BigDecimal)
    case object BidSuccessful
  }
}
