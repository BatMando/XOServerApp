/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author Thoraya Hamdy
 */
public class User {
    private String userName = "";
    private String email = "";
    private String password = "";
    private boolean isActive;
    private boolean isPlaying;
    private String ipAddress = "";
    private Integer score = 0;

    public User(){
        
    }
    public User(String userName, String email, String password, boolean isActive, boolean isPlaying, String ipAddress, Integer score) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.isPlaying = isPlaying;
        this.ipAddress = ipAddress;
        this.score = score;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }   
    
}
