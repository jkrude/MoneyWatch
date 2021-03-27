package com.jkrude.material;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

import com.jkrude.category.CategoryNode;
import java.net.URL;
import org.junit.Test;

public class PersistenceManagerTest {


  @Test
  public void testStore() {
    final URL WRITE = PersistenceManagerTest.class.getResource("persTestWrite.json");
    Profile p = TestData.getProfile();
    PersistenceManager.save(p, WRITE);
  }

  @Test
  public void testLoad() {
    final URL READ = PersistenceManagerTest.class.getResource("persTestRead.json");
    Profile loadedProfile = new Profile();
    PersistenceManager.load(loadedProfile, READ);
    Profile correctProfile = TestData.getProfile();
    CategoryNode rootLoaded = loadedProfile.getRootCategory();
    CategoryNode rootCorrect = correctProfile.getRootCategory();

    assertEquals(rootLoaded.getName(), rootCorrect.getName());
    assertFalse(rootLoaded.hasParent());
    assertEquals(rootLoaded, rootCorrect);
  }
}