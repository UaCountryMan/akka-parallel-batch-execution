package com.zemlyak.akka;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FetchingInFewStageTest {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final AtomicInteger counter = new AtomicInteger(0);

    private static Single<String> createDelayedEmitterScheduler(String value, long emissionDelayMillis) {
        return Single
                .<String>create(emitter -> scheduler
                        .schedule(
                                () -> {System.out.println("[emission] " + value);emitter.onSuccess(value);},
                                emissionDelayMillis,
                                TimeUnit.MILLISECONDS
                        )
                )
                .subscribeOn(Schedulers.io());
    }

    private static Single<String> createDelayedEmitterScheduler2(String value, long emissionDelayMillis) {
        System.out.println("[creation] " + value);
        SingleSubject<String> subject = SingleSubject.create();

        createDelayedEmitterScheduler(value, emissionDelayMillis)
                .subscribe(subject);
        return subject
                .subscribeOn(Schedulers.io());
    }

    private static Single<List<String>> fetchInSteps(List<Single<String>> singleSourceList, long remainingTimeMillis, long stageStepMillis) {
        long emmitAfter = remainingTimeMillis > stageStepMillis ? stageStepMillis : remainingTimeMillis;
        Single<String> singleTimeout = createDelayedEmitterScheduler("timeout", emmitAfter);
        return Single
                .zip(singleSourceList
                                .stream()
                                .map(single -> Single
                                        .merge(single, singleTimeout)
                                        .firstOrError())
                                .collect(Collectors.toList()),
                        strings -> Arrays
                                .stream(strings)
                                .map(r -> (String) r)
                                .collect(Collectors.toList())
                )
                .doOnSuccess(strings -> System.out.println("stage: " + counter.incrementAndGet() + "; remainingTimeMillis: " + remainingTimeMillis))
                .flatMap(strings -> strings.stream().allMatch("timeout"::equals) && remainingTimeMillis - emmitAfter > 0
                        ? fetchInSteps(singleSourceList, remainingTimeMillis - emmitAfter, stageStepMillis) : Single.just(strings));
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Single<String> firstString = createDelayedEmitterScheduler2("some1", 1100);
        Single<String> secondString = createDelayedEmitterScheduler2("some2", 2200);

        List<Single<String>> singleSourceList = Arrays.asList(secondString, firstString);


        System.out.println("-------------------- Working ----------------------");
        fetchInSteps(singleSourceList, 2400, 500)
                .subscribe(
                        strings -> {
                            System.out.println("[success] " + strings);
                            latch.countDown();
                        },
                        err -> {
                            System.out.println("[error] " + err);
                            latch.countDown();
                        });

        latch.await();
        System.out.println("------------------ NOT Working ---------------------");
        counter.set(0);

        Single<String> thirdString = createDelayedEmitterScheduler("some3", 1100);
        Single<String> forthString = createDelayedEmitterScheduler("some4", 2200);

        List<Single<String>> singleSource2List = Arrays.asList(forthString, thirdString);

        fetchInSteps(singleSource2List, 2400, 500)
                .subscribe(
                        strings -> {
                            System.out.println("[success] " + strings);
                            latch.countDown();
                        },
                        err -> {
                            System.out.println("[error] " + err);
                            latch.countDown();
                        });


    }

}
