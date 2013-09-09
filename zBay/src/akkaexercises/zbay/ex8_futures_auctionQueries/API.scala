package akkaexercises.zbay.ex8_futures_auctionQueries

import akka.actor.Actor
import Auction.Protocol._
import zBay.Protocol._
import akka.pattern._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import akka.util.Timeout
import org.joda.time.DateTime

class API extends Actor {
  import API.Protocol._
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)
  implicit val ec = context.dispatcher

  def receive = {
    case AuctionBidRequest(auctionId, userId, value) =>
      auctionActorFor(auctionId).tell(Bid(value, userActorFor(userId)), sender)
    case AuctionStatusRequest(auctionId) =>
      auctionActorFor(auctionId).tell(StatusRequest, sender)
    case Query(expectedEndTime, currentAuctions) =>
      val matchingAuctionIdFutures = currentAuctions.map { (auctionId) =>
        val detailsFuture = auctionActorFor(auctionId) ? DetailsRequest
        detailsFuture.map {
          case DetailsResponse(actualEndTime) if (actualEndTime==expectedEndTime) => Some(auctionId)
          case _ => None
        }
      };
      val matchingAuctionIdsFuture = Future.sequence(matchingAuctionIdFutures)
      matchingAuctionIdsFuture.map(ids => AuctionQueryResponse(ids.flatMap(x => x))).pipeTo(sender)
  }

  def userActorFor(userId: Long) = context.actorFor(s"../../user$userId")
  def auctionActorFor(auctionId: Long) = context.actorSelection(s"../../auction$auctionId")
}
object API {
  object Protocol {
    case class Query(endTime: DateTime, currentAuctions: Set[Long])
  }
}