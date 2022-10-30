package scheduling;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Timestamp;
import java.util.Optional;

public class AddCustomerController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField countryField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField address2Field;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField phoneField;
    @FXML
    private Button saveButton;
    @FXML
    private Button closeButton;
    @FXML
    private CheckBox activeField;

    public void initialize() {
        saveButton.setDisable(true);
        activeField.setSelected(true);
    }

    @FXML
    public void handleKeyReleased() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String address2 = address2Field.getText().trim();
        String city = cityField.getText().trim();
        String country = countryField.getText().trim();
        String postalCode = postalCodeField.getText().trim();
        String phone = phoneField.getText().trim();

        boolean disableButton = name.isEmpty() || address.isEmpty() || city.isEmpty()
                || country.isEmpty() || postalCode.isEmpty() || phone.isEmpty()
                || name.length() > 45 || address.length() > 50 || address2.length() > 50
                || city.length() > 50 || country.length() > 50 || postalCode.length() > 10 || phone.length() > 20 || !phone.matches("[0-9]+") || !postalCode.matches("[0-9]+");
        saveButton.setDisable(disableButton);
    }

    @FXML
    public void handleCloseButtonAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Adding Customer");
        alert.setContentText("Are you sure you wish to cancel?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && (result.get() == ButtonType.OK)) {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public Customer handleSaveButtonAction() throws Exception {
        String name = nameField.getText();
        String country = countryField.getText().trim();
        String city = cityField.getText().trim();
        String address = addressField.getText().trim();
        String address2 = address2Field.getText().trim();

        if (address2.equals("")) {
            address2 = address;
        }

        String postalCode = postalCodeField.getText().trim();
        String phone = phoneField.getText().trim();

        int active;
        if (activeField.isSelected()) {
            active = 1;
        } else {
            active = 0;
        }

        int addressId = SchedDataSource.getInstance().findAddressId(address, address2,
                SchedDataSource.getInstance().findCityId(city,
                        SchedDataSource.getInstance().findCountryId(country)),
                postalCode, phone);
        Timestamp timestampNow = new Timestamp(System.currentTimeMillis());

        SchedDataSource.getInstance().insertCustomer(name, addressId, active);

        Customer customer = new Customer(SchedDataSource.getCustomerCounter(), name,
                addressId, active, SchedDataSource.getInstance().makeNow(), SchedDataSource.getCurrentUser().getUserName(), timestampNow, SchedDataSource.getCurrentUser().getUserName());
        SchedDataSource.getInstance().getAllCustomers().add(customer);

        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
        return customer;
    }
}
