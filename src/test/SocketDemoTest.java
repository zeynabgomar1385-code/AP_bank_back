package test;

import java.io.*;
import java.net.Socket;

public class SocketDemoTest {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 4040);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
        System.out.println(in.readLine());
        out.println("{\"action\":\"PING\"}");
        System.out.println(in.readLine());
        out.println("{\"action\":\"LOGIN\",\"username\":\"zey\",\"password\":\"1234\"}");
        System.out.println(in.readLine());
        out.println("{\"action\":\"GET_ACCOUNTS\"}");
        System.out.println(in.readLine());
        s.close();
    }
}
