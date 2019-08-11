package com.github.simplesteph.grpc.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class GreetingClient {

    ManagedChannel channel;

    public static void main(String[] args) {
        System.out.println("Hello I am a gRPC client");

        GreetingClient main = new GreetingClient();
        main.run();


    }

    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                                       .usePlaintext()
                                       .build();

        //doUnaryCall(channel);
        //doServerStreaming(channel);

        //  doClientStreamingCall(channel);

        // doBiDiStreamingCall(channel);

        doUnaryCallWithDeadLine(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }


    private void doUnaryCall(Channel channel) {

        // Created a greet service client (blocking - synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // created a protocol buffer message
        Greeting greeting = Greeting.newBuilder()
                                    .setFirstName("Stephane")
                                    .setLastName("Maarek")
                                    .build();

        // created a protocol buffer message for GreetRequest
        GreetRequest request = GreetRequest.newBuilder()
                                           .setGreeting(greeting)
                                           .build();

        // call the rpc and get back a GreetResponse
        GreetResponse response = greetClient.greet(request);

        System.out.println(response.getResult());
    }

    private void doServerStreaming(Channel channel) {

        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                                                                           .setGreeting(Greeting.newBuilder()
                                                                                                .setFirstName("Aravind"))
                                                                           .build();
        greetClient.greetManyTimes(greetManyTimesRequest)
                   .forEachRemaining((greetManyTimesResponse -> {
                       System.out.println(greetManyTimesResponse.getResult());
                   }));
        System.out.println("Shutting down channel");
    }

    private void doClientStreamingCall(Channel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // we get a response from the server

                System.out.println("Received a response from server");

                System.out.println(value.getResult());

                // since it is client streaming onNext will be called only once
            }

            @Override
            public void onError(Throwable t) {
                // we get an error from the server
            }

            @Override
            public void onCompleted() {

                //the server is done sending us data

                System.out.println("Server says I am done");
                // onCompleted will be called right after onNext()
                latch.countDown();
            }
        });

        // streaming message #1
        requestObserver.onNext(LongGreetRequest
                .newBuilder()
                .setGreeting(Greeting
                        .newBuilder()
                        .setFirstName("Tyson")
                        .build())
                .build());

        // streaming message #2
        requestObserver.onNext(LongGreetRequest
                .newBuilder()
                .setGreeting(Greeting
                        .newBuilder()
                        .setFirstName("Aravind")
                        .build())
                .build());

        // streaming message #3
        requestObserver.onNext(LongGreetRequest
                .newBuilder()
                .setGreeting(Greeting
                        .newBuilder()
                        .setFirstName("Goku")
                        .build())
                .build());

        requestObserver.onCompleted(); // we tell the server that the client is done sending the data
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void doBiDiStreamingCall(Channel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response From Server : " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();

            }

            @Override
            public void onCompleted() {
                System.out.println("Server says I am done");
                latch.countDown();
            }
        });

        Arrays.asList("Tyson", "Goku", "Aravind")
              .forEach(name -> {
                  System.out.println(name);
                  requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                                                             .setGreeting(Greeting.newBuilder()
                                                                                  .setFirstName(name)
                                                                                  .build())
                                                             .build());

                  try {
                      Thread.sleep(100); // To check the async behaviour of sending messages and receiving it
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              });

        requestObserver.onCompleted();
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doUnaryCallWithDeadLine(Channel channel) {
        //First call
        try {
            System.out.println("Sending a request with deadline of 3000ms");
            GreetServiceGrpc.GreetServiceBlockingStub client = GreetServiceGrpc.newBlockingStub(channel);
            GreetWithDeadLineResponse response = client.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
                                                       .greetWithDeadLine(GreetWithDeadlineRequest.newBuilder()
                                                                                                  .setGreeting(Greeting.newBuilder()
                                                                                                                       .setFirstName("Aravind")
                                                                                                                       .build())
                                                                                                  .build());
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Dead line exceeded");
            } else {
                e.printStackTrace();
            }
        }

        //First call
        try {
            System.out.println("Sending a request with deadline of 100ms");
            GreetServiceGrpc.GreetServiceBlockingStub client = GreetServiceGrpc.newBlockingStub(channel);
            client.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                  .greetWithDeadLine(GreetWithDeadlineRequest.newBuilder()
                                                             .setGreeting(Greeting.newBuilder()
                                                                                  .setFirstName("Aravind")
                                                                                  .build())
                                                             .build());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Dead line exceeded");
            } else {
                e.printStackTrace();
            }
        }
    }
}





