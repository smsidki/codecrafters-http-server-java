package http;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import zip.Compressor;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class HttpResponse {

  private final String body;
  private final HttpStatus status;

  @Getter(AccessLevel.PRIVATE)
  private final Map<String, String> headers;

  @Builder
  public HttpResponse(String body, HttpStatus status) {
    this.body = StringUtils.defaultIfBlank(body, "");
    this.status = status;
    this.headers = new HashMap<>();
  }

  public void write(Socket socket, HttpRequest request) {
    var compressor = Compressor.NONE;

    var encoding = request.getHeader("Accept-Encoding");
    if (StringUtils.isNotBlank(encoding)) {
      compressor = Compressor.of(encoding);
      if (!compressor.equals(Compressor.NONE)) {
        this.headers.put("Content-Encoding", encoding);
      }
    }

    try(var os = socket.getOutputStream()) {
      var headerStr = this.headers.entrySet().stream()
        .map(entry -> "%s: %s\r\n".formatted(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining());
      var responseHeader = "HTTP/1.1 %s\r\n%s\r\n".formatted(this.status.value(), headerStr).getBytes();

      var responseBody = compressor.compress(this.body);
      this.headers.put("Content-Length", String.valueOf(responseBody.length));

      os.write(responseHeader);
      os.flush();
      os.write(responseBody);
      os.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public HttpResponse addHeader(String key, String value) {
    this.headers.put(key, value);
    return this;
  }

}
