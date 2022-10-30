package scheduling;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.*;
import java.util.*;

public class MainController {
    @FXML
    private TableView<Customer> customerTableView;
    @FXML
    private TableView<Appointment> appointmentsTableView;
    @FXML
    private TextField searchField;
    @FXML
    private Label currentUserName;
    @FXML
    private Button updateBusinessHoursButton;
    @FXML
    private Button addUserButton;
    @FXML
    private TextArea currentCustomerInfo;
    @FXML
    private TextArea currentAppointmentInfo;

    public void initialize() throws Exception {
        SchedDataSource.getInstance().open();
        SchedDataSource.getInstance().loadUsers();
        if (MainController.showLoginWindow()) {
            SchedDataSource.getInstance().loadBusinessHours();
            currentUserName.setText(SchedDataSource.getCurrentUser().getUserName());
            SchedDataSource.getInstance().logUserLogin();

            customerTableView.setItems(SchedDataSource.getInstance().getAllCustomers());
            customerTableView.getSelectionModel().selectFirst();
            SchedDataSource.setCustomer(customerTableView.getSelectionModel().getSelectedItem());

            appointmentsTableView.setItems(SchedDataSource.getInstance().getThisCustomersAppointments());
            appointmentsTableView.getSelectionModel().selectFirst();
            SchedDataSource.setCurrentAppointment(appointmentsTableView.getSelectionModel().getSelectedItem());
            handleCustomerClickListView();

            Customer customer = customerTableView.getSelectionModel().getSelectedItem();

            if (SchedDataSource.getCurrentUser().getUserName().equalsIgnoreCase("test")) {
                addUserButton.setDisable(false);
                updateBusinessHoursButton.setDisable(false);
            } else {
                addUserButton.setDisable(true);
                updateBusinessHoursButton.setDisable(true);
            }

            if (customer != null) {
                SchedDataSource.setCustomer(customer);
            }
            fifteenMinuteAlert();
        } else {
            handleExit();
        }
    }

    public void handleCustomerClickListView() {
        SchedDataSource.setCustomer(customerTableView.getSelectionModel().getSelectedItem());
        appointmentsTableView.setItems(SchedDataSource.getInstance().getThisCustomersAppointments());
        appointmentsTableView.getSelectionModel().selectFirst();
        loadCustomerInfo();
        handleAppointmentClickListView();
    }

    public void handleAppointmentClickListView() {
        if (SchedDataSource.getInstance().getThisCustomersAppointments()==null) {
            appointmentsTableView.setItems(null);
        } else {
            SchedDataSource.setCurrentAppointment(appointmentsTableView.getSelectionModel().getSelectedItem());
            loadAppointmentInfo();
        }
    }

