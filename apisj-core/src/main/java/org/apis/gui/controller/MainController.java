package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apis.gui.controller.addressmasking.AddressMaskingController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.TabMenuController;
import org.apis.gui.controller.popup.PopupRestartController;
import org.apis.gui.controller.popup.PopupSyncController;
import org.apis.gui.manager.*;
import org.apis.gui.model.MainModel;
import org.apis.gui.model.TokenModel;
import org.apis.util.blockchain.ApisUtil;

import java.net.URL;
import java.util.*;

public class MainController extends BaseViewController {
    private final int LAYER_POPUP_ADDRESS_INFO = 0;
    private int layerPopupType = -1;
    private boolean isShowLayerPopup = false;

    @FXML private TabPane tabPane;
    @FXML private AnchorPane tabWalletPane, tabTransferPane, tabSmartContractPane, tabAddressMaskingPane, tabTransactionPane;
    @FXML private GridPane popupLayout0, popupLayout1, popupLayout2, popupLayout3, popupLayout4;
    @FXML private Label totalNatural, totalUnit, peer, block, timestemp;
    @FXML private ComboBox selectLanguage, footerSelectTotalUnit;
    @FXML private ImageView btnAddressInfo, btnSetting, icAddressInfo, icSetting, logo;
    @FXML private VBox alertList;
    @FXML private Label mainFooterTotal, mainFooterPeers, mainFooterTimer;
    @FXML private TabMenuController tabMenuController;
    @FXML private AnchorPane layerPopupAddressInfoPane, layerPopupPane, layerPopupAnchor2, rootPane;
    @FXML private AddressInfoController addressInfoController;
    @FXML private GridPane addressInfoPane;

    private MainTab selectedIndex = MainTab.WALLET;
    private Image imageAddressInfo = ImageManager.btnAddressInfo;
    private Image imageAddressInfoHover = ImageManager.btnAddressInfoHover;
    private Image icCircleHalfShow = ImageManager.icCircleHalfShow;
    private Image icCircleHalfHover = ImageManager.icCircleHalfHover;
    private Image imageSetting = ImageManager.btnSetting;
    private Image imageSettingHover = ImageManager.btnSettingHover;
    private String cursorPane;

    private MainModel mainModel = new MainModel();
    private PopupSyncController syncController;
    private PopupRestartController restartController;

    // 이전 마이닝/마스터노드 참여(혹은 시도) 하던 지갑주소
    String miningAddress = AppManager.getGeneralPropertiesData("mining_address");
    String masternodeAddress = AppManager.getGeneralPropertiesData("masternode_address");

    private final String commonCssURL = "scene/css/common.css";
    private final String commonKrCssURL = "scene/css/common_kr.css";
    private final String commonScCssURL = "scene/css/common_sc.css";
    private final String commonTcCssURL = "scene/css/common_tc.css";

    public MainController(){ }

