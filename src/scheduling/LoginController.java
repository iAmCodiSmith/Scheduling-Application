package scheduling;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class LoginController {
    @FXML
    private TextField userNameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label loginPrompt;
    @FXML
    private Label loginStatus;
    @FXML
    private Button loginButton;
    @FXML
    private Button exitButton;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label passwordLabel;

    private Locale currentLocale;


    public void initialize() {
        currentLocale = SchedDataSource.getCurrentLocale();
        System.out.println("Locale: " + currentLocale);
        translateLabels(currentLocale);
    }

    @FXML
    public void handleLoginButtonAction() throws Exception {
        if (SchedDataSource.getInstance().loginUser(userNameField.getText(), passwordField.getText())) {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();
            System.out.println("Login Successful");
            SchedDataSource.getInstance().loadLoaders();
        } else {
            ResourceBundle rb = ResourceBundle.getBundle("LoginLanguages", currentLocale);
            loginStatus.setText(rb.getString("failed"));
        }
    }

    public void translateLabels(Locale locale) {
            ResourceBundle rb = ResourceBundle.getBundle("LoginLanguages", locale);
            loginPrompt.setText(rb.getString("login"));
            loginStatus.setText(rb.getString("noAttempt"));
            usernameLabel.setText(rb.getString("username"));
            passwordLabel.setText(rb.getString("password"));
            loginButton.setText(rb.getString("loginButton"));
            exitButton.setText(rb.getString("exitButton"));
    }

    @FXML
    public void handleExitButtonAction() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void changeEn() {
        currentLocale = Locale.US;
        translateLabels(currentLocale);
    }

    @FXML
    public void changeFr() {
        currentLocale = Locale.FRANCE;
        translateLabels(Locale.FRANCE);
    }
}