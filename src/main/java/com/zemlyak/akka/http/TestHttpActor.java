package com.zemlyak.akka.http;

import akka.actor.*;
import akka.http.javadsl.model.HttpResponse;

import java.util.Scanner;

public class TestHttpActor  extends AbstractActor {
    private final ActorRef httpActorRef;

    public TestHttpActor(ActorRef httpActorRef) {
        this.httpActorRef = httpActorRef;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("send", this::sendRequest)
                .match(HttpResponse.class, this::handleResponse)
                .match(Status.Failure.class, this::handleFailure)
                .build();
    }

    private void handleFailure(Status.Failure failure) {
        System.out.println("[handleFailure] " + failure);
    }

    private void sendRequest(String any) {
        httpActorRef.tell(new HttpRequestActor.HttpRequest(), self());
    }

    private void handleResponse(HttpResponse response) {
        System.out.println("[handleResponse] " + response);
    }

    static Props props(ActorRef httpActorRef) {
        return Props.create(TestHttpActor.class, httpActorRef);
    }


    //-------- MAIN ----------
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sample-http");

        final ActorRef httpActor = system.actorOf(HttpRequestActor.props(), "http");
        final ActorRef httpTestActor = system.actorOf(TestHttpActor.props(httpActor), "http-test");

        httpTestActor.tell("send", ActorRef.noSender());

        System.out.println("Press Enter to finish.");
        Scanner in = new Scanner(System.in);
        in.nextLine();
        system.terminate();
    }
}
