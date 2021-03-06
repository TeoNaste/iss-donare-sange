package donation.client.controllers;

import com.jfoenix.controls.*;
import com.jfoenix.transitions.hamburger.HamburgerBasicCloseTransition;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import donation.client.utils.GUIUtils;
import donation.client.utils.Timer;
import donation.model.*;
import donation.services.IMainService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CenterController extends AbstractController {

    @FXML
    private GridPane paneSendBloodBag;

    @FXML
    private StackPane stackPane;

    @FXML
    private AnchorPane drawerContent;
    @FXML
    private AnchorPane homePane, donationPane, questionnairePane, stockPane, requestsPane, notificationsPane, detailsPane;

    @FXML
    private JFXHamburger menuHamburger;

    @FXML
    private JFXDrawer drawer;

    @FXML
    private Label labelCenterName;
    @FXML
    private Label labelTotalDonationsSoFar, labelActiveBloodRequest;
    @FXML
    private Label labelGroupBR, labelRhBR, labelLeukocytesBR, labelThrombocytesBR, labelPlasmaBR;

    @FXML
    private JFXCheckBox checkBoxHealthy, checkBoxInexFever, checkBoxVaccines, checkBoxSexPartHiv,
            checkBoxSexPartWorker, checkBoxIntraDrugs, checkBoxSexPartChanged, checkBoxTatoos, checkBoxPregnancy,
            checkBoxDetention, checkBoxStroke, checkBoxConvultions, checkBoxSTD, checkBoxRefusedDelayed,
            checkBoxPostDonationAttention, checkBoxUnexpectedWeightLoss, checkBoxDentalTreat,
            checkBoxDrugTreat, checkBoxSexPartIntraDrug, checkBoxMultipleSexPart, checkBoxSexWorker,
            checkBoxSurgery, checkBoxRecievedBlood, checkBoxBLTForeign, checkBoxHepatitis,
            checkBoxITLMalaria, checkBoxHeartDisease, checkBoxAsthma, checkBoxDiabetisUlcerus,
            checkBoxSmoker;
    @FXML
    private JFXCheckBox checkBoxHIVOrAIDS, checkBoxHepatitisDonation, checkBoxSyphilis, checkBoxHTLV;

    @FXML
    private JFXComboBox<ABOBloodGroup> comboBoxBloodGroup;
    @FXML
    private JFXComboBox<RhBloodGroup> comboBoxBloodRh;
    @FXML
    private JFXComboBox<BloodTransfusionCenterProfile> comboBoxCenter;

    @FXML
    private JFXDatePicker datePickerChildBirthday, datePickerLastMenstruation, datePickerLastAlcoholCons, datePickerDonationDate;

    @FXML
    private JFXTextField fieldSearchDonation, fieldSearchQuestionnaire;
    @FXML
    private JFXTextField textFieldNoSexualPart, textFieldForeignLocation, textFieldForeignTime,
            textFieldAlcohol, textFieldAlcoholQuant, textFieldBloodPreasure, textFieldLevelALT;

    @FXML
    private JFXButton buttonHome, buttonDonation, buttonQuestionnaire, buttonStock, buttonRequests, buttonNotifications, buttonCenterAddComponent, buttonCenterSendToAnotherCenter;

    @FXML
    private JFXListView<DonorProfile> listDonationDonors, listQuestionnaireDonors;
    @FXML
    private JFXListView<BloodComponentQuantity> listDetailsBloodStock;
    @FXML
    private JFXListView<String> listNotifications;

    @FXML
    private TableView<BloodComponentQuantity> tableBloodStock;
    @FXML
    private TableColumn<BloodComponentQuantity, String> columnType, columnGroup, columnRh,
            columnQuantity, columnExpDate, columnStatus;

    @FXML
    private TableView<BloodRequest> tableBloodRequestsBR;
    @FXML
    private TableColumn<BloodRequest, String> columnDateBR, columnPatientBR, columnSenderBR, columnStatusBR;
    @FXML
    private TableColumn<BloodRequest, Void> columnActionBR;

    @FXML
    private TableView<BloodComponentQuantity> tableBloodStockRD;
    @FXML
    private TableColumn<BloodComponentQuantity, String> columnTypeRD, columnGroupRD, columnRhRD, columnQuantityRD, columnExpDateRD, columnStatusRD;

    private ObservableList<DonorProfile> modelListDonors = FXCollections.observableArrayList();
    private ObservableList<BloodComponentQuantity> modelBloodStock = FXCollections.observableArrayList();
    private ObservableList<BloodRequest> modelBR = FXCollections.observableArrayList();
    private ObservableList<BloodComponentQuantity> modelDetailsBloodStock = FXCollections.observableArrayList();
    private ObservableList<String> modelNotifications = FXCollections.observableArrayList();
    private ObservableList<BloodComponentQuantity> modelRD = FXCollections.observableArrayList();

    private HamburgerBasicCloseTransition burgerTask;
    private BloodRequest selectedBloodRequest;


    @FXML
    private void handleDrawer(MouseEvent actionEvent) {
        burgerTask.setRate(burgerTask.getRate() * -1);
        burgerTask.play();
        if (drawer.isClosed()) {
            drawer.setVisible(true);
            drawer.toggle();
            menuHamburger.getStyleClass().add("hamburger-white");
        } else {
            drawer.toggle();
            Timer.setTimeout(() -> drawer.setVisible(false), 400);
            menuHamburger.getStyleClass().remove("hamburger-white");
        }
    }

    @FXML
    private void handleButtonAddComponent(ActionEvent event) {

        BloodComponentQuantity selectedComponent = listDetailsBloodStock.getSelectionModel().getSelectedItem();
        if (selectedComponent == null) return;

        if (selectedBloodRequest.getPlasmaQuantity() == 0 && selectedComponent.getBloodComponent() == BloodComponent.Plasma) {
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Warning", "Plasma was not requested!", stackPane);
            return;
        }

        if (selectedBloodRequest.getThrombocytesQuantity() == 0 && selectedComponent.getBloodComponent() == BloodComponent.Thrombocytes) {
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Warning", "Thrombocytes was not requested!", stackPane);
            return;
        }
        if (selectedBloodRequest.getLeukocytesQuantity() == 0 && selectedComponent.getBloodComponent() == BloodComponent.Leukocytes) {
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Warning", "Leukocytes was not requested!", stackPane);
            return;
        }

        try {
            loadDetailsPane(
                    (selectedBloodRequest = mainService.addComponentToRequest(selectedBloodRequest, selectedComponent))
            );

            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Success", "Component was added successfully", stackPane);
        } catch (Exception e) {
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Error", e.getMessage(), stackPane);
        }
    }

    @FXML
    private void handleSendToAnotherCenter(ActionEvent event) {

        new Thread(() -> {
            try {

                Platform.runLater(() -> {
                    buttonCenterSendToAnotherCenter.setText("Sending...");
                    buttonCenterSendToAnotherCenter.setDisable(true);
                });

                try {
                    mainService.sendRequestToAnotherCenter(selectedBloodRequest, username);
                } catch (NullPointerException e) {
                    Platform.runLater(() -> {
                        GUIUtils.showDialogMessage(Alert.AlertType.ERROR, "Error", "You must be connected to the internet!", stackPane);
                    });
                    return;
                }

                Platform.runLater(() -> {
                    GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Information", "Request was sent to the nearest center!", stackPane);
                    toggleView(requestsPane, buttonRequests);
                    loadRequests();
                });

            } catch (Exception e) {
                Platform.runLater(() -> GUIUtils.showDialogMessage(Alert.AlertType.ERROR, "Error", e.getMessage(), stackPane));
                e.printStackTrace();

            } finally {
                Platform.runLater(() -> {
                    buttonCenterSendToAnotherCenter.setDisable(false);
                    buttonCenterSendToAnotherCenter.setText("Send to another center");
                });
            }
        }).start();
    }

    @FXML
    private void handleButtonHome(ActionEvent actionEvent) {
        toggleView(homePane, buttonHome);
        handleDrawer(null);
    }

    @FXML
    private void handleButtonDonation(ActionEvent actionEvent) {
        toggleView(donationPane, buttonDonation);
        handleDrawer(null);
        initListDonors("");
    }

    @FXML
    private void handleButtonQuestionnaire(ActionEvent actionEvent) {
        toggleView(questionnairePane, buttonQuestionnaire);
        handleDrawer(null);
        initListDonors("");
    }

    @FXML
    private void handleButtonStock(ActionEvent actionEvent) {
        toggleView(stockPane, buttonStock);
        setModelBloodStock();
        handleDrawer(null);
    }

    @FXML
    private void handleButtonRequests(ActionEvent actionEvent) {
        toggleView(requestsPane, buttonRequests);
        loadRequests();
        handleDrawer(null);
    }

    @FXML
    private void handleButtonNotifications(ActionEvent actionEvent) {
        toggleView(notificationsPane, buttonNotifications);
        handleDrawer(null);
    }

    @FXML
    private void handleButtonSubmitDonation(ActionEvent event) {

        Donation donation = new Donation();

        try {
            buildDonation(donation);
            mainService.addDonation(getUsername(), donation);
            initLabels();
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Success", "Donation was added successfully!", stackPane);
        } catch (Exception e) {
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Error", e.getMessage(), stackPane);
        }

    }

    @FXML
    private void handleButtonSubmitQuestionnaire(ActionEvent actionEvent) {

        MedicalQuestionnaire questionnaire = new MedicalQuestionnaire();

        DonorProfile donorProfile = listQuestionnaireDonors.getSelectionModel().getSelectedItem();

        if (donorProfile == null) {
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Error", "You must select a donor!", stackPane);
            return;
        }

        try {
            buildMedicalQuestionnaire(questionnaire, donorProfile);

            if (
                    questionnaire.isInexplicableFever() ||
                            questionnaire.isSexualPartnerHIVHepatitis() ||
                            questionnaire.isSexualPartnerSexWorker() ||
                            questionnaire.isIntravenousDrugs() ||
                            questionnaire.isTattoos() ||
                            questionnaire.isStroke() ||
                            questionnaire.isConvulsionsNervousDisease() ||
                            questionnaire.isSTD() ||
                            questionnaire.isSexualPartnerIntravenousDrug() ||
                            questionnaire.isSexWorker() ||
                            questionnaire.isSurgery() ||
                            questionnaire.isHepatitis() ||
                            questionnaire.isIcterusTuberculosisReumaticFeverMalaria() ||
                            questionnaire.isHeartDiseaseHLBloodPressure() ||
                            questionnaire.isDiabetisUlcerCancer()
                    ) {
                throw new Exception("Based on questionnaire, the person is not eligible for donation.");
            }

            mainService.addMedicalQuestionnaire(questionnaire);
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Success", "Questionnaire was added successfully", stackPane);
        } catch (Exception e) {
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Error", e.getMessage(), stackPane);
        }
    }

    @FXML
    private void handleButtonSendBloodBag(ActionEvent actionEvent) {
        try {

            BloodComponentQuantity bloodBag = tableBloodStock.getSelectionModel().getSelectedItem();
            bloodBag.setIDTransfusionCenter(comboBoxCenter.getSelectionModel().getSelectedItem().getIdUser());
            mainService.updateBloodComponentQuantity(bloodBag);
            handleButtonStock(null);
            handleDrawer(null);
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Success", "Blood bag was sent succesfully!", stackPane);
        } catch (Exception e) {
            GUIUtils.showDialogMessage(Alert.AlertType.ERROR, "Error", "Could not send blood bag to center. \n" + e.getMessage(), stackPane);
        }
    }


    private void initTableBr() {

        columnDateBR.setCellValueFactory(request -> new SimpleStringProperty(request.getValue().getDateRequested().toString().split(" ")[0]));
        columnPatientBR.setCellValueFactory(new PropertyValueFactory<>("patientName"));

        // TODO De pus nume in loc de username la reciever

        columnSenderBR.setCellValueFactory(request ->
                new SimpleStringProperty(request.getValue().getSender().getUsername())
        );

        columnStatusBR.setCellValueFactory(new PropertyValueFactory<>("bloodRequestStatus"));

        columnActionBR.setCellFactory(param -> new TableCell<BloodRequest, Void>() {
            private JFXButton detailsButton = new JFXButton();
            HBox pane = new HBox(detailsButton);

            {
                FontAwesomeIconView iconDetails = new FontAwesomeIconView();
                iconDetails.setIcon(FontAwesomeIcon.PENCIL);
                iconDetails.setFont(new Font(20));

                pane.setAlignment(Pos.CENTER);
                pane.setSpacing(10d);
                detailsButton.setId("ActionButton");
                detailsButton.getStyleClass().add("btn");
                detailsButton.setTooltip(new Tooltip("Edit request"));
                detailsButton.setText("Edit");

                detailsButton.setOnAction(event -> {
                    BloodRequest request = (BloodRequest) getTableRow().getItem();
                    loadDetailsPane((selectedBloodRequest = request));
                    toggleView(detailsPane, null);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : detailsButton);
            }
        });

        tableBloodRequestsBR.setItems(modelBR);
    }

    private void initTableRd() {

        columnTypeRD.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getBloodComponent().toString()));
        columnGroupRD.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getAboBloodGroup().toString()));
        columnRhRD.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getRhBloodGroup().toString()));
        columnQuantityRD.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getQuantity() + ""));
        columnExpDateRD.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getExpirationDate().toString().split(" ")[0]));
        columnStatusRD.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getBloodStatus().toString()));

        tableBloodStockRD.setItems(modelRD);
    }

    private void loadTableBloodStockRD(BloodRequest request) {
        modelRD.setAll(
                mainService.getBloodComponentsByRequest(request)
        );
    }

    private void loadDetailsPane(BloodRequest request) {
        setModelDetailsBR(request);

        labelPlasmaBR.setText(request.getPlasmaQuantity() + " ml");
        labelThrombocytesBR.setText(request.getThrombocytesQuantity() + " ml");
        labelLeukocytesBR.setText(request.getLeukocytesQuantity() + " ml");
        labelGroupBR.setText(request.getBloodGroup().toString());
        labelRhBR.setText(request.getRhBloodGroup().toString());

        loadTableBloodStockRD(request);
    }

    private void loadRequests() {
        modelBR.setAll(mainService.getBloodRequestsCenter(username));
    }

    private void initListDetailsBR() {

        listDetailsBloodStock.setOnMouseClicked(event -> buttonCenterAddComponent.setDisable(false));

        listDetailsBloodStock.setCellFactory(param -> new ListCell<BloodComponentQuantity>() {
            @Override
            public void updateItem(BloodComponentQuantity item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) setText(null);
                else {
                    setText(item.getBloodComponent() + ": " + item.getQuantity() + "ml  Expiration Date: " + item.getExpirationDate().toString().split(" ")[0]);
                }
            }
        });

        listDetailsBloodStock.setItems(modelDetailsBloodStock);
    }

    private void setModelDetailsBR(BloodRequest request) {
        modelDetailsBloodStock.setAll(
                mainService.getBloodStock(username).stream()
                        .filter(
                                br -> br.getBloodStatus() == BloodStatus.Valid &&
                                        br.getAboBloodGroup() == request.getBloodGroup() &&
                                        br.getRhBloodGroup() == request.getRhBloodGroup() &&
                                        br.getQuantity() > 0
                        )
                        .collect(Collectors.toList())
        );
    }

    private void initTableBloodStock() {
        columnType.setCellValueFactory(new PropertyValueFactory<>("bloodComponent"));
        columnGroup.setCellValueFactory(new PropertyValueFactory<>("aboBloodGroup"));
        columnQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        columnRh.setCellValueFactory(new PropertyValueFactory<>("rhBloodGroup"));
        columnStatus.setCellValueFactory(new PropertyValueFactory<>("bloodStatus"));
        columnExpDate.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));

        tableBloodStock.getSelectionModel().selectedItemProperty().addListener((o, n, m) ->
                paneSendBloodBag.setDisable(
                        tableBloodStock.getSelectionModel().getSelectedItem() == null ||
                                tableBloodStock.getSelectionModel().getSelectedItem().getBloodStatus() == BloodStatus.NotValid
                )
        );
        tableBloodStock.setItems(modelBloodStock);
    }

    private void setModelBloodStock() {
        modelBloodStock.setAll(mainService.getBloodStock(username));
    }

    private void initLabels() {
        labelTotalDonationsSoFar.setText(mainService.getAllDonationsForCenter(getUsername()) + "");
        labelActiveBloodRequest.setText(mainService.getBloodRequestsCenter(getUsername()).stream().filter(x -> x.getBloodRequestStatus() == BloodRequestStatus.Waiting).count() + "");
    }

    private void initComboBoxes() {
        comboBoxBloodGroup.getItems().addAll(ABOBloodGroup.values());
        comboBoxBloodRh.getItems().addAll(RhBloodGroup.values());

        List<BloodTransfusionCenterProfile> centers = mainService
                .getAllTransfusionCenterProfiles()
                .stream()
                .filter(p -> p.getID() != mainService.getTransfusionCenterProfile(username).getID())
                .collect(Collectors.toList());
        comboBoxCenter.getItems().addAll(centers);
    }

    private void initSearchFields() {
        fieldSearchDonation.textProperty().addListener(o -> handleSearchTextChanged(fieldSearchDonation));
        fieldSearchQuestionnaire.textProperty().addListener(o -> handleSearchTextChanged(fieldSearchQuestionnaire));
    }

    private void initListDonors() {
        listDonationDonors.setCellFactory(param -> new ListCell<DonorProfile>() {
            @Override
            public void updateItem(DonorProfile item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) setText(null);
                else {
                    setText(item.getFirstName() + " " + item.getLastName() + " - " + item.getCNP());
                }
            }
        });
        listQuestionnaireDonors.setCellFactory(listDonationDonors.getCellFactory());

        listDonationDonors.setItems(modelListDonors);
        listQuestionnaireDonors.setItems(modelListDonors);
    }

    private void initListNotifications() {

        listNotifications.setOnMouseClicked(ev -> {
            mainService.removeNotificationFromDonor(getUsername(), listNotifications.getSelectionModel().getSelectedItem());
            modelNotifications.remove(listNotifications.getSelectionModel().getSelectedItem());
        });

        listNotifications.setItems(modelNotifications);
    }

    private void initListDonors(String keyword) {
        modelListDonors.setAll(mainService.getDonorProfiles(keyword));
    }

    private void toggleView(AnchorPane viewToShow, JFXButton buttonClicked) {
        homePane.setVisible(false);
        donationPane.setVisible(false);
        questionnairePane.setVisible(false);
        requestsPane.setVisible(false);
        stockPane.setVisible(false);
        notificationsPane.setVisible(false);
        detailsPane.setVisible(false);

        buttonDonation.setDisable(false);
        buttonHome.setDisable(false);
        buttonNotifications.setDisable(false);
        buttonQuestionnaire.setDisable(false);
        buttonRequests.setDisable(false);
        buttonStock.setDisable(false);
        buttonCenterAddComponent.setDisable(true);

        viewToShow.setVisible(true);
        if (buttonClicked != null)
            buttonClicked.setDisable(true);
    }

    private void handleSearchTextChanged(JFXTextField searchBar) {

        if (searchBar.getText() != null || Objects.equals(searchBar.getText(), "")) {
            initListDonors(searchBar.getText());
            return;
        }

        initListDonors("");
    }

    private void buildMedicalQuestionnaire(MedicalQuestionnaire questionnaire, DonorProfile donorProfile) throws Exception {

        // First column
        questionnaire.setHealthy(checkBoxHealthy.isSelected());
        questionnaire.setInexplicableFever(checkBoxInexFever.isSelected());
        questionnaire.setVaccines(checkBoxVaccines.isSelected());
        questionnaire.setSexualPartnerHIVHepatitis(checkBoxSexPartHiv.isSelected());
        questionnaire.setSexualPartnerSexWorker(checkBoxSexPartWorker.isSelected());
        questionnaire.setIntravenousDrugs(checkBoxIntraDrugs.isSelected());
        questionnaire.setSexualPartnerChanged(checkBoxSexPartChanged.isSelected());
        questionnaire.setTattoos(checkBoxTatoos.isSelected());
        questionnaire.setPregnancy(checkBoxPregnancy.isSelected());
        questionnaire.setDetention(checkBoxDetention.isSelected());
        questionnaire.setStroke(checkBoxStroke.isSelected());
        questionnaire.setConvulsionsNervousDisease(checkBoxConvultions.isSelected());
        questionnaire.setSTD(checkBoxSTD.isSelected());
        questionnaire.setBeenRefusedDelayed(checkBoxRefusedDelayed.isSelected());
        questionnaire.setPostDonationAttention(checkBoxPostDonationAttention.isSelected());

        questionnaire.setNumberOfSexualPartners(
                textFieldNoSexualPart.getText().equals("") ? 0 : Integer.parseInt(textFieldNoSexualPart.getText())
        );

        questionnaire.setbLTFWhere(textFieldForeignLocation.getText());

        questionnaire.setbLTFWhen(textFieldForeignTime.getText());

        questionnaire.setAlcohol(textFieldAlcohol.getText());

        questionnaire.setAlcoholQuantity(
                textFieldAlcoholQuant.getText().equals("") ? 0 : Integer.parseInt(textFieldAlcoholQuant.getText())
        );


        //Second column
        questionnaire.setUnexpectedLossWeight(checkBoxUnexpectedWeightLoss.isSelected());
        questionnaire.setDentalTreatment(checkBoxDentalTreat.isSelected());
        questionnaire.setDrugTreatment(checkBoxDrugTreat.isSelected());
        questionnaire.setSexualPartnerIntravenousDrug(checkBoxSexPartIntraDrug.isSelected());
        questionnaire.setMultipleSexualPartners(checkBoxMultipleSexPart.isSelected());
        questionnaire.setSexWorker(checkBoxSexWorker.isSelected());
        questionnaire.setSurgery(checkBoxSurgery.isSelected());
        questionnaire.setReceivedBlood(checkBoxRecievedBlood.isSelected());
        questionnaire.setBornLivedTravelledForeign(checkBoxBLTForeign.isSelected());
        questionnaire.setHepatitis(checkBoxHepatitis.isSelected());
        questionnaire.setIcterusTuberculosisReumaticFeverMalaria(checkBoxITLMalaria.isSelected());
        questionnaire.setHeartDiseaseHLBloodPressure(checkBoxHeartDisease.isSelected());
        questionnaire.setAsthma(checkBoxAsthma.isSelected());
        questionnaire.setDiabetisUlcerCancer(checkBoxDiabetisUlcerus.isSelected());
        questionnaire.setSmoker(checkBoxSmoker.isSelected());

        if (datePickerChildBirthday.getValue() != null)
            questionnaire.setBirthdayOfChild(Date.valueOf(datePickerChildBirthday.getValue()));
        if (datePickerLastMenstruation.getValue() != null)
            questionnaire.setLastMenstruation(Date.valueOf(datePickerLastMenstruation.getValue()));
        if (datePickerLastAlcoholCons.getValue() != null)
            questionnaire.setLastAlcoholConsume(Date.valueOf(datePickerLastAlcoholCons.getValue()));

        if (textFieldBloodPreasure.getText().equals("")) throw new Exception("You must enter the blood pressure!");

        questionnaire.setBloodPressure(textFieldBloodPreasure.getText());
        questionnaire.setIdUser(donorProfile.getIdUser());
        questionnaire.setDate(Date.valueOf(LocalDate.now()));
    }

    private void buildDonation(Donation donation) throws Exception {

        if (datePickerDonationDate.getValue() == null || datePickerDonationDate.getValue().compareTo(LocalDate.now()) > 0)
            throw new Exception("You must enter a valid donation date!");

        if (textFieldLevelALT.getText().equals("")) throw new Exception("You must specify the ALT level!");

        if (textFieldLevelALT.getText().equals("")) throw new Exception("The ALT level must be specified!");

        if (comboBoxBloodRh.getValue() == null) throw new Exception("You must specify the Blood Group Rh!");

        if (comboBoxBloodGroup.getValue() == null) throw new Exception("You must specify the Blood Group");

        donation.setHepatitis(checkBoxHepatitisDonation.isSelected());
        donation.setHIVorAIDS(checkBoxHIVOrAIDS.isSelected());
        donation.setSyphilis(checkBoxSyphilis.isSelected());
        donation.setHTLV(checkBoxHTLV.isSelected());

        donation.setDonationDate(Date.valueOf(datePickerDonationDate.getValue()));

        try {
            donation.setLevelALT(Integer.parseInt(textFieldLevelALT.getText()));
        } catch (Exception e) {
            throw new Exception("You must enter an name in ALT field");
        }

        donation.setDonationStatus(DonationStatus.Taking);

        if (listDonationDonors.getSelectionModel().getSelectedItem() == null)
            throw new Exception("You must select a donor in order to continue!");

        User user = mainService.getUserById(listDonationDonors.getSelectionModel().getSelectedItem().getIdUser());
        donation.setDonor(user);

        DonorProfile donorProfile = mainService.getProfile(user.getUsername());
        donorProfile.setAboBloodGroup(comboBoxBloodGroup.getValue());
        donorProfile.setRhBloodGroup(comboBoxBloodRh.getValue());

        mainService.updateProfile(user.getUsername(), donorProfile);
    }


    @Override
    public void setMainService(IMainService mainService, String username, Stage stageLogin) {
        super.setMainService(mainService, username, stageLogin);
        initLabels();
        initComboBoxes();
        labelCenterName.setText(mainService.getTransfusionCenterProfile(username).getAddress());
    }

    @Override
    public void reloadCenterView() throws RemoteException {

        new Thread(() -> {
            try {
                Platform.runLater(() -> GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "INFORMATION", "Blood request was fulfilled", stackPane));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                toggleView(requestsPane, buttonRequests);
                loadRequests();
            });
        }).start();
    }

    @Override
    public void notifyNewRequestAdded(String username, String message) throws RemoteException {
        Platform.runLater(() -> {
            GUIUtils.showSnackBar("You have a new notification", stackPane);
            loadRequests();
            modelBR.setAll(mainService.getBloodRequestsCenter(getUsername()));
            modelNotifications.add(message);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        burgerTask = new HamburgerBasicCloseTransition(menuHamburger);
        burgerTask.setRate(-1);
        drawer.setSidePane(drawerContent);
        drawerContent.setVisible(true);

        initListDonors();
        initListNotifications();
        initSearchFields();
        initTableBloodStock();
        initListDetailsBR();
        initTableBr();
        initTableRd();

        toggleView(homePane, buttonHome);
    }
}
