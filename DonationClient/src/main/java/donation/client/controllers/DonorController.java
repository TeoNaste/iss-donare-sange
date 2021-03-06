package donation.client.controllers;

import com.jfoenix.controls.*;
import com.jfoenix.transitions.hamburger.HamburgerBasicCloseTransition;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import donation.client.utils.GUIUtils;
import donation.client.utils.Timer;
import donation.model.Donation;
import donation.model.DonorProfile;
import donation.services.IMainService;
import donation.utils.IObserver;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ResourceBundle;

public class DonorController extends AbstractController {


    @FXML
    private StackPane stackPaneContent;

    @FXML
    private AnchorPane drawerContent;
    @FXML
    private AnchorPane homePane, donationHistoryPane, personalProfilePane, notificationsPane;

    @FXML
    private Label labelNumeDonator, labelNrNotifications;
    @FXML
    private Label labelDonationsSoFar, labelDaysLeftUntilNextDonation;

    @FXML
    private JFXDatePicker datePickerBirthDate;

    @FXML
    private JFXHamburger menuHamburger;

    @FXML
    private JFXDrawer drawer;

    @FXML
    private JFXCheckBox checkBoxResidenceAddress;

    @FXML
    private JFXTextField textFieldFirstName, textFieldLastName, textFieldCNP,
            textFieldEmail, textFieldPhone, textFieldWeight,
            textFieldHeight, textFieldNationality;

    @FXML
    private JFXTextArea textAreaResidenceAddress, textAreaHomeAddress;

    @FXML
    private JFXButton buttonHome, buttonDonation, buttonProfile, buttonNotifications;

    @FXML
    private JFXListView<String> listNotifications;

    @FXML
    private TableView<Donation> tableDonation;
    @FXML
    private TableColumn<Donation, String> columnDate, columnLevelalt;
    @FXML
    private TableColumn<Donation, Boolean> columnHivoraids, columnHepatitis,
            columnSyphilis, columnHTLV;


    private DonorProfile profile;
    private HamburgerBasicCloseTransition burgerTask;

    private ObservableList<Donation> modelDonation = FXCollections.observableArrayList();
    private ObservableList<String> modelNotifications = FXCollections.observableArrayList();


    @FXML
    private void handleDrawer(MouseEvent actionEvent) {
        labelNrNotifications.setText(String.valueOf(mainService.getNrNotifications(username)));
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
    private void handleUpdateDonorProfile(ActionEvent event) {

        DonorProfile donorProfile = new DonorProfile();

        donorProfile.setFirstName(textFieldFirstName.getText());
        donorProfile.setLastName(textFieldLastName.getText());
        donorProfile.setCNP(textFieldCNP.getText());

        donorProfile.setBirthDate(null);
        if (datePickerBirthDate.getValue() != null)
            donorProfile.setBirthDate(Date.valueOf(datePickerBirthDate.getValue()));

        donorProfile.setEmail(textFieldEmail.getText());
        donorProfile.setPhone(textFieldPhone.getText());
        donorProfile.setWeight(Float.parseFloat(textFieldWeight.getText().equals("") ? "0.0" : textFieldWeight.getText()));
        donorProfile.setHeight(Integer.parseInt(textFieldHeight.getText().equals("") ? "0" : textFieldHeight.getText()));
        donorProfile.setNationality(textFieldNationality.getText());
        donorProfile.setAddress(textAreaHomeAddress.getText());
        donorProfile.setResidence(textAreaHomeAddress.getText());

        if (!checkBoxResidenceAddress.isSelected()) donorProfile.setResidence(textAreaResidenceAddress.getText());

        try {
            getMainService().updateProfile(getUsername(), donorProfile);
            profile = donorProfile;
            labelNumeDonator.setText(profile.getFirstName() + " " + profile.getLastName());
            GUIUtils.showDialogMessage(Alert.AlertType.INFORMATION, "Success", "Account profile modified successfully!", stackPaneContent);
        } catch (Exception e) {
            GUIUtils.showDialogMessage(Alert.AlertType.ERROR, "Error", e.getMessage(), stackPaneContent);
        }
    }

    @FXML
    private void handleButtonHome(ActionEvent actionEvent) {
        toggleView(homePane, buttonHome);
        handleDrawer(null);
    }

    @FXML
    private void handleButtonDonation(ActionEvent actionEvent) {
        toggleView(donationHistoryPane, buttonDonation);
        handleDrawer(null);
        setDonationTableData();
    }

    @FXML
    private void handleButtonProfile(ActionEvent actionEvent) {
        toggleView(personalProfilePane, buttonProfile);
        handleDrawer(null);
    }

    @FXML
    private void handleButtonNotifications(ActionEvent actionEvent) {
        toggleView(notificationsPane, buttonNotifications);
        handleDrawer(null);
    }


    private void initTable() {
        class MyCell extends TableCell<Donation, Boolean> {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✔" : "✘");
                    if (item) {
                        setStyle("-fx-background-color: #b31514; -fx-text-fill: white");
                        setTextFill(Color.WHITE);
                    }
                }
            }
        }

