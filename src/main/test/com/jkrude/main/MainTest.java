package com.jkrude.main;

import com.jkrude.material.Model;
import com.jkrude.material.TestData;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MainTest {

  private final static URL PERSISTENCE = Main.class.getClassLoader().getResource("pers.json");

  public static void main(String[] args) throws IOException, URISyntaxException {
    assert PERSISTENCE != null;
    Path backupFile = Files.createTempFile(null, ".json");
    System.out.println(backupFile.toAbsolutePath());
    backupFile.toFile().deleteOnExit();
    Files.copy(Path.of(PERSISTENCE.toURI()), backupFile, StandardCopyOption.REPLACE_EXISTING);
    var testData = TestData.getNewCamtWithTestData();
    testData.addExtendedTransaction(TestData.getExtraTransaction());
    Model.getInstance().getTransactionContainerList().add(testData);
    Model.getInstance().setActiveData(testData);
    Main.main(args);
    Files.copy(backupFile, Path.of(PERSISTENCE.toURI()), StandardCopyOption.REPLACE_EXISTING);
  }

}