package io.sereen.coconut;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.duckdb.DuckDBConnection;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * A DuckDb backed store
 * Kiran A (kiran@sereen.io)
 */
public class CoconutStore {
    private DuckDBConnection dbCon;

    public CoconutStore(DuckDBConnection dbCon) {
        this.dbCon = dbCon;
    }

    public void loadCsv(String filePath, String tableName) throws Exception {
        loadCsv(filePath, tableName, ',');
    }

    public void loadCsv(String filePath, String tableName, char delim) throws Exception {
        Statement stmt = dbCon.createStatement();
        String sql = String.format("create table %s as select * from read_csv('%s', delim='%c')", tableName, filePath, delim);
        stmt.execute(sql);
        stmt.close();
    }

    public <T> T getOne(String sql, Class<T> modelClass) throws Exception {
        Statement stmt = dbCon.createStatement();
        ResultSet resultSet = stmt.executeQuery(sql);
        BeanHandler<T> bh = new BeanHandler<>(modelClass, new BasicRowProcessor(new SimpleBeanProcessor()));
        T bean = bh.handle(resultSet);
        resultSet.close();
        stmt.close();
        return bean;
    }

    public <T> List<T> getList(String sql, Class<T> modelClass) throws Exception {
        Statement stmt = dbCon.createStatement();
        ResultSet resultSet = stmt.executeQuery(sql);
        BeanListHandler<T> bh = new BeanListHandler<>(modelClass, new BasicRowProcessor(new SimpleBeanProcessor()));
        List<T> beans = bh.handle(resultSet);
        resultSet.close();
        stmt.close();
        return beans;
    }

    public <T> List<T> getPagedList(Class<T> modelClass, String tableName, long rowId, int pageSize) throws SQLException {
        Statement stmt = dbCon.createStatement();
        String sql = String.format("select * from %s where ROWID >= %d LIMIT %d", tableName, rowId, pageSize);
        ResultSet resultSet = stmt.executeQuery(sql);
        BeanListHandler<T> bh = new BeanListHandler<>(modelClass, new BasicRowProcessor(new SimpleBeanProcessor()));
        List<T> beans = bh.handle(resultSet);
        resultSet.close();
        stmt.close();
        return beans;
    }

    public String generateJavaModelFrom(String tableName) throws SQLException {
        Statement stmt = dbCon.createStatement();
        ResultSet resultSet = stmt.executeQuery(String.format("select * from %s limit 1", tableName));
        ResultSetMetaData rsmd = resultSet.getMetaData();
        String javaSource = CoconutUtil.generateJavaModel(tableName, rsmd);
        resultSet.close();
        stmt.close();
        return javaSource;
    }

    public DuckDBConnection getDbConnection() {
        return dbCon;
    }

    public void close() throws SQLException {
        dbCon.close();
    }
}
