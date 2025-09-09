package org.example;

import java.io.*;
import java.net.*;

public class FacadeServer {
    private static final String BACKEND_URL = "http://localhost:35000";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(36000);
        System.out.println("Fachada lista en puerto 36000...");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            String[] parts = requestLine.split(" ");
            String path = parts[1];

            String backendResponse = forwardToBackend(path);

            out.println("HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json;"
                    + "charset=utf-8\r\n\r\n"
                    + backendResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String forwardToBackend(String path) throws IOException {
        URL url = new URL(BACKEND_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) response.append(inputLine);
        in.close();

        return response.toString();
    }
}