import akka.actor._
import scala.util.control.Breaks._

case class StartWorker(list : Array[String], zeroes : Int)
case class WorkStopped(name : String)
case class StartController(zeroes : Int, strings : Int)
case class ResumeController(zeroes : Int)
case class DataReturnSix(count : Int)
case class DataReturnFour(count : Int)
case class DataReturnFive(count : Int)
case class DataReturnSev(count : Int)
case class ChildPrint(str : Array[String])
case object StopWork

class StringGenerator{
	val charList : Array[Char] = new Array[Char](100)
	charList(0) = ' '
	var charListLoc = 0
	val baseString = "AnupamBahl@"
	var found = false
	
	def setStart(startPoint : Int) ={
		getList(startPoint)
	}
	
	def getList(length : Int): Array[String] = {
		val arrLst : Array[String] = new Array[String](length)
		for(i <- 0 to length-1)
			arrLst(i) = generateNextString()
		return arrLst
	}
	
	def generateNextString():String = {
		if(charList(charListLoc) == 126){
			breakable {
				for(i <- (0 to charListLoc-1).reverse){
					if(charList(i)<126){
						charList(i) = (charList(i) + 1).toChar
						for(j <- i+1 to charListLoc)
							charList(j) = '!'
						found = true
						break
					}
				}
			}
			if(!found){
				if(charListLoc == 99)
					println("have to stop now")
				else{
					charListLoc += 1
					for(i <- (0 to charListLoc))
						charList(i) = '!'
				}
			}
			else{
				found = false
			}		
		} 
		else
			charList(charListLoc) = (charList(charListLoc) + 1).toChar
	
		return ( baseString + charList.slice(0, charListLoc+1).mkString ) 
	}
		
}


class ConvertAndPrint{
	var zero = 0
	var list = List[String]()
	var m = Array[Byte](32)
	var arr = ""
	var allZeroes = true
	
	def shaEvaluator(inArr : Array[String]){
		for(i <- 0 to inArr.length){
		allZeroes = true
			for(i <- 0 to zero-1){
				if(list(i) != '0')
					allZeroes = false
			}
			if(allZeroes && list(zero) != '0')
				printf("String : %s \t Hash : %s\n",inArr(i), list(i))
		}
	}
	
	def shaCalculator(inArr : Array[String], zeroes : Int) = {
		zero = zeroes
		for(i <- 0 to inArr.length - 1){
			m = java.security.MessageDigest.getInstance("SHA-256").digest(inArr(i).getBytes("UTF-8"))
			arr = m.map("%02x".format(_)).mkString
			list = arr :: list
			list = list.reverse
		}
	}
}

case class GetEvaluator(obj : ConverAndPrint)
case class GetObject(obj : ConverAndPrint)

class ActorOne extends Actor{
	val obj = new ConvertAndPrint()
	var one = List[String]()
	val time = System.nanoTime
	def receive = {
		case StartWorker(array : Array[String], zeroes : Int) =>
			obj.shaCalculator(array, zeroes)
			sender ! GetObject(obj)
			
		case StopWork =>
			println("One : "+(System.nanoTime - time)/(1e6*1000))
			//one.foreach((str : String) => printf(str))
			sender ! WorkStopped("First")
			context.stop(self)
	}
}


class ActorTwo extends Actor{
	var two = List[String]()
	val time = System.nanoTime
	
	def receive = {
		case StartEvaluator(obj : ConverAndPrint, array : Array[String]) =>
			obj.shaEvaluator(array)
			
		case StopWork =>
			println("Two : "+(System.nanoTime - time)/(1e6*1000))
			//two.foreach(print)
			sender ! WorkStopped("Second")
			context.stop(self)
	}
}

class ActorThree extends Actor{
	val obj = new ConvertAndPrint()
	var three = List[String]()
	val time = System.nanoTime
	def receive = {
		case StartWorker(array : Array[String], zeroes : Int) =>
			obj.shaCalculator(array, zeroes)
			
		case StopWork =>
			println("Three : "+(System.nanoTime - time)/(1e6*1000))
			//three.foreach(print)
			sender ! WorkStopped("Third")
			context.stop(self)
	}
}

