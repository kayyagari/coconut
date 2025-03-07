package io.sereen.coconut;

import io.sereen.coconut.types.Animal;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

;

/**
 * Kiran A (kiran@sereen.io)
 */
public class CoconutStoreTest {
    @ClassRule
    public static TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void testOpen() throws Exception {
        File rootDir = tmpFolder.newFolder("coconut");
        Coconut.init(rootDir);
        Coconut cn = Coconut.getInstance();
        String tableName = "animals";
        CoconutStore store = cn.openStore("animals");
        store.loadCsv("src/test/resources/animals.csv", tableName);

        // this is a dynamically generated Animal type
        String javaSource = "public class Animal { public long id; public String name;}";
        Class animal = cn.compileJavaModel("Animal", javaSource);
        List animals = store.getList("select * from animals", animal);
        assertEquals(4, animals.size());

        // this is a test with statically defined Animal type
        List<Animal> animalList = store.getList("select * from animals where id = 2", Animal.class);
        assertEquals(1, animalList.size());
        assertEquals("Squirrel", animalList.get(0).name);

        animalList = store.getPagedList(Animal.class, tableName, 0, 2);
        assertEquals(2, animalList.size());
        assertEquals("Rabbit", animalList.get(0).name);
        assertEquals("Squirrel", animalList.get(1).name);

        animalList = store.getPagedList(Animal.class, tableName, 2, 2);
        assertEquals(2, animalList.size());
        assertEquals("Panda", animalList.get(0).name);
        assertEquals("Lion", animalList.get(1).name);

        String generatedModelSource = store.generateJavaModelFrom(tableName);
        assertNotNull(generatedModelSource);
        System.out.println(generatedModelSource);
        Class generatedClass = cn.compileJavaModel("Animals", generatedModelSource);
        assertNotNull(generatedClass);

        cn.removeStore(tableName);
        assertFalse(new File(rootDir, "/data/" + tableName).exists());
        cn.close();
    }

    @Test
    public void testLoadCsvUsingRawSql() throws Exception {
        File rootDir = tmpFolder.newFolder("animals");
        Coconut.init(rootDir);
        Coconut cn = Coconut.getInstance();
        CoconutStore store = cn.openStore("animal-data");
        String sql = "create table animals as select * from read_csv('src/test/resources/animals.csv');";
        store.loadCsvWithRawSql(sql);
        List<Animal> animals = store.getList("select * from animals", Animal.class);
        assertEquals(4, animals.size());
    }
}
