package scheduling;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class BusinessHoursController {
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

    private Time startTime;
    private Time endTime;

    public void initialize() {
        updateButton.setDisable(true);
        //Start Load
        StringBuilder startBreakdown = new StringBuilder(SchedDataSource.getBusinessOpen().toString());
        int startingHour = Integer.parseInt(startBreakdown.substring(0, 2));
        if (startingHour == 0) {
            startingHour = 12;
            startAmRB.setSelected(true);
        } else if (startingHour >= 13) {
            startingHour -= 12;
            startPmRB.setSelected(true);
        } else if(startingHour==12){
            startPmRB.setSelected(true);
        }else {
            startAmRB.setSelected(true);
        }
        startHour.getValueFactory().setValue(startingHour);
        startBreakdown.delete(0,3);
        int minute = Integer.parseInt(startBreakdown.substring(0,2));
        startMinute.getValueFactory().setValue(minute);

//End Load
        StringBuilder endBreakdown = new StringBuilder(SchedDataSource.getBusinessClose().toString());
        int endHourValue = Integer.parseInt(endBreakdown.substring(0,2));
        if(endHourValue==0){
            endHourValue=12;
            endAmRB.setSelected(true);
        } else if(endHourValue==12){
        endPmRB.setSelected(true);
        } else if(endHourValue>=13) {
            endHourValue -= 12;
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
            int startHourText = Integer.parseInt(startHour.getValue().toString());
            if(startHourText==12&&startAmRB.isSelected()){
                startHourText = 0;
            } else if(startPmRB.isSelected()&&startHourText!=12){
                startHourText+=12;
            }
            int startMinuteText = Integer.parseInt(startMinute.getValue().toString());
            startTime = Time.valueOf(startHourText+":"+startMinuteText+":00");

            int endHourText = Integer.parseInt(endHour.getValue().toString());
            if(endHourText==12&&endAmRB.isSelected()){
                endHourText = 0;
            } else if(endPmRB.isSelected()&&endHourText!=12){
                endHourText+=12;
            }
            int endMinuteText = Integer.parseInt(endMinute.getValue().toString());
            endTime = Time.valueOf(endHourText+":"+endMinuteText+":00");

            if(startTime.toString().equals(endTime.toString())){
                System.out.println("Times are equal");
                updateButton.setDisable(true);
            } else {
                updateButton.setDisable(false);
            }
    }

    @FXML
    public void handleCloseButtonAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Changing Business Hours");
        alert.setContentText("Are you sure you wish to cancel?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && (result.get() == ButtonType.OK)) {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void handleUpdateButtonAction(){
        int startHourText = Integer.parseInt(startHour.getValue().toString());
        if(startHourText==12&&startAmRB.isSelected()){
            startHourText = 0;
        } else if(startPmRB.isSelected()&&startHourText!=12){
            startHourText+=12;
        }

        int startMinuteText = Integer.parseInt(startMinute.getValue().toString());

        startTime = Time.valueOf(startHourText+":"+startMinuteText+":00");

        int endHourText = Integer.parseInt(endHour.getValue().toString());

        if(endHourText==12&&endAmRB.isSelected()){
            endHourText = 0;
        } else if(endPmRB.isSelected()&&endHourText!=12){
            endHourText+=12;
        }
        int endMinuteText = Integer.parseInt(endMinute.getValue().toString());

        endTime = Time.valueOf(endHourText+":"+endMinuteText+":00");

        LocalDate today = LocalDate.now();
        LocalDateTime ldtStart = LocalDateTime.of(today,startTime.toLocalTime());
        LocalDateTime utcStartTime = SchedDataSource.getInstance().makeUTC(ldtStart);

        LocalDateTime ldtEnd = LocalDateTime.of(today,endTime.toLocalTime());
        LocalDateTime utcEndTime = SchedDataSource.getInstance().makeUTC(ldtEnd);

        startTime = Time.valueOf(utcStartTime.toLocalTime()+":00");
        endTime = Time.valueOf(utcEndTime.toLocalTime()+":00");

        SchedDataSource.setBusinessOpen(startTime);
        SchedDataSource.setBusinessClose(endTime);
        System.out.println("Update Successful");
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
