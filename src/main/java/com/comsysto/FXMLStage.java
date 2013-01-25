package com.comsysto;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;

public class FXMLStage extends Stage {
    public FXMLStage(StageController controller, URL fxml, Stage owner) {
        this(controller, fxml, owner, StageStyle.DECORATED);
    }

    public FXMLStage(final StageController controller, URL fxml, Stage owner, StageStyle style) {
        super(style);
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        owner.setResizable(true);
        FXMLLoader loader = new FXMLLoader(fxml);
        try {
            loader.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> aClass) {
                    return controller;
                }
            });
            controller.setStage(this);
            setScene(new Scene((Parent) loader.load()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
