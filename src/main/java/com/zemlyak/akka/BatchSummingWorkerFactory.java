package com.zemlyak.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.zemlyak.akka.parallelization.AbstractWorkerWithDelayActor;
import com.zemlyak.akka.parallelization.ParallelExecutingWorkerFactory;

public class BatchSummingWorkerFactory implements ParallelExecutingWorkerFactory {

    @Override
    public ActorRef createWorker(AbstractActor.ActorContext context, String name) {
        return context.actorOf(BatchSummingWorkerActor.props(), name);
    }

    private static class BatchSummingWorkerActor extends AbstractWorkerWithDelayActor {

        @Override
        public Object processTaskWithResult(Object dataForProcessing) {
            BatchSummingActor.SumTask task = (BatchSummingActor.SumTask) dataForProcessing;
            return task.numberList.stream().mapToInt(Integer::intValue).sum();
        }

        public static Props props() {
            return Props.create(BatchSummingWorkerActor.class);
        }
    }
}
