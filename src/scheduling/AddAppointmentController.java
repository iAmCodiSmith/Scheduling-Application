package scheduling;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.Optional;

public class AddAppointmentController {
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextArea typeField;
    @FXML
    private TextField urlField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField locationField;

    @FXML
    private Button addButton;
    @FXML
    private Button closeButton;

    @FXML
    private DatePicker date;
    @FXML
    private Spinner startHour;
    @FXML
    private Spinner startMinute;
    @FXML
    private Spinner endHour;
    @FXML
    private Spinner endMinute;
    @FXML
    private RadioButton startAmRB;
    @FXML
    private RadioButton startPmRB;
    @FXML
    private RadioButton endAmRB;
    @FXML
    private RadioButton endPmRB;


    public void initialize() {
        addButton.setDisable(true);
        startAmRB.setSelected(true);
        endAmRB.setSelected(true);
    }

    @FXML
    public void handleKeyReleased() {
        addButton.setDisable(false);

        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String type = typeField.getText().trim();
        String url = urlField.getText().trim();
        String contact = contactField.getText().trim();
        String location = locationField.getText().trim();

        if (date.getValue() == null) {
            System.out.println("Date Not Set");
            addButton.setDisable(true);
        } else {
            int startHourText = Integer.parseInt(startHour.getValue().toString());
            if (startHourText == 12 && startAmRB.isSelected()) {
                startHourText = 0;
            } else if (startPmRB.isSelected()) {
                startHourText += 12;
            }

            int startMinuteText = Integer.parseInt(startMinute.getValue().toString());
            LocalDateTime checkStartDate = makeTime.convertTime(date.getValue().toString(), startHourText, startMinuteText);

            int endHourText = Integer.parseInt(endHour.getValue().toString());
            if (endHourText == 12 && endAmRB.isSelected()) {
                endHourText = 0;
            } else if (endPmRB.isSelected()) {
                endHourText += 12;
            }
            int endMinuteText = Integer.parseInt(endMinute.getValue().toString());
            LocalDateTime checkEndDate = makeTime.convertTime(date.getValue().toString(), endHourText, endMinuteText);

            if (checkEndDate.isBefore(checkStartDate)) {
                System.out.println("End Time cannot be before Start Time");
                addButton.setDisable(true);
            }
            if (checkStartDate.toString().equals(checkEndDate.toString())) {
                System.out.println("Times are equal");
                addButton.setDisable(true);
            }

            if (SchedDataSource.getInstance().checkBusinessHours(checkStartDate, checkEndDate)) {
                System.out.println("Within Business Hours");
                if (SchedDataSource.getInstance().checkUserAvailability(SchedDataSource.getCurrentUser().getUserId(), checkStartDate, checkEndDate)) {
                    System.out.println("User is free... Checking Customer Availability...");
                    if (SchedDataSource.getInstance().checkCustomerAvailability(SchedDataSource.getCustomer().getCustomerId(),
                            checkStartDate, checkEndDate)) {
                        System.out.println("Customer is free... Adding appointment...");
                        if (title.length() <= 0 || description.length() <= 0 || type.length() <= 0 || contact.length() <= 0
                                || location.length() <= 0 || url.length() <= 0) {
                            addButton.setDisable(true);
                        }
                        if (title.length() > 255) {
                            System.out.println("Title Too Long");
                            addButton.setDisable(true);
                        }
                        if (url.length() > 255) {
                            System.out.println("URL Too Long");
                            addButton.setDisable(true);
                        }


                        addButton.setDisable(!(!checkEndDate.isBefore(checkStartDate)&&
                                !checkStartDate.toString().equals(checkEndDate.toString())&&
                                SchedDataSource.getInstance().checkBusinessHours(checkStartDate, checkEndDate)&&
                                SchedDataSource.getInstance().checkUserAvailability(SchedDataSource.getCurrentAppointment().getAppointmentId(),
                                        SchedDataSource.getCurrentUser().getUserId(), checkStartDate, checkEndDate)&&
                                SchedDataSource.getInstance().checkCustomerAvailability(SchedDataSource.getCurrentAppointment().getAppointmentId(),
                                        SchedDataSource.getCustomer().getCustomerId(),
                                        checkStartDate, checkEndDate)&&
                                !(title.length()<=0) &&!(description.length()<=0)&&!(type.length()<=0)&&!(contact.length()<=0)
                                &&!(location.length()<=0)&&!(url.length()<=0)&&!(title.length()>255)&&!(url.length()>255)));
                    } else {
                        addButton.setDisable(true);
                    }
                } else {
                    addButton.setDisable(true);
                }
            } else {
                System.out.println("Outside of Business Hours");
                addButton.setDisable(true);
            }
        }
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
    public void handleAddButtonAction() throws Exception {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String type = typeField.getText().trim();
        String url = urlField.getText().trim();
        String contact = contactField.getText().trim();
        String location = locationField.getText().trim();

        int startHourText = Integer.parseInt(startHour.getValue().toString());
        if (startHourText == 12 && startAmRB.isSelected()) {
            startHourText = 0;
        } else if (startPmRB.isSelected()) {
            startHourText += 12;
        }
        int startMinuteText = Integer.parseInt(startMinute.getValue().toString());
        LocalDateTime finalStartDate = makeTime.convertTime(date.getValue().toString(), startHourText, startMinuteText);

        int endHourText = Integer.parseInt(endHour.getValue().toString());
        if (startHourText == 12 && endAmRB.isSelected()) {
            endHourText = 0;
        } else if (endPmRB.isSelected()) {
            endHourText += 12;
        }
        int endMinuteText = Integer.parseInt(endMinute.getValue().toString());
        LocalDateTime finalEndDate = makeTime.convertTime(date.getValue().toString(), endHourText, endMinuteText);

        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
        SchedDataSource.getInstance().insertAppointment(title, description, location, contact, type, url, finalStartDate, finalEndDate);
    }
//Lambda 2
// Converts DateTime Picker and Spinners into Local Date Time
    Lambda2 makeTime = (String dateTime, int hour, int minute) -> {
        StringBuilder date = new StringBuilder(dateTime);
        int year = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int month = Integer.parseInt(date.substring(0, date.indexOf("-")));
        date.delete(0, date.indexOf("-") + 1);
        int day = Integer.parseInt(date.substring(0, date.length()));
        if (hour == 24)
            hour = 12;

        return LocalDateTime.of(year, month, day, hour, minute, 0, 0);
    };
}

interface Lambda2 {
    LocalDateTime convertTime(String dateTime, int hour, int minute);
}