class ActorIV extends Actor{
	val obj = new ConvertAndPrint()
	var four = List[String]()
	val time = System.nanoTime
	def receive = {
		case StartWorker(array : Array[String], zeroes : Int) =>
			obj.shaCalculator(array, zeroes)
			
		case StopWork =>
			println("Four(IV) : "+(System.nanoTime - time)/(1e6*1000))
			//four.foreach(print)
			sender ! WorkStopped("Fourth")
			context.stop(self)
	}
}

class ActorFour(actorOne : ActorRef, actorTwo : ActorRef) extends Actor{
	val obj = new StringGenerator()
	val converterObj = new ConverAndPrint()
	var size = 50
	var list = new Array[String](size)
	var count = 500
	val time = System.nanoTime
	
	def receive = {
		case StartController(zeroes : Int, strings : Int) =>
			size = 1000
			count = (strings / size).toInt
			list = obj.getList(size)
			actorOne ! StartWorker(list, zeroes)
			count = count -1 
			sender ! DataReturnFour(count)
		case ResumeController(zeroes : Int) =>
			list = obj.getList(size)
			actorOne ! StartWorker(list, zeroes)
			count = count -1 
			sender ! DataReturnFour(count)
			
		case GetObect(obj : ConverAndPrint)=>
			converterObj = obj
			actorTwo ! StartEvaluator(converterObj)
			
		case StopWork =>
			actorOne forward StopWork
			actorTwo forward StopWork
			println("Four : "+(System.nanoTime - time)/(1e6*1000))
			context.stop(self)
			
	}
}

class ActorFive(actorTwo : ActorRef) extends Actor{
	val obj = new StringGenerator()
	var size = 50
	var list = new Array[String](size)
	var count = 500
	val time = System.nanoTime
	
	def receive = {
		case StartController(zeroes : Int, strings : Int) =>
			size = 1000
			count = (strings / size).toInt
			obj.setStart(strings * 1)
			list = obj.getList(size)
			actorTwo ! StartWorker(list, zeroes : Int)
			count = count - 1
			sender ! DataReturnFive(count)
		case ResumeController(zeroes : Int) =>
			list = obj.getList(size)
			actorTwo ! StartWorker(list, zeroes : Int)
			count = count - 1
			sender ! DataReturnFive(count)
		
		case StopWork =>
			actorTwo forward StopWork
			println("Five : "+(System.nanoTime - time)/(1e6*1000))
			context.stop(self)
		}
}

class ActorSix(actorThree : ActorRef) extends Actor{
	val obj = new StringGenerator()
	var size = 50
	var list = new Array[String](size)
	var count = 500
	val time = System.nanoTime
	
	def receive = {
		case StartController(zeroes : Int, strings : Int) =>
			size = 1000
			count = (strings / size).toInt
			obj.setStart(strings * 2)
			list = obj.getList(size)
			actorThree ! StartWorker(list, zeroes : Int)
			count = count - 1
			sender ! DataReturnSix(count)
		case ResumeController(zeroes : Int) =>
			list = obj.getList(size)
			actorThree ! StartWorker(list, zeroes : Int)
			count = count - 1
			sender ! DataReturnSix(count)
			
		case StopWork =>
			actorThree forward StopWork
			println("Six : "+(System.nanoTime - time)/(1e6*1000))
			context.stop(self)
		}
}

class ActorSev(actorIV : ActorRef) extends Actor{
	val obj = new StringGenerator()
	var size = 50
	var list = new Array[String](size)
	var count = 500
	val time = System.nanoTime
	
