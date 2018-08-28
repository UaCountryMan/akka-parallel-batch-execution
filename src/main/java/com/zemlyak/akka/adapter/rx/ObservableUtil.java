package com.zemlyak.akka.adapter.rx;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import io.reactivex.Single;

public class ObservableUtil {

    public static <T> Single<T> fromActor(ActorSystem system, ActorRef actor, Object message){
        return Single.create(singleEmitter -> Patterns
                .ask(actor, message, 5000)
                .onComplete(new OnComplete<Object>() {
                    @Override
                    public void onComplete(Throwable failure, Object success) {
                        if (failure != null) {
                            singleEmitter.onError(failure);
                        } else {
                            singleEmitter.onSuccess((T)success);
                        }
                    }
                }, system.dispatcher()));
    }
}
