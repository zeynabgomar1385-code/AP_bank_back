package net;

import service.*;
import storage.JsonStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BankServer {
    private final int port;
    private final ServerSocket serverSocket;

    private final JsonStore store;
    private final AuthService authService;
    private final BankService bankService;
    private final GroupService groupService;
    private final ServicesService servicesService;

    public BankServer(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);

        this.store = new JsonStore("data");
        this.authService = new AuthService(store);
        this.bankService = new BankService(store);
        this.groupService = new GroupService(store, authService);

        this.servicesService = new ServicesService(store);
    }

    public void start() throws IOException {
        System.out.println("Starting bank server on port " + port);
        while (true) {
            Socket client = serverSocket.accept();
            Thread t = new Thread(new ClientHandler(
                client,
                authService,
                bankService,
                groupService,
                servicesService
            ));
            t.start();
        }
    }

    public static void main(String[] args) throws Exception {
        new BankServer(4040).start();
    }
}
