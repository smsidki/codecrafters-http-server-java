package util;

import java.io.*;
import java.nio.file.Path;

public final class FileUtils {

  private FileUtils() {}

  public static String readFile(FileInputStream is) {
    var sb = new StringBuilder();
    try (var reader = new BufferedReader(new InputStreamReader(is))) {
      var line = reader.readLine();
      while (line != null) {
        sb.append(line);
        line = reader.readLine();
      }
      return sb.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeFile(Path path, String content) {
    try(var writer = new BufferedWriter(new FileWriter(path.toFile()))) {
      writer.write(content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
