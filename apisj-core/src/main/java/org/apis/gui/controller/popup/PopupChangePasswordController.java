package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupChangePasswordController extends BasePopupController {

    private WalletItemModel model;
    private boolean isChangeable = false;

    @FXML
    private Label changeBtn;
    @FXML
    private ApisTextFieldController currentFieldController, newFieldController, reFieldController;
    @FXML
    private Label title, subTitle, currentPasswordLabel, newPasswordLabel;

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordSubTitle);
        currentPasswordLabel.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordCurrentPw);
        newPasswordLabel.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordNewPw);
        changeBtn.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordChange);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        currentFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        newFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        reFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());

        currentFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text = currentFieldController.getText();

                if (text == null || text.equals("")) {
                    currentFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else {
                    currentFieldController.succeededForm();
                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {

            }

            @Override
            public void onAction() {

            }
        });
        newFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if (newFieldController.getCheckBtnEnteredFlag()) {
                    newFieldController.setText("");
                }

                text = newFieldController.getText();

                if (text == null || text.equals("")) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if (text.length() < 8) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordMinSize.get());
                } else if (!newFieldController.pwValidate(text)) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordCombination.get());
                } else {
                    newFieldController.succeededForm();
                }

                if (!newFieldController.getText().isEmpty()) {
                    if(reFieldController.getHandler() != null){
                        reFieldController.getHandler().onFocusOut();
                    }
                }else{

                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {
                String text;

                if (newFieldController.getCheckBtnEnteredFlag()) {
                    newFieldController.setText("");
                }

                text = newFieldController.getText();

                if (text == null || text.equals("")) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if (text.length() < 8) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordMinSize.get());
                } else if (!newFieldController.pwValidate(text)) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordCombination.get());
                } else {
                    newFieldController.succeededForm();
                }

                if (!newFieldController.getText().isEmpty()) {
                    if(reFieldController.getHandler() != null){
                        reFieldController.getHandler().onFocusOut();
                    }
                }else{

                }

                checkChangeNext();
            }

            @Override
            public void onAction() {

            }
        });

        reFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(reFieldController.getCheckBtnEnteredFlag()) {
                    reFieldController.setText("");
                }

                text = reFieldController.getText();

                if(text == null || text.equals("")) {
                    reFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if(!text.equals(newFieldController.getText())) {
                    reFieldController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
                } else {
                    reFieldController.succeededForm();
                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {
                String text;

                if(reFieldController.getCheckBtnEnteredFlag()) {
                    reFieldController.setText("");
                }

                text = reFieldController.getText();

                if(text == null || text.equals("")) {
                    reFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if(!text.equals(newFieldController.getText())) {
                    reFieldController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
                } else {
                    reFieldController.succeededForm();
                }

                checkChangeNext();
            }

            @Override
            public void onAction() {

            }
        });
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (WalletItemModel)model;

    }

    public void checkChangeNext(){

        if( currentFieldController.getText().length() >= 8
            && currentFieldController.pwValidate(currentFieldController.getText())
            && newFieldController.getText().length() >= 8
            && newFieldController.getText().equals(reFieldController.getText())){
            succeededForm();
        }else{
            failedForm();
            currentFieldController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
        }

    }

    public void failedForm(){
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #d8d8d8 ;");
        isChangeable = false;
    }

    public void succeededForm(){
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #910000 ;");
        isChangeable = true;
    }

    public void change(){

        if(isChangeable == false){
            return;
        }

        boolean isChanged = KeyStoreManager.getInstance().updateWalletPassword(this.model.getId(), this.model.getAlias(), currentFieldController.getText(), newFieldController.getText());
        if(isChanged){
            AppManager.getInstance().guiFx.getWallet().removeWalletCheckList();
            AppManager.getInstance().guiFx.getWallet().update();
            exit();
        }else{
            failedForm();
            currentFieldController.failedForm("Please check your password.");
        }
    }

    public ApisTextFieldController getCurrentFieldController() {
        return this.currentFieldController;
    }
}
