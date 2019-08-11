package com.github.simplesteph.grpc.greeting.server;

import com.proto.greet.*;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        // extract the fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();
        //String lastName = greeting.getLastName();


        // create the response
        String result = "Hello " + firstName;

        GreetResponse response = GreetResponse.newBuilder()
                                              .setResult(result)
                                              .build();

        // send response
        responseObserver.onNext(response);

        // complete rpc call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        //super.greetManyTimes(request, responseObserver);

        String firstName = request.getGreeting()
                                  .getFirstName();
        try {
            for (int i = 0; i < 10; i++) {
                String result = "Hello " + firstName + ", resume number: " + i;
                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                                                                        .setResult(result)
                                                                        .build();
                responseObserver.onNext(response);
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }

    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {

        StreamObserver<LongGreetRequest> streamObserverOfRequest = new StreamObserver<LongGreetRequest>() {

            StringBuilder builder = new StringBuilder();

            @Override
            public void onNext(LongGreetRequest value) {
                // client sends a message
                builder.append("Hello " + value.getGreeting()
                                               .getFirstName() + "! ");
            }

            @Override
            public void onError(Throwable t) {
                // client sends an error

            }

            @Override
            public void onCompleted() {
                // client is done
                responseObserver.onNext(
                        LongGreetResponse.newBuilder()
                                         .setResult(builder.toString())
                                         .build()
                );

                responseObserver.onCompleted();

                // this is when we want to return a response (responseObserver)
            }
        };

        return streamObserverOfRequest;
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        StreamObserver<GreetEveryoneRequest> requestStreamObserver = new StreamObserver<GreetEveryoneRequest>() {
            @Override
            public void onNext(GreetEveryoneRequest value) {
                String response = "Hello " + value.getGreeting()
                                                  .getFirstName();
                GreetEveryoneResponse greetEveryoneResponse = GreetEveryoneResponse.newBuilder()
                                                                                   .setResult(response)
                                                                                   .build();
                responseObserver.onNext(greetEveryoneResponse);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

        return requestStreamObserver;
    }

    @Override
    public void greetWithDeadLine(GreetWithDeadlineRequest request, StreamObserver<GreetWithDeadLineResponse> responseObserver) {
        Context current = Context.current();
        for (int i = 0; i < 3; i++) {
            try {
                if (!current.isCancelled()) {
                    System.out.println("sleep for 100ms");
                    Thread.sleep(100);
                } else {
                    return;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("send response");
            responseObserver.onNext(GreetWithDeadLineResponse.newBuilder()
                                                             .setResult("Hello " + request.getGreeting()
                                                                                          .getFirstName())
                                                             .build());
            responseObserver.onCompleted();

        }
    }
}
