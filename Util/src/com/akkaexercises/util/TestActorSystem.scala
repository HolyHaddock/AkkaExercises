package com.akkaexercises.util

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.util.Timeout

trait TestActorSystem extends App {
  val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)
  implicit val ec = system.dispatcher
}