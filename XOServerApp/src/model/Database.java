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
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    public synchronized void selectResultSet(){
        
        try {
            this.preStmt =con.prepareStatement("Select * from player",ResultSet.TYPE_SCROLL_SENSITIVE ,ResultSet.CONCUR_READ_ONLY);
            this.result = preStmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void register(Player p)
    {
        try {
            String stmt = "insert into Player(username,email,password) values (?,?,?)";
            preStmt = con.prepareStatement(stmt);
            
            preStmt.setString(1,p.getUserName());
            preStmt.setString(2,p.getEmail());
            preStmt.setString(3,p.getPassword());
            
            int isOK = preStmt.executeUpdate();
            
            if (isOK > 0){
                System.out.println("Inserted successfully");
            }
            else{
                System.out.println("Inserted failed");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        
    }
    
    public synchronized void login(String email,String password){
        changePlaying(email);
    }
    
    
    public synchronized boolean validateRegister(Player p){
        String stmt="select email from Player where email=?";
        PreparedStatement pStmt;
        ResultSet rs;
        boolean isExist=false;
        try {
            pStmt = con.prepareStatement(stmt, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pStmt.setString(1, p.getEmail());
            rs = pStmt.executeQuery();
            if(rs.next()){
                isExist=true;
            }
            else{
                isExist=false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isExist;
    }
    
    public synchronized boolean validateLogin(String email,String password){
        Player ptemp = this.getPlayer(email);
        if(ptemp!=null){
            String pass=ptemp.getPassword();
            if(pass.equals(password)){
                if(checkIsActive(email)){
                    System.out.println("player already signed in!");
                }
                else{
                    return true;
                }
            }
            else{
                System.out.println("incorrect password!");
            }
        }
        else{
            System.out.println("No player with that email!");
        }
        return false;
    }
    
    //get player data:
    public synchronized Player getPlayer(String email){
        String stmt = "select * from Player where email=?";
        PreparedStatement pStmt;
        ResultSet rs;
        Player p;
        try {
            pStmt = con.prepareStatement(stmt, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pStmt.setString(1,email);
            rs = pStmt.executeQuery();
            if(rs.next()){
                p=new Player();
                p.setUserName(rs.getString(1));
                p.setEmail(rs.getString(2));
                p.setPassword(rs.getString(3));
                p.setIsActive(rs.getBoolean(4));
                p.setIsPlaying(rs.getBoolean(5));
                p.setIpAddress(rs.getString(6));
                p.setScore(rs.getInt(7));
                return p;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    public synchronized String getUsername(String email){
        String stmt = "select username from Player where email=?";
        PreparedStatement pStmt;
        ResultSet rs;
        try {
            pStmt = con.prepareStatement(stmt, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pStmt.setString(1,email);
            rs = pStmt.executeQuery();
            if(rs.next()){
                return rs.getString(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "this email doesn't exist!";
    }
    
    
    public synchronized int getScore(String email){
        String stmt = "select score from Player where email=?";
        PreparedStatement pStmt;
        ResultSet rs;
        try {
            pStmt = con.prepareStatement(stmt, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pStmt.setString(1,email);
            rs = pStmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    
    public synchronized boolean checkIsActive(String email){
        String stmt = "select isActive from Player where email=?";
        PreparedStatement pStmt;
        ResultSet rs;
        try {
            pStmt = con.prepareStatement(stmt, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pStmt.setString(1,email);
            rs = pStmt.executeQuery();
            if(rs.next()){
                if(rs.getBoolean(1)){
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public synchronized boolean checkIsPlaying(String email){
        String stmt = "select isPlaying from Player where email=?";
        PreparedStatement pStmt;
        ResultSet rs;
        try {
            pStmt = con.prepareStatement(stmt, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pStmt.setString(1,email);
            rs = pStmt.executeQuery();
            if(rs.next()){
                if(rs.getBoolean(1)){
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    
    
    //update player data
    public synchronized void changeActivation(String email){
        Player ptemp = this.getPlayer(email);
        boolean state = ptemp.isIsActive();
        try {
            preStmt = con.prepareStatement("update player set isActive = ? where email = ?");
            preStmt.setBoolean(1, state);
            preStmt.setString(2, email);
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        selectResultSet();
    }
    
    public synchronized void changePlaying(String email){
        Player ptemp = this.getPlayer(email);
        boolean state = ptemp.isIsPlaying();
        try {
            preStmt = con.prepareStatement("update player set isPlaying = ? where email = ?");
            preStmt.setBoolean(1, state);
            preStmt.setString(2, email);
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        selectResultSet();
    }
} 
