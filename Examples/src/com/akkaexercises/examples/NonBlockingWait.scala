package com.akkaexercises.examples

import akka.actor.Actor
import akka.actor.Stash
import com.akkaexercises.util.TestActorSystem
import akka.actor.Props
import scala.concurrent.Future
import akka.pattern.pipe

case class PrintTotal

class NonBlockingWait extends Actor with Stash {
  implicit val ec = context.dispatcher 
  var total = 0;
  
  def receive = handleRequests
  
  def handleRequests: Receive = {
    case Request(index) => {
      println(s"handling $index")
      Future { someLongBlockingDBOp(index) } pipeTo self
      context become waitingForDBResponse
    }
    case PrintTotal => println(total)
  }
  
  def waitingForDBResponse: Receive = {
    case (i, j: Integer) => {
      println(s"received response to $i as $j")
      total += j
      context.unbecome()
      unstashAll
    }
    case _ => stash()
  }
  
  def someLongBlockingDBOp(i: Integer) = {
    Thread.sleep(1000); 
    (i, i*5); 
  }
}

object NonBlockingWait extends App with TestActorSystem {
  val actor = system.actorOf(Props[NonBlockingWait])
  
  actor ! Request(1)
  actor ! Request(2)
  actor ! Request(3)
  actor ! Request(4)
  actor ! Request(5)
  
  actor ! PrintTotal
}
