package com.zemlyak.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.time.Duration;

public class WorkerActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Task.class, this::processTaskAsync)
                .match(ResponseReady.class, this::transformAndSendBack)
                .build();
    }

    private void processTaskAsync(Task task) {

        getContext().getSystem().scheduler()
                .scheduleOnce(
                        Duration.ofSeconds(5),
                        getSelf(),
                        new ResponseReady(task),
                        getContext().dispatcher(),
                        sender()
                );
    }

    private void transformAndSendBack(ResponseReady result) {
        Task task = result.task;
        int sum = task.sumTask.numberList.stream().mapToInt(Integer::intValue).sum();
        sender().tell(new ParallelExecutingActor.TaskProcessed(task.butchId, task.indexInBatch, sum), getSelf());
    }

    public static Props props() {
        return Props.create(WorkerActor.class);
    }

    //////////////
    // Messages //
    //////////////

    static class Task {
        final Integer butchId;
        final Integer indexInBatch;
        final ParallelExecutingActor.TaskBatch.SumTask sumTask;

        Task(Integer butchId, Integer indexInBatch, ParallelExecutingActor.TaskBatch.SumTask sumTask) {
            this.butchId = butchId;
            this.indexInBatch = indexInBatch;
            this.sumTask = sumTask;
        }
    }

    static class ResponseReady {
        final Task task;

        public ResponseReady(Task task) {
            this.task = task;
        }
    }

}
