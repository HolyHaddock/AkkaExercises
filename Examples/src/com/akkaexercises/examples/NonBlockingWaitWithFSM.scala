package com.akkaexercises.examples

import akka.actor.Actor
import akka.actor.Stash
import com.akkaexercises.util.TestActorSystem
import akka.actor.Props
import scala.concurrent.Future
import akka.pattern.{pipe, ask}
import akka.actor.{ Actor, ActorRef, FSM }
import akka.dispatch.OnComplete

sealed trait State
case object HandleRequest extends State
case object WaitForDBResponse extends State
 
sealed case class Total(total: Integer)

class NonBlockingWaitWithFSM extends Actor with Stash with FSM[State, Total] { 
  implicit val ec = context.dispatcher 

  startWith(HandleRequest, Total(0))
  
  when(HandleRequest) {
    case Event(Request(index), total) => {
      println(s"handling $index")
      Future { someLongBlockingDBOp(index) } pipeTo self
      goto(WaitForDBResponse) using total
    }
    case Event(PrintTotal, Total(t)) => stay replying(t)
  }
  
  when(WaitForDBResponse) {
    case Event((i,j: Integer), Total(t)) => {
      println(s"received response to $i as $j")
      unstashAll
      goto(HandleRequest) using Total(t + j) 
    }
    case _ => stash(); stay
  }
}

object NonBlockingWaitWithFSM extends App with TestActorSystem {
  val actor = system.actorOf(Props[NonBlockingWaitWithFSM])
  
  actor ! Request(1)
  actor ! Request(2)
  actor ! Request(3)
  actor ! Request(4)
  actor ! Request(5)
  
  actor ? PrintTotal onSuccess {
    case t => println(s"Total is: $t"); system.shutdown
  } 
}
