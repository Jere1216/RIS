/* EDITED BY: JANET MORALES
 * 
 * 
*/
package aasim.ris;

import datastorage.InputValidation;
import datastorage.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserInfo extends Stage {

    // Navigation bar
    // shows the user's name, based on who is logged in
    private final int IMAGES_PER_ROW = 6;
    HBox navbar = new HBox();
    Label usernameLabel = new Label("Logged In as: " + App.user.getFullName());
    ImageView pfp = new ImageView(App.user.getPfp());
    Button logOut = new Button("Log Out");

    // End Navigation bar
    // table
    // Scene
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);

    // End Scene
    List<File> fileList = new ArrayList<File>();

    public UserInfo() {
    	
        this.setTitle("RIS - Radiology Information System (Profile)");
        // Navigation bar
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });
        usernameLabel.setId("navbar");
        usernameLabel.setOnMouseClicked(eh -> userInfo());

        navbar.getChildren().addAll(usernameLabel, pfp, logOut);
        pfp.setPreserveRatio(true);
        pfp.setFitHeight(38);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        // End of navigation bar coomands
        // Center

        // if the patient/user changes their password
        Label userIDTxt = new Label("ID:");
        TextField userID = new TextField(App.user.getUserID() + "");
        userID.setEditable(false);
        VBox c = new VBox(userIDTxt, userID);

        Label usernameTxt = new Label("Username:");
        TextField username = new TextField(App.user.getUsername());
        username.setEditable(false);
        VBox c1 = new VBox(usernameTxt, username);

        Label emailTxt = new Label("Email Address:");
        TextField email = new TextField(App.user.getEmail());
        VBox c2 = new VBox(emailTxt, email);

        Label changePWTxt = new Label("Change Password:");
        PasswordField password = new PasswordField();
        VBox c3 = new VBox(changePWTxt, password);

        Label confirmPWTxt = new Label("Confirm Password:");
        PasswordField passwordConfirm = new PasswordField();
        VBox c4 = new VBox(confirmPWTxt, passwordConfirm);

        HBox c5 = new HBox(c, c1, c2);
        c5.setSpacing(10);
        HBox c6 = new HBox(c3, c4);
        c6.setSpacing(10);
        c5.setAlignment(Pos.CENTER);
        c6.setAlignment(Pos.CENTER);

        // is asked to confirm the change by pressing the confirm button
        Button goBack = new Button("Go Back");
        goBack.setId("cancel");
        Button confirm = new Button("Confirm");
        confirm.setId("complete");
        HBox toTheRight = new HBox(confirm);
        toTheRight.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(toTheRight, Priority.ALWAYS);
        HBox btnContainer = new HBox(goBack, toTheRight);
        VBox container = new VBox(c5, c6, btnContainer);
        container.setPadding(new Insets(10));
        main.setCenter(container);

        populateImgList();

        VBox imgContainer = new VBox();
        imgContainer.setPadding(new Insets(10));
        imgContainer.setSpacing(10);
        ArrayList<HBox> hboxList = new ArrayList<HBox>();

        for (int i = 0; i < (fileList.size() / IMAGES_PER_ROW) + 1; i++) {
            hboxList.add(new HBox());
        }

        int counter = 0;
        int hboxCounter = 0;
        for (File x : fileList) {
            FileInputStream te = null;
            try {
                if (counter > IMAGES_PER_ROW) {
                    hboxCounter++;
                    counter = 0;
                }
                te = new FileInputStream(x.getAbsoluteFile());
                Image tem = new Image(te);
                ImageView temp = new ImageView(tem);
                temp.setPreserveRatio(true);
                temp.setFitHeight(150);
                Label label = new Label(x.getName());
                VBox temp1 = new VBox(temp, label);

        // sets an image/profile picture for the user's profile
                temp1.setId("navbar");
                temp1.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent eh) {
                        String sql = "UPDATE users SET pfp = '" + label.getText() + "' WHERE user_id = '" + App.user.getUserID() + "';";
                        App.executeSQLStatement(sql);
                        App.user.setPfp(tem);
                        navbar.getChildren().removeAll(pfp, logOut);
                        pfp = new ImageView(tem);
                        pfp.setPreserveRatio(true);
                        pfp.setFitHeight(38);
                        navbar.getChildren().addAll(pfp, logOut);
                    }
                });
                hboxList.get(hboxCounter).getChildren().add(temp1);
                counter++;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    te.close();
                } catch (IOException ex) {
                    Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // scrolling panel/adjustment
        ScrollPane s1 = new ScrollPane();
        s1.setPrefHeight(600);
        container.getChildren().add(s1);
        for (HBox temp : hboxList) {
            temp.setAlignment(Pos.CENTER);
            temp.setPadding(new Insets(10));
            temp.setSpacing(10);
            imgContainer.getChildren().add(temp);

        }
        s1.setContent(imgContainer);
        goBack.setOnAction(eh -> goBack());

        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            // if user updates their  account, they are asked to enter their password in order to make any changes
            public void handle(ActionEvent eh) {
                if (!InputValidation.validateEmail(email.getText())) {
                    return;
                }

                if (password.getText().isBlank()) {
                    String sql = "UPDATE users SET email = '" + email.getText() + "' WHERE user_id = '" + App.user.getUserID() + "';";
                    App.executeSQLStatement(sql);
                    goBack();
                } else if (password.getText().equals(passwordConfirm.getText())) {
                    if (!InputValidation.validatePassword(password.getText())) {
                        return;
                    }
                    String sql = "UPDATE users SET email = '" + email.getText() + "', password = '" + password.getText() + "' WHERE user_id = '" + App.user.getUserID() + "';";
                    App.executeSQLStatement(sql);
                    goBack();

                // user will recieve an error if password or email do not match
                } else {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Error");
                    a.setHeaderText("Try Again");
                    a.setContentText("Please check both passwords to make sure they are the same. \n");
                    a.show();
                }
            }
        });

        // End Center
        // Set Scene and Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
    }

    private void logOut() {
        App.user = new User();
        Stage x = new Login();
        x.show();
        x.setMaximized(true);
        this.close();
    }
    private void goBack() {
        if (App.user.getRole() == 2) {
            // Receptionist
            Stage x = new Receptionist();
            x.show();
            x.setMaximized(true);
            this.hide();
        } else if (App.user.getRole() == 3) {
            // Technician
            Stage x = new Technician();
            x.show();
            x.setMaximized(true);
            this.hide();
        } else if (App.user.getRole() == 4) {
            // Radiologist
            Stage x = new Rad();
            x.show();
            x.setMaximized(true);
            this.hide();
        } else if (App.user.getRole() == 5) {
            // Referral Doctor
            Stage x = new ReferralDoctor();
            x.show();
            x.setMaximized(true);
            this.hide();
            // Billing
        } else if (App.user.getRole() == 6) {
            Stage x = new Billing();
            x.show();
            x.setMaximized(true);
            this.hide();
            // Administrator
        } else if (App.user.getRole() == 1) {
            Stage x = new Administrator();
            x.show();
            x.setMaximized(true);
            this.hide();
        }
    }

    private void userInfo() {
        Stage x = new UserInfo();
        x.show();
        x.setMaximized(true);
        this.close();
    }
    // images of the profile pictures/images that the user is able to select from
    private void populateImgList() {
        String dirname = "Favicons";
        File dir = new File(dirname);
        if (!dir.exists()) {
            fileList = Collections.emptyList();
        }
        fileList = Arrays.stream(Objects.requireNonNull(dir.listFiles())).collect(Collectors.toList());

    }

}