package com.zemlyak.akka.adapter.rx;

import akka.actor.ActorRef;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import io.reactivex.Single;
import scala.concurrent.ExecutionContextExecutor;

public class ObservableUtil {

    public static <T> Single<T> fromActor(ActorRef actor, Object message, ExecutionContextExecutor executor){
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
                }, executor));
    }
}
