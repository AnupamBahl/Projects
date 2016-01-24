import akka.actor._

case class InitializeList(list : List[ActorRef], index : Int)
case class StartSpreadingRumour(list : List[ActorRef], index : Int)
case class SetNoOfNodes(nodes : Int)
case class GetSum(s : Double, w : Double)
case object ForwardSum
case object InitializeActorSystem
case object StartActorSystem
case object ListenToRumour
case object NodeStartNotification
case object InitializationComplete
case object SpreadRumour
case object CreateFrequency
case object NodeStopNotification
case object SelfTimer
case object NoTransmissionPossible
case object NodeResetNotification
case object IncrementCounter
