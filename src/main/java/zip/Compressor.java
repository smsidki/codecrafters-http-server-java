package zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public enum Compressor {

  GZIP {
    @Override
    public byte[] compress(String content) {
      try(
        var os = new ByteArrayOutputStream();
        var gzip = new GZIPOutputStream(os)
      ) {
        gzip.write(content.getBytes(StandardCharsets.UTF_8));
        gzip.finish();
        return os.toByteArray();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  },
  NONE {
    @Override
    public byte[] compress(String content) {
      return content.getBytes(StandardCharsets.UTF_8);
    }
  };

  private static final Compressor[] COMPRESSORS;

  static {
    COMPRESSORS = values();
  }

  public abstract byte[] compress(String content);

  public static Compressor of(String name) {
    for (Compressor compressor : COMPRESSORS) {
      if (compressor.name().equalsIgnoreCase(name)) {
        return compressor;
      }
    }
    return NONE;
  }

}
