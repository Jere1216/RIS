package aasim.ris;

import static aasim.ris.App.ds;
import datastorage.Appointment;
import datastorage.InputValidation;
import datastorage.Order;
import datastorage.Patient;
import datastorage.PatientAlert;
import datastorage.User;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Administrator extends Stage {

    //Navbar
    HBox navbar = new HBox();
    Label username = new Label("Logged In as Administrator: " + App.user.getFullName());
    ImageView pfp = new ImageView(App.user.getPfp());
    Label users = new Label("Users");
    Label patients = new Label("Patients");
    Label appointments = new Label("Appointments");
    Label modalities = new Label("Modalities");
    Label patientAlerts = new Label("Patient Alerts");
    Button logOut = new Button("Log Out");
    //End Navbar

    //table
    TableView table = new TableView();
    VBox usersContainer = new VBox();
    VBox patientsContainer = new VBox();
    VBox appointmentsContainer = new VBox();
    VBox modalitiesContainer = new VBox();
    VBox patientAlertsContainer = new VBox();
    //
    //Scene
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);

    //End Scene
    private FilteredList<User> flUsers;
    private FilteredList<Patient> flPatient;
    private FilteredList<Appointment> flAppointment;

    /*
        Administrator Constructor.
        Creates and populates the Administrator Page
     */
    public Administrator() {
        this.setTitle("RIS - Radiology Information System (Administrator)");
        //Navbar
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });
        pfp.setPreserveRatio(true);
        pfp.setFitHeight(38);
        username.setId("navbar");
        username.setOnMouseClicked(eh -> userInfo());
        HBox navButtons = new HBox(users, patients, appointments, modalities, patientAlerts);
        navButtons.setAlignment(Pos.TOP_LEFT);
//        navButtons.setSpacing(10);
        HBox.setHgrow(navButtons, Priority.ALWAYS);
        navbar.getChildren().addAll(navButtons, username, pfp, logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);

        users.setId("navbar");
        patients.setId("navbar");
        appointments.setId("navbar");
        modalities.setId("navbar");
        patientAlerts.setId("navbar");
        //End navbar

        //Center
        //made user view the starting page instead of a blank page Jeremy Hall
        usersPageView();
        users.setOnMouseClicked(eh -> usersPageView());
        patients.setOnMouseClicked(eh -> patientsPageView());
        appointments.setOnMouseClicked(eh -> appointmentsPageView());
        modalities.setOnMouseClicked(eh -> modalitiesPageView());
        patientAlerts.setOnMouseClicked(eh -> patientAlertsPageView());
        
        //added to increase the table size and not waste as much space as before Jeremy Hall
        table.setMinHeight(600);

        //End Center
        //Set Scene and Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
    }

    /*
        Logout
        stage x to stage stage Jeremy Hall
     */
    private void logOut() {
        App.user = new User();
        Stage stage = new Login();
        stage.show();
        stage.setMaximized(true);
        this.close();
    }

    /*
        User Info Page
        stage x to stage stage Jeremy Hall
     */
    private void userInfo() {
        Stage stage = new UserInfo();
        stage.show();
        stage.setMaximized(true);
        this.close();
    }

