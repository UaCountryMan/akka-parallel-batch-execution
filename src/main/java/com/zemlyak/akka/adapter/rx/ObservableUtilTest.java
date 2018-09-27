package com.zemlyak.akka.adapter.rx;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import io.reactivex.schedulers.Schedulers;
import java.util.Scanner;

public class ObservableUtilTest {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sample-observable");
        System.out.println("Start");

        final ActorRef pongActor = system.actorOf(PongActor.props(), "pong");

        ObservableUtil
                .fromActor(pongActor, "ping", system.dispatcher())
                .observeOn(Schedulers.computation())
                .subscribe(s -> System.out.println("[" + Thread.currentThread().getName() + "] " + s), t -> System.out.println("[error] " + t));

        ObservableUtil
                .fromActor(pongActor, "some", system.dispatcher())
                .observeOn(Schedulers.computation())
                .subscribe(System.out::println, t -> System.out.println("[error] " + t));

        System.out.println("Press Enter to finish.");
        Scanner in = new Scanner(System.in);
        in.nextLine();
        system.terminate();
    }
}
