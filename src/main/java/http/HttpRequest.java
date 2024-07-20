package http;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Getter
public class HttpRequest {

  private final String body;
  private final String path;
  private final String method;
  private final Map<String, String> headers;

  public HttpRequest(InputStream is) throws IOException {
    var reader = new BufferedReader(new InputStreamReader(is));
    var requestLine = reader.readLine().split(" ");
    this.method = requestLine[0];
    this.path = requestLine[1];

    this.headers = new HashMap<>();
    var rawHeaders = reader.readLine();
    while (StringUtils.isNotBlank(rawHeaders)) {
      var kv = rawHeaders.split(": ");
      this.headers.put(kv[0], kv[1]);
      rawHeaders = reader.readLine();
    }

    var contentLength = Integer.parseInt(this.headers.getOrDefault("Content-Length", "0"));
    if (contentLength == 0) {
      this.body = null;
    } else {
      var body = new StringBuilder();
      while (reader.ready()) {
        body.append((char) reader.read());
      }
      this.body = body.toString();
    }
  }

  public boolean hasBody() {
    return body != null;
  }

  public String getHeader(String name) {
    return headers.getOrDefault(name, StringUtils.EMPTY);
  }

}