//<editor-fold defaultstate="collapsed" desc="Users Section">
    //creates the table to hold the information for all users Jeremy Hall
    private void createTableUsers() {
        table.getColumns().clear();
        //All of the Columns
        TableColumn pfpCol = new TableColumn("PFP");
        TableColumn userIDCol = new TableColumn("User ID");
        TableColumn emailCol = new TableColumn("Email");
        TableColumn fullNameCol = new TableColumn("Full Name");
        TableColumn usernameCol = new TableColumn("Username");
        TableColumn roleCol = new TableColumn("Role");
        TableColumn enabledCol = new TableColumn("Enabled");
        TableColumn buttonCol = new TableColumn("Update User");

        //And all of the Value setting
        pfpCol.setCellValueFactory(new PropertyValueFactory<>("pfpView"));
        userIDCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("roleVal"));
        enabledCol.setCellValueFactory(new PropertyValueFactory<>("enabledLabel"));
        buttonCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        //Couldn't put all the styling
        pfpCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        userIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.09));
        emailCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        fullNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        usernameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        roleCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        enabledCol.prefWidthProperty().bind(table.widthProperty().multiply(0.07));
        buttonCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //Together again
        table.getColumns().addAll(pfpCol, userIDCol, emailCol, fullNameCol, usernameCol, roleCol, enabledCol, buttonCol);
        //Add Status Update Column:
    }

    //adds user values from sql database to the table created in createTableUsers Jeremy Hall
    private void populateUsersTable() {
        table.getItems().clear();
        //Connect to database
        String sql = "Select users.user_id, users.email, users.full_name, users.username, users.enabled, users.pfp, roles.role as roleID"
                + " FROM users "
                + " INNER JOIN roles ON users.role = roles.roleID "
                + ";";

        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<User> list = new ArrayList<User>();

            
            while (rs.next()) {
                //What I receieve:  int userID, String email, String fullName, String username, int role, int enabled
                User user = new User(rs.getString("user_id"), rs.getString("email"), rs.getString("full_name"), rs.getString("username"), 1, rs.getBoolean("enabled"), rs.getString("roleID"));
                try {
                    user.setPfp(new Image(new FileInputStream(App.imagePathDirectory + rs.getString("pfp"))));
                } catch (FileNotFoundException ex) {
                    user.setPfp(null);
                }
                list.add(user);
            }
            //the update user button Jeremy Hall
            for (User z : list) {
                z.placeholder.setText("Update User");
                z.placeholder.setOnAction(eh -> updateUser(z));
            }

            flUsers = new FilteredList(FXCollections.observableList(list), p -> true);
            table.getItems().addAll(flUsers);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //creates a page to view all users and their information after clicking the user button 
    //information includes username, name, role, user id, email and more Jeremy Hall
    private void usersPageView() {
        usersContainer.getChildren().clear();

        main.setCenter(usersContainer);
        //creates the table for users Jeremy Hall
        createTableUsers();
        //adds users from the sql database to the table created Jeremy Hall
        populateUsersTable();

        Button addUser = new Button("Add User");
        HBox buttonContainer = new HBox(addUser);
        buttonContainer.setSpacing(10);
        usersContainer.getChildren().addAll(table, buttonContainer);
        usersContainer.setSpacing(10);
        //added to create padding for the table Jeremy Hall
        usersContainer.setPadding(new Insets(20));
        users.setId("selected");
        patients.setId("navbar");
        appointments.setId("navbar");
        modalities.setId("navbar");
        patientAlerts.setId("navbar");

        //
        //Searchbar Structure
        ChoiceBox<String> choiceBox = new ChoiceBox();
        TextField search = new TextField("Search Users");
        HBox searchContainer = new HBox(choiceBox, search);
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("User ID", "Full Name", "Email", "Role");
        choiceBox.setValue("User ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
         	//make into a switch 
            //updated comments to represent what is being searched Jeremy hall
        	switch(choiceBox.getValue()) {
        	case "User ID": //filter table by User ID
        		flUsers.setPredicate(p -> new String(p.getUserID() + "").contains(newValue));
        	break;
        	case "Full Name": //filter table by full name
        		flUsers.setPredicate(p -> p.getFullName().toLowerCase().contains(newValue.toLowerCase()));
        	break;
        	case "Email": //filter table by user email
        		flUsers.setPredicate(p -> p.getEmail().toLowerCase().contains(newValue.toLowerCase()));
        	break;
        	case "Role": //filter table by User role
        		flUsers.setPredicate(p -> p.getRoleVal().toLowerCase().contains(newValue.toLowerCase()));
        	break;
        	default:;
        	}
            table.getItems().clear();
            table.getItems().addAll(flUsers);
        });
        buttonContainer.getChildren().add(searchContainer);

        //button that allows the admin to add a user 
        addUser.setOnAction(eh -> addUser());
    }

    //called by addUser button to add a user 
    //creates a separate small page to add in the information
    //stage x to stage stage BorderPane y to BorderPane borderPane Jeremy Hall
    private void addUser() {
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Add User");
        stage.initModality(Modality.WINDOW_MODAL);
        BorderPane borderPane = new BorderPane();
        
        //textfields to add information about the new user 
        //add labels for each text box Jeremy Hall
        Label txt = new Label("Enter the user's email:");
        TextField email = new TextField("Email");
        Label txt2 = new Label("Enter the user's full name:");
        TextField name = new TextField("Full Name");
        Label txt3 = new Label("Enter the user's username:");
        TextField username = new TextField("username");
        Label txt4 = new Label("Enter the user's password:");
        TextField password = new TextField("password");
        
        //used to let admin chose from all the possible roles Jeremy Hall
        ComboBox role = new ComboBox();
        role.setValue("Administrator");
        role.getItems().addAll("Administrator", "Referral Doctor", "Receptionist", "Technician", "Radiologist", "Biller");
        Button submit = new Button("Submit");
        submit.setId("complete");

        //made each label and text field its own row and center the role chooser dropdown Jeremy Hall
        HBox c = new HBox(txt2, name);
        HBox c3 = new HBox(txt, email);
        HBox c1 = new HBox(txt3, username);
        HBox c4 = new HBox(txt4, password);
        HBox c2 = new HBox(role);
        c2.setAlignment(Pos.CENTER);
        VBox center = new VBox(c, c3, c1, c4, c2, submit);

        //added spacing between the items so the GUI looks better Jeremy Hall
        center.setAlignment(Pos.CENTER);
        center.setSpacing(10);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));
        stage.show();

        //makes sure the information entered in is valid use input validation methods 
        //if they arent they created alerts Jeremy Hall
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                if (!InputValidation.validateName(name.getText())) {
                    return;
                }
                if (!InputValidation.validateEmail(email.getText())) {
                    return;
                }
                if (!InputValidation.validateUsername(username.getText())) {
                    return;
                }
                if (!InputValidation.validatePassword(password.getText())) {
                    return;
                }

                //if it is it adds the user to the sql database and calls the user page Jeremy Hall
                insertUserIntoDatabase(email.getText(), name.getText(), username.getText(), password.getText(), role.getValue().toString());
                usersPageView();
                stage.close();

            }
        });
    }

    //used to insert new user data into the sql database
    private void insertUserIntoDatabase(String email, String name, String username, String password, String role) {
        String sql = "INSERT INTO users(email, full_name, username, password, role) VALUES ('" + email + "','" + name + "','" + username + "','" + password + "', (SELECT roleID FROM roles WHERE role = '" + role + "'));";
        App.executeSQLStatement(sql);

    }

    //called by the updateUser button to update user information or disable/enable them
    //a small page is created for the method 
    //stage x to stage stage BorderPane y to BorderPane borderPane Jeremy Hall
    private void updateUser(User z) {
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Update User");
        stage.initModality(Modality.WINDOW_MODAL);
        BorderPane borderPane = new BorderPane();

        //creates buttons for changing email, password and if a user is enabled
        Button updateUserEmail = new Button("Change User Email");
        Button updateUserPW = new Button("Change User Password");
        Button disableUser = new Button("Disable User");
        disableUser.setId("cancel");

        //if user is not enabled then the button says enables the user
        if (!z.getEnabled()) {
            disableUser.setText("Enable User");
            disableUser.setId("complete");
        }

        HBox buttonContainer = new HBox(updateUserEmail, updateUserPW, disableUser);
        buttonContainer.setSpacing(20);
        Button submit = new Button("Submit");
        
        //used for when button it clicked and values need to be entered Jeremy Hall
        Label txt = new Label("Insert Value Here:");
        TextField input = new TextField("...");
        input.setPrefWidth(200);
        HBox hidden = new HBox(txt, input);
        hidden.setVisible(false);
        //
        
        VBox center = new VBox(buttonContainer, hidden, submit);
        center.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));
        stage.show();

        //if the change email button is clicked Jeremy Hall
        updateUserEmail.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {

            	//sets the input block to visible and uses it to change the email Jeremy Hall
                center.getChildren().remove(buttonContainer);
                hidden.setVisible(true);
                txt.setText("Email: ");
                input.setText("example@email.com");
                submit.setId("complete");
                submit.setOnAction(eh2 -> updateEmail());
            }

            //checks if the email is valid and then adds it to the sql database Jeremy Hall
            private void updateEmail() {
                if (InputValidation.validateEmail(input.getText())) {
                    String sql = "UPDATE users SET email = '" + input.getText() + "' WHERE user_id = '" + z.getUserID() + "';";
                    App.executeSQLStatement(sql);
                    //recalls the user page to update the information
                    //might be easier to to redo the entire page every time Jeremy Hall
                    usersPageView();
                    stage.close();
                }
            }
        });

        //if the password button is clicked
        updateUserPW.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
            	
            	//sets the input block to visible and uses it to change the password Jeremy Hall
                center.getChildren().remove(buttonContainer);
                hidden.setVisible(true);
                txt.setText("Password: ");
                input.setText("Good passwords are long but easy to remember, try phrases with numbers and special chars mixed in.");
                submit.setId("complete");
                submit.setOnAction(eh2 -> updatePassword());
            }

            //checks if the email is valid and then adds it to the sql database Jeremy Hall
            private void updatePassword() {
                String sql = "UPDATE users SET password = '" + input.getText() + "' WHERE user_id = '" + z.getUserID() + "';";
                App.executeSQLStatement(sql);
                //recalls the user page to update the information
                //might be easier to to redo the entire page every time Jeremy Hall
                usersPageView();
                stage.close();
            }
        });

        //if the disable user button is clicked
        disableUser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
            	
            	//sets the input block to visible and uses it to confirm you want to do this Jeremy Hall
                center.getChildren().remove(buttonContainer);
                hidden.setVisible(true);
                txt.setText("Enter 'CONFIRM' to continue: ");
                input.setText("Are you sure?");

                submit.setId("cancel");
                if (!z.getEnabled()) {
                    submit.setId("complete");
                }
                submit.setOnAction(eh2 -> disableUser());
            }

            private void disableUser() {
            	//checks if you are trying to disable/enable yourself  Jeremy Hall
                if (!z.getUserID().equals(App.user.getUserID())) {
                	//checks if the user entered CONFIRM otherwise creates and alert  Jeremy Hall
                    if (InputValidation.validateConfirm(input.getText())) {
                    	//sets user enable to false or true by inverting their previous value 
                    	//If they where true then false if they where false then to true Jeremy Hall
                        boolean enabled = false;
                        if (!z.getEnabled()) {
                            enabled = true;
                        }
                        //the updated value is added to the sql database and the entire page is called again Jeremy Hall
                        String sql = "UPDATE users SET enabled = '" + enabled + "' WHERE user_id = '" + z.getUserID() + "';";
                        App.executeSQLStatement(sql);
                        usersPageView();
                        stage.close();
                    }
                } else {
                	//when you try to disable yourself
                	//alert a to alert alert Jeremy Hall
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText("Cannot Disable Self");
                    alert.setContentText("You cannot disable yourself. \n");
                    alert.show();
                }
            }
        });
    }
