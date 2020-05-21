package com.samod.fun7demo;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbController {
    final private String protocol = "jdbc:derby:";
    final private static String fun7db = "fun7db;create=true";
    Connection conn;

    public DbController() throws SQLException{
        conn = DriverManager.getConnection(protocol + fun7db, null);

        var checkDb = conn.getMetaData().getTables(null,"APP","LOGINS", new String[] {"TABLE"});
        var exists = false;
        while (checkDb.next()) {
            exists = checkDb.getString("TABLE_NAME").equals("LOGINS") || exists;
        }
        if(!exists){
            conn.createStatement().execute("CREATE TABLE LOGINS(USERID INT, COUNT INT)");
        }

    }

    public void addNewUser(int userid) throws SQLException{
        conn.createStatement().executeUpdate(String.format("INSERT INTO LOGINS VALUES (%d, 0)", userid));
    }

    public void incrementLoginCount(int userid) throws SQLException{
        PreparedStatement psUpdate;
        psUpdate = conn.prepareStatement(
                "UPDATE LOGINS SET COUNT=COUNT+1 WHERE USERID=?");

        psUpdate.setInt(1,userid);
        psUpdate.executeUpdate();

    }

    public int getLogins(int userid) throws SQLException{
        ResultSet rs;
        PreparedStatement psGet;
        psGet = conn.prepareStatement("SELECT COUNT FROM LOGINS WHERE USERID=?");
        psGet.setInt(1, userid);
        rs = psGet.executeQuery();
        int a = -1;
        while(rs.next()){
            a = rs.getInt(1);
        }
        return a;
    }

    public void close() {
        try {
            DriverManager.getConnection(protocol + ";shutdown=true");
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

    public static void printSQLException(SQLException e)
    {
        while (e != null)
        {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            e = e.getNextException();
        }
    }

}
