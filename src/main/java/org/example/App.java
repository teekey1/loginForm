package org.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App
{
    public static void main( String[] args ) throws IOException {
        Database database = new Database();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/login", new Login(database));
        server.setExecutor(null);
        server.start();
    }
}

