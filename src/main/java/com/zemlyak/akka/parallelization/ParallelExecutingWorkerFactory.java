package com.zemlyak.akka.parallelization;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public interface ParallelExecutingWorkerFactory {
    ActorRef createWorker(AbstractActor.ActorContext context, String name);
}
