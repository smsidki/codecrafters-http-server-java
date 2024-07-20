package http;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Getter
public class HttpRequest {

  private final String path;
  private final String method;

  public HttpRequest(InputStream is) throws IOException {
    var reader = new BufferedReader(new InputStreamReader(is));
    var requestLine = reader.readLine().split(" ");
    this.method = requestLine[0];
    this.path = requestLine[1];
  }

}
