package http;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class HttpResponse {

  private final String body;
  private final HttpStatus status;
  private final Map<String, String> headers;

  @Builder
  public HttpResponse(String body, HttpStatus status, Map<String, String> headers) {
    this.body = StringUtils.defaultIfBlank(body, "");
    this.status = status;
    this.headers = Objects.requireNonNullElseGet(headers, HashMap::new);
  }

  public void write(Socket socket) {
    try(var os = socket.getOutputStream()) {
      var headerStr = this.headers.entrySet().stream()
        .map(entry -> "%s: %s\r\n".formatted(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining());
      var responseStr = "HTTP/1.1 %s\r\n%s\r\n%s".formatted(
        this.status.value(), headerStr, this.body
      );
      System.out.println("Writing response: " + responseStr);
      os.write(responseStr.getBytes());
      os.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
