package com.zemlyak.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.util.Arrays;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sample1");

        final ActorRef parallelExecutingActor = system.actorOf(ParallelExecutingActor.props(), "ParallelExecutingActor");

        for (int i = 0; i < 5; i++) {
            int some = i;
            new Thread(() ->
            {
                for (int j = 0; j < 5; j++) {
                    parallelExecutingActor.tell(new ParallelExecutingActor.TaskBatch(some * 5 + j, Arrays.asList(new ParallelExecutingActor.TaskBatch.SumTask(Arrays.asList(1,2,3)), new ParallelExecutingActor.TaskBatch.SumTask(Arrays.asList(4,5,6)), new ParallelExecutingActor.TaskBatch.SumTask(Arrays.asList(7,8,9)))), ActorRef.noSender());
                }
            })
            .start();
        }

        System.out.println("Start");
        Scanner in = new Scanner(System.in);
        in.nextLine();
        System.out.println("Finish");
        System.out.println(parallelExecutingActor.toString());
        system.terminate();
    }
}
