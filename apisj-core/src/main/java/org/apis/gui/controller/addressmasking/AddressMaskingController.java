package org.apis.gui.controller.addressmasking;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.controller.module.TabMenuController;
import org.apis.gui.controller.popup.PopupContractWarningController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.HttpRequestManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddressMaskingController extends BaseViewController {

    private final int TAB_REGISTER_MASK = 0;
    private final int TAB_HAND_OVER_MASK = 1;
    private final int TAB_REGISTER_DOMAIN = 2;
    private int tabIndex = TAB_REGISTER_MASK;

    private String abi = ContractLoader.readABI(ContractLoader.CONTRACT_ADDRESS_MASKING);
    private byte[] addressMaskingAddress = AppManager.getInstance().constants.getADDRESS_MASKING_ADDRESS();
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionRegisterMask = contract.getByName("registerMask");
    private CallTransaction.Function functionHandOverMask = contract.getByName("handOverMask");
    private CallTransaction.Function functionDefaultFee = contract.getByName("defaultFee");

    @FXML private Label sideTabLabel1, sideTabLabel2;
    @FXML private Pane sideTabLinePane1, sideTabLinePane2;
    @FXML private AnchorPane tab1LeftPane, tab1RightPane, tabRightHandOverReceiptPane, tabLeftHandOfMask, tab2LeftPane1, tab2LeftPane3;
    @FXML private GridPane commercialDescGrid, publicDescGrid, tab2RightPane1;
    @FXML private ImageView domainRequestBtn;
    @FXML private TextField publicDomainTextField, emailTextField;
    @FXML private TextArea publicTextArea;

    @FXML private GridPane btnPay;

    // Multilingual Support Label
    @FXML
    private Label tabTitle,
                  registerDomainLabel, registerDomainDesc, sideTab1Desc1, sideTab1Desc2, sideTab1Desc3, sideTab2Desc1, sideTab2Desc2, sideTab2Desc3, sideTab2Desc4,
                  emailAddrLabel, emailDesc1, emailDesc2, emailDesc3, requestBtnLabel, publicDomainTitle, publicDomainDesc, publicDomainDesc1, publicDomainDesc2,
                  publicDomainDesc3, publicDomainDesc4, publicMessageTitle, publicMessageDesc;

    @FXML private TabMenuController tabMenuController;
    @FXML private AddressMaskingRegisterController registerController;
    @FXML private AddressMaskingReceiptController receiptController;
    @FXML private AddressMaskingHandOverController handOverMaskController;
    @FXML private AddressMaskingHandOverReceiptController handOverReceiptController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setAddressMasking(this);

        // Multilingual Support
        languageSetting();

        tabMenuController.setHandler(new TabMenuController.TabMenuImpl() {
            @Override
            public void onMouseClicked(String text, int index) {
                initStyleTab(index);
            }
        });
        receiptController.setHandler(new AddressMaskingReceiptController.AddressMaskingReceiptImpl() {
            @Override
            public void transfer() {
                String faceAddress = registerController.getAddress();
                String name = registerController.getMask();
                String domainId = registerController.getDomainId();

                if(name.length() <= 0){
                    return;
                }

                String payerAddress = registerController.getPayerAddress();
                BigInteger value = registerController.getValue();
                String gasLimit = registerController.getGasLimit().toString();
                String gasPrice = registerController.getGasPrice().toString();

                Object[] args = new Object[3];
                args[0] = Hex.decode(faceAddress);   //_faceAddress
                args[1] = name;   //_name
                args[2] = new BigInteger(domainId);   //_domainId
                byte[] functionCallBytes = functionRegisterMask.encode(args);

                // 완료 팝업 띄우기
                PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup("popup_contract_warning.fxml", 0);
                controller.setData(payerAddress, value.toString(), gasPrice, gasLimit, addressMaskingAddress, functionCallBytes);
                controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                    @Override
                    public void success(Transaction tx) {
                    }
                    @Override
                    public void fail(Transaction tx){

                    }
                });
            }
        });


        registerController.setHandler(new AddressMaskingRegisterController.AddressMaskingRegisterImpl() {
            @Override
            public void settingLayoutData() {
                AddressMaskingController.this.settingLayoutData();
            }
        });
        handOverMaskController.setHandler(new AddressMaskingHandOverController.AddressMaskingHandOverImpl() {
            @Override
            public void settingLayoutData() {
                AddressMaskingController.this.settingLayoutData();
            }
        });
        handOverReceiptController.setHandler(new AddressMaskingHandOverReceiptController.AddressMaskingHandOverReceiptImpl() {
            @Override
            public void transfer() {
                String fromAddress = handOverMaskController.getHandOverFromAddress();
                String toAddress = handOverMaskController.getHandOverToAddress();
                Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
                BigInteger value = new BigInteger(""+values[0]);
                BigInteger gasPrice = handOverMaskController.getGasPrice();
                BigInteger gasLimit = handOverMaskController.getGasLimit();

                Object[] args = new Object[1];
                args[0] = Hex.decode(toAddress);
                byte[] functionCallBytes = functionHandOverMask.encode(args);

                // 완료 팝업 띄우기
                PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup("popup_contract_warning.fxml", 0);
                controller.setData(fromAddress, value.toString(), gasPrice.toString(), gasLimit.toString(), addressMaskingAddress, functionCallBytes);
                controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                    @Override
                    public void success(Transaction tx) {
                    }
                    @Override
                    public void fail(Transaction tx){

                    }
                });
            }
        });

        this.publicDomainTextField.focusedProperty().addListener(textFieldListener);

        initStyleTab(0);
        tabMenuController.selectedMenu(TAB_REGISTER_MASK);
    }

    public void languageSetting() {
        tabTitle.textProperty().bind(StringManager.getInstance().addressMasking.tabTitle);


        tabMenuController.addItem(StringManager.getInstance().addressMasking.tabRegisterMask, TAB_REGISTER_MASK);
        tabMenuController.addItem(StringManager.getInstance().addressMasking.tabHandOverMask, TAB_HAND_OVER_MASK);
        tabMenuController.addItem(StringManager.getInstance().addressMasking.tabRegisterDomain, TAB_REGISTER_DOMAIN);
        registerDomainLabel.textProperty().bind(StringManager.getInstance().addressMasking.registerDomainLabel);
        registerDomainDesc.textProperty().bind(StringManager.getInstance().addressMasking.registerDomainDesc);
        sideTabLabel1.textProperty().bind(StringManager.getInstance().addressMasking.sideTabLabel1);
        sideTabLabel2.textProperty().bind(StringManager.getInstance().addressMasking.sideTabLabel2);
        sideTab1Desc1.textProperty().bind(StringManager.getInstance().addressMasking.sideTab1Desc1);
        sideTab1Desc2.textProperty().bind(StringManager.getInstance().addressMasking.sideTab1Desc2);
        sideTab1Desc3.textProperty().bind(StringManager.getInstance().addressMasking.sideTab1Desc3);
        sideTab2Desc1.textProperty().bind(StringManager.getInstance().addressMasking.sideTab2Desc1);
        sideTab2Desc2.textProperty().bind(StringManager.getInstance().addressMasking.sideTab2Desc2);
        sideTab2Desc3.textProperty().bind(StringManager.getInstance().addressMasking.sideTab2Desc3);
        sideTab2Desc4.textProperty().bind(StringManager.getInstance().addressMasking.sideTab2Desc4);
        emailAddrLabel.textProperty().bind(StringManager.getInstance().addressMasking.emailAddrLabel);
        emailTextField.promptTextProperty().bind(StringManager.getInstance().addressMasking.emailPlaceholder);
        emailDesc1.textProperty().bind(StringManager.getInstance().addressMasking.emailDesc1);
        emailDesc2.textProperty().bind(StringManager.getInstance().addressMasking.emailDesc2);
        emailDesc3.textProperty().bind(StringManager.getInstance().addressMasking.emailDesc3);
        requestBtnLabel.textProperty().bind(StringManager.getInstance().addressMasking.requestBtnLabel);
        publicDomainTitle.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainTitle);
        publicDomainDesc.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainDesc);
        publicDomainDesc1.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainDesc1);
        publicDomainDesc2.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainDesc2);
        publicDomainDesc3.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainDesc3);
        publicDomainDesc4.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainDesc4);
        publicDomainTextField.promptTextProperty().bind(StringManager.getInstance().addressMasking.publicDomainPlaceholder);
        publicMessageTitle.textProperty().bind(StringManager.getInstance().addressMasking.publicMessageTitle);
        publicMessageDesc.textProperty().bind(StringManager.getInstance().addressMasking.publicMessageDesc);
        publicTextArea.promptTextProperty().bind(StringManager.getInstance().addressMasking.publicTextareaPlaceholder);
    }

    private ChangeListener<Boolean> textFieldListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            // Focus in Function
            if(newValue) {
                if(tab2LeftPane3.isVisible()) {
                    publicDomainTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
            }

            // Focus out Function
            else {
                if(tab2LeftPane3.isVisible()) {
                    publicDomainTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
            }
        }
    };

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(id.equals("sideTab1")) {
            initStyleSideTab(0);

        } else if(id.equals("sideTab2")) {
            initStyleSideTab(1);

        } else if(id.equals("domainRequestBtn")) {
            if(commercialDescGrid.isVisible()) {
                this.tab2LeftPane1.setVisible(false);
                this.tab2RightPane1.setVisible(true);

            } else {
                this.tab2LeftPane1.setVisible(false);
                this.tab2LeftPane3.setVisible(true);

                //publicSendBtn
                // 오른쪽 뷰 보이
                this.tab2RightPane1.setVisible(true);
            }

        } else if(id.equals("commercialBackBtn")) {
            this.tab2RightPane1.setVisible(false);
            this.tab2LeftPane1.setVisible(true);

        } else if(id.equals("publicBackBtn")) {
            this.tab2LeftPane3.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.tab2LeftPane1.setVisible(true);

        }
    }

    public void initStyleTab(int index){
        this.tabIndex = index;
        if(index == TAB_REGISTER_MASK) {
            this.tab1LeftPane.setVisible(true);         this.tab1LeftPane.setPrefHeight(-1);
            this.tabLeftHandOfMask.setVisible(false);   this.tabLeftHandOfMask.setPrefHeight(0);
            this.tab2LeftPane1.setVisible(false);       this.tab2LeftPane1.setPrefHeight(0);
            this.tab2LeftPane3.setVisible(false);       this.tab2LeftPane3.setPrefHeight(0);

            this.tab1RightPane.setVisible(true);
            this.tab2RightPane1.setVisible(false);
            this.tabRightHandOverReceiptPane.setVisible(false);

        } else if(index == TAB_HAND_OVER_MASK) {
            this.tab1LeftPane.setVisible(false);        this.tab1LeftPane.setPrefHeight(0);
            this.tabLeftHandOfMask.setVisible(true);    this.tabLeftHandOfMask.setPrefHeight(-1);
            this.tab2LeftPane1.setVisible(false);       this.tab2LeftPane1.setPrefHeight(0);
            this.tab2LeftPane3.setVisible(false);       this.tab2LeftPane3.setPrefHeight(0);
            this.handOverMaskController.update();

            this.tab1RightPane.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.tabRightHandOverReceiptPane.setVisible(true);

        } else if(index == TAB_REGISTER_DOMAIN) {
            this.tab1LeftPane.setVisible(false);         this.tab1LeftPane.setPrefHeight(0);
            this.tabLeftHandOfMask.setVisible(false);   this.tabLeftHandOfMask.setPrefHeight(0);
            this.tab2LeftPane1.setVisible(true);       this.tab2LeftPane1.setPrefHeight(-1);
            this.tab2LeftPane3.setVisible(false);       this.tab2LeftPane3.setPrefHeight(0);

            this.tab1RightPane.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.tabRightHandOverReceiptPane.setVisible(false);

            this.publicDescGrid.setVisible(false);
            this.commercialDescGrid.setVisible(true);

            initStyleSideTab(0);
        }
    }
    public void initStyleSideTab(int index){
        if(index == 0) {
            //Commercial domain
            this.commercialDescGrid.setVisible(true);
            this.publicDescGrid.setVisible(false);
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 14px;");
            this.sideTabLinePane1.setVisible(true);
            this.sideTabLabel2.setTextFill(Color.web("#999999"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size: 14px;");
            this.sideTabLinePane2.setVisible(false);
            this.domainRequestBtn.setVisible(false);

        } else if(index == 1) {
            //Public domain
            this.commercialDescGrid.setVisible(false);
            this.publicDescGrid.setVisible(true);
            this.sideTabLabel2.setTextFill(Color.web("#910000"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 14px;");
            this.sideTabLinePane2.setVisible(true);
            this.sideTabLabel1.setTextFill(Color.web("#999999"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size: 14px;");

            this.sideTabLinePane1.setVisible(false);
            this.domainRequestBtn.setVisible(true);
        }
    }

    public void settingLayoutData() {
        if(this.tabIndex == TAB_REGISTER_MASK){
            settingLayoutDataRegisterMask();
        }else if(this.tabIndex == TAB_HAND_OVER_MASK){
            settingLayoutDataHandOverAddress();
        }

    }

    public void settingLayoutDataRegisterMask(){
        String address = registerController.getAddress();
        String payerAddress = registerController.getPayerAddress();
        String mask = registerController.getMask();
        String domain = registerController.getDomain();
        Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
        BigInteger value = new BigInteger(""+values[0]);

        receiptController.setAddress(address);
        receiptController.setPayerAddress(payerAddress);
        receiptController.setMask(mask+domain);
        receiptController.setValue(ApisUtil.readableApis(value, ',', true) + " APIS");
    }
    public void settingLayoutDataHandOverAddress(){
        String fromAddress = handOverMaskController.getHandOverFromAddress();
        String toAddress = handOverMaskController.getHandOverToAddress();
        String mask = handOverMaskController.getHandOverFromMask();
        Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
        BigInteger value = new BigInteger(""+values[0]);

        handOverReceiptController.setFromAddress(fromAddress);
        handOverReceiptController.setToAddress(toAddress);
        handOverReceiptController.setMask(mask);
        handOverReceiptController.setValue(ApisUtil.readableApis(value, ',', true) + " APIS");
    }

    public void domainRequestMouseClicked(){
        String domain = this.publicDomainTextField.getText().trim();
        String message = this.publicTextArea.getText().trim();
        String email = this.emailTextField.getText().trim();

        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("domain" , domain);
            params.put("message" , message);
            params.put("email" , email);

            String response = HttpRequestManager.sendRequestPublicDomain(params);
            System.out.println("response > \n" + response);

        }catch (MalformedURLException e){
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 완료 팝업
        PopupManager.getInstance().showMainPopup("popup_success.fxml",1);
    }

    public void update() {
        registerController.update();
        handOverMaskController.update();
        settingLayoutData();
    }
}
