package application;

import config.Config;
import server.MultiThreadServer;

import java.io.IOException;

public class RentCompanyServerApp {

    public static void main(String[] args) throws IOException {
        System.out.println("Don't forget to launch the client side");
        MultiThreadServer multiThreadServer = new MultiThreadServer(Config.PORT, Config.ONE_THREAD_PROTOCOL);
        multiThreadServer.go();
    }
}
