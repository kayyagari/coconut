package io.sereen.coconut;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.duckdb.DuckDBConnection;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kiran A (kiran@sereen.io)
 */
public class Coconut {
    public static Coconut INSTANCE;
    private static final ConcurrentHashMap<String, CoconutStore> stores = new ConcurrentHashMap<>();

    private static final Logger LOG = Logger.getLogger(Coconut.class);
    private File rootDir;
    private File srcDir;
    private File classesDir;
    private File dataDir;

    private Coconut(File rootDir) {
        this.rootDir = rootDir;
        CoconutUtil.createDir(rootDir);
        srcDir = new File(rootDir, "src");
        CoconutUtil.createDir(srcDir);
        classesDir = new File(rootDir, "classes");
        CoconutUtil.createDir(classesDir);
        dataDir = new File(rootDir, "data");
        CoconutUtil.createDir(dataDir);
    }

    static synchronized void init(File dataDir) {
        if(INSTANCE == null) {
            INSTANCE = new Coconut(dataDir);
        }
    }

    public static Coconut getInstance() {
        if(INSTANCE == null) {
            throw new IllegalStateException("Coconut is not initialized");
        }
        return INSTANCE;
    }

    private void loadClasses() {

    }

    public void close() {
        LOG.info("closing all Coconut stores");
        for(Map.Entry<String, CoconutStore> entry : stores.entrySet()) {
            try {
                entry.getValue().close();
            }
            catch (Exception e) {
                LOG.warn("error while closing the store " + entry.getKey());
                LOG.warn("", e);
            }
        }
        stores.clear();
    }

    public synchronized CoconutStore openStore(String name) throws Exception {
        CoconutStore store = stores.get(name);
        if(store != null) {
            return store;
        }

        File storeDir = new File(dataDir, name);
        DuckDBConnection dbCon = (DuckDBConnection) DriverManager.getConnection("jdbc:duckdb:" + storeDir.getAbsolutePath());
        store = new CoconutStore(dbCon);
        stores.put(name, store);
        return store;
    }

    public synchronized void removeStore(String name) throws SQLException {
        CoconutStore store = stores.remove(name);
        if(store != null) {
            store.close();
        }
        FileUtils.deleteQuietly(new File(dataDir, name));
    }

    public Class<?> compileJavaModel(String className, String javaSource) throws Exception {
        return compileJavaModel(className, javaSource, Thread.currentThread().getContextClassLoader());
    }

    public Class<?> compileJavaModel(String className, String javaSource, ClassLoader parentClassLoader) throws Exception {
        return CoconutUtil.compileJavaModel(srcDir, classesDir, className, javaSource, parentClassLoader);
    }
}
