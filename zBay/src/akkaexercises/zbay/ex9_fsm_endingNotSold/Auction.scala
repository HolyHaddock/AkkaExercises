package akkaexercises.zbay.ex9_fsm_endingNotSold

import akka.actor.{FSM, ActorRef, Actor}
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import User.Protocol.BidOnNotification
import Auction._
import Math.max

class Auction(endTime: DateTime) extends Actor with FSM[State, Double] {
  import Protocol._

  context.system.scheduler.scheduleOnce(atEndTime(), self, EndNotification)(context.system.dispatcher)

  startWith(Running, 0.0)

  when(Running) { respondToStatusRequestAs(Running) orElse {
    case Event(Bid(value, from), highestBid) => from ! BidOnNotification(self);
                                                stay using max(highestBid, value)
    case Event(EndNotification,  0.0)        => goto(NotSold)
    case Event(EndNotification,  _)          => goto(Sold)
    case Event(DetailsRequest,   _)          => stay replying DetailsResponse(endTime)
  }}

  when(Sold){ respondToStatusRequestAs(Sold) }
  when(NotSold){ respondToStatusRequestAs(NotSold) }

  def respondToStatusRequestAs(state: Auction.State): StateFunction = {
    case Event(StatusRequest, winningBid) => stay replying StatusResponse(winningBid, state)
  }

  def atEndTime() = FiniteDuration((DateTime.now() to endTime).millis,
    TimeUnit.MILLISECONDS)

}

object Auction {
  object Protocol {
    case object StatusRequest
    case class StatusResponse(currentHighestBid: Double, state: State)
    case object DetailsRequest
    case class DetailsResponse(endTime: DateTime)
    case class Bid(value: Double, from: ActorRef)
    case object EndNotification
  }


  sealed trait State
  case object Running extends State
  case object Sold extends State
  case object NotSold extends State
}