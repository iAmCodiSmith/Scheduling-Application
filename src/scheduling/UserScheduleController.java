package scheduling;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.*;
import java.util.*;

public class UserScheduleController {
    @FXML
    private TableView<Appointment> appointmentsTableView;
    @FXML
    private Button exitButton;
    @FXML
    private TextArea currentAppointmentInfo;
    @FXML
    private ComboBox<User> userList;

    public void initialize(){
        userList.setItems(SchedDataSource.getInstance().getAllUsers());
        userList.getSelectionModel().selectFirst();

        StringConverter<User> userNames = new StringConverter<>() {
            @Override
            public String toString(User user) {
                return user.getUserName();
            }

            @Override
            public User fromString(String s) {
                return null;
            }
        };

        userList.setConverter(userNames);

        appointmentsTableView.setItems(SchedDataSource.getInstance().userSchedule());
        appointmentsTableView.getSelectionModel().selectFirst();
        handleAppointmentClickListView();
    }

    public void handleAppointmentClickListView() {
        if (appointmentsTableView.getItems().isEmpty()) {
            appointmentsTableView.setItems(null);
        } else {
            SchedDataSource.setCurrentAppointment(appointmentsTableView.getSelectionModel().getSelectedItem());
            loadAppointmentInfo();
        }
    }

    public void handleUserComboBoxClick() {
        appointmentsTableView.setItems(SchedDataSource.getInstance().loadThisUsersAppointments(userList.getSelectionModel().getSelectedItem().getUserId()));
        appointmentsTableView.getSelectionModel().selectFirst();
        handleAppointmentClickListView();
    }

    @FXML
    public void handleExitButtonAction() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    public void loadAppointmentInfo() {
        if (appointmentsTableView.getSelectionModel().getSelectedItem() != null)
            currentAppointmentInfo.setText(SchedDataSource.getInstance().getAppointmentInfo());
        else
            currentAppointmentInfo.setText(" ");
    }

    @FXML
    public void calendarByWeek() {
        ObservableList<Appointment> thisWeek = FXCollections.observableArrayList();
        LocalDateTime aWeekOut = LocalDateTime.now().plusWeeks(1);
        appointmentsTableView.setItems(SchedDataSource.getInstance().loadThisUsersAppointments(userList.getSelectionModel().getSelectedItem().getUserId()));
        for (Appointment appointment : appointmentsTableView.getItems()) {
            if (appointment.getStart().isAfter(LocalDateTime.now().toLocalDate().atStartOfDay()) && appointment.getStart().isBefore(aWeekOut.toLocalDate().atStartOfDay())) {
                thisWeek.add(appointment);
            }
        }
        appointmentsTableView.setItems(thisWeek);
        appointmentsTableView.getSelectionModel().selectFirst();
        handleAppointmentClickListView();
    }

    @FXML
    public void calendarByMonth() {
        ObservableList<Appointment> thisMonth = FXCollections.observableArrayList();
        LocalDateTime aMonthOut = LocalDateTime.now().plusMonths(1);
        appointmentsTableView.setItems(SchedDataSource.getInstance().loadThisUsersAppointments(userList.getSelectionModel().getSelectedItem().getUserId()));
        for (Appointment appointment : appointmentsTableView.getItems()) {
            if (appointment.getStart().isAfter(LocalDateTime.now().toLocalDate().atStartOfDay()) && appointment.getStart().isBefore(aMonthOut.toLocalDate().atStartOfDay())) {
                thisMonth.add(appointment);
            }
        }
        appointmentsTableView.setItems(thisMonth);
        appointmentsTableView.getSelectionModel().selectFirst();
        handleAppointmentClickListView();
    }

    @FXML
    public void resetCalendarView() {
        appointmentsTableView.setItems(SchedDataSource.getInstance().loadThisUsersAppointments(userList.getSelectionModel().getSelectedItem().getUserId()));
        appointmentsTableView.getSelectionModel().selectFirst();
        handleAppointmentClickListView();
    }

    @FXML
    public void handleGetTypesByMonth() {
        HashMap<String, Integer> types = new HashMap<>();
        StringBuilder allTypes = new StringBuilder();
        StringBuilder lastMonthsTypes = new StringBuilder();
        ObservableList<Appointment> theseAppointments = SchedDataSource.getInstance().getAllAppointments();
        Month currentMonth = theseAppointments.get(0).getStart().getMonth();
        int currentYear = theseAppointments.get(0).getStart().getYear();

        //LAMBDA 1
        // Sort All Appointments Regardless of Who they're for or when they were created By Date to build Schedule
        theseAppointments.sort((a, b) -> Appointment.compareDate(a.getStart(), b.getStart()));
        Collections.reverse(theseAppointments);

        // Go through all appointments
        allTypes.append("Number of Appointment Types By Month \n___________________________________________________\n");
        for (Appointment appointment : SchedDataSource.getInstance().getAllAppointments()) {
            if (appointment.getStart().getMonth().equals(currentMonth)&&appointment.getStart().getYear()==currentYear) {
                if (!types.containsKey(appointment.getType()))
                    types.put(appointment.getType(), 1);
                else {
                    //If Type Found go through list
                    for (Map.Entry item : types.entrySet()) {
                        if (appointment.getType().equalsIgnoreCase(item.getKey().toString()))
                            item.setValue(((Integer) item.getValue() + 1));
                    }
                }
            } else { //New Month
                StringBuilder currentMonthsTypes = new StringBuilder();
                currentMonthsTypes.append("Appointment Types For ").append(currentMonth).append(" ").append(currentYear).append("\n");
                for (Map.Entry item : types.entrySet()) {
                    currentMonthsTypes.append("Type: ").append(item.getKey()).append(", # of Times ").append(item.getValue()).append("\n");
                }
                allTypes.append(currentMonthsTypes);
                currentMonth = appointment.getStart().getMonth();
                if (appointment.getStart().getYear()!=currentYear){
                    currentYear=appointment.getStart().getYear();
                }
                types = new HashMap<>();
                types.put(appointment.getType(), 1);
            }
        }
        //Final Month
        lastMonthsTypes.append("Appointment Types For ").append(currentMonth).append(" ").append(currentYear).append("\n");
        for (Map.Entry item : types.entrySet()) {
            lastMonthsTypes.append("Type: ").append(item.getKey()).append(", # of Times ").append(item.getValue()).append("\n");
        }
        allTypes.append(lastMonthsTypes);
        currentAppointmentInfo.setText(allTypes.toString());
    }
}
