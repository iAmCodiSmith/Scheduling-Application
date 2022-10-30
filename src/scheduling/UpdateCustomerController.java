package scheduling;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.util.Optional;

public class UpdateCustomerController {

    @FXML
    private TextField customerIdField;
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

    private Customer customer = new Customer();
    private Address address = new Address();
    private City city = new City();
    private Country country = new Country();
    int sameCustomerId;
    int sameAddressId;
    int sameCityId;
    int sameCountryId;
    LocalDateTime sameCreateDate;
    String sameCreateBy;
    String initialName;
    String initialAddress;
    String initialAddress2;
    String initialPostalCode;
    String initialPhone;
    String initialCity;
    String initialCountry;
    int initialActive = 0;

    int finalAddressId;
    int finalCityId;
    int finalCountryId;

    private boolean disableButton = true;

    public void initialize() {
        saveButton.setDisable(disableButton);
        //set vars
        customer = SchedDataSource.getCustomer();
        address = SchedDataSource.getInstance().getAddress(customer.getAddressId());
        city = SchedDataSource.getInstance().getCity(address.getCityId());
        country = SchedDataSource.getInstance().getCountry(city.getCountryId());
        sameCustomerId = customer.getCustomerId();
        sameAddressId = customer.getAddressId();
        sameCityId = address.getCityId();
        sameCountryId = city.getCountryId();
        sameCreateDate = customer.getCreateDate();
        sameCreateBy = customer.getCreatedBy();
        initialName = customer.getCustomerName();
        initialAddress = address.getAddress();
        initialAddress2 = address.getAddress2();
        initialPostalCode = address.getPostalCode();
        initialPhone = address.getPhone();
        initialCity = city.getCity();
        initialCountry = country.getCountry();
        finalCountryId = country.getCountryId();
        finalCityId = city.getCityId();
        finalAddressId = address.getAddressId();

        boolean active;
        if (customer.getActive() == 1) {
            active = true;
            initialActive = 1;
        } else {
            active = false;
            initialActive = 0;
        }

        //Loads into textfields
        customerIdField.setText(String.valueOf(customer.getCustomerId()));
        nameField.setText(customer.getCustomerName());
        addressField.setText(address.getAddress());
        address2Field.setText(address.getAddress2());
        if (address.getAddress().equals(address.getAddress2())) {
            address2Field.setText("");
        }
        cityField.setText(city.getCity());
        countryField.setText(country.getCountry());
        postalCodeField.setText(address.getPostalCode());
        phoneField.setText(address.getPhone());
        activeField.setSelected(active);
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

        disableButton = name.isEmpty() || address.isEmpty() || city.isEmpty()
                || country.isEmpty() || postalCode.isEmpty() || phone.isEmpty()
                || name.length() > 45 || address.length() > 50 || address2.length() > 50
                || city.length() > 50 || country.length() > 50 || postalCode.length() > 10 || phone.length() > 20||!phone.matches("[0-9]+")||!postalCode.matches("[0-9]+");
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
        int active;
        if (activeField.isSelected()) {
            active = 1;
        } else {
            active = 0;
        }

        String nameParse = nameField.getText();
        String addressParse = addressField.getText().trim();
        String address2Parse = address2Field.getText().trim();
        if (address2Parse.equals("")) {
            address2Parse = addressParse;
        }
        String postalCodeParse = postalCodeField.getText().trim();
        String phoneParse = phoneField.getText().trim();
        String cityParse = cityField.getText().trim();
        String countryParse = countryField.getText().trim();

        //Store Customers Appointments For Reinsert
        ObservableList<Appointment> tempAppointments = FXCollections.observableArrayList();

        for (Appointment appointment : SchedDataSource.getInstance().getThisCustomersAppointments()) {
            Appointment altAppointment = new Appointment(appointment.getAppointmentId(), appointment.getCustomerId(), appointment.getUserId(), appointment.getTitle(), appointment.getDescription(),
                    appointment.getLocation(), appointment.getContact(), appointment.getType(), appointment.getUrl(), appointment.getStart(), appointment.getEnd(),
                    appointment.getCreateDate(), appointment.getCreatedBy(), appointment.getLastUpdate(), appointment.getLastUpdateBy());

            tempAppointments.add(altAppointment);
        }


        //Store Address
        Address tempAddress = new Address(customer.getAddressId(), address.getAddress(), address.getAddress2(), address.getCityId(), address.getPostalCode(),
                address.getPhone(), address.getCreateDate(), address.getCreatedBy(), address.getLastUpdate(), address.getLastUpdateBy());

        //Store City
        City tempCity = new City(city.getCityId(), city.getCity(), city.getCountryId(), city.getCreateDate(), city.getCreatedBy(), city.getLastUpdate(), city.getLastUpdateBy());


        //Store Country
        Country tempCountry = new Country(country.getCountryId(), country.getCountry(), country.getCreateDate(), country.getCreatedBy(), country.getLastUpdate(), country.getLastUpdateBy());

        SchedDataSource.getInstance().deleteCustomer(customer.getCustomerId());
        //Customer deleted.

        if (countryParse.equals(initialCountry)) {
            System.out.println("Country Text Unchanged");
            if (cityParse.equals(initialCity)) {
                System.out.println("City Text Unchanged");
                //If Address Unchanged  -- AT END REPLACE WITH JUST RETURNING CURRENTCUSTOMER
                if (addressParse.equals(initialAddress) && address2Parse.equals(initialAddress2) &&
                        postalCodeParse.equals(initialPostalCode) && phoneParse.equals(initialPhone)) {
                    System.out.println("Only Customer Name changed.");
                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.close();
                    return SchedDataSource.getInstance().updateCustomer(sameCustomerId, nameParse, active, sameAddressId, sameCreateDate, sameCreateBy, tempAppointments);
                }
                //If Address Different and Deleted(Not in use)
                else if (SchedDataSource.getInstance().deleteAddress(sameAddressId)) {
                    System.out.println("Only Address Changed, no Customers using. Reusing Address ID unless Address Exists, Updating Customer...");
                    //Get new address' addressID, existing or the same
                    int updatedAddressId = SchedDataSource.getInstance().updateAddress(sameAddressId, addressParse, address2Parse, sameCityId, postalCodeParse, phoneParse, sameCreateDate, sameCreateBy);
                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.close();
                    return SchedDataSource.getInstance().updateCustomer(sameCustomerId, nameParse, updatedAddressId, active, sameCreateDate, sameCreateBy, tempAppointments);
                }
                //If Address Different and In Use By Another
                else {
                    System.out.println("Only Address Changed, Though Other Customer Using");
                    SchedDataSource.getInstance().insertAddress(addressParse, address2Parse, sameCityId, postalCodeParse, phoneParse);
                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.close();
                    return SchedDataSource.getInstance().updateCustomer(sameCustomerId, nameParse, SchedDataSource.getAddressCounter(), active, sameCreateDate, sameCreateBy, tempAppointments);
                }
            }
            //CITY CHANGED
            else {
                //Try to delete address, if successful try to delete city
                if (SchedDataSource.getInstance().deleteAddress(sameAddressId)) {
                    System.out.println("City Text Changed, ADDRESS DELETED");
                    //address deleted -> try to delete city -> if successful reuse both IDs
                    if (SchedDataSource.getInstance().deleteCity(sameCityId)) {
                        System.out.println("City Text Changed, CITY DELETED, reusing CityID & AddressID");
                        int updatedCityId = SchedDataSource.getInstance().updateCity(sameCityId, cityParse, sameCountryId, sameCreateDate, sameCreateBy);
                        int updatedAddressId = SchedDataSource.getInstance().updateAddress(sameAddressId, addressParse, address2Parse, updatedCityId, postalCodeParse, phoneParse, sameCreateDate, sameCreateBy);
                        Stage stage = (Stage) saveButton.getScene().getWindow();
                        stage.close();
                        return SchedDataSource.getInstance().updateCustomer(sameCustomerId, nameParse, updatedAddressId, active, sameCreateDate, sameCreateBy, tempAppointments);
                    } else {
                        System.out.println("Address May Have Been Deleted, but City in use");
                        SchedDataSource.getInstance().findCityId(cityParse, sameCountryId);
                        SchedDataSource.getInstance().updateAddress(sameAddressId, addressParse, address2Parse, SchedDataSource.getCityCounter(), postalCodeParse, phoneParse, SchedDataSource.getInstance().makeNow(), SchedDataSource.getCurrentUser().getUserName());
                        Stage stage = (Stage) saveButton.getScene().getWindow();
                        stage.close();
                        return SchedDataSource.getInstance().updateCustomer(sameCustomerId, nameParse, sameAddressId, active, sameCreateDate, sameCreateBy, tempAppointments);
                    }
                } else {
                    //Address couldnt be deleted, so city cannot be deleted, SEARCH IF NEW CITY EXISTS if not insert new city, insert new address
                    int updatedCityId = SchedDataSource.getInstance().findCityId(cityParse, sameCountryId);
                    int updatedAddressId = SchedDataSource.getInstance().findAddressId(addressParse, address2Parse, updatedCityId, postalCodeParse, phoneParse);
                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.close();
                    return SchedDataSource.getInstance().updateCustomer(sameCustomerId, nameParse, updatedAddressId, active, sameCreateDate, sameCreateBy, tempAppointments);
                }
            }
        }
        //COUNTRY CHANGED
        else {
            //Try to delete address, if successful try to delete city
            if (SchedDataSource.getInstance().deleteAddress(sameAddressId)) {
                System.out.println("Country Text Changed\nADDRESS DELETED");
                //address deleted -> try to delete city -> if successful try to delete country
                if (SchedDataSource.getInstance().deleteCity(sameCityId)) {
                    System.out.println("CITY DELETED");
                    //city deleted -> try to delete country -> if successful reuse all IDs
                    if (SchedDataSource.getInstance().deleteCountry(sameCountryId)) {
                        System.out.println("COUNTRY DELETED, reusing ALL IDs");

                        int updatedCountryId = SchedDataSource.getInstance().updateCountry(sameCountryId, countryParse, tempCountry.getCreateDate(), tempCountry.getCreatedBy());
                        int updatedCityId = SchedDataSource.getInstance().updateCity(sameCityId, cityParse, updatedCountryId, tempCity.getCreateDate(), tempCity.getCreatedBy());
                        int updatedAddressId = SchedDataSource.getInstance().updateAddress(sameAddressId, addressParse, address2Parse, updatedCityId, postalCodeParse, phoneParse, tempAddress.getCreateDate(), tempAddress.getCreatedBy());
                        Stage stage = (Stage) saveButton.getScene().getWindow();
                        stage.close();
                        return SchedDataSource.getInstance().updateCustomer(sameCustomerId, nameParse, updatedAddressId, active, sameCreateDate, sameCreateBy, tempAppointments);
                    } else {
                        //deleted address and city, insert country unless exists, reuse city and address IDs
                        int updatedCountryId = SchedDataSource.getInstance().findCountryId(countryParse);
                        int updatedCityId = SchedDataSource.getInstance().updateCity(sameCityId, cityParse, updatedCountryId, tempCity.getCreateDate(), tempCity.getCreatedBy());
                        int updatedAddressId = SchedDataSource.getInstance().updateAddress(sameAddressId, addressParse, address2Parse, updatedCityId, postalCodeParse, phoneParse, tempAddress.getCreateDate(), tempAddress.getCreatedBy());
                        Stage stage = (Stage) saveButton.getScene().getWindow();
                        stage.close();
                        return SchedDataSource.getInstance().updateCustomer(sameCustomerId, nameParse, updatedAddressId, active, sameCreateDate, sameCreateBy, tempAppointments);
                    }
                } else {
                    //deleted address, insert city and country unless exists, reuse address ID
                    int updatedCountryId = SchedDataSource.getInstance().findCountryId(countryParse);
                    int updatedCityId = SchedDataSource.getInstance().findCityId(cityParse, updatedCountryId);
                    int updatedAddressId = SchedDataSource.getInstance().updateAddress(sameAddressId, addressParse, address2Parse, updatedCityId, postalCodeParse, phoneParse, tempAddress.getCreateDate(), tempAddress.getCreatedBy());
                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.close();
                    return SchedDataSource.getInstance().updateCustomer(sameCustomerId, nameParse, updatedAddressId, active, sameCreateDate, sameCreateBy, tempAppointments);
                }
            } else {
                //couldn't delete country, insert country unless exists, reuse nothing
                int updatedCountryId = SchedDataSource.getInstance().findCountryId(countryParse);
                int updatedCityId = SchedDataSource.getInstance().findCityId(cityParse, updatedCountryId);
                int updatedAddressId = SchedDataSource.getInstance().findAddressId(addressParse, address2Parse, updatedCityId, postalCodeParse, phoneParse);
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
                return SchedDataSource.getInstance().updateCustomer(sameCustomerId, nameParse, updatedAddressId, active, sameCreateDate, sameCreateBy, tempAppointments);
            }
        }
    }
}