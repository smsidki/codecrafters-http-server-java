import http.HttpRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
         writeOK(clientSocket, "", "");
       } else {
         writeNotFound(clientSocket, "", "");
       }
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }

  static void writeOK(Socket socket, String header, String body) {
    writeResponse(socket, "200 OK", header, body);
  }

  static void writeNotFound(Socket socket, String header, String body) {
    writeResponse(socket, "404 Not Found", header, body);
  }

  static void writeResponse(Socket clientSocket, String status, String header, String body) {
    try(var os = clientSocket.getOutputStream()) {
      os.write("HTTP/1.1 %s\r\n%s\r\n%s".formatted(status, header, body).getBytes());
      os.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
