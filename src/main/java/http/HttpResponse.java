package http;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import zip.Compressor;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class HttpResponse {

  private final String body;
  private final HttpStatus status;

  @Getter(AccessLevel.PRIVATE)
  private final Map<String, List<String>> headers;

  @Builder
  public HttpResponse(String body, HttpStatus status) {
    this.body = StringUtils.defaultIfBlank(body, "");
    this.status = status;
    this.headers = new HashMap<>();
  }

  public void write(Socket socket, HttpRequest request) {
    var compressor = Compressor.NONE;

    for (var encoding : request.getHeaders("Accept-Encoding")) {
      compressor = Compressor.of(encoding);
      if (!compressor.equals(Compressor.NONE)) {
        this.addHeader("Content-Encoding", encoding);
      }
    }

    try(var os = socket.getOutputStream()) {
      var responseBody = compressor.compress(this.body);
      this.addHeader("Content-Length", String.valueOf(responseBody.length));

      var responseHeader = "HTTP/1.1 %s\r\n%s\r\n"
        .formatted(this.status.value(), this.getHeadersAsString())
        .getBytes();

      os.write(responseHeader);
      os.flush();
      os.write(responseBody);
      os.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public HttpResponse addHeader(String key, String value) {
    this.headers.computeIfAbsent(key, exKey -> new ArrayList<>()).add(value);
    return this;
  }

  public String getHeadersAsString() {
    return this.headers.entrySet().stream()
      .map(entry -> "%s: %s\r\n".formatted(
        entry.getKey(), String.join(", ", entry.getValue())
      ))
      .collect(Collectors.joining());
  }

}
