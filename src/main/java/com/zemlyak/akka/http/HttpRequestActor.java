package com.zemlyak.akka.http;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.headers.RawHeader;
import akka.pattern.PatternsCS;
import scala.concurrent.ExecutionContextExecutor;

public class HttpRequestActor extends AbstractActor {
    private final Http http;
    private final ExecutionContextExecutor dispatcher;

    public HttpRequestActor() {
        http = Http.get(context().system());
        dispatcher = context().dispatcher();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(HttpRequest.class, this::handleRequest)
                .build();
    }

    private void handleRequest(HttpRequest request) {
        akka.http.javadsl.model.HttpRequest httpRequest = akka.http.javadsl.model.HttpRequest
                .POST("http://somertb.com/")
                //TODO [WARN] Explicitly set HTTP header 'Content-Type: application/json' is ignored, illegal RawHeader
                //.addHeader(RawHeader.create("Content-Type", "application/json"))
                .addHeader(RawHeader.create("x-openrtb-version", "2.5"))
                .addHeader(RawHeader.create("Accept", "*/*"))
                .withEntity("{\n" +
                        "  \"ext\": {\n" +
                        "    \"udi\": {\n" +
                        "      \"gaid\": \"2530aa38-5d90-40f9-91b8-09798ab1bc15\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"id\": \"e6f556a22977c2eb922f1cc38251a694\",\n" +
                        "  \"imp\": [\n" +
                        "    {\n" +
                        "      \"ext\": {\n" +
                        "        \"brandsafe\": 0\n" +
                        "      },\n" +
                        "      \"id\": \"1\",\n" +
                        "      \"banner\": {\n" +
                        "        \"w\": 320,\n" +
                        "        \"h\": 480,\n" +
                        "        \"api\": [\n" +
                        "          3,\n" +
                        "          5\n" +
                        "        ]\n" +
                        "      },\n" +
                        "      \"displaymanager\": \"third_party_sdk\",\n" +
                        "      \"displaymanagerver\": \"3.0\",\n" +
                        "      \"bidfloor\": 0.001,\n" +
                        "      \"bidfloorcur\": \"USD\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"app\": {\n" +
                        "    \"id\": \"69513\",\n" +
                        "    \"name\": \"Test MobfoxRTBImage Maxim\",\n" +
                        "    \"domain\": \"com.loopme.test\",\n" +
                        "    \"cat\": [\n" +
                        "      \"IAB9\"\n" +
                        "    ],\n" +
                        "    \"bundle\": \"com.loopme.test\",\n" +
                        "    \"publisher\": {\n" +
                        "      \"id\": \"41313\"\n" +
                        "    },\n" +
                        "    \"storeurl\": \"https://play.google.com/store/apps/details?id=com.loopme\"\n" +
                        "  },\n" +
                        "  \"device\": {\n" +
                        "    \"dnt\": 0,\n" +
                        "    \"ua\": \"Mozilla/5.0 (Linux; Android 4.4.4; XT1063 Build/KXB21.85-23) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36\",\n" +
                        "    \"ip\": \"80.193.23.30\",\n" +
                        "    \"geo\": {\n" +
                        "      \"lat\": 0,\n" +
                        "      \"lon\": 0,\n" +
                        "      \"country\": \"USA\",\n" +
                        "      \"type\": 1\n" +
                        "    },\n" +
                        "    \"dpidsha1\": \"94f8e4365007b25bb42b90d21cf17bfce2963f0c\",\n" +
                        "    \"dpidmd5\": \"daa9dc601498acca092815bd34695ba9\",\n" +
                        "    \"make\": \"Motorola\",\n" +
                        "    \"model\": \"XT1063\",\n" +
                        "    \"os\": \"Android\",\n" +
                        "    \"osv\": \"5.1\",\n" +
                        "    \"connectiontype\": 2,\n" +
                        "    \"devicetype\": 1\n" +
                        "  },\n" +
                        "  \"user\": {\n" +
                        "    \"id\": \"ed05740e8e3ecc3bb191c1e92297fedb\"\n" +
                        "  },\n" +
                        "  \"at\": 2,\n" +
                        "  \"tmax\": 300,\n" +
                        "  \"allimps\": 0,\n" +
                        "  \"cur\": [\n" +
                        "    \"USD\"\n" +
                        "  ],\n" +
                        "  \"bcat\": [\n" +
                        "    \"IAB26\",\n" +
                        "    \"IAB25\",\n" +
                        "    \"IAB24\"\n" +
                        "  ]\n" +
                        "}");

        PatternsCS
                .pipe(http.singleRequest(httpRequest), dispatcher)
                .to(sender());
    }

    static Props props() {
        return Props.create(HttpRequestActor.class);
    }

    public static class HttpRequest {

    }
}
