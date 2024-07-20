import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.stream.Collectors;

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
       if (request.getPath().startsWith("/echo")) {
         var responseBody = StringUtils.substringAfter(request.getPath(), "/echo/");
         var response = HttpResponse.builder()
           .body(responseBody)
           .status(HttpStatus.OK)
           .headers(Map.of(
             "Content-Type", "text/plain",
             "Content-Length", String.valueOf(responseBody.length())
           ))
           .build();
         writeResponse(clientSocket, response);
       } else {
         writeResponse(clientSocket, HttpResponse.builder()
           .status(HttpStatus.NOT_FOUND)
           .build()
         );
       }
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }

  static void writeResponse(Socket clientSocket, HttpResponse response) {
    try(var os = clientSocket.getOutputStream()) {
      var headerStr = response.getHeaders().entrySet().stream()
        .map(entry -> "%s: %s\r\n".formatted(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining());
      var responseStr = "HTTP/1.1 %s\r\n%s\r\n%s".formatted(
        response.getStatus().value(), headerStr, response.getBody()
      );
      System.out.println("Writing response: " + responseStr);
      os.write(responseStr.getBytes());
      os.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
