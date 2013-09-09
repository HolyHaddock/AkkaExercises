package akkaexercises.zbay.ex6_hierarchy_zBayActor

import akka.actor.Actor
import Auction.Protocol.{Bid, StatusRequest}
import zBay.Protocol._

class API extends Actor {

  def receive = {
    case AuctionBidRequest(auctionId, userId, value) =>
      auctionActorFor(auctionId).tell(Bid(value, userActorFor(userId)), sender)
    case AuctionStatusRequest(auctionId) =>
      auctionActorFor(auctionId).tell(StatusRequest, sender)
  }

  def userActorFor(userId: Long) = context.actorFor(s"../user$userId")
  def auctionActorFor(auctionId: Long) = context.actorSelection(s"../auction$auctionId")
}
