import akka.actor._
import scala.math._

class RootActor(system : ActorSystem, noOfNodes : Int, topology : String, algorithm : String) extends Actor{
  val fullTop = "full"
  val d3Top = "3D"
  val lineTop = "line"
  val imp3DTop = "imp3D"
  val gossipAlgo = "gossip"
  val pushSumAlgo = "push-sum"

  var initializationComplete = false
  var newNoOfNodes = noOfNodes
  var cubeDim = 0
  var squareCube = 0
  var initializationCount = 0
  var time = System.currentTimeMillis
  var listOfAllActors = List[ActorRef]()

  val pushSumDeathWatchActor = context.actorOf(Props[PushSumDeathWatchActor], "PushSumDeathWatchActor")
  val deathWatchActor = context.actorOf(Props[DeathWatchActor], "DeathWatchActor")

  //Watch the child to know when everyone has received the data
  context.watch(deathWatchActor)
  context.watch(pushSumDeathWatchActor)

  def receive = {

    case InitializeActorSystem => {
      if(!initializationComplete)
      {
        //Create a list of the number of actors required
        if(!initializeAllActorsList()){
          printf("Invliad Algorithm. Valid values are : '%s' and '%s'\n", gossipAlgo, pushSumAlgo)
          sender ! "error"
        }
        //Initiate actors with respective neighbours based on topology
        if(!createTopology()){
          printf("Invalid Topology. Valid values are : '%s', '%s', '%s' and '%s'\n",fullTop, d3Top, lineTop, imp3DTop)
          sender ! "error"
        }
        initializationComplete = true
      }
      deathWatchActor ! SetNoOfNodes(newNoOfNodes)
      pushSumDeathWatchActor ! SetNoOfNodes(newNoOfNodes)
      if(initializationCount == newNoOfNodes){
        sender ! "true"
      }
      else
        sender ! "false"
    }

    case StartActorSystem => {
      val randomObject = scala.util.Random
      //Start measuring time
      time = System.currentTimeMillis
      //Start actor system
      if(algorithm == gossipAlgo)
        listOfAllActors(randomObject.nextInt(newNoOfNodes-1)) ! ListenToRumour
      else
        listOfAllActors(randomObject.nextInt(newNoOfNodes-1)) ! GetSum(0, 0)
    }

    case InitializationComplete =>
      initializationCount += 1

    case Terminated(deathRef) => {
      //Stop all processing and print time
      time = System.currentTimeMillis - time
      println(time+" milliseconds")
      printf("_________________________________\n\n")

      system.shutdown
    }
  }

  // Creating a list of all actors based on their algorithm
  def initializeAllActorsList() : Boolean = {
    if(topology == d3Top || topology == imp3DTop){
      // Setting up data for 3D topology
      newNoOfNodes = getProperNumberOfNodes()
      cubeDim = math.cbrt(newNoOfNodes).toInt
      squareCube = cubeDim*cubeDim
    }

    if(algorithm == gossipAlgo){
      for(i<- 0 to newNoOfNodes-1){
        val actor = context.actorOf(Props(new GossipAlgorithmActor(deathWatchActor)), "GossipAlgorithmActor"+i.toString)
        listOfAllActors = listOfAllActors ::: List[ActorRef](actor)
      }
    return true
    }
    else if(algorithm == pushSumAlgo){
      for(i <- 0 to newNoOfNodes-1){
        val actor = context.actorOf(Props(new PushSumAlgorithmActor(pushSumDeathWatchActor)), "PushSumAlgorithmActor"+i.toString)
        listOfAllActors = listOfAllActors ::: List[ActorRef](actor)
      }
    return true
    }
    return false
  }

  //Initializing all actors with their respective lists
  def createTopology() : Boolean = {
    topology match{
        case `fullTop` =>
          //Initialize first and last actors of list
          listOfAllActors(0) ! InitializeList(listOfAllActors.drop(1), 0)
          listOfAllActors(noOfNodes-1) ! InitializeList(listOfAllActors.dropRight(1), noOfNodes-1)
          //Initialize the rest - each actor gets a list of all actors except itself
          for(i <- 1 to noOfNodes-2){
            var tempList = listOfAllActors.dropRight(noOfNodes-i) ::: listOfAllActors.drop(i+1)
            listOfAllActors(i) ! InitializeList(tempList , i)
          }
          return true

        case `d3Top` =>
          for(i <- 0 to newNoOfNodes-1){
            listOfAllActors(i) ! InitializeList(getNeighbours(i), i)
          }
          return true

        case `lineTop` =>
          //Initialize first and last actors of the list
          listOfAllActors(0) ! InitializeList(List(listOfAllActors(1)), 0)
          listOfAllActors(noOfNodes-1) ! InitializeList(List(listOfAllActors(noOfNodes-2)), noOfNodes-1)
          //Initialize the rest - each actor gets a list of all actors except itself
          for(i <- 1 to noOfNodes-2){
            listOfAllActors(i) ! InitializeList(List(listOfAllActors(i-1)) ::: List(listOfAllActors(i+1)), i)
          }
          return true

        case `imp3DTop` =>
          val randomObject = scala.util.Random
          val randomInt = randomObject.nextInt(newNoOfNodes-1)
          for(i <- 0 to newNoOfNodes-1){
            var localList = getNeighbours(i)
            localList = listOfAllActors(randomInt)::localList
            listOfAllActors(i) ! InitializeList(localList, i)
          }
          return true

        case _ =>
          return false
    }
  }

  // Getting a number which can be evenly divided into a cube
  def getProperNumberOfNodes() : Int = {
    var cubeRoot = math.cbrt(noOfNodes)
    var newNo = cubeRoot.toInt
    if((cubeRoot - newNo) != 0){
      val big = math.pow(newNo+1, 3)
      val small = math.pow(newNo, 3)
      if(big-noOfNodes < noOfNodes-small)
        newNo += 1
    }
    return (math.pow(newNo,3).toInt)
  }

  // Getting neighbours of a cube at a particular index
  def getNeighbours(index : Int) : List[ActorRef] = {
    var inn = index % squareCube
    var inn_n = inn % cubeDim
    var neighbours = List[ActorRef]()
    //Right
      if(inn_n + 1 < cubeDim)
        neighbours = (listOfAllActors(index+1))::neighbours
    //Left
      if(inn_n - 1 >= 0)
        neighbours = (listOfAllActors(index-1))::neighbours
    //Up
      if(inn - cubeDim >= 0)
        neighbours = (listOfAllActors(index-cubeDim))::neighbours
    //Down
      if(inn + cubeDim < squareCube)
        neighbours = (listOfAllActors(index+cubeDim))::neighbours
    //Further
      if(index+squareCube<(newNoOfNodes))
        neighbours = (listOfAllActors(index+(squareCube)))::neighbours
    //Behind
      if(index-squareCube>=0)
        neighbours = (listOfAllActors(index-(squareCube)))::neighbours
    return neighbours.reverse
  }

}