	def receive = {
		case StartController(zeroes : Int, strings : Int) =>
			size = 1000
			count = (strings / size).toInt
			obj.setStart(strings * 3)
			list = obj.getList(size)
			actorIV ! StartWorker(list, zeroes : Int)
			count = count - 1
			sender ! DataReturnSev(count)
		case ResumeController(zeroes : Int) =>
			list = obj.getList(size)
			actorIV ! StartWorker(list, zeroes : Int)
			count = count - 1
			sender ! DataReturnSev(count)
			
		case StopWork =>
			actorIV forward StopWork
			println("Sev : "+(System.nanoTime - time)/(1e6*1000))
			context.stop(self)
		}
}


class Controller(actorOne:ActorRef, actorTwo:ActorRef, actorThree:ActorRef, actorFour:ActorRef,
				actorFive:ActorRef, actorSix:ActorRef, actorSev:ActorRef, actorIV:ActorRef, 
				system:ActorSystem, noOfZeroes:Int, noOfStrings:Int) extends Actor{
	var respOne = false
	var respTwo = false
	var respThree = false
	var respFour = false
	val time = System.nanoTime
	def receive={
		case StartController =>
			actorFour ! StartController(noOfZeroes, noOfStrings)
			//actorFive ! StartController(noOfZeroes, noOfStrings)
			//actorSix ! StartController(noOfZeroes, noOfStrings)
			//actorSev ! StartController(noOfZeroes, noOfStrings)
			
		case DataReturnFour(count : Int) =>
			if(count == 0)
				actorFour ! StopWork
			else
				actorFour ! ResumeController(noOfZeroes)
			
		/*case DataReturnSix(count : Int) =>
			if(count == 0)
				actorSix ! StopWork
			else
				actorSix ! ResumeController(noOfZeroes)
			
		case DataReturnFive(count : Int) =>
			if(count == 0)
				actorFive ! StopWork
			else
				actorFive ! ResumeController(noOfZeroes)
			
		case DataReturnSev(count : Int) =>
			if(count == 0)
				actorSev ! StopWork
			else
				actorSev ! ResumeController(noOfZeroes)*/
			
		case WorkStopped(str : String) =>
			if(str == "First")
				respOne = true
			else if(str == "Second")
				respTwo = true
			else if(str == "Third")
				respThree = true
			else if(str == "Fourth")
				respFour = true
			if(respOne && respTwo){
				println("Main Time :"+(System.nanoTime - time)/(1e6*1000))
				system.shutdown
			}
		
	}
}

object mainObject{
	var noOfZeroes = 3
	var noOfStrings = 25000
	
	def main(args : Array[String]){
		if(args.isEmpty){
			println("Taking 3 zeroes by default\nTaking 100000 strings by default")
		}
		else{
			if(args(0).contains(".")){
				
			}
			else
				noOfZeroes = args(0).toInt
				noOfStrings = (noOfStrings*(args(1).toInt))
			}
		val bitcoinSystem = ActorSystem("BitcoinSystem")
		val actorOne = bitcoinSystem.actorOf(Props[ActorOne], "ActorOne")
		val actorTwo = bitcoinSystem.actorOf(Props[ActorTwo], "ActorTwo")
		val actorThree = bitcoinSystem.actorOf(Props[ActorThree], "ActorThree")
		val actorIV = bitcoinSystem.actorOf(Props[ActorIV], "ActorIV")
		val actorFour = bitcoinSystem.actorOf(Props(new ActorFour(actorOne, actorTwo)), "ActorFour")
		val actorFive = bitcoinSystem.actorOf(Props(new ActorFive(actorTwo)), "ActorFive")
		val actorSix = bitcoinSystem.actorOf(Props(new ActorSix(actorThree)), "ActorSix")
		val actorSev = bitcoinSystem.actorOf(Props(new ActorSev(actorIV)), "ActorSev")
		val controller = bitcoinSystem.actorOf(Props(new Controller(actorOne, actorTwo, actorThree, actorFour, 
												actorFive, actorSix,actorSev, actorIV, bitcoinSystem, noOfZeroes, noOfStrings)), "Controller")
		
		
		controller ! StartController
	}
}


