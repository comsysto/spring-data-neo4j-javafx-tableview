package com.comsysto;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CustomerApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(CustomerAppConfiguration.class);
        ScreensConfiguration screens = context.getBean(ScreensConfiguration.class);

        screens.setPrimaryStage(stage);
        screens.mainStage().show();
    }
}
