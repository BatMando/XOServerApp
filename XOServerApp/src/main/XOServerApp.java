/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Server;

/**
 *
 * @author Thoraya Hamdy
 */
public class XOServerApp extends Application {
    Server server;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/FXMLHome.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest((event) -> {
            
           Thread listenerThread = new Thread(() -> {
                try {
                server.notifyAllClients();
            } catch (IOException ex) {
                Logger.getLogger(XOServerApp.class.getName()).log(Level.SEVERE, null, ex);
            }
            });
            
            System.exit(1);
        });       
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
