import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

public class Main {

  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

     // Uncomment this block to pass the first stage
     try(ServerSocket serverSocket = new ServerSocket(4221)) {

       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);

       var clientSocket = serverSocket.accept(); // Wait for connection from client.
       System.out.println("accepted new connection");

       var request = new HttpRequest(clientSocket.getInputStream());
       if (request.getPath().equals("/")) {
         HttpResponse.builder()
           .status(HttpStatus.OK)
           .build()
           .write(clientSocket);
       } else if (request.getPath().startsWith("/echo")) {
         var responseBody = StringUtils.substringAfter(request.getPath(), "/echo/");
         HttpResponse.builder()
           .body(responseBody)
           .status(HttpStatus.OK)
           .headers(Map.of(
             "Content-Type", "text/plain",
             "Content-Length", String.valueOf(responseBody.length())
           ))
           .build()
           .write(clientSocket);
       } else if (request.getPath().equals("/user-agent")) {
         var responseBody = request.getHeaders().get("User-Agent");
         HttpResponse.builder()
           .body(responseBody)
           .status(HttpStatus.OK)
           .headers(Map.of(
             "Content-Type", "text/plain",
             "Content-Length", String.valueOf(responseBody.length())
           ))
           .build()
           .write(clientSocket);
       } else {
         HttpResponse.builder()
           .status(HttpStatus.NOT_FOUND)
           .build()
           .write(clientSocket);
       }
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }

}
