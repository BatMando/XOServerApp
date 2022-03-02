/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.derby.jdbc.ClientDriver;
/**
 *
 * @author Thoraya Hamdy
 */
public class Database {
    private static Database databaseInstance;
    public Connection con;
    private PreparedStatement preStmt;
    private ResultSet result;
    
    private Database() throws SQLException{
        DriverManager.registerDriver(new ClientDriver());
        con = DriverManager.getConnection("jdbc:derby://localhost:1527/TicTacToeDatabase","root","root");
    }
    
    public static Database getConnection() throws SQLException{
        if(databaseInstance == null){
            databaseInstance = new Database();
        }
        return databaseInstance;
    }
    
} 
