package akkaexercises.zbay.basicActorWithAuctionEnding

import akka.actor.Actor
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

class Auction(endTime: DateTime) extends Actor {
  import Auction.Protocol._

  var currentHighestBid = BigDecimal(0)
  var currentStatus: State = Running

  context.system.scheduler.scheduleOnce(atEndTime(), self, EndNotification)
                                       (context.system.dispatcher)

  def receive = {
    case StatusRequest   => sender ! StatusResponse(currentHighestBid, currentStatus)
    case Bid(value)      => currentHighestBid = currentHighestBid max value
    case EndNotification => currentStatus = Ended
  }

  def atEndTime() = FiniteDuration((DateTime.now() to endTime).millis,
                                   TimeUnit.MILLISECONDS)
}

object Auction {
  object Protocol {
    case object StatusRequest
    case class StatusResponse(currentHighestBid: BigDecimal, state: State)
    case class Bid(value: BigDecimal)
    case object EndNotification

    sealed trait State
    case object Running extends State
    case object Ended extends State
  }
}