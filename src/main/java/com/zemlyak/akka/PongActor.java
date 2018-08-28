package com.zemlyak.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.Status;

public class PongActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("ping", message -> getSender().tell("pong", self()))
                .match(String.class, message -> getSender().tell(new Status.Failure(new RuntimeException("Unsupported command: " + message)), self()))
                .build();
    }

    static Props props() {
        return Props.create(PongActor.class);
    }
}
