package com.zemlyak.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ParallelExecutingActor extends AbstractActor {
    private Map<Integer, BatchState> batchesInProcessing = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskBatch.class, this::processButch)
                .match(TaskProcessed.class, this::onWorkerFinished)
                .build();
    }

    public void processButch(TaskBatch taskBatch) {
        prepareActorsChildren(taskBatch.sumTasks.size());

        batchesInProcessing.put(taskBatch.id, new BatchState(taskBatch, getSender()));

        int butchIndex = 0;
        for (ActorRef child : getContext().getChildren()) {
            child.tell(new WorkerActor.Task(taskBatch.id, butchIndex, taskBatch.sumTasks.get(butchIndex)), self());
            butchIndex++;
        }
    }

    public void onWorkerFinished(TaskProcessed result) {
        BatchState batchState = batchesInProcessing.get(result.butchId);
        if (batchState != null) {
            batchState.processedNumber++;
            batchState.sum += result.sum;

            if (batchState.task.sumTasks.size() <= batchState.processedNumber) {
                //batchState.sender.tell(, self());
                System.out.println("[QQQQQ] butch (" + batchState.task.id + ") sum: " + batchState.sum);
                batchesInProcessing.remove(result.butchId);
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
                context.actorOf(WorkerActor.props(), "worker-" + i);
            }
        }
    }

    public static Props props() {
        return Props.create(ParallelExecutingActor.class);
    }

    //////////////
    // Messages //
    //////////////

    static class TaskBatch {
        final Integer id;
        final List<SumTask> sumTasks;

        TaskBatch(Integer id, List<SumTask> sumTasks) {
            this.id = id;
            this.sumTasks = sumTasks;
        }

        static class SumTask {
            final List<Integer> numberList;

            public SumTask(List<Integer> numberList) {
                this.numberList = numberList;
            }
        }
    }

    static class TaskProcessed {
        final Integer butchId;
        final Integer indexInBatch;
        final Integer sum;

        TaskProcessed(Integer butchId, Integer indexInBatch, Integer sum) {
            this.butchId = butchId;
            this.indexInBatch = indexInBatch;
            this.sum = sum;
        }
    }

    //////////////////
    // Local Object //
    //////////////////

    static class BatchState {
        final TaskBatch task;
        final ActorRef sender;
        int processedNumber;
        int sum;

        BatchState(TaskBatch task, ActorRef sender) {
            this.task = task;
            this.sender = sender;
        }
    }
}
