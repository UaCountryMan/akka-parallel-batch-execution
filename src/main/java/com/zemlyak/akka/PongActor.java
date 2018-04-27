package com.zemlyak.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class PongActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(message -> getSender().tell("pong", self()))
                .build();
    }

    static Props props() {
        return Props.create(PongActor.class);
    }
}