    public void initLayoutFooter(){
        this.totalUnit.setText("APIS");
        this.peer.textProperty().bind(mainModel.peerProperty());
        this.block.textProperty().bind(mainModel.blockProperty());
        this.timestemp.textProperty().bind(mainModel.timestempProperty());

        ObservableList<String> langOptions = FXCollections.observableArrayList( "English", "한국어", "日本語", "简体中文", "繁體中文");
        selectLanguage.setItems(langOptions);
        selectLanguage.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(!rootPane.getStylesheets().contains(commonCssURL)) {
                    rootPane.getStylesheets().remove(commonKrCssURL);
                    rootPane.getStylesheets().remove(commonScCssURL);
                    rootPane.getStylesheets().remove(commonTcCssURL);
                    rootPane.getStylesheets().add(commonCssURL);
                }

                if(newValue.equals("English")){
                    AppManager.saveGeneralProperties("language", "English");
                    StringManager.getInstance().changeBundleEng();

                }else if(newValue.equals("한국어")){
                    if(!rootPane.getStylesheets().contains(commonKrCssURL)) {
                        rootPane.getStylesheets().remove(commonCssURL);
                        rootPane.getStylesheets().add(commonKrCssURL);
                    }
                    AppManager.saveGeneralProperties("language", "한국어");
                    StringManager.getInstance().changeBundleKor();

                }else if(newValue.equals("简体中文")){
                    if(!rootPane.getStylesheets().contains(commonScCssURL)) {
                        rootPane.getStylesheets().remove(commonCssURL);
                        rootPane.getStylesheets().add(commonScCssURL);
                    }
                    AppManager.saveGeneralProperties("language", "简体中文");
                    StringManager.getInstance().changeBundleChn();

                }else if(newValue.equals("繁體中文")) {
                    if(!rootPane.getStylesheets().contains(commonTcCssURL)) {
                        rootPane.getStylesheets().remove(commonCssURL);
                        rootPane.getStylesheets().add(commonTcCssURL);
                    }
                    AppManager.saveGeneralProperties("language", "繁體中文");
                    StringManager.getInstance().changeBundleTwn();

                }else if(newValue.equals("日本語")){
                    AppManager.saveGeneralProperties("language", "日本語");
                    StringManager.getInstance().changeBundleJpn();
                }

                // Update font css
                if (AppManager.getInstance().guiFx.getMain() != null) {
                    MainController.MainTab selectedIndex = AppManager.getInstance().guiFx.getMain().getSelectedIndex();
                    AppManager.getInstance().guiFx.getMain().fontUpdate();

                    switch (selectedIndex) {
                        case WALLET:
                            if (AppManager.getInstance().guiFx.getWallet() != null) {
                                AppManager.getInstance().guiFx.getWallet().fontUpdate();
                            }
                            break;
                        case TRANSFER:
                            if (AppManager.getInstance().guiFx.getTransfer() != null) {
                                AppManager.getInstance().guiFx.getTransfer().fontUpdate();
                            }
                            break;
                        case SMART_CONTRECT:
                            if (AppManager.getInstance().guiFx.getSmartContract() != null)
                                AppManager.getInstance().guiFx.getSmartContract().fontUpdate();
                            break;
                        case ADDRESS_MASKING:
                            if (AppManager.getInstance().guiFx.getAddressMasking() != null) {
                                AppManager.getInstance().guiFx.getAddressMasking().fontUpdate();
                            }
                            break;
                        case TRANSACTION:
                            if (AppManager.getInstance().guiFx.getTransactionNative() != null) {
                                AppManager.getInstance().guiFx.getTransactionNative().fontUpdate();
                            }
                            break;
                    }
                }
            }
        });
        selectLanguage.setValue(AppManager.getGeneralPropertiesData("language"));

        ObservableList<FooterTotalModel> footOptions = FXCollections.observableArrayList();
        for(TokenModel token : AppManager.getInstance().getTokens()){
            footOptions.add(new FooterTotalModel(token.getTokenAddress(), token.getTokenName()));
        }

        footerSelectTotalUnit.setItems(footOptions);
        footerSelectTotalUnit.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                FooterTotalModel data = (FooterTotalModel)newValue;
                setFooterTotalData(data);

            }
        });

        AppManager.getInstance().guiFx.hideLoadingStage();
    }
    public void selectedHeader(MainTab index){
        this.selectedIndex = index;
        this.tabMenuController.stateActive(index.num);

        // change tab pane
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(this.selectedIndex.num);
        switch (this.selectedIndex){
            case WALLET :
//                if(AppManager.getInstance().guiFx.getWallet() == null){
//                    try {
//                        BaseFxmlController fxmlController = new BaseFxmlController("wallet/wallet.fxml");
//                        Node node = fxmlController.getNode();
//                        AppManager.getInstance().guiFx.setWallet((WalletController)fxmlController.getController());
//                        tabWalletPane.getChildren().clear();
//                        tabWalletPane.getChildren().add(node);
//                        AnchorPane.setTopAnchor(node, 0.0);
//                        AnchorPane.setRightAnchor(node, 0.0);
//                        AnchorPane.setBottomAnchor(node, 0.0);
//                        AnchorPane.setLeftAnchor(node, 0.0);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                AppManager.getInstance().guiFx.getWallet().update();
                break;
            case TRANSFER :
//                if(AppManager.getInstance().guiFx.getTransfer() == null){
//                    try {
//                        BaseFxmlController fxmlController = new BaseFxmlController("transfer/transfer.fxml");
//                        Node node = fxmlController.getNode();
//                        AppManager.getInstance().guiFx.setTransfer((TransferController)fxmlController.getController());
//                        tabTransferPane.getChildren().clear();
//                        tabTransferPane.getChildren().add(node);
//                        AnchorPane.setTopAnchor(node, 0.0);
//                        AnchorPane.setRightAnchor(node, 0.0);
//                        AnchorPane.setBottomAnchor(node, 0.0);
//                        AnchorPane.setLeftAnchor(node, 0.0);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                AppManager.getInstance().guiFx.getTransfer().update();
                break;
            case SMART_CONTRECT :
//                if(AppManager.getInstance().guiFx.getSmartContract() == null){
//                    try {
//                        BaseFxmlController fxmlController = new BaseFxmlController("smartcontract/smart_contract.fxml");
//                        Node node = fxmlController.getNode();
//                        AppManager.getInstance().guiFx.setSmartContract((SmartContractController)fxmlController.getController());
//                        tabSmartContractPane.getChildren().clear();
//                        tabSmartContractPane.getChildren().add(node);
//                        AnchorPane.setTopAnchor(node, 0.0);
//                        AnchorPane.setRightAnchor(node, 0.0);
//                        AnchorPane.setBottomAnchor(node, 0.0);
//                        AnchorPane.setLeftAnchor(node, 0.0);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                AppManager.getInstance().guiFx.getSmartContract().update();
                break;
            case ADDRESS_MASKING :
//                if(AppManager.getInstance().guiFx.getAddressMasking() == null){
//                    try {
//                        BaseFxmlController fxmlController = new BaseFxmlController("addressmasking/address_masking.fxml");
//                        Node node = fxmlController.getNode();
//                        AppManager.getInstance().guiFx.setAddressMasking((AddressMaskingController) fxmlController.getController());
//                        tabAddressMaskingPane.getChildren().clear();
//                        tabAddressMaskingPane.getChildren().add(node);
//                        AnchorPane.setTopAnchor(node, 0.0);
//                        AnchorPane.setRightAnchor(node, 0.0);
//                        AnchorPane.setBottomAnchor(node, 0.0);
//                        AnchorPane.setLeftAnchor(node, 0.0);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                AppManager.getInstance().guiFx.getAddressMasking().update();
                AppManager.getInstance().guiFx.getAddressMasking().initStyleTab(AddressMaskingController.TAB_MENU);
                break;
            case TRANSACTION :
//                if(AppManager.getInstance().guiFx.getTransactionNative() == null){
//                    try {
//                        BaseFxmlController fxmlController = new BaseFxmlController("transaction/transaction_native.fxml");
//                        Node node = fxmlController.getNode();
//                        AppManager.getInstance().guiFx.setTransactionNative((TransactionNativeController) fxmlController.getController());
//                        tabTransactionPane.getChildren().clear();
//                        tabTransactionPane.getChildren().add(node);
//                        AnchorPane.setTopAnchor(node, 0.0);
//                        AnchorPane.setRightAnchor(node, 0.0);
//                        AnchorPane.setBottomAnchor(node, 0.0);
//                        AnchorPane.setLeftAnchor(node, 0.0);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                AppManager.getInstance().guiFx.getTransactionNative().hideDetail();
                AppManager.getInstance().guiFx.getTransactionNative().update();
                break;
        }
    }
    public void setFooterTotalData(FooterTotalModel data){
        if(data == null){
            totalNatural.setText("0.00000000");
            totalUnit.setText("APIS");
        }else {
            if (data.tokenAddress.equals("-1")) {
                totalNatural.setText(ApisUtil.readableApis(AppManager.getInstance().getTotalApis(), ',', true));
                totalUnit.setText("APIS");
            } else if (data.tokenAddress.equals("-2")) {
                totalNatural.setText(ApisUtil.readableApis(AppManager.getInstance().getTotalMineral(), ',', true));
                totalUnit.setText("MNR");
            } else {
                totalNatural.setText(ApisUtil.readableApis(AppManager.getInstance().getTotalTokenValue(data.tokenAddress), ',', true));
                totalUnit.setText(AppManager.getInstance().getTokenSymbol(data.tokenAddress));
            }
        }
    }

    public void setBlock(long myBestBlock, long worldBestBlock){
        mainModel.setBlock(myBestBlock, worldBestBlock);
    }

    public void setTimestemp(long timeStemp, long nowStemp){
        mainModel.setTimestemp(timeStemp, nowStemp);
    }

    public void setPeer(long peer){
        mainModel.setPeer(""+peer);
    }

    public void init(){
        int size = AppManager.getInstance().getKeystoreList().size();
        if (size <= 0) {

        }

        if(AppManager.getInstance().isSyncDone()){

        }else{
            if(AppManager.getGeneralPropertiesData("network_id").equals("1")) {
                syncController = (PopupSyncController)PopupManager.getInstance().showMainPopup(null,"popup_sync.fxml", 0);
            }
        }
    }

    public void showLayerPopup(int index){
        this.layerPopupType = index;
        layerPopupPane.setVisible(true);

        if(index == LAYER_POPUP_ADDRESS_INFO){
            layerPopupAddressInfoPane.setVisible(true);
            layerPopupAddressInfoPane.setPrefHeight(0);
            icAddressInfo.setVisible(true);
            icAddressInfo.setImage(icCircleHalfShow);
            addressInfoController.requestFocus();

            isShowLayerPopup = true;
        }
    }

    public void hideLayerPopup(){
        layerPopupType = -1;
        layerPopupPane.setVisible(false);
        icAddressInfo.setVisible(false);
        icSetting.setVisible(false);
        isShowLayerPopup = false;
    }

    public void onMouseClickedSetting(){
        PopupManager.getInstance().showMainPopup(null,"setting.fxml", -1);
    }
    public void onMouseClickedAddressInfo(){
        if(isShowLayerPopup){
            hideLayerPopup();
        }else{
            showLayerPopup(LAYER_POPUP_ADDRESS_INFO);
        }
    }
    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();

    }

    @FXML
    public void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        cursorPane = id;
        if(id.equals("btnAddressInfo")){
            btnAddressInfo.setImage(imageAddressInfoHover);
            icAddressInfo.setVisible(true);
            if(layerPopupType == LAYER_POPUP_ADDRESS_INFO){
                icAddressInfo.setImage(icCircleHalfShow);
            }else{
                icAddressInfo.setImage(icCircleHalfHover);
            }
        }else if(id.equals("btnSetting")){
            btnSetting.setImage(imageSettingHover);
            icSetting.setVisible(true);
            icSetting.setImage(icCircleHalfHover);
        }
    }
    @FXML
    public void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        cursorPane = null;
        if(id.equals("btnAddressInfo")){
            icAddressInfo.setVisible(this.layerPopupType == LAYER_POPUP_ADDRESS_INFO);
            btnAddressInfo.setImage(imageAddressInfo);
        }else if(id.equals("btnSetting")){
            icSetting.setVisible(false);
            btnSetting.setImage(imageSetting);
        }
    }

    @FXML
    public void onMousePressed(InputEvent event) {
        String id = ((Node) event.getSource()).getId();

        if(id.equals("btnAddressInfo")){
            icAddressInfo.setVisible(true);
            icAddressInfo.setImage(icCircleHalfShow);
        } else if(id.equals("btnSetting")){
            icSetting.setVisible(true);
            icSetting.setImage(icCircleHalfShow);
        }
    }

    @FXML
    public void onMouseReleased(InputEvent event) {
        String id = ((Node) event.getSource()).getId();

        if(id.equals("btnAddressInfo")){
            if(layerPopupType == LAYER_POPUP_ADDRESS_INFO){
                icAddressInfo.setImage(icCircleHalfShow);
            }else{
                icAddressInfo.setImage(icCircleHalfHover);
            }
        } else if(id.equals("btnSetting")){
            icSetting.setImage(icCircleHalfHover);
        }
    }


    @FXML
    private void onClickTabEvent(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("tab1")) {
            selectedHeader(MainTab.WALLET);
        }else if(id.equals("tab2")) {
            selectedHeader(MainTab.TRANSFER);
        }else if(id.equals("tab3")) {
            selectedHeader(MainTab.SMART_CONTRECT);
        }else if(id.equals("tab4")) {
            selectedHeader(MainTab.ADDRESS_MASKING);
        }else if(id.equals("tab5")) {
            selectedHeader(MainTab.TRANSACTION);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        AppManager.getInstance().guiFx.setMain(this);

        // 언어 설정
        languageSetting();

        tabMenuController.setHandler(new TabMenuController.TabMenuImpl() {
            @Override
            public void onMouseClicked(String text, int index) {
                if(index == MainTab.WALLET.num){
                    selectedHeader(MainTab.WALLET);
                }else if(index == MainTab.TRANSFER.num){
                    selectedHeader(MainTab.TRANSFER);
                }else if(index == MainTab.SMART_CONTRECT.num){
                    selectedHeader(MainTab.SMART_CONTRECT);
                }else if(index == MainTab.ADDRESS_MASKING.num){
                    selectedHeader(MainTab.ADDRESS_MASKING);
                }else if(index == MainTab.TRANSACTION.num){
                    selectedHeader(MainTab.TRANSACTION);
                }

                // Address Info Layer Popup Close
                hideLayerPopup();
            }
        });
        tabMenuController.selectedMenu(MainTab.WALLET.num);
        tabMenuController.setFontSize14(40);

        this.tabPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.UP
                        || event.getCode() == KeyCode.DOWN
                        || event.getCode() == KeyCode.LEFT
                        || event.getCode() == KeyCode.RIGHT
                        || (event.getCode() == KeyCode.PAGE_DOWN && event.isControlDown())
                        || (event.getCode() == KeyCode.PAGE_UP && event.isControlDown())) {

                    if(MainController.this.tabPane.isFocused()){
                        event.consume();
                    }else{
                    }
                }
            }
        });

        addressInfoController.setHandler(new AddressInfoController.AddressInfoImpl() {
            @Override
            public void close() {
                hideLayerPopup();
            }
        });
        layerPopupAnchor2.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hideLayerPopup();
            }
        });

        initLayoutFooter();

        PopupManager.getInstance().setMainPopup0(popupLayout0);
        PopupManager.getInstance().setMainPopup1(popupLayout1);
        PopupManager.getInstance().setMainPopup2(popupLayout2);
        PopupManager.getInstance().setMainPopup3(popupLayout3);
        PopupManager.getInstance().setMainPopup4(popupLayout4);

        init();
        hideLayerPopup();

    }
    public void languageSetting() {

        tabMenuController.addItem(StringManager.getInstance().main.tabWallet, MainTab.WALLET.num);
        tabMenuController.addItem(StringManager.getInstance().main.tabTransfer, MainTab.TRANSFER.num);
        tabMenuController.addItem(StringManager.getInstance().main.tabSmartContract, MainTab.SMART_CONTRECT.num);
        tabMenuController.addItem(StringManager.getInstance().main.tabAddressMasking, MainTab.ADDRESS_MASKING.num);
        tabMenuController.addItem(StringManager.getInstance().main.tabTransaction, MainTab.TRANSACTION.num);
        mainFooterTotal.textProperty().bind(StringManager.getInstance().main.footerTotal);
        mainFooterPeers.textProperty().bind(StringManager.getInstance().main.footerPeers);
        mainFooterTimer.textProperty().bind(StringManager.getInstance().main.footerTimer);

        StyleManager.fontStyle(mainFooterTotal, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(footerSelectTotalUnit, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(totalNatural, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(totalUnit, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(peer, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(mainFooterPeers, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(block, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(mainFooterTimer, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(selectLanguage, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size14, StringManager.getInstance().langCode);
    }

    @Override
    public void update(){
        if(footerSelectTotalUnit.getValue() == null) {
            footerSelectTotalUnit.getSelectionModel().select(0);
        }else{
            FooterTotalModel model = (FooterTotalModel)footerSelectTotalUnit.getValue();
            setFooterTotalData(model);
        }
    }

    @Override
    public void fontUpdate() {
        StyleManager.fontStyle(mainFooterTotal, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(footerSelectTotalUnit, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(totalNatural, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(totalUnit, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(peer, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(mainFooterPeers, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(block, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(mainFooterTimer, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size12, StringManager.getInstance().langCode);
        StyleManager.fontStyle(selectLanguage, StyleManager.Standard.SemiBold, StyleManager.AFontSize.Size14, StringManager.getInstance().langCode);

        tabMenuController.fontUpdate();
    }

    public void succesSync(){
        if((this.miningAddress != null && this.miningAddress.length() > 0) || (this.masternodeAddress != null && this.masternodeAddress.length() > 0)){
            String masterNodeAlias = "";
            String masterNodeAddress = "";
            String miningAlias = "";
            String miningAddress = "";

            for(int i=0; i<AppManager.getInstance().getKeystoreExpList().size(); i++){
                String apis = AppManager.getInstance().getKeystoreExpList().get(i).balance.toString();
                if(AppManager.getInstance().getKeystoreExpList().get(i).address.equals(this.miningAddress)){
                    miningAlias = AppManager.getInstance().getKeystoreExpList().get(i).alias;
                    miningAddress = this.miningAddress;

                    this.miningAddress = null;
                    AppManager.saveGeneralProperties("mining_address","");

                }else if(AppManager.getInstance().getKeystoreExpList().get(i).address.equals(this.masternodeAddress)){
                    if(AppManager.getGeneralPropertiesData("masternode_state").equals(Integer.toString(AppManager.MnState.REQUEST_MASTERNODE.num))
                        || AppManager.getGeneralPropertiesData("masternode_state").equals(Integer.toString(AppManager.MnState.MASTERNODE.num))
                        || AppManager.getGeneralPropertiesData("masternode_state").equals(Integer.toString(AppManager.MnState.EMPTY_MASTERNODE.num))) {
                        if(AppManager.getInstance().isMasterNode(this.masternodeAddress)) {
                            masterNodeAlias = AppManager.getInstance().getKeystoreExpList().get(i).alias;
                            masterNodeAddress = this.masternodeAddress;
                        } else {
                            if (apis.equals("50000000000000000000000")
                                || apis.equals("200000000000000000000000")
                                || apis.equals("500000000000000000000000")) {
                                masterNodeAlias = AppManager.getInstance().getKeystoreExpList().get(i).alias;
                                masterNodeAddress = this.masternodeAddress;
                            }
                        }
                    }

                    this.masternodeAddress = null;
                }
            }

            if(AppManager.getInstance().getNetworkId().equals("1")) {
                restartController = (PopupRestartController) PopupManager.getInstance().showMainPopup(null, "popup_restart.fxml", 0);
                restartController.setData(masterNodeAlias, masterNodeAddress, miningAlias, miningAddress);
            }

        }
    }

    public void exitSyncPopup(){
        if(this.syncController != null){
            this.syncController.exit();
            this.syncController = null;
        }
    }

    public void syncSubMessage(long lastBlock, long bestBlock){
        if(this.syncController != null) {
            this.syncController.setSubMessage(lastBlock, bestBlock);
        }
    }

    public MainTab getSelectedIndex(){
        return this.selectedIndex;
    }

    public void setSettingImageUpdate(boolean isUpdate) {
        if(isUpdate) {
            imageSetting = ImageManager.btnSettingUpdate;
            imageSettingHover = ImageManager.btnSettingUpdateHover;
        } else {
            imageSetting = ImageManager.btnSetting;
            imageSettingHover = ImageManager.btnSettingHover;
        }

        if(icSetting.isVisible()) {
            btnSetting.setImage(imageSettingHover);
        } else {
            btnSetting.setImage(imageSetting);
        }
    }


    class FooterTotalModel {
        public String tokenAddress;
        public String tokenName;

        public FooterTotalModel(String tokenAddress, String tokenName){
            this.tokenAddress = tokenAddress;
            this.tokenName = tokenName;
        }

        @Override
        public String toString(){
            return tokenName;
        }
    }


    public enum MainTab {
        /* wallet(main) */ WALLET(0),
        /* transfer */ TRANSFER(1),
        /* smart contract */ SMART_CONTRECT(2),
        /* address masking */ ADDRESS_MASKING(3),
        /* transaction */ TRANSACTION(4);
        int num;
        MainTab(int num) {
            this.num = num;
        }
    }

}
