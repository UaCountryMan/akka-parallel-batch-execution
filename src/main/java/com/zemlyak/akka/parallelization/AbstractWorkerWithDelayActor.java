package com.zemlyak.akka.parallelization;

import akka.actor.AbstractActor;

import java.time.Duration;

public abstract class AbstractWorkerWithDelayActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WorkerRequest.class, this::processTaskAsync)
                .match(ResponseReady.class, this::transformAndSendBack)
                .build();
    }

    private void processTaskAsync(WorkerRequest task) {

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
        WorkerRequest task = result.task;
        sender().tell(new WorkerResult(task.butchId, task.indexInBatch, processTaskWithResult(task.dataForProcessing)), getSelf());
    }

    public abstract Object processTaskWithResult(Object dataForProcessing);

    //////////////
    // Messages //
    //////////////

    static class WorkerRequest {
        final Integer butchId;
        final Integer indexInBatch;
        final Object dataForProcessing;

        WorkerRequest(Integer butchId, Integer indexInBatch, Object dataForProcessing) {
            this.butchId = butchId;
            this.indexInBatch = indexInBatch;
            this.dataForProcessing = dataForProcessing;
        }
    }

    static class WorkerResult {
        final Integer butchId;
        final Integer indexInBatch;
        final Object processingResult;

        WorkerResult(Integer butchId, Integer indexInBatch, Object processingResult) {
            this.butchId = butchId;
            this.indexInBatch = indexInBatch;
            this.processingResult = processingResult;
        }
    }

    //////////////////
    // Local Object //
    //////////////////

    static class ResponseReady {
        final WorkerRequest task;

        ResponseReady(WorkerRequest task) {
            this.task = task;
        }
    }

}