        columnDate.setCellValueFactory(date -> new SimpleStringProperty(date.getValue().getDonationDate().toString().split(" ")[0]));
        columnHepatitis.setCellValueFactory(new PropertyValueFactory<>("hepatitis"));
        columnHivoraids.setCellValueFactory(new PropertyValueFactory<>("HIVorAIDS"));
        columnHTLV.setCellValueFactory(new PropertyValueFactory<>("HTLV"));
        columnLevelalt.setCellValueFactory(new PropertyValueFactory<>("levelALT"));
        columnSyphilis.setCellValueFactory(new PropertyValueFactory<>("syphilis"));

        columnHepatitis.setCellFactory(param -> new MyCell());
        columnHivoraids.setCellFactory(param -> new MyCell());
        columnHTLV.setCellFactory(param -> new MyCell());
        columnSyphilis.setCellFactory(param -> new MyCell());

        tableDonation.setItems(modelDonation);
    }

    private void initList() {

        listNotifications.setOnMouseClicked(ev -> {
            mainService.removeNotificationFromDonor(getUsername(), listNotifications.getSelectionModel().getSelectedItem());
            modelNotifications.remove(listNotifications.getSelectionModel().getSelectedItem());
        });

        listNotifications.setItems(modelNotifications);
    }

    private void initProfile() {
        checkBoxResidenceAddress.setOnAction(ev -> {
            if (checkBoxResidenceAddress.isSelected()) textAreaResidenceAddress.setDisable(true);
            else textAreaResidenceAddress.setDisable(false);
        });

        textAreaHomeAddress.setText(profile.getAddress());
        textAreaResidenceAddress.setText(profile.getResidence());
        textFieldCNP.setText(profile.getCNP());
        textFieldEmail.setText(profile.getEmail());
        textFieldFirstName.setText(profile.getFirstName());
        textFieldHeight.setText(String.valueOf(profile.getHeight()));
        textFieldLastName.setText(profile.getLastName());
        textFieldNationality.setText(profile.getNationality());
        textFieldPhone.setText(profile.getPhone());
        textFieldWeight.setText(String.valueOf(profile.getWeight()));
        datePickerBirthDate.setValue(profile.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        if (profile.getAddress().equals(profile.getResidence())) {
            checkBoxResidenceAddress.setSelected(true);
            textAreaResidenceAddress.setDisable(true);
        }
    }

    private void updateLabels() {

        new Thread(() -> {
            setDonationTableData();
            Platform.runLater(() -> {
                labelDonationsSoFar.setText("" + modelDonation.size());
                labelDaysLeftUntilNextDonation.setText(mainService.getDaysUntilNextDonationForDonor(getUsername(), Date.valueOf(LocalDate.now())) + "");
            });
        }).start();

    }

    private void toggleView(AnchorPane viewToShow, JFXButton buttonClicked) {
        donationHistoryPane.setVisible(false);
        homePane.setVisible(false);
        notificationsPane.setVisible(false);
        personalProfilePane.setVisible(false);

        buttonDonation.setDisable(false);
        buttonHome.setDisable(false);
        buttonNotifications.setDisable(false);
        buttonProfile.setDisable(false);

        viewToShow.setVisible(true);
        buttonClicked.setDisable(true);
    }

    private void setDonationTableData() {
        modelDonation.setAll(
                mainService.getHistory(getUsername())
        );
    }


    @Override
    public void setMainService(IMainService mainService, String username, Stage stageLogin) {

        super.setMainService(mainService, username, stageLogin);

        profile = mainService.getProfile(username);
        labelNumeDonator.setText(profile.getFirstName() + " " + profile.getLastName());
        initProfile();
        updateLabels();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        burgerTask = new HamburgerBasicCloseTransition(menuHamburger);
        burgerTask.setRate(-1);
        drawer.setSidePane(drawerContent);
        drawerContent.setVisible(true);

        labelDonationsSoFar.setText("");
        labelDaysLeftUntilNextDonation.setText("");

        initTable();
        initList();

        toggleView(homePane, buttonHome);
    }

    @Override
    public void notifyDonorAnalyseFinished(String username, String message) throws RemoteException {

        System.out.println("Notified->" + message);

        Platform.runLater(() -> {
            GUIUtils.showSnackBar("You have a new notification.", stackPaneContent);
            modelNotifications.add(message);
            System.out.println("Nr notif: " + mainService.getNrNotifications(username));
            labelNrNotifications.setText(String.valueOf(mainService.getNrNotifications(username)));
        });
    }

    @Override
    public void notifyDonorUpdateHistory(String username) throws RemoteException {
        Platform.runLater(() -> {
            setDonationTableData();
            updateLabels();
        });
    }

}
