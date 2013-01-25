package com.comsysto;

import javafx.fxml.FXML;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class StandardController implements DialogController {
    private FXMLDialog dialog;

    public void setDialog(FXMLDialog dialog) {
        this.dialog = dialog;
    }

    @FXML
    public void email() {
        try {
            java.awt.Desktop.getDesktop().mail(new URI("mailto:Elisabeth.Engel@comsysto.com"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void close() {
        dialog.close();
    }
}
