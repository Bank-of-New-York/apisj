package org.apis.gui.controller;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.model.SelectBoxWalletItemModel;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemAliasController implements Initializable {
    private SelectBoxWalletItemModel itemModel;
    private SelectBoxItemAliasInterface handler;

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label aliasLabel, addressLabel, maskLabel;
    @FXML
    private ImageView icon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle( this.icon.getFitWidth()-0.5, this.icon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        icon.setClip(clip);
    }

    public void setModel(SelectBoxWalletItemModel model) {
        this.itemModel = model;

        if(model != null) {
            aliasLabel.textProperty().unbind();
            addressLabel.textProperty().unbind();
            maskLabel.textProperty().unbind();

            aliasLabel.textProperty().bind(this.itemModel.aliasProperty());
            addressLabel.textProperty().bind(this.itemModel.addressProperty());
            maskLabel.textProperty().bind(this.itemModel.maskProperty());

            icon.setImage(this.itemModel.getIdenticon());
        }
    }

    public void onMouseEntered(){
        rootPane.setStyle("-fx-background-color: f2f2f2");
    }

    public void onMouseExited(){
        rootPane.setStyle("-fx-background-color: transparent");
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        if(handler != null){
            handler.onMouseClicked(this.itemModel);
        }

        event.consume();
    }

    public SelectBoxItemAliasInterface getHandler() {
        return handler;
    }

    public void setHandler(SelectBoxItemAliasInterface handler) {
        this.handler = handler;
    }


    interface SelectBoxItemAliasInterface{
        void onMouseClicked(SelectBoxWalletItemModel itemModel);
    }
}
