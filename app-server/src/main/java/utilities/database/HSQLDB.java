package utilities.database;
import org.hsqldb.DatabaseManager;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import utilities.FileOptions;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jarndt on 5/8/17.
 */
public class HSQLDB {
    public static void main(String[] args) throws IOException, SQLException {
        try {
            HSQLDBCommons.executeQuery("CREATE TABLE test (num INT IDENTITY, answer VARCHAR(250));");
            HSQLDBCommons.executeQuery("INSERT INTO test (answer) values ('this is a new answer');");
            List<Map<String, Object>> result = HSQLDBCommons.executeQuery("select * from test;");
            System.out.println(result);
            HSQLDBCommons.executeQuery("DROP TABLE test;");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            HSQLDBCommons.getDatabase().stopDBServer();
        }
//        HSQLDB hs = null;
//        try {
//            hs = new HSQLDB("tableMetaDataDB");
//            hs.executeQuery("CREATE TABLE test (num INT IDENTITY, answer VARCHAR(250));");
//            hs.executeQuery("INSERT INTO test (answer) values ('this is a new answer');");
//            List<Map<String, Object>> result = hs.executeQuery("select * from test;");
//            System.out.println(result);
//        } catch (IOException | SQLException e) {
////            e.printStackTrace();
//        }finally{
//            hs.stopDBServer();
//        }
    }

    public String dbName;
    final String dbLocation = FileOptions.cleanFilePath(FileOptions.getDefaultDir()+"/databases/HSQLDB/");
    public HSQLDB(String dbName) throws IOException, SQLException {
        Logger.getLogger("hsqldb.db").setLevel(Level.OFF);
        System.setProperty("hsqldb.reconfig_logging", "false");
        this.dbName = dbName;
        startDBServer(dbName);
    }
    Server sonicServer;
    Connection dbConn = null;

    public void startDBServer(String dbName) throws SQLException {
        if(sonicServer != null){
            stopDBServer();
        }
        HsqlProperties props = new HsqlProperties();
        props.setProperty("server.database.0", "file:" + dbLocation + dbName+";");
        props.setProperty("server.dbname.0", "xdb");
        props.setProperty("shutdown","true");
        props.setProperty("hsqldb.reconfig_logging", "false");
        props.setProperty("sql.syntax_pgs","true");
        sonicServer = new org.hsqldb.Server();
        try {
            sonicServer.setProperties(props);
        } catch (Exception e) {
            return;
        }
        sonicServer.start();
//        getDBConn().createStatement().executeUpdate("CREATE TYPE TEXT AS VARCHAR(1000000);");
    }

    public void stopDBServer() {
        DatabaseManager.closeDatabases(0);
        sonicServer.shutdown();
    }

    public Connection getDBConn() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            dbConn = DriverManager.getConnection(
                    "jdbc:hsqldb:hsql://localhost/xdb", "SA", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbConn;
    }

    public List<Map<String,Object>> executeQuery(String query) throws SQLException{
        if(sonicServer == null)
            startDBServer(query);

        dbConn = getDBConn();
        Statement stmt = dbConn.createStatement();
        ResultSet rs = null;
        for(String s: query.split(";"))
            try{
                if(s.replaceAll("[\\s|\t|\n]","").isEmpty())
                   continue;
                if(Arrays.asList("INSERT","UPDATE","DELETE","CREATE").stream().filter(a->s.toLowerCase().contains(a.toLowerCase())).count() == 0)
                    rs = stmt.executeQuery(s);
                else
                    stmt.executeUpdate(s);
            }catch(SQLException sql){
                if(sql.getMessage().contains("Table already exists")){
                    return null;
                }else if(sql.getMessage().contains("Unexpected token: POSITION in statement")){
                    rs = stmt.executeQuery(s.toUpperCase().replace("POSITION", "\"POSITION\""));
                }else{
                    throw sql;
                }
            }
        if(rs == null)
            return null;
        List<Map<String,Object>> results = new ArrayList<Map<String, Object>>();
        while(rs.next()){
            Map<String, Object> subMap = new HashMap<String, Object>();
            ResultSetMetaData rsmd = rs.getMetaData();
            for(int i = 1; i<=rsmd.getColumnCount(); i++){
                subMap.put(rsmd.getColumnLabel(i).toLowerCase(), rs.getObject(i));
            }
            results.add(subMap);
        }
        return results;
    }
}