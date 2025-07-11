package com.cpp.library;

import com.cpp.library.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Initialize Hibernate
        HibernateUtil.getSessionFactory();
        
        // Load the main dashboard
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
        Parent root = loader.load();
        
        // Set up the primary stage
        primaryStage.setTitle("Ashley's Library Management System");
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();
        
        // Set up shutdown hook
        primaryStage.setOnCloseRequest(event -> {
            HibernateUtil.shutdown();
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 