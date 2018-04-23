package com.zemlyak.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.zemlyak.akka.parallelization.ParallelExecutingActor.BatchRequest;
import com.zemlyak.akka.parallelization.ParallelExecutingActor.BatchResult;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class BatchSummingActor extends AbstractActor {
    private static final int BATCH_SIZE = 3;

    private final ActorRef parallelBatchExecutor;
    private Integer butchNumber = 0;

    public BatchSummingActor(ActorRef parallelBatchExecutor) {
        this.parallelBatchExecutor = parallelBatchExecutor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SumTask.class, this::onSumRequestCome)
                .match(BatchResult.class, this::onBatchResult)
                .build();
    }

    private void onSumRequestCome(SumTask sumTask) {
        parallelBatchExecutor.tell(new BatchRequest(this.butchNumber++, toBatchTasks(sumTask)), self());
    }

    private void onBatchResult(BatchResult batchResult) {
        System.out.println("[onBatchResult] Batch [" + batchResult.id + "] result: " + batchResult.responses.stream().mapToInt(i -> (Integer) i).sum());
    }

    private List<Object> toBatchTasks(SumTask incomingTask) {
        List<Object> sumTasks;
        if (incomingTask.numberList.size() <= BATCH_SIZE) {
            sumTasks = singletonList(incomingTask);
        } else {
            int butchNumber = bucketsNumber(incomingTask.numberList.size());
            sumTasks = new ArrayList<>(butchNumber);
            for (int i = 0; i < butchNumber; i++) {
                int lowerBound = i * BATCH_SIZE;
                int upperBound = (i + 1) * BATCH_SIZE < incomingTask.numberList.size() ? (i + 1) * BATCH_SIZE : incomingTask.numberList.size();
                sumTasks.add(new SumTask(incomingTask.numberList.subList(lowerBound, upperBound)));
            }
        }
        return sumTasks;
    }

    private int bucketsNumber(int numberListSize) {
        return numberListSize % BATCH_SIZE > 0 ? (numberListSize / BATCH_SIZE) + 1 : numberListSize / BATCH_SIZE;
    }

    public static Props props(ActorRef parallelBatchExecutor) {
        return Props.create(BatchSummingActor.class, parallelBatchExecutor);
    }


    //////////////
    // Messages //
    //////////////

    static class SumTask {
        final List<Integer> numberList;

        public SumTask(List<Integer> numberList) {
            this.numberList = numberList;
        }
    }
}
