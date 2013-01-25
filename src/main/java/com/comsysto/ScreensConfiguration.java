package com.comsysto;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
@Lazy
public class ScreensConfiguration {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage newPrimaryStage) {
        primaryStage = newPrimaryStage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showScreen(Parent screen) {
        primaryStage.setScene(new Scene(screen, 777, 500));
        primaryStage.show();
    }

    @Bean
    MainController mainController() {
        return new MainController(this);
    }

    @Bean
    FXMLStage mainStage() {
        return new FXMLStage(mainController(), getClass().getResource("Main.fxml"), primaryStage);
    }

    @Bean
    @Scope("prototype")
    StandardController aboutController() {
        return new StandardController();
    }

    @Bean(name="aboutDialog")
    FXMLDialog aboutDialog() {
        return new FXMLDialog(aboutController(), getClass().getResource("About.fxml"), primaryStage);
    }

}
