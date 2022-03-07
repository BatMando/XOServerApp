/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Chart;
import model.Server;

/**
 * FXML Controller class
 *
 * @author mando
 */
public class FXMLHomeController implements Initializable {
    Server server;
    ResultSet chartData;
    
    public static boolean serverState ;
    private boolean flageStartThrea = false;
    private boolean flageStartCharThread = true;
    private boolean onlineOrOfflineFlag = true;
    private boolean showingChart = false;
    private Thread updateListThread;
    private Thread chartThread;
    private int countOnline = 0;
    private int countOffline = 0;
    private Chart chart;
    private Stage thisStage;
    int id;
    
    @FXML
    private Button ipAddressBtn;
    @FXML
    private Button statBtn;
    @FXML
    private RadioButton onlineRadioBtn;
    @FXML
    private RadioButton offlineRadioBtn;
    @FXML
    private Button onBtn;
    @FXML
    private Button offBtn;
    @FXML
    private ScrollPane scrollPane;
   
  
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serverState = false;
        server = Server.getServer();
        disableBtn();
        handleOnOffButtons();
        updateListThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true && serverState){
                     Platform.runLater(()->{
                       if(onlineOrOfflineFlag){
                         listPlayers(true);
                       }else{
                          listPlayers(false); 
                       }  
                     });
                   try{
                     Thread.sleep(2000);  

                   }catch(InterruptedException ex){
                     emptyList();
                     
                   }
                }
            }
        });   
        
        chartThread = new Thread(new Runnable() {
                      
            @Override
            public void run() {
                while(true && showingChart){
                    if(!Chart.getFlag()){
                        ObservableList<PieChart.Data> pieChartData;
                        pieChartData =
                        FXCollections.observableArrayList(
                            new PieChart.Data("Offline", countOffline),
                            new PieChart.Data("Online", countOnline));

                        chart.setChartData(pieChartData);                        
                        Platform.runLater(() -> {
                            try {
                                chart.start(thisStage);
                            } catch (Exception ex) {
                                Logger.getLogger(FXMLHomeController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        });

                        try{
                            Thread.sleep(3000);
                        }catch(InterruptedException ex){

                        }

                    }else{
                        flageStartCharThread = false;
                        chartThread.suspend();
                   }
                        
                } 
            }
        });
    }    
    private synchronized void listPlayers(Boolean state){
        
        server.getResultSet();
        
        try {
            Button button;
            VBox vbox = new VBox();
            HBox hbox;
            
            countOnline = 0;
            
            while(server.databaseInstance.getResultSet().next()){
                if(server.databaseInstance.getResultSet().getString("ISACTIVE").equals(state+"")){
                    //System.out.println("platform check action action");
                    
                    ImageView view,view2;
                        // avatar view
                    view = new ImageView(new Image(this.getClass().getResourceAsStream("/assets/man@2x.png")));
                    view.setFitHeight(30);
                    view.setPreserveRatio(true);

                    // active icon view
                    if(state){

                        view2 = new ImageView(new Image(this.getClass().getResourceAsStream("/assets/online@2x.png")));
                        countOnline++;
                    }else{

                           view2 = new ImageView(new Image(this.getClass().getResourceAsStream("/assets/offline@2x.png")));
                        countOffline++;
                    }


                    view2.setFitHeight(20);
                    view2.setPreserveRatio(true);

                    button = new Button(""+server.getResultSet().getString("USERNAME"),view);
                    button.setAlignment(Pos.BOTTOM_LEFT);

                    hbox = new HBox(button,view2);
                    HBox.setMargin(view2, new Insets(10,0,0,5)); // top right bottom left
                    //button.getStyleClass().add("button1");
                    vbox.getChildren().add(hbox);

                    scrollPane.setContent(vbox);
                    
                    
                }
            } 
            server.databaseInstance.getResultSet().beforeFirst();
        } catch (SQLException ex) {
            Logger.getLogger(FXMLHomeController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    @FXML
    private void toggleServer(ActionEvent event) throws InterruptedException, IOException{
        
        serverState = !serverState;
        handleOnOffButtons();
        
        if(serverState){
            try {
                System.out.println("toggle");
                server.enableConnections();
                
//                listPlayers(true);
                enableBtn();    // enable list online and offline btn;
                
                if(Platform.isFxApplicationThread()){
                    if(!flageStartThrea){
                      updateListThread.start();  
                    }else{
                        updateListThread.resume();
                    }
                }
            
            }catch(SQLException e){
                System.out.println("Connection Issues, Try again later");
                serverState = !serverState;
                 handleOnOffButtons();
            }
        }else{ // state is true needed to be deactivate
            try {
                server.notifyAllClients();
                updateListThread.suspend();
                flageStartThrea = true;
                onlineOrOfflineFlag = true;
                //Platform.exit();
            }
            
            finally{
                server.disableConnections();
                handleOnOffButtons();
                emptyList();
                disableBtn();
            }
        }
            
    }
 
    
  
    
    @FXML
    private void toggleList(ActionEvent e){

        if(onlineOrOfflineFlag){
            System.out.println("list on");
            scrollPane.setContent(null);
            onlineOrOfflineFlag = false;
            onlineRadioBtn.setSelected(true);
            onlineRadioBtn.setDisable(true);
            offlineRadioBtn.setSelected(false);
            offlineRadioBtn.setDisable(false);
            listPlayers(true);

        }else{
            System.out.println("list off");
            scrollPane.setContent(null);
            onlineOrOfflineFlag = true;
            onlineRadioBtn.setSelected(false);  
            onlineRadioBtn.setDisable(false);
            offlineRadioBtn.setSelected(true); 
            offlineRadioBtn.setDisable(true); 
            listPlayers(false);
           
        }
    }
    @FXML
    public void getip(ActionEvent e){
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Get IP");
        alert.setHeaderText(null);
        try {
            alert.setHeaderText("Server IP is :" +Inet4Address.getLocalHost().getHostAddress());
            ButtonType Yes = new ButtonType("OK"); 
            alert.getDialogPane().getButtonTypes().add(Yes);
            
            DialogPane dialogPane = alert.getDialogPane();

            
        } catch (UnknownHostException ex) {
            Logger.getLogger(FXMLHomeController.class.getName()).log(Level.SEVERE, null, ex);
        }

        alert.showAndWait();
    }
    @FXML
    private void chartHandle(ActionEvent e){
        countOffline = server.databaseInstance.getCountOfOfflinePlayers();
        showingChart = true; 
        chart.setFlag(false);
        chart = Chart.getChartObj();
        thisStage = (Stage) ipAddressBtn.getScene().getWindow();
        
        if(Platform.isFxApplicationThread() && showingChart){
            if(flageStartCharThread){
              chartThread.start();  
            }else{
               
               chartThread.resume();
            }
            
        }
        
    }
    private void emptyList(){
        scrollPane.setContent(null);
    }
    private void disableBtn(){
        ipAddressBtn.setDisable(true);
        statBtn.setDisable(true);
        onlineRadioBtn.setDisable(true);
        offlineRadioBtn.setDisable(true);
    }
    private void enableBtn(){
        //offBtn.setDisable(false);
        ipAddressBtn.setDisable(false);
        statBtn.setDisable(false);
        onlineRadioBtn.setDisable(false);
        offlineRadioBtn.setDisable(false);
    }   

    private void handleOnOffButtons() {
        if (serverState == true ){
            onBtn.setDisable(true);
            offBtn.setDisable(false);
        }else {
            onBtn.setDisable(false);
            offBtn.setDisable(true);
        }
             
    }
    
}
