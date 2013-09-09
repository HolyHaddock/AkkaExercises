package akkaexercises.zbay.ex5_actorSelection_APIActor

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

  def receive = runningBehaviour

  val runningBehaviour: Receive = {
    case StatusRequest    => sender ! StatusResponse(currentHighestBid, Running)
    case Bid(value, from) => currentHighestBid = currentHighestBid max value
                             from ! BidOnNotification(self)
    case EndNotification  => context.become(endedBehaviour)
  }

  val endedBehaviour: Receive  = {
    case StatusRequest   => sender ! StatusResponse(currentHighestBid, Ended)
  }

  def atEndTime() = FiniteDuration((DateTime.now() to endTime).millis,
    TimeUnit.MILLISECONDS)
}

object Auction {
  object Protocol {
    case object StatusRequest
    case class StatusResponse(currentHighestBid: BigDecimal, state: State)
    case class Bid(value: BigDecimal, from: ActorRef)
    case object EndNotification

    sealed trait State
    case object Running extends State
    case object Ended extends State
  }
}