//</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="Patients Section">

    //creates the patient page to show all patients and their information Jeremy Hall
    //very similar to the user page 
    //information includes patient id, name, email, insurance and date of birth Jeremy Hall
    private void patientsPageView() {
        patientsContainer.getChildren().clear();

        main.setCenter(patientsContainer);
        //creates table to hold information Jeremy Hall
        createTablePatients();
        //adds information to the table created Jeremy Hall
        populatePatientsTable();

        patientsContainer.getChildren().addAll(table);
        patientsContainer.setSpacing(10);
        //added to create padding for the table Jeremy Hall
        patientsContainer.setPadding(new Insets(20));

        users.setId("navbar");
        patients.setId("selected");
        appointments.setId("navbar");
        modalities.setId("navbar");
        patientAlerts.setId("navbar");

        //Searchbar Structure
        ChoiceBox<String> choiceBox = new ChoiceBox();
        TextField search = new TextField("Search Patients");
        HBox searchContainer = new HBox(choiceBox, search);
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("Patient ID", "Full Name", "Email", "Date of Birth", "Insurance");
        choiceBox.setValue("Patient ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
        	//make into a switch 
            //updated comments to represent what is being searched Jeremy hall
            switch(choiceBox.getValue()) {
        	case "Patient ID": //filter table by Patient Id
        		flPatient.setPredicate(p -> new String(p.getPatientID() + "").contains(newValue));
        	break;
        	case "Full Name": //filter table by Full name
        		flPatient.setPredicate(p -> p.getFullName().toLowerCase().contains(newValue.toLowerCase()));
       		break;
        	case "Email": //filter table by Email
        		flPatient.setPredicate(p -> p.getEmail().toLowerCase().contains(newValue.toLowerCase()));
        	break;
        	case "Date of Birth"://filter table by Date of birth
        		flPatient.setPredicate(p -> p.getDob().contains(newValue));
        	break;
        	case "Insurance"://filter table by Insurance name
        		flPatient.setPredicate(p -> p.getInsurance().contains(newValue));
        	break;
        	default:;
            }
            
            table.getItems().clear();
            table.getItems().addAll(flPatient);
        });
        patientsContainer.getChildren().add(searchContainer);
    }

    //creates the table to hold each patients information Jeremy Hall
    private void createTablePatients() {
        table.getColumns().clear();
        //All of the Columns
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn fullNameCol = new TableColumn("Full Name");
        TableColumn emailCol = new TableColumn("Email");
        TableColumn DOBCol = new TableColumn("Date of Birth");
        TableColumn insuranceCol = new TableColumn("Insurance");

        //And all of the Value setting
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        DOBCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
        insuranceCol.setCellValueFactory(new PropertyValueFactory<>("insurance"));

        //Couldn't put the table
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.09));
        fullNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        emailCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        DOBCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        insuranceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));

        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //back together again
        table.getColumns().addAll(patientIDCol, fullNameCol, emailCol, DOBCol, insuranceCol);
    }

    //gets information from sql database to add in patient information Jeremy Hall
    private void populatePatientsTable() {
        table.getItems().clear();
        //Connect to database
        String sql = "Select patients.patientID, patients.email, patients.full_name, patients.dob, patients.address, patients.insurance"
                + " FROM patients"
                + " "
                + " ;";

        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Patient> list = new ArrayList<Patient>();
            while (rs.next()) {
                //What I receieve:  patientID, email, full_name, dob, address, insurance
                Patient pat = new Patient(rs.getString("patientID"), rs.getString("email"), rs.getString("full_name"), rs.getString("dob"), rs.getString("address"), rs.getString("insurance"));
                list.add(pat);
            }

            for (Patient z : list) {
                z.placeholder.setText("Patient Overview");
                z.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {

                    }

                });
            }

            flPatient = new FilteredList(FXCollections.observableList(list), p -> true);
            table.getItems().addAll(flPatient);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="Appointments Section">
    
    //creates a page to view appointments and their infomation 
    //like the appointment id, name of the patient and their id
    //the appointment date, its status and what was ordered like a CT Scan Jeremy Hall
    private void appointmentsPageView() {
        appointmentsContainer.getChildren().clear();

        main.setCenter(appointmentsContainer);
        //creates the table for information Jeremy Hall
        createTableAppointments();
        //adds information to the table created Jeremy Hall
        populateTableAppointments();

        appointmentsContainer.getChildren().addAll(table);
        appointmentsContainer.setSpacing(10);
        //added to create padding for the table Jeremy Hall
        appointmentsContainer.setPadding(new Insets(20));

        users.setId("navbar");
        patients.setId("navbar");
        appointments.setId("selected");
        modalities.setId("navbar");
        patientAlerts.setId("navbar");

        //Searchbar Structure
        ChoiceBox<String> choiceBox = new ChoiceBox();
        TextField search = new TextField("Search Appointments");
        HBox searchContainer = new HBox(choiceBox, search);
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("Appointment ID", "Patient ID", "Full Name", "Date/Time", "Status");
        choiceBox.setValue("Appointment ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
        	//make into a switch Jeremy hall
            switch(choiceBox.getValue()) {
        	case "Appointment ID": //filter table by Appt ID
        		flAppointment.setPredicate(p -> new String(p.getApptID() + "").contains(newValue));
        	break;
        	case "Patient ID": //filter table by Patient Id
        		flAppointment.setPredicate(p -> new String(p.getPatientID() + "").contains(newValue));
        	break;
        	case "Full Name": //filter table by Full name
        		 flAppointment.setPredicate(p -> p.getFullName().toLowerCase().contains(newValue.toLowerCase()));
        	break;
        	case "Date/Time"://filter table by Date/Time
        		 flAppointment.setPredicate(p -> p.getTime().contains(newValue));
        	break;
        	case "Status"://filter table by Status
        		flAppointment.setPredicate(p -> p.getStatus().toLowerCase().contains(newValue.toLowerCase()));
        	break;
        	default:;
            }
            table.getItems().clear();
            table.getItems().addAll(flAppointment);
        });

        appointmentsContainer.getChildren().addAll(searchContainer);
    }

    //creates the table to hold appointment information Jeremy Hall
    private void createTableAppointments() {
        table.getColumns().clear();
        //Vbox to hold the table
        //Allow Table to read Appointment class
        TableColumn apptIDCol = new TableColumn("Appointment ID");
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn firstNameCol = new TableColumn("Full Name");
        TableColumn timeCol = new TableColumn("Time of Appt.");
        TableColumn orderCol = new TableColumn("Orders Requested");
        TableColumn status = new TableColumn("Status");

        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        orderCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        status.setCellValueFactory(new PropertyValueFactory<>("statusAsLabel"));

        //Set Column Widths
        apptIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.09));
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.09));
        firstNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        orderCol.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        status.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        //Add columns to table
        table.getColumns().addAll(apptIDCol, patientIDCol, firstNameCol, timeCol, orderCol, status);
    }

    //gets information from sql database and populates the appointment table Jeremy Hall
    private void populateTableAppointments() {
        table.getItems().clear();
        //Connect to database
        String sql = "Select appt_id, patient_id, patients.full_name, time, statusCode.status"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID "
                + " INNER JOIN patients ON patients.patientID = appointments.patient_id"
                + " "
                + " ORDER BY time ASC;";

        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Appointment> list = new ArrayList<Appointment>();

            while (rs.next()) {
                //What I receieve:  apptId, patientID, fullname, time, address, insurance, referral, status, order
                Appointment appt = new Appointment(rs.getString("appt_id"), rs.getString("patient_id"), rs.getString("time"), rs.getString("status"), getPatOrders(rs.getString("patient_id"), rs.getString("appt_id")));
                appt.setFullName(rs.getString("full_name"));
                list.add(appt);
            }

            flAppointment = new FilteredList(FXCollections.observableList(list), p -> true);
            table.getItems().addAll(flAppointment);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //gets orders from sql database and used by populateTableAppointments Jeremy Hall
    private String getPatOrders(String patientID, String aInt) {
        String sql = "Select orderCodes.orders "
                + " FROM appointmentsOrdersConnector "
                + " INNER JOIN orderCodes ON appointmentsOrdersConnector.orderCodeID = orderCodes.orderID "
                + " WHERE apptID = '" + aInt + "';";

        String value = "";
        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {

                value += rs.getString("orders") + ", ";
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return value;
    }

    //</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="Modalities Section">
    //creates a page to view modalities and their infomation 
    //like their cost, name, order id  
    //should have and edit button Jeremy Hall
    private void modalitiesPageView() {
        modalitiesContainer.getChildren().clear();

        main.setCenter(modalitiesContainer);
        //creates table to hold information Jeremy Hall
        createTableModalities();
        //fills table with information for each modality Jeremy Hall
        populateTableModalities();

        Button addModality = new Button("Add Modality");
        HBox btnContainer = new HBox(addModality);

        modalitiesContainer.getChildren().addAll(table, btnContainer);
        modalitiesContainer.setSpacing(10);
        //added to create padding for the table Jeremy Hall
        modalitiesContainer.setPadding(new Insets(20));

        users.setId("navbar");
        patients.setId("navbar");
        appointments.setId("navbar");
        modalities.setId("selected");
        patientAlerts.setId("navbar");

        //button that allows additional modalities to be added
        addModality.setOnAction(eh -> addModality());
    }

    //creates table for the modalities and their information Jeremy Hall
    private void createTableModalities() {
        table.getColumns().clear();
        //All of the Columns
        TableColumn orderIDCol = new TableColumn("Order ID");
        TableColumn orderCol = new TableColumn("Order");
        TableColumn buttonCol = new TableColumn("Delete");
        TableColumn costCol = new TableColumn("Cost");
        TableColumn buttonCol2 = new TableColumn("Update");

        //And all of the Value setting
        orderIDCol.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        orderCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        buttonCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
        buttonCol2.setCellValueFactory(new PropertyValueFactory<>("placeholder2"));

        //Couldn't put all the styling
        orderIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        orderCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        buttonCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        costCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        buttonCol2.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        //Together again
        table.getColumns().addAll(orderIDCol, orderCol, costCol, buttonCol, buttonCol2);
        //Add Status Update Column:
    }

    //gets modalities and their information from sql database to populate the table Jeremy Hall
    private void populateTableModalities() {
        table.getItems().clear();
        //Connect to database
        String sql = "Select * "
                + " FROM orderCodes "
                + " "
                + " ;";

        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Order> list = new ArrayList<Order>();
            while (rs.next()) {
                //What I receieve:  patientID, email, full_name, dob, address, insurance
                Order order = new Order(rs.getString("orderID"), rs.getString("orders"));
                order.setCost(rs.getFloat("cost"));
                list.add(order);
            }

            //allows the delete button to delete a modality Jeremy Hall
            for (Order z : list) {
                z.placeholder.setText("Delete");
                z.placeholder.setId("cancel");
                z.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                    	deleteModality(z);
                    
                    }
                });
            }
            
            //used to create the update button for each modality
            for (Order z : list) {
                z.placeholder2.setText("Update");
                z.placeholder2.setId("update"); 
                z.placeholder2.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                    	editModality(z);
                    }
                    
                });   
            }
            table.getItems().addAll(list);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //called by addUser button to add a modality
    //creates a separate small page to add in the information
    //stage x to stage stage BorderPane y to BorderPane borderPane Jeremy Hall
    private void addModality() {
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Add Modality");
        stage.initModality(Modality.WINDOW_MODAL);
        BorderPane borderPane = new BorderPane();
        
        //allows for the modalities name and cost to be entered Jeremy Hall
        //changed to make the GUI look better
        Label txt = new Label("Enter the new modality name: ");
        TextField order = new TextField("");
        order.setPrefWidth(200);
        HBox nameInput = new HBox(txt, order);
        Label text = new Label("Enter the new modality cost: ");
        TextField cost = new TextField("");
        cost.setPrefWidth(200);
        HBox costInput = new HBox(text, cost);
        
        Button submit = new Button("Submit");
        submit.setId("complete");

        VBox center = new VBox(nameInput, costInput, submit);

        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));
        stage.show();

        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
            	//makes sure the modality cost is valid otherwise it triggers an alert Jeremy Hall
                if(!InputValidation.validateCost(cost.getText())) {
                    return;
                }
                //adds modality to the sql database file and reloads the page Jeremy Hall
                String sql = "INSERT INTO orderCodes(orders,cost) VALUES ('" + order.getText() + "','" + cost.getText() + "') ;";
                App.executeSQLStatement(sql);
                populateTableModalities();
                stage.close();
            }

        });
    }
    
    //created to ask the user the confirming of deleting a modality so it isn't 
    //done accidently with a miss click Jeremy Hall
    private void deleteModality(Order z) {
    	 Stage stage = new Stage();
    	 stage.initOwner(this);
         stage.setTitle("Delete Modality");
         stage.initModality(Modality.WINDOW_MODAL);
         BorderPane borderPane = new BorderPane();
         
         Label confirmTxt = new Label("Type 'CONFIRM' to continue");
         TextField confirm = new TextField();
         HBox cont = new HBox(confirmTxt, confirm);
         Button submit = new Button("Submit");
         submit.setId("cancel");
         
         VBox helper = new VBox(cont,submit);
         helper.setPadding(new Insets(20));
         helper.setSpacing(10);
         helper.setAlignment(Pos.CENTER); 
         borderPane.setCenter(helper);
         borderPane.getStylesheets().add("file:stylesheet.css");
         stage.setScene(new Scene(borderPane));
         stage.show();
         
         submit.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent e) {
            	 if(!InputValidation.validateConfirm(confirm.getText())) {
                     return;
                 }
            	 
            	 String sql = "DELETE FROM orderCodes WHERE orderID = '" + z.getOrderID() + "' ";
                 App.executeSQLStatement(sql);
                 populateTableModalities();
                 stage.close();
             }
         });
    }
    
    //added to allow admin to update a modality's name and their costs Jeremy Hall
    private void editModality(Order z) {
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Edit Modality");
        stage.initModality(Modality.WINDOW_MODAL);
        BorderPane borderPane = new BorderPane();
   
        //creates buttons for changing email, password and if a user is enabled
        Button updateModalityName = new Button("Change Modality Name");
        Button updateModalityCost = new Button("Change Modality Cost");
        Button updateModality = new Button("Change Modality name and cost");

        HBox buttonContainer = new HBox(updateModalityName, updateModalityCost, updateModality);
        buttonContainer.setSpacing(20);
        Button submit = new Button("Submit");
        submit.setId("complete");
        
        //used for when button it clicked and values need to be entered Jeremy Hall
        Label txt = new Label("Enter the new modality name: ");
        TextField order = new TextField("");
        order.setPrefWidth(200);
        HBox nameInput = new HBox(txt, order);
        Label text = new Label("Enter the new modality cost: ");
        TextField cost = new TextField("");
        cost.setPrefWidth(200);
        HBox costInput = new HBox(text, cost);
        nameInput.setVisible(false);
        costInput.setVisible(false);
        //
        
        VBox center = new VBox(buttonContainer, nameInput, costInput , submit);
        center.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));
        stage.show();

        //if the updateModalityCost button is clicked Jeremy Hall
        updateModalityCost.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {

            	//sets the input block to visible and uses it to change the email Jeremy Hall
                center.getChildren().remove(buttonContainer);
                costInput.setVisible(true);
                submit.setId("complete");
                submit.setOnAction(eh2 -> updateCost());
            }

            //checks if the email is valid and then adds it to the sql database Jeremy Hall
            private void updateCost() {
            	if(!InputValidation.validateCost(cost.getText())) {
                    return;
                }
                String sql = "UPDATE orderCodes SET cost = '" + cost.getText() + "' WHERE orderID = '" + z.getOrderID() + "' ";
                App.executeSQLStatement(sql);
                populateTableModalities();
                stage.close();
                
            }
        });

        //if the updateModalityName button is clicked
        updateModalityName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
            	
            	//sets the input block to visible and uses it to change the modality name Jeremy Hall
                center.getChildren().remove(buttonContainer);
                nameInput.setVisible(true);
                submit.setId("complete");
                submit.setOnAction(eh2 -> updateName());
            }

            //updates the modality name in the sql database Jeremy Hall
            private void updateName() {
                String sql1 = "UPDATE orderCodes SET orders = '" + order.getText() + "' WHERE orderID = '" + z.getOrderID() + "' ";
                App.executeSQLStatement(sql1);
                populateTableModalities();
                stage.close();
            }
        });

        //allows the updating of both the modality's name and cost
        updateModality.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
            	
            	//removes clears the buttons to allow for input Jeremy Hall
                center.getChildren().remove(buttonContainer);
               
                //allows for the modality's new name and cost to be entered Jeremy Hall
                nameInput.setVisible(true);
                costInput.setVisible(true);
                submit.setId("complete");
                submit.setOnAction(eh2 -> changeBoth());
            }    
                    //updates the modality's name and cost
                    public void changeBoth() {
                    	//makes sure the modality cost is valid otherwise it triggers an alert Jeremy Hall
                        if(!InputValidation.validateCost(cost.getText())) {
                            return;
                        }
                        String sql = "UPDATE orderCodes SET cost = '" + cost.getText() + "' WHERE orderID = '" + z.getOrderID() + "' ";
                        App.executeSQLStatement(sql);
                        String sql2 = "UPDATE orderCodes SET orders = '" + order.getText() + "' WHERE orderID = '" + z.getOrderID() + "' ";
                        App.executeSQLStatement(sql2);
                        populateTableModalities();
                        stage.close();
                    }

         });
    }

    //</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="Patient Alerts Section">
    //creates a page to view patient health alerts  
    //like what they are alergic to and how that affects which modalities they can use
    //should have and edit button for existing flags Jeremy Hall
    private void patientAlertsPageView() {
        patientAlertsContainer.getChildren().clear();

        main.setCenter(patientAlertsContainer);
        //creates table to house information Jeremy Hall
        createTablePatientAlerts();
        //add information to the table Jeremy Hall
        populatePatientAlerts();

        Button addPatientAlert = new Button("Add Patient Alert");
        HBox btnContainer = new HBox(addPatientAlert);

        patientAlertsContainer.getChildren().addAll(table, btnContainer);
        patientAlertsContainer.setSpacing(10);
        //added to create padding for the table Jeremy Hall
        patientAlertsContainer.setPadding(new Insets(20));

        users.setId("navbar");
        patients.setId("navbar");
        appointments.setId("navbar");
        modalities.setId("navbar");
        patientAlerts.setId("selected");
        
        //button used to add new alerts Jeremy Hall
        addPatientAlert.setOnAction(eh -> addPatientAlert());
    }

    //creates the table for patient alerts and other information Jeremy Hall
    private void createTablePatientAlerts() {
        table.getColumns().clear();
        //All of the Columns
        TableColumn alertIDCol = new TableColumn("ID");
        TableColumn alertCol = new TableColumn("Alert");
        TableColumn flagsCol = new TableColumn("Flags");
        TableColumn button1Col = new TableColumn("Add Flag");
        TableColumn buttonCol = new TableColumn("Delete");
        TableColumn button2Col = new TableColumn("Edit");

        //And all of the Value setting
        alertIDCol.setCellValueFactory(new PropertyValueFactory<>("alertID"));
        alertCol.setCellValueFactory(new PropertyValueFactory<>("alert"));
        flagsCol.setCellValueFactory(new PropertyValueFactory<>("flags"));
        button1Col.setCellValueFactory(new PropertyValueFactory<>("placeholder1"));
        buttonCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));
        button2Col.setCellValueFactory(new PropertyValueFactory<>("placeholder2"));

        //Couldn't put all the styling
        alertIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.09));
        alertCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        flagsCol.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        buttonCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        button1Col.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        button2Col.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        //Together again
        table.getColumns().addAll(alertIDCol, alertCol, flagsCol, button1Col, buttonCol, button2Col);
        //Add Status Update Column:
    }

    //used to add information to patient alert table from sql database Jeremy Hall
    private void populatePatientAlerts() {
        table.getItems().clear();
        //Connect to database
        String sql = "Select patientAlerts.alertID, patientAlerts.alert "
                + " FROM patientAlerts "
                + " "
                + " ;";

        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<PatientAlert> list = new ArrayList<PatientAlert>();
            while (rs.next()) {
                //What I receieve:  patientID, email, full_name, dob, address, insurance
                PatientAlert pa = new PatientAlert(rs.getString("alertID"), rs.getString("alert"), getFlagsFromDatabase(rs.getString("alertID")));

                //used for deleting alerts Jeremy Hall
                pa.placeholder.setText("Delete Alert");
                pa.placeholder.setId("cancel");
                pa.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                    	deletePatientAlert(pa);
                    }
                });
                
                //used to add flags Jeremy Hall
                pa.placeholder1.setText("Add Flag");
                pa.placeholder1.setId("complete");
                pa.placeholder1.setOnAction(eh -> addFlag(pa));
    
                //used to edit patient alerts Jeremy Hall
                pa.placeholder2.setText("Edit");
                pa.placeholder2.setId("change");
                pa.placeholder2.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                    	editPatientAlert(pa);
                        //populatePatientAlerts();
                    }
                });
                
                list.add(pa);

            }
            table.getItems().addAll(list);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    //used to force the user to confirm the deletion of a patient alert 
    //to stop a miss click or other simple accident of deleting an alert Jeremy Hall
    private void deletePatientAlert(PatientAlert pa) {
   	    Stage stage = new Stage();
   	    stage.initOwner(this);
        stage.setTitle("Delete Patient Alert");
        stage.initModality(Modality.WINDOW_MODAL);
        BorderPane borderPane = new BorderPane();
        
        Label confirmTxt = new Label("Type 'CONFIRM' to continue");
        TextField confirm = new TextField();
        HBox cont = new HBox(confirmTxt, confirm);
        Button submit = new Button("Submit");
        submit.setId("cancel");
        
        VBox helper = new VBox(cont,submit);
        helper.setPadding(new Insets(20));
        helper.setSpacing(10);
        helper.setAlignment(Pos.CENTER); 
        borderPane.setCenter(helper);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));
        stage.show();
        
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
           	 if(!InputValidation.validateConfirm(confirm.getText())) {
                    return;
                }
           	 
           	 	String sql = "DELETE FROM patientAlerts WHERE alertID = '" + pa.getAlertID() + "' ";
           	 	App.executeSQLStatement(sql);
            	sql = "DELETE FROM flags WHERE alertID = '" + pa.getAlertID() + "' ";
            	App.executeSQLStatement(sql);
            	sql = "DELETE FROM alertsPatientConnnector WHERE alertID = '" + pa.getAlertID() + "' ";
            	App.executeSQLStatement(sql);
            	populatePatientAlerts();
                stage.close();
            }
        });
   
    }

    //allows for the editing and changing of existing patient alerts
    private void editPatientAlert(PatientAlert pa) {
    	
    	Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Edit Modality");
        stage.initModality(Modality.WINDOW_MODAL);
        BorderPane borderPane = new BorderPane();
   
        //creates buttons for changing the name or removing a specific flag on a patient alert
        Button updatePatientAlertName = new Button("Change Patient Alert Name");
        Button updateFlags = new Button("Remove Flag");

        HBox buttonContainer = new HBox(updatePatientAlertName, updateFlags);
        buttonContainer.setSpacing(20);
        Button submit = new Button("Submit");
        submit.setId("complete");
        
        //used for when button it clicked and values need to be entered Jeremy Hall
        Label txt = new Label("Enter the new Patient Alert name: ");
        TextField order = new TextField("");
        order.setPrefWidth(200);
        HBox nameInput = new HBox(txt, order);
        nameInput.setVisible(false);


        
        Label text1 = new Label("Order to remove: ");
        text1.setPrefWidth(150);
        ComboBox dropdown = populateOrdersDropdown();
        HBox container = new HBox(text1, dropdown);
        Label confirmTxt = new Label("Type 'CONFIRM' to continue");
        TextField confirm = new TextField();
        HBox cont = new HBox(confirmTxt, confirm);
        container.setVisible(false);
        cont.setVisible(false);
        //End Hidden Containers
        //
        
        VBox center = new VBox(buttonContainer, nameInput, container, cont, submit);
        center.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));
        stage.show();
        
  

        //if the remove flag button is clicked Jeremy Hall
        updateFlags.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
            	
            	 center.getChildren().remove(buttonContainer);
                 container.setVisible(true);
                 cont.setVisible(true);
                 submit.setId("cancel");
                 submit.setOnAction(eh2 -> removeFlag());

            }

           
            private void removeFlag() {
            	
            	//checks if a modality was selected 
            	if (!dropdown.getValue().toString().isBlank()) {
            		
            		//Validation that CONFIRM was entered
                    if (!InputValidation.validateConfirm(confirm.getText())) {
                        return;
                    }
                    
                    //if the modality choosen is part of the patient alert it deletes the flag
                    if (pa.getFlags().contains(dropdown.getValue().toString())) {
                    	String sql = "DELETE FROM flags WHERE alertID = '" + pa.getAlertID() + "' "
                    	+ "AND orderID = (SELECT orderID FROM orderCodes WHERE orders = '"+ dropdown.getValue().toString() + "') ";
                        App.executeSQLStatement(sql);
                        populatePatientAlerts();
                        stage.close();
                    }
                    //if the modality chosen was not a part in the patient alert
                    else {
                    	Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText("Try Again");
                        alert.setContentText("The Patient Alert does not have that Order\n");
                        alert.show();
                        return;
                    }
                 }
            }
        });

        //if the updatePatientAlertName button is clicked
        updatePatientAlertName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
            	
            	//sets the input block to visible and uses it to change the patient alert name Jeremy Hall
                center.getChildren().remove(buttonContainer);
                nameInput.setVisible(true);
                submit.setId("complete");
                submit.setOnAction(eh2 -> updateName());
            }

            //updates the patient alert name in the sql database Jeremy Hall
            private void updateName() {
                String sql = "UPDATE patientAlerts SET alert = '" + order.getText() + "' WHERE alertID = '" + pa.getAlertID() + "' ";
                App.executeSQLStatement(sql);
                populatePatientAlerts();
                stage.close();
                
            }
        });	
    }
    
    
    
  	//called by addUser button to add a patient alert
    //creates a separate small page to add in the information
    //stage x to stage stage BorderPane y to BorderPane borderPane Jeremy Hall
    private void addPatientAlert() {
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Add Patient Alert");
        stage.initModality(Modality.WINDOW_MODAL);
        BorderPane borderPane = new BorderPane();
        
        //used by user to enter in the new alert Jeremy Hall
        //made the GUI look nice and added spacing between the items
        Label txt = new Label("Please enter the name for the alert: ");
        TextField alert = new TextField("Ex: Allergic to peanuts");
        HBox helper = new HBox(txt,alert);
        alert.setPrefWidth(200);
        Button submit = new Button("Submit");
        submit.setId("complete");

        VBox center = new VBox(helper, submit);

        center.setAlignment(Pos.CENTER);
        center.setSpacing(10);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));
        stage.show();

        //alert is added to sql database Jeremy Hall
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                String sql = "INSERT INTO patientAlerts(alert) VALUES ('" + alert.getText() + "') ;";
                App.executeSQLStatement(sql);
                populatePatientAlerts();
                stage.close();
            }

        });
    }
