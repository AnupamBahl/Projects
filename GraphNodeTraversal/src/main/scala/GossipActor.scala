import akka.actor._

class DeathWatchActor extends Actor{
  var nodeStartSum = 0
  var nodeStopSum = 0
  var noOfNodes = 0
  var nonTransmittingNodes = 0
  var counter = 0

  def receive = {
    case SetNoOfNodes(nodes : Int) =>
      noOfNodes = nodes

    case NodeStartNotification =>
      nodeStartSum += 1
      counter = 0
      if(nodeStartSum == noOfNodes){
        println(nodeStartSum)
        printf("\n\n_________________________________\nSystem Converged. Time : ")
        context.stop(self)
      }
      self ! CreateFrequency

    case NodeResetNotification =>
      counter = 0

    case CreateFrequency =>
      if(counter > 500){
        printf("\n\n_________________________________\nNO Convergence. Time : ")
        context.stop(self)
      }
      counter += 1
      Thread sleep 2
      self ! CreateFrequency

    case NodeStopNotification =>
      nodeStopSum += 1
      counter = 0

  }
}

class GossipAlgorithmActor(deathWatchActor : ActorRef) extends Actor{
  var personalList = List[ActorRef]()
  var timeToLive = 10
  var index = 0
  var startedSpreading = false
  var randomInt = 0
  var stop = false
  val randomObject = scala.util.Random

  def receive = {
    case InitializeList(list : List[ActorRef], idx : Int)=> {
      personalList = list
      index = idx
      //printf("Index : %d, List Length : %d\n",index, personalList.length)
      sender ! InitializationComplete
    }

    case ListenToRumour => {
      if(!startedSpreading){
        //println(index + " Started with actors :"+personalList.length)
        startedSpreading = true
        timeToLive -= 1
        deathWatchActor ! NodeStartNotification
        self ! SpreadRumour
      }
      if(timeToLive < 1){
        //println("inside kill zone of "+index.toString)
        deathWatchActor ! NodeStopNotification
        context.stop(self)
      }
      timeToLive -= 1
      deathWatchActor ! NodeResetNotification
    }

    case SpreadRumour =>
        randomInt = randomObject.nextInt(personalList.length)
        //printf("rumour from %d to %s\n", index, personalList(randomInt).toString)
        personalList(randomInt) ! ListenToRumour
        self ! CreateFrequency

    case CreateFrequency =>
      Thread sleep 20
      self ! SpreadRumour

  }
}
