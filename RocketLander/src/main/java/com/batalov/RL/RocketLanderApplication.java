package com.batalov.RL;

import com.batalov.RL.render.RocketLanderView;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RocketLanderApplication extends Application {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private Stage stage;
    private RocketLander rocketLander = new RocketLander();
    RocketLanderView rocketLanderView = new RocketLanderView(rocketLander, 800, 600);

    public static void main(String[] args) {
        launch(args);
    }

    public void setControls() {
        Scene scene = stage.getScene();
        /*
        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_PRESSED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.LEFT))
                .subscribe((KeyEvent e) -> rocketLander.setBurnRight(true));

        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_RELEASED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.LEFT))
                .subscribe((KeyEvent e) -> rocketLander.setBurnRight(false));

        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_PRESSED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.RIGHT))
                .subscribe((KeyEvent e) -> rocketLander.setBurnLeft(true));

        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_RELEASED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.RIGHT))
                .subscribe((KeyEvent e) -> rocketLander.setBurnLeft(false));
                */
        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_PRESSED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.RIGHT))
                .subscribe((KeyEvent e) -> {
                    rocketLanderView.move(5, 0);
                });
        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_PRESSED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.LEFT))
                .subscribe((KeyEvent e) -> {
                    rocketLanderView.move(-5, 0);
                });

        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_PRESSED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.UP))
                .subscribe((KeyEvent e) -> {
                    rocketLanderView.move(0, -5);
                });
        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_PRESSED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.DOWN))
                .subscribe((KeyEvent e) -> {
                    rocketLanderView.move(0, 5);
                });
    }


    public void start(Stage stage) {
        this.stage = stage;
        Group root = new Group();
        Scene scene = new Scene(root);
        root.getChildren().add(rocketLanderView);
        stage.setScene(scene);
        stage.setTitle("Rocket Lander!");
        setControls();
        stage.show();
        new AnimationTimer() {
            public void handle(long now) {
                //System.out.println("timer");
                rocketLanderView.render();
            }
        }.start();
    }
}
