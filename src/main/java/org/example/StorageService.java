package org.example;

import java.io.*;
import java.net.*;
import java.util.*;

public class StorageService {
    private static Map storage = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(35000);
        System.out.println("Backend listo en puerto 35000...");
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

            String response = processRequest(path);
            out.println("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json; charset=utf-8\r\n\r\n" + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String processRequest(String path) {
        if (path.startsWith("/setkv")) {
            Map params = queryParams(path);
            String key = (String) params.get("key");
            String value = (String) params.get("value");
            if (key == null || value == null) {
                return "{ \"error\": \"bad_request\", \"message\": \"missing key or value\" }";
            }
            storage.put(key, value);
            return "{ \"key\": \""+key+"\", \"value\": \""+value+"\", \"status\": \"created\" }";
        } else if (path.startsWith("/getkv")) {
            Map params = queryParams(path);
            String key = (String) params.get("key");
            if (storage.containsKey(key)) {
                return "{ \"key\": \""+key+"\", \"value\": \""+storage.get(key)+"\" }";
            }
            return "{ \"error\": \"key_not_found\", \"key\": \""+key+"\" }";
        }
        return "{ \"error\": \"unknown_endpoint\" }";
    }

    private static Map queryParams(String path) {
        Map map = new HashMap<>();
        if (!path.contains("?")) return map;
        String[] q = path.split("\\?")[1].split("&");
        for (String p : q) {
            String[] kv = p.split("=");
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }
}