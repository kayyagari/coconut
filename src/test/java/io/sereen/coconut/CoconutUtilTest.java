package io.sereen.coconut;

import com.mirth.connect.server.userutil.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.*;

/**
 * Kiran A (kiran@sereen.io)
 */
public class CoconutUtilTest {

    @ClassRule
    public static TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void testCompileJavaModel() throws Exception {
        File rootDir = tmpFolder.newFolder("coconut");
        File srcDir = new File(rootDir, "src");
        srcDir.mkdir();
        File classesDir = new File(rootDir, "classes");
        classesDir.mkdir();
        String javaSource = "public class A { public long id; public A(){this.id = 1;} public long getId() {System.out.println(id); return id;}}";
        ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classesDir.toURI().toURL()});
        Class<?> newClass = CoconutUtil.compileJavaModel(srcDir, classesDir, "A", javaSource, classLoader);
        assertNotNull(newClass);
        assertNotNull(classLoader.loadClass("A"));
        Object a = newClass.newInstance();
        Object id = newClass.getDeclaredMethod("getId").invoke(a);
        assertTrue(id instanceof Long);
        assertEquals(1, (long)id);

        String javaSourceWithPackage = "package a.b;" + "public class B { public long id; public B(){this.id = 1;} public long getId() {System.out.println(id); return id;}}";
        newClass = CoconutUtil.compileJavaModel(srcDir, classesDir, "a.b.B", javaSourceWithPackage, classLoader);
        assertNotNull(newClass);
        a = newClass.newInstance();
        id = newClass.getDeclaredMethod("getId").invoke(a);
        assertTrue(id instanceof Long);
        assertEquals(1, (long)id);

        ClassLoader anotherClassLoader = URLClassLoader.newInstance(new URL[0]);
        try {
            anotherClassLoader.loadClass("A");
            fail("class A should not be available in this ClassLoader");
        }
        catch (ClassNotFoundException e) {
            assertTrue(true);
        }

        FileUtils.deleteDirectory(classesDir);
        classesDir.mkdir();
        anotherClassLoader = URLClassLoader.newInstance(new URL[]{classesDir.toURI().toURL()});
        CoconutUtil.compileClasses(srcDir, classesDir, anotherClassLoader);
        assertTrue(new File(classesDir, "A.class").exists());
        assertTrue(new File(classesDir, "a/b/B.class").exists());
        assertNotNull(anotherClassLoader.loadClass("A"));
        assertNotNull(anotherClassLoader.loadClass("a.b.B"));
    }
}
