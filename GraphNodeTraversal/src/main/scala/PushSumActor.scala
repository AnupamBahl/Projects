import akka.actor._
import scala.math._

class PushSumDeathWatchActor extends Actor{
  var nodeStartSum = 0
  var nodeStopSum = 0
  var noOfNodes = 0

  def receive = {
    case SetNoOfNodes(nodes : Int) =>
      noOfNodes = nodes
      //println("Number of nodes : "+nodes)

    case NodeStartNotification =>
      nodeStartSum += 1
      //println("Actors Started : "+nodeStartSum)
      if(nodeStartSum == noOfNodes){
        printf("\n\n_________________________________\nSystem Converged. Time : ")
        context.stop(self)
      }

    case NodeStopNotification =>
      nodeStopSum += 1
      //println("Actors stopped : "+nodeStopSum)
    /*  if(nodeStartSum == noOfNodes)
        printf("\n\n_________________________________\nSystem Converged. Time : ")

      else
        printf("\n\n_________________________________\nNO Convergence. Time : ")

      context.stop(self)*/
  }
}



class PushSumAlgorithmActor(deathWatchActor : ActorRef) extends Actor{
  var personalList = List[ActorRef]()
  var index = 0
  var s = 0.0
  var w = 1.0
  var stopCounter = 0
  var startedSpreading = false
  var randomInt = 0
  val randomObject = scala.util.Random
  val differenceConst = math.pow(10, -10)
  val r = scala.util.Random

  def receive = {
    case InitializeList(list : List[ActorRef], idx : Int)=>
      personalList = list
      index = idx
      s = idx+1
      sender ! InitializationComplete

    case GetSum(receivedS : Double, receivedW : Double) => {
      if(!startedSpreading){
        startedSpreading = true
        deathWatchActor ! NodeStartNotification
        self ! ForwardSum
      }
      else if( ((s+receivedS)/(w+receivedW)) - (s/w) < differenceConst){
        stopCounter += 1
        if(stopCounter == 3){
          deathWatchActor ! NodeStopNotification
          context.stop(self)
        }
      }
      else
        stopCounter = 0

      s += receivedS
      w += receivedW
      //println("Received \tSum : "+s+"\t\tW : "+w+"\t\tindex : "+index)
      //self ! ForwardSum
    }

    case ForwardSum => {
      randomInt = randomObject.nextInt(personalList.length)
      personalList(randomInt) ! GetSum(s/2, w/2)
      s -= s/2
      w -= w/2
      //println("Sent \t\tSum : "+s+"\t\tW : "+w+"\t\tindex : "+index)
      self ! CreateFrequency
    }

    case CreateFrequency =>
      Thread sleep 20
      self ! ForwardSum

  }
}
