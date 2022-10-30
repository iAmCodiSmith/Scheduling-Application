package scheduling;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class UpdateAppointmentController {
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
    @FXML
    private Button updateButton;
    @FXML
    private Button closeButton;

    final private Appointment updatedAppointment = SchedDataSource.getCurrentAppointment();
    int sameAppointmentId = updatedAppointment.getAppointmentId();
    int sameCustomerId = updatedAppointment.getCustomerId();
    int sameUserId = updatedAppointment.getUserId();
    String sameCreatedBy = updatedAppointment.getCreatedBy();
    LocalDateTime sameDate = updatedAppointment.getStart();
    LocalDateTime sameEnd = updatedAppointment.getEnd();
    LocalDateTime sameCreateDate = updatedAppointment.getCreateDate();

    public void initialize() {
        updateButton.setDisable(true);
        titleField.setText(updatedAppointment.getTitle());
        descriptionField.setText(updatedAppointment.getDescription());
        typeField.setText(updatedAppointment.getDescription());
        urlField.setText(updatedAppointment.getUrl());
        contactField.setText(updatedAppointment.getContact());
        locationField.setText(updatedAppointment.getLocation());

//Start Load
        StringBuilder startBreakdown = new StringBuilder(sameDate.toString());
        String dateText = startBreakdown.substring(0,10);
        StringBuilder finalDate = new StringBuilder(dateText);
        int year = Integer.parseInt(finalDate.substring(0,4));
        finalDate.delete(0,5);
        int month = Integer.parseInt(finalDate.substring(0,2));
        finalDate.delete(0,3);
        int day = Integer.parseInt(finalDate.toString());
        date.setValue(LocalDate.of(year,month,day));
        startBreakdown.delete(0,11);

        int startingHour = Integer.parseInt(startBreakdown.substring(0,2));
        if(startingHour==0){
            startingHour=12;
            startAmRB.setSelected(true);
        }  else if(startingHour==12){
            startPmRB.setSelected(true);
        } else if(startingHour>=13){
            startingHour-=12;
            startPmRB.setSelected(true);
        }else {
            startAmRB.setSelected(true);
        }
        startHour.getValueFactory().setValue(startingHour);
        startBreakdown.delete(0,3);
        int minute = Integer.parseInt(startBreakdown.substring(0,2));

        startMinute.getValueFactory().setValue(minute);

//End Load
        StringBuilder endBreakdown = new StringBuilder(sameEnd.toString());
        endBreakdown.delete(0,11);
        int endHourValue = Integer.parseInt(endBreakdown.substring(0,2));
        if(endHourValue==0){
            endHourValue=12;
            endAmRB.setSelected(true);
        }else if(endHourValue==12){
            endPmRB.setSelected(true);
        } else if(endHourValue>=13){
            endHourValue-=12;
            endPmRB.setSelected(true);
        }else {
            endAmRB.setSelected(true);
        }
        endHour.getValueFactory().setValue(endHourValue);
        endBreakdown.delete(0,3);


        int endMinuteValue = Integer.parseInt(endBreakdown.substring(0,2));
        endMinute.getValueFactory().setValue(endMinuteValue);


    }

    @FXML
    public void handleKeyReleased() {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String type = typeField.getText().trim();
        String url = urlField.getText().trim();
        String contact = contactField.getText().trim();
        String location = locationField.getText().trim();

            if(date.getValue()==null) {
                System.out.println("Date Not Set");
                updateButton.setDisable(true);
            } else {
                int startHourText = Integer.parseInt(startHour.getValue().toString());
                if(startHourText==12&&startAmRB.isSelected()){
                    startHourText = 0;
                } else if(startPmRB.isSelected()&&startHourText!=12){
                    startHourText+=12;
                }

                int startMinuteText = Integer.parseInt(startMinute.getValue().toString());
                LocalDateTime checkStartDate = makeTime.convertTime(date.getValue().toString(), startHourText, startMinuteText);

                int endHourText = Integer.parseInt(endHour.getValue().toString());
                if(endHourText==12&&endAmRB.isSelected()){
                    endHourText = 0;
                } else if(endPmRB.isSelected()&&endHourText!=12){
                    endHourText+=12;
                }
                int endMinuteText = Integer.parseInt(endMinute.getValue().toString());
                LocalDateTime checkEndDate = makeTime.convertTime(date.getValue().toString(), endHourText, endMinuteText);

                if (checkEndDate.isBefore(checkStartDate)){
                    System.out.println("End Time cannot be before Start Time");
                    updateButton.setDisable(true);
                }
                if(checkStartDate.toString().equals(checkEndDate.toString())){
                    System.out.println("Times are equal");
                    updateButton.setDisable(true);
                }
                if (SchedDataSource.getInstance().checkBusinessHours(checkStartDate, checkEndDate)) {
                    System.out.println("Within Business Hours");
                    if (SchedDataSource.getInstance().checkUserAvailability(SchedDataSource.getCurrentAppointment().getAppointmentId(), SchedDataSource.getCurrentUser().getUserId(), checkStartDate, checkEndDate)) {
                        System.out.println("User is free... Checking Customer Availability...");
                        if (SchedDataSource.getInstance().checkCustomerAvailability(SchedDataSource.getCurrentAppointment().getAppointmentId(), SchedDataSource.getCustomer().getCustomerId(),
                                checkStartDate, checkEndDate)) {
                            System.out.println("Customer is free... Adding appointment...");
                            updateButton.setDisable(title.length()<=0 ||description.length()<=0||type.length()<=0||contact.length()<=0
                                    ||location.length()<=0||url.length()<=0);

                            if(title.length()>255)
                            {
                                System.out.println("Title Too Long");
                                updateButton.setDisable(true);
                            }
                            if(url.length()>255)
                            {
                                System.out.println("URL Too Long");
                                updateButton.setDisable(true);
                            }

                                        updateButton.setDisable(!(!checkEndDate.isBefore(checkStartDate)&&
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
                            updateButton.setDisable(true);
                        }
                    } else {
                        updateButton.setDisable(true);
                    }
                } else {
                    System.out.println("Outside of Business Hours");
                    updateButton.setDisable(true);
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
    public void handleUpdateButtonAction() throws Exception {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String type = typeField.getText().trim();
        String url = urlField.getText().trim();
        String contact = contactField.getText().trim();
        String location = locationField.getText().trim();

        int startHourText = Integer.parseInt(startHour.getValue().toString());
        if(startHourText==12&&startAmRB.isSelected()){
            startHourText = 0;
        } else if(startPmRB.isSelected()){
            startHourText+=12;
        }
        int startMinuteText = Integer.parseInt(startMinute.getValue().toString());
        LocalDateTime finalStartDate = makeTime.convertTime(date.getValue().toString(),startHourText,startMinuteText);

        int endHourText = Integer.parseInt(endHour.getValue().toString());
        if(endHourText==12&&endAmRB.isSelected()){
            endHourText = 0;
        } else if(endPmRB.isSelected()){
            endHourText+=12;
        }
        int endMinuteText = Integer.parseInt(endMinute.getValue().toString());
        LocalDateTime finalEndDate = makeTime.convertTime(date.getValue().toString(),endHourText,endMinuteText);

        Stage stage = (Stage) updateButton.getScene().getWindow();
        stage.close();
        SchedDataSource.getInstance().updateAppointment(sameAppointmentId, sameCustomerId, sameUserId,
                title,description,location,contact,type,url,finalStartDate,finalEndDate, sameCreateDate, sameCreatedBy);
        System.out.println("Update Successful");
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