//

    //used to get flags in the sql database Jeremy Hall
    //flags = modalities like CT Scan X-ray
    private String getFlagsFromDatabase(String aInt) {

        String val = "";
        String sql = "Select orderCodes.orders "
                + " FROM flags "
                + " INNER JOIN orderCodes ON flags.orderID = orderCodes.orderID "
                + " WHERE alertID = '" + aInt + "' "
                + ";";

        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<PatientAlert> list = new ArrayList<PatientAlert>();
            while (rs.next()) {
                //What I receieve:  patientID, email, full_name, dob, address, insurance
                val += rs.getString("orders") + ", ";
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return val;
    }

    //called by addFlag button to add a flag
    //creates a separate small page to add in the information
    //stage x to stage stage BorderPane y to BorderPane borderPane Jeremy Hall
    private void addFlag(PatientAlert pa) {
    	//add click to remove before the buttons
        ArrayList<String> modalities = new ArrayList<String>();
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Add Flag");

        stage.initModality(Modality.WINDOW_MODAL);
        
        //used to allow the new flag to be selected and entered Jeremy Hall
        BorderPane borderPane = new BorderPane();
        Label txt = new Label("Select: ");

        Button submit = new Button("Submit");
        submit.setId("complete");

        ComboBox orders = populateOrdersDropdown();
        orders.setPrefWidth(100);
        HBox buttonContainer = new HBox();
        buttonContainer.setSpacing(10);
        buttonContainer.setPadding(new Insets(10));
        Text tutorial = new Text("Click to remove: ");
        buttonContainer.getChildren().add(tutorial);
        buttonContainer.setAlignment(Pos.CENTER);
        VBox center = new VBox(txt, orders, buttonContainer, submit);

        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));
        stage.setHeight(400);
        stage.setWidth(500);
        stage.show();

        //adds modalities selected into sql database and reloads the table Jeremy Hall
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                for (String z : modalities) {
                    String sql = "INSERT INTO flags VALUES ('" + pa.getAlertID() + "', (SELECT orderID FROM orderCodes WHERE orders = '" + z + "') )";
                    App.executeSQLStatement(sql);
                }
                populatePatientAlerts();
                stage.close();
            }

        });

        //When a modality is chosen from the dropdown menu it is added to the page as a button Jeremy Hall
        //adds a MRI button when it is chosen
        orders.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {

            	
                modalities.add(orders.getValue().toString());
                Button temp = new Button(orders.getValue().toString());
                buttonContainer.getChildren().add(temp);
                username.setId("navbar");  
                
                //if the created button is clicked then it is removed and will not be added to the sql database Jeremy Hall
                //if this MRI button is clicked it is removed and will no longer be added as a flag 
                temp.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {  
                        if (!orders.getValue().toString().isBlank()) {
                            stage.setTitle("Add a Flag");
                            modalities.remove(temp.getText());
                            buttonContainer.getChildren().remove(temp);
                        }
                    } 
                });
            }

        });

    }

    //dropdown menu with all the orders to allow one to be selected Jeremy Hall
    private ComboBox populateOrdersDropdown() {
        String sql = "Select orders "
                + " FROM orderCodes;";
        ComboBox dropdown = new ComboBox();
        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
                dropdown.getItems().add(rs.getString("orders"));
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dropdown;
    }
    //</editor-fold>
//
}