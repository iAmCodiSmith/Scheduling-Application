package scheduling;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddUserController {
    @FXML
    private TextField userNameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label addStatus;
    @FXML
    private Button addButton;
    @FXML
    private Button exitButton;

    public void initialize() {
        addButton.setDisable(true);
    }

    @FXML
    public void handleKeyReleased() {
        String userName = userNameField.getText().trim();
        String password = passwordField.getText().trim();
        boolean disableButton = userName.isEmpty() || password.isEmpty();
        addButton.setDisable(disableButton);
    }

    @FXML
    public void handleAddButtonAction() throws Exception {
        if (SchedDataSource.getInstance().findUserName(userNameField.getText().trim().toUpperCase())) {
            //Insert User
            SchedDataSource.getInstance().insertUser(userNameField.getText().trim(),
                    passwordField.getText().trim(), 1);

            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.close();
            System.out.println("User Added");
        } else {
            addStatus.setText("User Exists");
        }
    }

    @FXML
    public void handleExitButtonAction() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }
}
