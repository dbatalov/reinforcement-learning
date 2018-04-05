package com.batalov.RL;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;


public class RocketLanderApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root);
        RocketLander rocketLander = new RocketLander();
        RocketLanderViewFX rocketLanderViewFX = new RocketLanderViewFX(rocketLander, 800, 600);
        root.getChildren().add(rocketLanderViewFX);
        stage.setScene(scene);
        stage.setTitle("Hello, World!");
        stage.show();
        new AnimationTimer() {
            public void handle(long now) {
                System.out.println("timer");
                rocketLanderViewFX.render();
            }
        }.start();
    }
}