    public static boolean showLoginWindow() {
        //        DATABASE CONNECTED
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(MainController.class.getResource("loginWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 320, 280);
            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.showAndWait();

            return (SchedDataSource.getLoggedIn() == 1);
        } catch (IOException e) {
            System.out.println("Failed to Login");
            e.printStackTrace();
            return false;
        }
    }


    public void showAddCustomerDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("addCustomer.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);
            Stage stage = new Stage();
            stage.setTitle("Add Customer");
            stage.setScene(scene);
            stage.showAndWait();
            customerTableView.setItems(SchedDataSource.getInstance().getAllCustomers());
            customerTableView.getSelectionModel().selectLast();
            SchedDataSource.setCustomer(customerTableView.getSelectionModel().getSelectedItem());

            loadCustomerInfo();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAddUserDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("addUserWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 320, 280);
            Stage stage = new Stage();
            stage.setTitle("Add User");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showUpdateCustomerDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("updateCustomerWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);
            Stage stage = new Stage();
            stage.setTitle("Update Customer");
            stage.setScene(scene);
            stage.showAndWait();
            customerTableView.setItems(SchedDataSource.getInstance().getAllCustomers());
            customerTableView.getSelectionModel().selectLast();
            SchedDataSource.setCustomer(customerTableView.getSelectionModel().getSelectedItem());
            handleCustomerClickListView();
        } catch (IOException e) {
            System.out.println("No Customer To Update");
        }
    }

    public void deleteCustomer() throws Exception {
        try{
            Customer customer = customerTableView.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Customer");
        alert.setTitle("Deleting Customer: '" + customer.getCustomerName() + "'");
        alert.setContentText("Are you sure you wish to continue?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && (result.get() == ButtonType.OK)) {

            int addressId = customer.getAddressId();
            int cityId = SchedDataSource.getInstance().getAddress(addressId).getCityId();
            int countryId = SchedDataSource.getInstance().getCity(cityId).getCountryId();

            SchedDataSource.getInstance().deleteCustomer(customer.getCustomerId());
//            SchedDataSource.getInstance().deleteAddress(addressId);
//            SchedDataSource.getInstance().deleteCity(cityId);
//            SchedDataSource.getInstance().deleteCountry(countryId);
            if(SchedDataSource.getInstance().deleteAddress(addressId)) System.out.println("Address Deleted");
            if(SchedDataSource.getInstance().deleteCity(cityId)) System.out.println("City Deleted");
            if(SchedDataSource.getInstance().deleteCountry(countryId)) System.out.println("Country Deleted");
            customerTableView.getSelectionModel().selectFirst();
            handleCustomerClickListView();

        }} catch (NullPointerException E){
            System.out.println("No Customer To Delete");
        }
    }

    public void showAddAppointmentDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("addAppointmentWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 810, 365);
            Stage stage = new Stage();
            stage.setTitle("Add Appointment");
            stage.setScene(scene);
            stage.showAndWait();

            if (!SchedDataSource.getInstance().getAllAppointments().isEmpty()) {
                appointmentsTableView.getSelectionModel().selectLast();
                SchedDataSource.setCurrentAppointment(appointmentsTableView.getSelectionModel().getSelectedItem());
            }
            handleCustomerClickListView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showModifyAppointmentDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("updateAppointmentWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 810, 395);
            Stage stage = new Stage();
            stage.setTitle("Update Appointment");
            stage.setScene(scene);
            stage.showAndWait();
            if (!SchedDataSource.getInstance().getAllAppointments().isEmpty()) {
                appointmentsTableView.getSelectionModel().selectLast();
                SchedDataSource.setCurrentAppointment(appointmentsTableView.getSelectionModel().getSelectedItem());
            }
            handleCustomerClickListView();
        } catch (IOException E) {
            System.out.println("No Appointment To Update");
        }
    }

    public void deleteAppointment() throws Exception {
        try{
        Appointment appointment = appointmentsTableView.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Appointment");
        alert.setTitle("Deleting Appointment: '" + appointment.getTitle() + "'");
        alert.setContentText("Are you sure you wish to continue?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && (result.get() == ButtonType.OK)) {
            SchedDataSource.getInstance().deleteAppointment(appointment.getAppointmentId());
        }
        handleCustomerClickListView();
        } catch (NullPointerException N){
            System.out.println("No Appointment To delete");
        }
    }

    public void showScheduleDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("userScheduleWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 500);
            Stage stage = new Stage();
            stage.setTitle("User Schedule");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showBusinessHoursDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("businessHoursWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 320, 150);
            Stage stage = new Stage();
            stage.setTitle("Change Business Hours");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePartsFilterButton() {
        String customerName = searchField.getText().trim();
        ObservableList<Customer> sortedList = SchedDataSource.getInstance().lookupCustomer(customerName);
        customerTableView.setItems(sortedList);
        customerTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        customerTableView.getSelectionModel().selectFirst();
        handleCustomerClickListView();
    }

    @FXML
    public void handleAddUserButtonAction(){
        showAddUserDialog();
    }

    @FXML
    public void handleExit(){
        Platform.exit();
    }

    @FXML
    public void getSchedule() {
        showScheduleDialog();
    }

    public void loadCustomerInfo() {
        if (customerTableView.getSelectionModel().getSelectedItem() != null)
            currentCustomerInfo.setText(SchedDataSource.getInstance().getCustomerInfo());
        else
            currentCustomerInfo.setText(" ");
    }

    public void loadAppointmentInfo() {
        if (appointmentsTableView.getSelectionModel().getSelectedItem() != null)
            currentAppointmentInfo.setText(SchedDataSource.getInstance().getAppointmentInfo());
        else
            currentAppointmentInfo.setText(" ");
    }


    //TESTS
    @FXML
    public void fifteenMinuteAlert() {
        if (!SchedDataSource.getInstance().getAllAppointments().isEmpty()) {
            for (Appointment appointment : SchedDataSource.getInstance().getAllAppointments()) {
                if (appointment.getStart().isAfter(LocalDateTime.now().minusMinutes(15))
                        && appointment.getStart().isBefore(LocalDateTime.now().plusMinutes(15))
                        && appointment.getUserId() == SchedDataSource.getCurrentUser().getUserId()) {

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("15 Minute Alert");
                    alert.setContentText("You Have The Appointment:\n\n" + appointment.getTitle().toUpperCase() + "\n\n Within The Next 15 Minutes");
                    alert.showAndWait();
                }
            }
        }
    }

    public void wastedIdButtonFunction(){
        SchedDataSource.getInstance().findWastedIDs();
    }
}
