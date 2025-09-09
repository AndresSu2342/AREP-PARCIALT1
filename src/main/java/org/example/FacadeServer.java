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
            if (path.equals("/") || path.startsWith("/client")) {
                out.println("HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html; charset=utf-8\r\n\r\n"
                        + "<!DOCTYPE html>\n"
                        + "<html>\n"
                        + "<head>\n"
                        + "<meta charset=\"UTF-8\">\n"
                        + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                        + "<title>Key-Value Store</title>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "    <h1>Form SET</h1>\n"
                        + "    <form>\n"
                        + "        <label for=\"key\">Key:</label><br>\n"
                        + "        <input type=\"text\" id=\"key\" name=\"key\" value=\"Number\"><br><br>\n"
                        + "        <label for=\"value\">Value:</label><br>\n"
                        + "        <input type=\"text\" id=\"value\" name=\"value\" value=\"1234\"><br><br>\n"
                        + "        <input type=\"button\" value=\"Create\" onclick=\"postGetMsg()\">\n"
                        + "    </form>\n"
                        + "    <div id=\"postrespmsg\"></div>\n"
                        + "    <script>\n"
                        + "        function postGetMsg() {\n"
                        + "            let keyVar = document.getElementById(\"key\").value;\n"
                        + "            let valueVar = document.getElementById(\"value\").value;\n"
                        + "            const xhttp = new XMLHttpRequest();\n"
                        + "            xhttp.onload = function () {\n"
                        + "                document.getElementById(\"postrespmsg\").innerHTML = this.responseText;\n"
                        + "            }\n"
                        + "            xhttp.open(\"GET\", \"/setkv?key=\" + keyVar + \"&value=\" + valueVar);\n"
                        + "            xhttp.send();\n"
                        + "        }\n"
                        + "    </script>\n"
                        + "\n"
                        + "    <h1>Form GET</h1>\n"
                        + "    <form>\n"
                        + "        <label for=\"keyquery\">Key:</label><br>\n"
                        + "        <input type=\"text\" id=\"keyquery\" name=\"keyquery\" value=\"Number\"><br><br>\n"
                        + "        <input type=\"button\" value=\"Consultar\" onclick=\"loadGetMsg()\">\n"
                        + "    </form>\n"
                        + "    <div id=\"getrespmsg\"></div>\n"
                        + "    <script>\n"
                        + "        function loadGetMsg() {\n"
                        + "            let keyVar = document.getElementById(\"keyquery\").value;\n"
                        + "            const xhttp = new XMLHttpRequest();\n"
                        + "            xhttp.onload = function () {\n"
                        + "                document.getElementById(\"getrespmsg\").innerHTML = this.responseText;\n"
                        + "            }\n"
                        + "            xhttp.open(\"GET\", \"/getkv?key=\" + keyVar);\n"
                        + "            xhttp.send();\n"
                        + "        }\n"
                        + "    </script>\n"
                        + "</body>\n"
                        + "</html>\n");

            }
            else if (path.startsWith("/setkv") || path.startsWith("/getkv")) {
                out.println("HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/json;"
                        + "charset=utf-8\r\n\r\n"
                        + backendResponse);
            }
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