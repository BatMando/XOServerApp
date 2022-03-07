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
    public synchronized ResultSet getResultSet(){
        return result;
    }
    public synchronized void disableConnection() throws SQLException{
        resetStatus();
        result.close();
        preStmt.close();
        con.close();
        databaseInstance = null;
    }
       public synchronized void resetStatus() throws SQLException{
        setAllPlayersToNotPlaying();
        setAllPlayersToOffline();
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
        changeActivation(true,email);
    }
    
    
    public synchronized String validateRegister(String userName, String email){
        String stmt="select email from Player where email=?";
        PreparedStatement pStmt;
        ResultSet rs;
        try {
            pStmt = con.prepareStatement(stmt, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pStmt.setString(1, email);
            rs = pStmt.executeQuery();
            if(rs.next()){
                return "already signed-up";
            }
            else{
               return "Registered Successfully";
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return "Connection Issues";
        }
    }
    
    public synchronized String validateLogin(String email,String password){
        Player ptemp = this.getPlayer(email);
        if(ptemp!=null){
            String pass=ptemp.getPassword();
            if(pass.equals(password)){
                if(checkIsActive(email)){
                    return "This Email is alreay sign-in";
                }
                else{
                    return "Logged in successfully";
                }
            }
            else{
                return "Password is incorrect";
            }
        }
        else{
            return "Email is incorrect";
        }
    }
    
    
    public synchronized int getCountOfOfflinePlayers(){
        try {
            this.preStmt =con.prepareStatement("select count(*) from player where isActive = false",ResultSet.TYPE_SCROLL_SENSITIVE ,ResultSet.CONCUR_READ_ONLY);
            ResultSet r =preStmt.executeQuery();
            if(r.next()){
                return r.getInt(1);
            }
            else{
                return -1;
            }
        }catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("catch getactive");
        }
        return -1;
    }
    
    public synchronized int getCountOfOnlinePlayers(){
        try {
            this.preStmt =con.prepareStatement("select count(*) from player where isActive = true",ResultSet.TYPE_SCROLL_SENSITIVE ,ResultSet.CONCUR_READ_ONLY);
            ResultSet r =preStmt.executeQuery();
            if(r.next()){
                return r.getInt(1);
            }
            else{
                return -1;
            }
        }catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("catch getactive");
        }
        return -1;
    }
    
    
    public synchronized ResultSet getActivePlayers( ){
        
        try {
            this.preStmt =con.prepareStatement("Select * from player where isActive = true ",ResultSet.TYPE_SCROLL_SENSITIVE ,ResultSet.CONCUR_READ_ONLY);
            return preStmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("catch getactive");
            return null;
        }
        
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
                p.setScore(rs.getInt(6));
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
    public synchronized void changeActivation(boolean state,String email){
       
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
    
    public synchronized void changePlaying(boolean state,String email){
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
    
    public synchronized void updateScore(String email, int score){
        try {
            preStmt = con.prepareStatement("update player set score = ?  where email = ?",ResultSet.TYPE_SCROLL_SENSITIVE ,ResultSet.CONCUR_UPDATABLE  );
            preStmt.setInt(1, score);
            preStmt.setString(2, email);
            preStmt.executeUpdate();
            selectResultSet();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public synchronized void setAllPlayersToNotPlaying(){
         try {
            preStmt = con.prepareStatement("update player set isPlaying = ? ",ResultSet.TYPE_SCROLL_SENSITIVE ,ResultSet.CONCUR_UPDATABLE);
            preStmt.setString(1, "false");
            preStmt.executeUpdate();
            selectResultSet();
        } catch (SQLException ex) {
            System.out.println("set all players to not playing");
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void setAllPlayersToOffline(){
        try {
            preStmt = con.prepareStatement("update player set isActive = ? ",ResultSet.TYPE_SCROLL_SENSITIVE ,ResultSet.CONCUR_UPDATABLE);
            preStmt.setString(1, "false");
            preStmt.executeUpdate();
            selectResultSet();
        } catch (SQLException ex) {
            System.out.println("set all players to offline");
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public synchronized void makePlaying(String player1, String player2){
        changePlaying(true,player1);
        changePlaying(true,player2);
    }
    
} 
