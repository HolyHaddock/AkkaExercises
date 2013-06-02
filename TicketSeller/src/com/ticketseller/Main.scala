package com.ticketseller

import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.collection.mutable.ListBuffer


case class Game(name : String, date : String, attendance : Int)

case class Report

case class BuyTickets(whoFor : String, quantity : Int)

class TicketSeller(game : Game) extends Actor with ActorLogging {
  
  var remainingTickets = game.attendance
  val report = new ListBuffer[String]()
  report += s"Selling ${game.attendance} tickets for ${game.name} on ${game.date}"
  
  def receive = {
    case BuyTickets(name, amount) => if (remainingTickets >= amount) {
      report += s"Sold ${amount} to ${name}."
      remainingTickets -= amount;
    } else 
      report += s"Could not sell ${amount} to ${name}."
      
    case Report => {
      report += s"I have ${game.attendance} tickets left."
      sender ! report.mkString("\n")
    }
  }
}


object Main {

  def main(args : Array[String]) {
    implicit val timeout = Timeout(5 seconds)

	val system = ActorSystem()
	
	implicit val ec = system.dispatcher

	val streetUrchin = system.actorOf(Props(creator = {() => new TicketSeller(Game("Rugby Sevens", "02-06-2013", 30))}))
	
	streetUrchin ? BuyTickets("Howard", 4)
	
	streetUrchin ? BuyTickets("Dave", 5)
	
	streetUrchin ? BuyTickets("TicketMeister", 21)
	
	streetUrchin ? BuyTickets("Mr Langston", 1)
	
	val reportFuture = streetUrchin ? Report

	reportFuture onSuccess {
      case report : String => {
        println(report);
        system.shutdown()
      }
	}

  }
  
}