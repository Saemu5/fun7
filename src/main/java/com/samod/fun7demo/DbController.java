package com.samod.fun7demo;

import java.sql.*;

public class DbController {
    final private String protocol = "jdbc:derby:";
    final private static String dbName = "fun7db";
    Connection conn;
    PreparedStatement psUpdate;
    PreparedStatement psGet;
    PreparedStatement psNew;

    //establishes the connection to database and prepares statements to be used,
    //checks for existing 'LOGINS' table and creates it if one is not found
    public DbController() throws SQLException{

        //establish connection to db
        conn = DriverManager.getConnection(protocol + dbName + ";create=true", null);
        System.out.println("Connected to Derby database " + dbName);

        //create a table if one is not found
        var checkDb = conn.getMetaData().getTables(null,"APP","LOGINS", new String[] {"TABLE"});
        var exists = false;
        while (checkDb.next()) {
            exists = checkDb.getString("TABLE_NAME").equals("LOGINS") || exists;
        }
        if(!exists){
            conn.createStatement().execute("CREATE TABLE LOGINS(USERID VARCHAR(40) PRIMARY KEY, COUNT INT)");
            System.out.println("Logins table created.");
        } else {
            System.out.println("Logins table found.");
        }

        //prepare statements for querying db
        psNew = conn.prepareStatement("INSERT INTO LOGINS VALUES (?, 0)");
        psGet = conn.prepareStatement("SELECT COUNT FROM LOGINS WHERE USERID=?");
        psUpdate = conn.prepareStatement("UPDATE LOGINS SET COUNT=COUNT+1 WHERE USERID=?");

    }

    //adds new user to db
    public void addNewUser(String userid) throws SQLException{
        psNew.setString(1, userid);
        psNew.executeUpdate();
    }

    //raises user's login count
    public void incrementLoginCount(String userid) throws SQLException{
        psUpdate.setString(1,userid);
        psUpdate.executeUpdate();
    }

    //returns user's login count or -1 if user is not in db yet
    public int getLogins(String userid) throws SQLException{
        ResultSet rs;
        psGet.setString(1, userid);
        rs = psGet.executeQuery();
        int a = -1;
        while(rs.next()){
            a = rs.getInt(1);
        }
        return a;
    }

    //closes connection to db and resources
    public void close() {
        try {
            DriverManager.getConnection(protocol + ";shutdown=true");
            psUpdate.close();
            psUpdate = null;
            psNew.close();
            psNew = null;
            psGet.close();
            psGet = null;
            conn.close();
            conn = null;
        } catch (SQLException se) {
            if (((se.getErrorCode() == 50000)
                    && ("XJ015".equals(se.getSQLState())))) {
                System.out.println("Derby shut down normally");
            } else {
                System.err.println("Derby did not shut down normally");
                printSQLException(se);
            }
        }
    }

    //prints sql error info
    public static void printSQLException(SQLException e)
    {
        while (e != null)
        {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            e.printStackTrace();
            e = e.getNextException();
        }
    }

}
