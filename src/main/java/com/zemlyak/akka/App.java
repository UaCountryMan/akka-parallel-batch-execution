package com.zemlyak.akka;

import akka.actor.ActorSystem;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sample1");

        /*final ParallelExecutingWorkerFactory workerFactory = new BatchSummingWorkerFactory();
        final ActorRef parallelExecutingActor = system.actorOf(ParallelExecutingActor.props(workerFactory), "ParallelExecutingActor");
        final ActorRef batchSummingActor = system.actorOf(BatchSummingActor.props(parallelExecutingActor), "BatchSummingActor");

        for (int i = 0; i < 5; i++) {
            new Thread(() ->
            {
                for (int j = 0; j < 5; j++) {
                    batchSummingActor.tell(new BatchSummingActor.SumTask(Arrays.asList(1,2,3,4,5,6,7,8,9)), ActorRef.noSender());
                }
            })
            .start();
        }*/

        System.out.println("Start");
        Scanner in = new Scanner(System.in);
        in.nextLine();
        System.out.println("Finish");
        system.terminate();
    }
}
