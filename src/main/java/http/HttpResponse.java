package http;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

}
