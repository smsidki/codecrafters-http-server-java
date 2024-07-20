import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import util.FileUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;

public class Main {

  public static void main(String[] args) {
    System.out.println("Args: " + Arrays.toString(args));
    var arg = Arg.builder();
    if (args.length > 1) {
      if (args[0].equals("--directory")) {
        arg.directory(args[1]);
      }
    }

    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    try (
      var serverSocket = new ServerSocket(4221);
      var executor = Executors.newFixedThreadPool(5)
    ) {
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      while (true) {
        var clientSocket = serverSocket.accept(); // Wait for connection from client.
        System.out.println("accepted new connection");
        executor.submit(() -> handleRequest(clientSocket, arg.build()));
      }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }

  static void handleRequest(Socket socket, Arg arg) {
    try {
      var request = new HttpRequest(socket.getInputStream());
      if (request.getPath().equals("/")) {
        HttpResponse.builder()
          .status(HttpStatus.OK)
          .build()
          .write(socket);
      } else if (request.getPath().startsWith("/echo")) {
        handleEcho(socket, request);
      } else if (request.getPath().equals("/user-agent")) {
        handleUserAgent(socket, request);
      } else if (request.getPath().startsWith("/files")) {
        handleFile(socket, request, arg);
      } else {
        handleNotFound(socket);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static void handleEcho(Socket socket, HttpRequest request) {
    var responseBody = StringUtils.substringAfter(request.getPath(), "/echo/");
    HttpResponse.builder()
      .body(responseBody)
      .status(HttpStatus.OK)
      .headers(Map.of("Content-Type", "text/plain"))
      .build()
      .write(socket);
  }

  static void handleUserAgent(Socket socket, HttpRequest request) {
    var responseBody = request.getHeader("User-Agent");
    HttpResponse.builder()
      .body(responseBody)
      .status(HttpStatus.OK)
      .headers(Map.of("Content-Type", "text/plain"))
      .build()
      .write(socket);
  }

  static void handleFile(Socket socket, HttpRequest request, Arg arg) {
    var filePath = Path.of(arg.directory, StringUtils.substringAfter(request.getPath(), "/files"));

    var contentType = request.getHeader("Content-Type");
    if (contentType.equalsIgnoreCase("application/octet-stream") && request.hasBody()) {
      FileUtils.writeFile(filePath, request.getBody());
      HttpResponse.builder()
        .status(HttpStatus.CREATED)
        .build()
        .write(socket);
    } else {
      try(var is = new FileInputStream(filePath.toFile())) {
        var responseBody = FileUtils.readFile(is);
        HttpResponse.builder()
          .body(responseBody)
          .status(HttpStatus.OK)
          .headers(Map.of("Content-Type", "application/octet-stream"))
          .build()
          .write(socket);
      } catch (FileNotFoundException e) {
        System.err.println("File not found: " + e.getMessage());
        handleNotFound(socket);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  static void handleNotFound(Socket socket) {
    HttpResponse.builder()
      .status(HttpStatus.NOT_FOUND)
      .build()
      .write(socket);
  }

  @Builder
  record Arg(String directory) { }

}
