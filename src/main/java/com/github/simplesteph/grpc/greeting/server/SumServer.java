package com.github.simplesteph.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class SumServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8000).addService(new SumServiceImpl()).build();


        server.start();

        System.out.println("Server Started");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
        }));


        server.awaitTermination();
    }
}
