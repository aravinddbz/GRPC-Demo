package com.github.simplesteph.grpc.greeting.server;

import com.sum.service.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class SumServiceImpl extends SumServiceGrpc.SumServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        int firstNumber = request.getFirstNumber();
        int secondNumber = request.getSecondNumber();
        int result = firstNumber + secondNumber;

        responseObserver.onNext(SumResponse.newBuilder()
                                           .setResult(result)
                                           .build());

        responseObserver.onCompleted();
    }

    @Override
    public void primeNumber(PrimeNumberRequest request, StreamObserver<PrimeNumberResponse> responseObserver) {
        int number = request.getNumber();
        int k = 2;
        while (number > 1) {
            if (number % k == 0) {
                responseObserver.onNext(PrimeNumberResponse.newBuilder()
                                                           .setPrimeNumber(k)
                                                           .build());
                number = number / k;
            } else {
                k = k + 1;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<AverageRequest> computeAverage(StreamObserver<AverageResponse> responseObserver) {
        StreamObserver<AverageRequest> streamObserver = new StreamObserver<AverageRequest>() {
            Double sum = 0d;
            Integer numOfRequests = 0;

            @Override
            public void onNext(AverageRequest value) {
                sum = sum + value.getNumber();
                numOfRequests++;
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(AverageResponse.newBuilder()
                                                       .setAverage(sum / numOfRequests)
                                                       .build());
                responseObserver.onCompleted();
            }
        };

        return streamObserver;
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        Integer number = request.getNumber();

        if (number >= 0) {
            double numberRoot = Math.sqrt(number);
            responseObserver.onNext(SquareRootResponse.newBuilder()
                                                      .setNumberRoot(numberRoot)
                                                      .build());
            responseObserver.onCompleted();
        } else {
            // we construct the exception and client will be able to see the description
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("The number being sent is not positive")
                                           .augmentDescription("Number sent " + number)
                                           .asRuntimeException()
            );
        }
    }
}
