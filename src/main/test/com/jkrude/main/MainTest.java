package com.jkrude.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MainTest {

  private final static URL BACKUP = Main.class.getClassLoader().getResource("persBackup.json");
  private final static URL OVERWRITE = Main.class.getClassLoader().getResource("pers.json");

  public static void main(String[] args) throws IOException, URISyntaxException {
    Main.main(args);
    Files.copy(Path.of(BACKUP.toURI()), Path.of(OVERWRITE.toURI()),
        StandardCopyOption.REPLACE_EXISTING);
  }

}