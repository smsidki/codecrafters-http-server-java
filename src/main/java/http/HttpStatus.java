package http;

public enum HttpStatus {

  OK(200, "OK"),
  CREATED(201, "Created"),
  NOT_FOUND(404, "Not Found");

  private final int code;
  private final String phrase;

  HttpStatus(int code, String phrase) {
    this.code = code;
    this.phrase = phrase;
  }

  public String value() {
    return "%d %s".formatted(code, phrase);
  }

}
