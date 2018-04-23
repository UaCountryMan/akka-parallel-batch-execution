package com.zemlyak.akka.parallelization;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.zemlyak.akka.parallelization.AbstractWorkerWithDelayActor.WorkerResult;
import com.zemlyak.akka.parallelization.AbstractWorkerWithDelayActor.WorkerRequest;

import java.util.*;

public class ParallelExecutingActor extends AbstractActor {
    private final Map<Integer, BatchState> batchesInProcessing = new HashMap<>();
    private final ParallelExecutingWorkerFactory workerFactory;

    public ParallelExecutingActor(ParallelExecutingWorkerFactory workerFactory) {
        this.workerFactory = workerFactory;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BatchRequest.class, this::onBatchRequest)
                .match(WorkerResult.class, this::onWorkerFinished)
                .build();
    }

    public void onBatchRequest(BatchRequest batch) {
        prepareActorsChildren(batch.tasks.size());

        batchesInProcessing.put(batch.id, new BatchState(batch, getSender()));

        int butchIndex = 0;
        for (ActorRef child : getContext().getChildren()) {
            child.tell(new WorkerRequest(batch.id, butchIndex, batch.tasks.get(butchIndex)), self());
            butchIndex++;
        }
    }

    public void onWorkerFinished(WorkerResult result) {
        BatchState batchState = batchesInProcessing.get(result.butchId);
        if (batchState != null) {
            batchState.processedNumber++;
            batchState.taskResult[result.indexInBatch] = result.processingResult;

            if (batchState.batch.tasks.size() <= batchState.processedNumber) {
                batchesInProcessing.remove(result.butchId);
                batchState.sender.tell(new BatchResult(batchState.batch.id, Arrays.asList(batchState.taskResult)), self());
            }
        }
    }

    private void prepareActorsChildren(int minimumChildrenNumber) {
        ActorContext context = getContext();
        Iterator<ActorRef> children = context.getChildren().iterator();
        for (int i = 0; i < minimumChildrenNumber; i++) {
            boolean hasNext = children.hasNext();
            if (hasNext) {
                children.next();
            } else {
                workerFactory.createWorker(context, "worker-" + i);
            }
        }
    }

    public static Props props(ParallelExecutingWorkerFactory workerFactory) {
        return Props.create(ParallelExecutingActor.class, workerFactory);
    }

    //////////////
    // Messages //
    //////////////

    public static class BatchRequest {
        final Integer id;
        final List<Object> tasks;

        public BatchRequest(Integer id, List<Object> tasks) {
            this.id = id;
            this.tasks = tasks;
        }
    }

    public static class BatchResult {
        public final Integer id;
        public final List<Object> responses;

        public BatchResult(Integer id, List<Object> responses) {
            this.id = id;
            this.responses = responses;
        }
    }

    //////////////////
    // Local Object //
    //////////////////

    static class BatchState {
        final BatchRequest batch;
        final ActorRef sender;
        int processedNumber;
        Object[] taskResult;

        BatchState(BatchRequest batch, ActorRef sender) {
            this.batch = batch;
            this.sender = sender;
            this.taskResult = new Object[batch.tasks.size()];
        }
    }
}
