package com.zemlyak.akka.adapter.rx;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import io.reactivex.Single;

import java.util.function.Consumer;

public class ObservableUtil {

    public static <T> Single<T> fromActor(ActorSystem system, ActorRef actor, Object message){
        return Single.create(singleEmitter -> system
                .actorOf(AdapterActor.props(actor, singleEmitter::onSuccess))
                .tell(message, ActorRef.noSender()));
    }

    private static class AdapterActor<T> extends AbstractActor {
        private final ActorRef actorRef;
        private final Consumer<T> onSuccessCallback;
        private final Receive finalState;

        AdapterActor(ActorRef actorRef, Consumer<T> onSuccessCallback) {
            this.actorRef = actorRef;
            this.onSuccessCallback = onSuccessCallback;
            this.finalState = receiveBuilder()
                    .matchAny(this::processResponse)
                    .build();
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchAny(this::sendMessage)
                    .build();
        }

        private void sendMessage(Object message) {
            actorRef.tell(message, self());
            getContext().become(finalState);
        }

        private void processResponse(Object message) {
            onSuccessCallback.accept((T)message);
            getContext().stop(self());
        }

        static <T> Props props(ActorRef actorRef, Consumer<T> onSuccessCallback) {
            return Props.create(AdapterActor.class, actorRef, onSuccessCallback);
        }
    }
}
