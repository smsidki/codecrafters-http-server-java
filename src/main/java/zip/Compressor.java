package zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public enum Compressor {

  GZIP {
    @Override
    public byte[] compress(String content) {
      try(
        var os = new ByteArrayOutputStream();
        var gzip = new GZIPOutputStream(os)
      ) {
        gzip.write(content.getBytes());
        return os.toByteArray();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  },
  NONE {
    @Override
    public byte[] compress(String content) {
      return content.getBytes();
    }
  };

  public abstract byte[] compress(String content);

}
