package com.github.simplesteph.grpc.greeting.client;

import com.sum.service.*;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SumClient {

    ManagedChannel channel = null;

    public static void main(String[] args) {

        System.out.println("Welcome to Sum client");
        SumClient client = new SumClient();
        client.run();
    }

    private void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 8000)
                                       .usePlaintext()
                                       .build();
        // doSum(channel);
        // doPrimeNumberDecompose(channel);
        //dpStreamingAverageCompute(channel);
        doErrorCall(channel);
        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doSum(Channel channel) {
        System.out.println("Enter the first Number : ");
        Scanner in = new Scanner(System.in);
        int firstNumber = in.nextInt();

        System.out.println("Enter the second Number : ");
        int secondNumber = in.nextInt();

        SumServiceGrpc.SumServiceBlockingStub sumClient = SumServiceGrpc.newBlockingStub(channel);

        SumRequest sumRequest = SumRequest.newBuilder()
                                          .setFirstNumber(firstNumber)
                                          .setSecondNumber(secondNumber)
                                          .build();
        SumResponse response = sumClient.sum(sumRequest);
        System.out.println("The sum is " + response.getResult());
    }

    private void doPrimeNumberDecompose(Channel channel) {
        System.out.println("Enter the  Number : ");
        Scanner in = new Scanner(System.in);
        int number = in.nextInt();

        SumServiceGrpc.SumServiceBlockingStub sumClient = SumServiceGrpc.newBlockingStub(channel);

        sumClient.primeNumber(PrimeNumberRequest.newBuilder()
                                                .setNumber(number)
                                                .build())
                 .forEachRemaining(primeNumberResponse -> {
                     System.out.println(primeNumberResponse.getPrimeNumber());
                 });
    }

    private void dpStreamingAverageCompute(Channel channel) {
        System.out.println("Enter the number of digits : ");
        Scanner in = new Scanner(System.in);
        SumServiceGrpc.SumServiceStub asyncSumClient = SumServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<AverageRequest> requestStreamObserver = asyncSumClient.computeAverage(new StreamObserver<AverageResponse>() {
            @Override
            public void onNext(AverageResponse value) {
                System.out.println("The computed average by grpc server is " + value);

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("The server says I am done");
                latch.countDown();
            }
        });
        Integer numberOfDigits = in.nextInt();
        for (int i = 0; i < numberOfDigits; i++) {
            System.out.println("Enter digit " + (i + 1) + ": ");
            Integer digit = in.nextInt();
            requestStreamObserver.onNext(AverageRequest.newBuilder()
                                                       .setNumber(digit)
                                                       .build());
        }
        requestStreamObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doErrorCall(Channel channel) {
        SumServiceGrpc.SumServiceBlockingStub client = SumServiceGrpc.newBlockingStub(channel);
        try {
            SquareRootResponse resp = client.squareRoot(SquareRootRequest.newBuilder()
                                                                         .setNumber(-1)
                                                                         .build());
            System.out.println("Square Root " + resp.getNumberRoot());
        } catch (StatusRuntimeException e) {
            System.out.println("Got Error");
            e.printStackTrace();
        }
    }


}
