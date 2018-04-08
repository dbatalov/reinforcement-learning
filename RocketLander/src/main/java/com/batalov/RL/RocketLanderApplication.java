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
                    //rocketLanderView.move(5, 0);
                    rocketLander.setBurnRight(true);
                });
        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_PRESSED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.LEFT))
                .subscribe((KeyEvent e) -> {
                    //rocketLanderView.move(-5, 0);
                    rocketLander.setBurnLeft(true);
                });


        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_RELEASED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.RIGHT))
                .subscribe((KeyEvent e) -> {
                    rocketLander.setBurnRight(false);
                });
        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_RELEASED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.LEFT))
                .subscribe((KeyEvent e) -> {
                    rocketLander.setBurnLeft(false);
                });

        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_PRESSED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.UP))
                .subscribe((KeyEvent e) -> {
                    //rocketLanderView.move(0, -5);
                    rocketLander.setBurnLeft(true);
                    rocketLander.setBurnRight(true);
                });
        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_RELEASED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.UP))
                .subscribe((KeyEvent e) -> {
                    rocketLander.setBurnLeft(false);
                    rocketLander.setBurnRight(false);
                });
        JavaFxObservable.eventsOf(scene, KeyEvent.KEY_PRESSED)
                .filter((KeyEvent e) -> e.getCode().equals(KeyCode.DOWN))
                .subscribe((KeyEvent e) -> {
                    //rocketLanderView.move(0, 5);
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
        final long tick_ns = 20_000_000L;
        new AnimationTimer() {
            long prevTick_ns = System.nanoTime();

            public void handle(long now_ns) {
                //System.out.println("timer");

                rocketLanderView.render();
                long delta_ns = now_ns - prevTick_ns;
                //log.info("delta_ns: {}", delta_ns);
                if (delta_ns >= tick_ns) {
                    double tick_s = (double) delta_ns / 1_000_000_000.;
                    //log.info("Tick {}", tick_s);
                    rocketLander.tick(tick_s);
                    prevTick_ns = now_ns;
                }
            }
        }.start();
    }
}
