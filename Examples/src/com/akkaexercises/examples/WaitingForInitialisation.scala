package com.akkaexercises.examples

import akka.actor.Actor
import akka.actor.Stash
import com.akkaexercises.util.TestActorSystem
import akka.actor.Props
 
case class Initialisation

class WaitingForInitialisation extends Actor with Stash {
  var initialised = false
  
  def receive = {
    case r: Request if !initialised => stash()
    case r: Request if initialised  => println(s"Processing $r")
    case Initialisation             => { println("Initialised!"); 
									     initialised = true; 
									     unstashAll()    
    }
  }
}

object WaitingForInitialisation extends App with TestActorSystem {
  val actor = system.actorOf(Props[WaitingForInitialisation])
  
  actor ! Request(1)
  actor ! Request(2)
  actor ! Request(3)
  actor ! Initialisation
  actor ! Request(4)
  actor ! Request(5)
  
  system.shutdown
}