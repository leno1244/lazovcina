package com.example.lazovcina;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        StackPane root = fxmlLoader.load();

        Scene scene = new Scene(root, 1280, 720);
        stage.setTitle("Card Game: Varalica");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    }