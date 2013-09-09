package akkaexercises.zbay.ex9_fsm_endingNotSold

import akka.actor.{ActorRef, Actor}
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import User.Protocol.BidOnNotification

class Auction(endTime: DateTime) extends Actor {
  import Auction.Protocol._

  var currentHighestBid = BigDecimal(0)

  context.system.scheduler.scheduleOnce(atEndTime(), self, EndNotification)(context.system.dispatcher)

  def receive = runningNoBidsBehaviour

  val runningNoBidsBehaviour: Receive = {
    case StatusRequest    => sender ! StatusResponse(currentHighestBid, RunningNoBids)
    case Bid(value, from) => currentHighestBid = currentHighestBid max value
      from ! BidOnNotification(self); context.become(runningBehaviour)
    case EndNotification  => context.become(notSoldBehaviour)
    case DetailsRequest   => sender ! DetailsResponse(endTime)
  }

  val runningBehaviour: Receive = {
    case StatusRequest    => sender ! StatusResponse(currentHighestBid, RunningWithBids)
    case Bid(value, from) => currentHighestBid = currentHighestBid max value
                             from ! BidOnNotification(self)
    case EndNotification  => context.become(soldBehaviour)
    case DetailsRequest   => sender ! DetailsResponse(endTime)
  }

  val soldBehaviour: Receive  = {
    case StatusRequest   => sender ! StatusResponse(currentHighestBid, Sold)
  }

  val notSoldBehaviour: Receive  = {
    case StatusRequest   => sender ! StatusResponse(currentHighestBid, NotSold)
  }

  def atEndTime() = FiniteDuration((DateTime.now() to endTime).millis,
    TimeUnit.MILLISECONDS)
}

object Auction {
  object Protocol {
    case object StatusRequest
    case class StatusResponse(currentHighestBid: BigDecimal, state: State)
    case object DetailsRequest
    case class DetailsResponse(endTime: DateTime)
    case class Bid(value: BigDecimal, from: ActorRef)
    case object EndNotification

    sealed trait State
    case object RunningNoBids extends State
    case object RunningWithBids extends State
    case object Sold extends State
    case object NotSold extends State
  }
}