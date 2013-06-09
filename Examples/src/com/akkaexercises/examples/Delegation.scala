package com.akkaexercises.examples

import akka.actor.Actor
import akka.actor.Stash
import akka.pattern.ask
import com.akkaexercises.util.TestActorSystem
import akka.actor.Props
import scala.concurrent.Future
import akka.pattern.pipe

class Parent extends Actor {
  def receive = {
    case r:Request => {
      context.system.actorOf(Props(new Child(r.index))) forward r
    }
  }
}

class Child(i: Integer) extends Actor {
  def receive = {
    case r:Request => sender ! s"Child $i processing $r"
  }
}

object Delegation extends App with TestActorSystem {
  val actor = system.actorOf(Props[Parent])
  
  actor ? Request(1) onSuccess { case response => println(response) }
  actor ? Request(2) onSuccess { case response => println(response) }
  actor ? Request(3) onSuccess { case response => println(response) }
  actor ? Request(4) onSuccess { case response => println(response) }
  actor ? Request(5) onSuccess { case response => println(response) }
  
}