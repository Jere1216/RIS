package aasim.ris;

import static aasim.ris.App.ds;
import static aasim.ris.App.url;
import datastorage.Appointment;
import datastorage.InputValidation;
import datastorage.Patient;
import datastorage.PatientAlert;
import datastorage.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.postgresql.ds.PGSimpleDataSource;

public class ReferralDoctor extends Stage {
    //Navbar

    HBox navbar = new HBox();
    Label username = new Label("Logged In as Doctor: " + App.user.getFullName());
    ImageView pfp = new ImageView(App.user.getPfp());

    Button logOut = new Button("Log Out");

    //End Navbar
    //table
    TableView patientsTable = new TableView();
    TableView appointmentsTable = new TableView();
    HBox buttonContainer = new HBox();
    VBox tableContainer = new VBox(patientsTable, buttonContainer);
    //
    //Scene
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);

    //End Scene
    private FilteredList<Patient> flPatient;
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Patients");
    HBox searchContainer = new HBox(choiceBox, search);

    ArrayList<PatientAlert> paList = new ArrayList<PatientAlert>(); //GeneralOverview 
    ArrayList<PatientAlert> allergies = new ArrayList<PatientAlert>(); //Specific to the Patient

    //this is the main page for the Referral Doctor
    //it provides basic information like name, patient id, email 
    //the ability to add patients and access a patient overview which is a separate page
    public ReferralDoctor() {
    	
        this.setTitle("RIS - Radiology Information System (Doctor View)");
        //Navbar
        pfp.setPreserveRatio(true);
        pfp.setFitHeight(38);
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });
        username.setId("navbar");
        username.setOnMouseClicked(eh -> userInfo());
        navbar.getChildren().addAll(username, pfp, logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        //End navbar

        //Center
        //set the sizes of both tables so that less space is wasted on both pages 
        //ei the main referral doctor page and patient overview page Jeremy Hall
        patientsTable.setMinHeight(600);
        appointmentsTable.setMinHeight(560);
 
        tableContainer.setSpacing(10);
        //added to give padding to patientsTable so that it looks better and 
        //doesn't go to the edges of the screen Jeremy Hall
        tableContainer.setPadding(new Insets(20));
        main.setCenter(tableContainer);
        
        //creates table for patient data Jeremy Hall
        createTablePatients();
        //adds patient data to the table Jeremy Hall
        populateTable();
        //Button Container
        buttonContainer.setSpacing(10);
        buttonContainer.setPadding(new Insets(5));

        //adds add patient button and calls addPatient(); if it is clicked Jeremy Hall
        Button addPatient = new Button("Add Patient");
        addPatient.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                addPatient();
            }

        });
        buttonContainer.getChildren().addAll(addPatient);
        //End Button Container
        //End Center
        //Searchbar Structure
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.setValue("Patient ID");
        choiceBox.getItems().addAll("Patient ID", "Full Name", "Email", "Date of Birth", "Insurance");
        choiceBox.setValue("Appointment ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
        	//replaced with if chain with switch
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
        	
            patientsTable.getItems().clear();
            patientsTable.getItems().addAll(flPatient);
        });
        buttonContainer.getChildren().add(searchContainer);
        //End Searchbar Structure

        //Set Scene and Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);

        //calls this to get patient alerts like being allergic to rubber
        populatePaList();
    }

    //creates the table and columns to hold patient information
    //same code as in admin page
    private void createTablePatients() {
        //All of the Columns
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn fullNameCol = new TableColumn("Name");
        TableColumn emailCol = new TableColumn("Email");
        TableColumn DOBCol = new TableColumn("D.O.B");
        TableColumn updateStatusCol = new TableColumn("Patient Information");

        //And all of the Value setting
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        DOBCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
        updateStatusCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        
        //fixed to display the the table values with more room for each and make it easier to find information Jeremy Hall
        patientIDCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        fullNameCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        emailCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        DOBCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        updateStatusCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));

        patientsTable.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //back together again
        patientsTable.getColumns().addAll(patientIDCol, fullNameCol, emailCol, DOBCol, updateStatusCol);
    }

    //gets information from sql database to add in patient information Jeremy Hall
    //same code as in admin page
    private void populateTable() {
        patientsTable.getItems().clear();
        //Connect to database

        String sql = "Select docPatientConnector.patientID, patients.email, patients.full_name, patients.dob, patients.address, patients.insurance"
                + " FROM docPatientConnector"
                + " INNER JOIN patients ON docPatientConnector.patientID = patients.patientID"
                + " WHERE docPatientConnector.referralDocID = '" + App.user.getUserID() + "';";

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
                        patientOverviewScreen(z);
                    }

                });
            }

            flPatient = new FilteredList(FXCollections.observableList(list), p -> true);
            patientsTable.getItems().addAll(flPatient);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //allows the user to logout
    //stage x to stage stage
    private void logOut() {
        App.user = new User();
        Stage stage = new Login();
        stage.show();
        stage.setMaximized(true);
        this.close();
    }

    //allows the user to see user info
    //stage x to stage stage
    private void userInfo() {
        Stage stage = new UserInfo();
        stage.show();
        stage.setMaximized(true);
        this.close();
    }

    //called by addPatient button to add a patient 
    //creates a separate small page to add in the information
    //stage x to stage stage BorderPane y to BorderPane borderPane Jeremy Hall
    private void addPatient() {

        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Add Patient");
        stage.initModality(Modality.WINDOW_MODAL);
        BorderPane borderPane = new BorderPane();
        
        //used to see if patient full name and email already exist in the sql database 
        //if it does then a alert is generated Jeremy Hall
        Label text1 = new Label("Full Name: ");
        TextField name = new TextField("");
        name.setPrefWidth(150);
        Label text = new Label("Email: ");
        TextField email = new TextField();
        email.setPrefWidth(150);
        Button pullData = new Button("Check for Patient");
        //enter name then email instead of email then name Jeremy Hall
        HBox container = new HBox(text1, name, text, email, pullData);
        
        //Hidden Containers
        //become visible if patient full name and email entered doesn't match ones in the sql database Jeremy Hall
        Label text2 = new Label("Date of Birth:\n(Press Enter after Entering date)\n(MM/DD/YYYY)");
        text2.setPrefWidth(120);
        text2.setWrapText(true);
        DatePicker datePicker = new DatePicker();
        Label text3 = new Label("Address: ");
        TextField address = new TextField("");
        address.setPrefWidth(200);
        Label text4 = new Label("Insurance: ");
        TextField insurance = new TextField("");
        insurance.setPrefWidth(200);
        //
        
        //if they already exist in the database
        Label text10 = new Label("This patient already exists in the system");
        Button hiddenAdd = new Button("Add exsting patient");
        hiddenAdd.setId("complete");
        Button hiddenReject = new Button("Do not add existing patient");
        hiddenReject.setId("cancel");
        HBox container2 = new HBox(text10,hiddenAdd,hiddenReject);
        container2.setVisible(false);
        container2.setSpacing(20);
        
        //used to add patient alerts to new patients 
        //each alert in the patient alert list paList has a label and a dropdown menu Jeremy Hall
        ArrayList<PatientAlert> alertsToAddForThisPatient = new ArrayList<PatientAlert>();
        VBox patientAlertContainer = new VBox();
        ArrayList<HBox> hbox = new ArrayList<HBox>();
        for (int i = 0; i < (paList.size() / 2) + 1; i++) {
            hbox.add(new HBox());
        }
        int counter = 0;
        int hboxCounter = 0;
        for (PatientAlert z : paList) {
            if (counter > 2) {
                hboxCounter++;
                counter = 0;
            }
            Label label = new Label(z.getAlert());
            ComboBox dropdown = new ComboBox();
            dropdown.getItems().addAll("Yes", "No");
            dropdown.setValue("No");

            HBox temp = new HBox(label, dropdown);
            temp.setSpacing(10);
            temp.setPadding(new Insets(10));

            hbox.get(hboxCounter).getChildren().add(temp);

            //checks to see what value between yes and no was choosen for each patient alert 
            //then adds or removes them from the new patient accordingly Jeremy Hall
            dropdown.setOnAction(new EventHandler() {
                @Override
                public void handle(Event eh) {
                    if (dropdown.getValue().toString().equals("Yes")) {
                        alertsToAddForThisPatient.add(z);
                    } else if (dropdown.getValue().toString().equals("No")) {
                        alertsToAddForThisPatient.remove(z);
                    }
                }
            });
            counter++;
        }
        for (HBox cont : hbox) {
            patientAlertContainer.getChildren().add(cont);
        }
        //
        //used to organize inputs for new patient's data Jeremy Hall
        ScrollPane s1 = new ScrollPane(patientAlertContainer);
        s1.setPrefHeight(200);
        s1.setVisible(false);

        HBox hiddenContainer1 = new HBox(text2, datePicker, text3, address);
        HBox hiddenContainer3 = new HBox(text4, insurance);
        Button hiddenSubmit = new Button("Create New Patient");

        text1.setPrefWidth(100);
        text2.setPrefWidth(100);
        text3.setPrefWidth(100);
        text4.setPrefWidth(100);
        hiddenContainer1.setSpacing(10);
        hiddenContainer1.setPadding(new Insets(10));

        hiddenContainer3.setSpacing(10);
        hiddenContainer3.setPadding(new Insets(10));

        hiddenSubmit.setPadding(new Insets(10));

        hiddenContainer1.setVisible(false);
        hiddenContainer3.setVisible(false);
        hiddenSubmit.setVisible(false);
        //End Hidden Containers
        Label text5 = new Label("Patient ID: ");
        text5.setPrefWidth(150);
        VBox center = new VBox(container, container2, hiddenContainer1, hiddenContainer3, s1, hiddenSubmit);

        container.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setSpacing(10);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));
        stage.show();

        //gets the date from the date picker Jeremy Hall
        datePicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                LocalDate date = datePicker.getValue();
            }
        });

        //validates the values entered and then checks if the name and email entered already exist
        //if they do it creates an error if not it sets the hidden inputs for patient data to visible Jeremy Hall
        pullData.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                //validation
                if (!InputValidation.validateName(name.getText())) {
                    return;
                }
                if (!InputValidation.validateEmail(email.getText())) {
                    return;
                }
                //end validation
                Patient temp = checkDatabaseForPatient(name.getText(), email.getText());
                //Temp != null (patient already exists)
                if (temp != null) {
                     //removed previous items as patient information can be updated through 
                	 //the patient overview page and that page shows all the information this small page would
                	 //instead it asks the user if they want to add the this exsting patient or not Jeremy Hall
             
 	                 email.setEditable(false);
 	                 name.setEditable(false);
 	                 pullData.setVisible(false);
 	                 container2.setVisible(true);
 	                 
 	                 //adds the patient 
 	                 hiddenAdd.setOnAction(new EventHandler<ActionEvent>() {
 	                     @Override
 	                     public void handle(ActionEvent eh) {
 	                        
 	                        String sql1 = "INSERT INTO docPatientConnector "
 	                                + " VALUES ('" + App.user.getUserID() + "', '" + temp.getPatientID() + "');";
 	                        App.executeSQLStatement(sql1);
 	                        populateTable();
 	                        stage.close();
 	                     }

 	                 });
 	                 
 	                 //does not add the patient 
 	                 hiddenReject.setOnAction(new EventHandler<ActionEvent>() {
	                      @Override
	                      public void handle(ActionEvent eh) {
	                         stage.close();
	                      }

	                  });
 	                 
 	               
                }
                //added else to only show hidden inputs if user is not in the database Jeremy Hall
                else {

	                s1.setVisible(true);
	                email.setEditable(false);
	                name.setEditable(false);
	                hiddenContainer1.setVisible(true);
	                hiddenContainer3.setVisible(true);
	                pullData.setVisible(false);
	                hiddenSubmit.setVisible(true);
                }
            }
        });

        //validates the values entered are valid and if they are the patient is created and added to the sql database Jeremy Hall
        hiddenSubmit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                //Validation
                if (!InputValidation.validateDOB(datePicker.getValue().toString())) {
                    return;
                }
                if (!InputValidation.validateAddress(address.getText())) {
                    return;
                }

                if (!InputValidation.validateInsurance(insurance.getText())) {
                    return;
                }
                //End Validation
                insertPatientIntoDatabase(name.getText(), email.getText(), datePicker.getValue().toString(), address.getText(), insurance.getText());
                populateTable();
                for (PatientAlert z : alertsToAddForThisPatient) {
                    String sql = "INSERT INTO alertsPatientConnector VALUES ( (SELECT patientID FROM patients WHERE full_name = '" + name.getText() + "' AND email = '" + email.getText() + "') , '" + z.getAlertID() + "');";
                    App.executeSQLStatement(sql);
                }
                stage.close();
            }

        });
        
    }

    //used to check to see if name and email already exist in the sql database Jeremy Hall
    private Patient checkDatabaseForPatient(String name, String email) {
        Patient temp = null;

        String sql = "Select * "
                + " FROM patients"
                + " WHERE email = '" + email + "' AND full_name = '" + name + "';";

        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            while (rs.next()) {
                //What I receieve:  patientID, email, full_name, dob, address, insurance
                temp = new Patient(rs.getString("patientID"), rs.getString("email"), rs.getString("full_name"), rs.getString("dob"), rs.getString("address"), rs.getString("insurance"));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return temp;
    }

    //inserts new patient information into sql database Jeremy Hall
    private void insertPatientIntoDatabase(String name, String email, String dob, String address, String insurance) {
        String sql = "INSERT INTO patients(full_name, email, dob, address, insurance) "
                + " VALUES ('" + name + "','" + email + "','" + dob + "','" + address + "','" + insurance + "');";
        App.executeSQLStatement(sql);

        String patientID = checkDatabaseForPatient(name, email).getPatientID();
        String sql1 = "INSERT INTO docPatientConnector "
                + " VALUES ('" + App.user.getUserID() + "', '" + patientID + "');";
        App.executeSQLStatement(sql1);

    }

    //creates a separate page for a patient it includes patient information like name, email at the top
    //table like appointment id, orders(CT Scan, X-Ray), status and radiology reports Jeremy Hall
    private void patientOverviewScreen(Patient z) {
    	//gets allergies and alerts based on the patient
        populateAllergies(z);
        // Appointments table
        appointmentsTable.getColumns().clear();
        //Patient Info
        Label text = new Label("Name: " + z.getFullName() + "\t\t Email: " + z.getEmail() + "\t\t Date of Birth: " + z.getDob());
        Label text1 = new Label("Address: " + z.getAddress() + "\t\t Insurance Provider: " + z.getInsurance() + "\t\t Orders Requested: " + getPatOrders(z.getPatientID()));
        Label text2 = new Label(); 
        
        VBox patInfo = new VBox();
        
        //uses a label to store patient alergies at the top with other patient information 
        //each allergy added concatenated with the previous ones in the label
        //much simpler that how they orginally did and it doesn't conflict with the padding and larger table
        for (PatientAlert i : allergies) {
            text2.setText(text2.getText() + " \t\t " + i.getAlert());
        }
        
        if(text2.getText() == null || text2.getText().isBlank()) {
        	text2.setText(text2.getText() +"Allergies: None");
        	patInfo.getChildren().addAll(text, text1,text2);
        	patInfo.setAlignment(Pos.CENTER);
        }
        else {
        	patInfo.getChildren().addAll(text, text1,text2);
	        patInfo.setAlignment(Pos.CENTER);
        }

        //Create appointments table columns
        TableColumn apptIDCol = new TableColumn("Appt. ID");
        TableColumn timeCol = new TableColumn("Time");
        TableColumn ordersCol = new TableColumn("Orders");
        TableColumn statusCol = new TableColumn("Status");
        //changed to View Radiology Reports as it holds Radiology Reports
        TableColumn updateStatusCol = new TableColumn("View Radiology Reports");
        //Set tableColumn getters
        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        ordersCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusAsLabel"));
        updateStatusCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));
        //Set tableColumn sizes
        //readjusted to fit data better and make it easier to read Jeremy Hall
        apptIDCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        timeCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        ordersCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        statusCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        updateStatusCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        appointmentsTable.getColumns().addAll(apptIDCol, timeCol, ordersCol, statusCol, updateStatusCol);
        //Buttons
        Button goBack = new Button("Go Back");
        Button delete = new Button("Remove Patient from View");
        Button updatePatientInformation = new Button("Update Patient Information");
        Button newOrder = new Button("New Order");
        Button removeOrder = new Button("Remove Order");
        goBack.setId("cancel");
        delete.setId("cancel");
        newOrder.setId("complete");
        HBox toTheLeft = new HBox(goBack);
        HBox.setHgrow(toTheLeft, Priority.ALWAYS);
        HBox toTheRight = new HBox(delete);
        HBox.setHgrow(toTheRight, Priority.ALWAYS);
        toTheRight.setAlignment(Pos.CENTER_RIGHT);
        HBox overviewButtonContainer = new HBox(toTheLeft, newOrder, removeOrder, updatePatientInformation, toTheRight);
        overviewButtonContainer.setSpacing(20);
        //End Buttons
        
        //doesn't work and show alerts needs to be fixed Jeremy Hall
        //removed and allergies show with patient information

        VBox overviewContainer = new VBox(patInfo, appointmentsTable, overviewButtonContainer);
        overviewContainer.setSpacing(10);
        overviewContainer.setPadding(new Insets(20));
        main.setCenter(overviewContainer);
        //
        populateAppointmentsTable(z);
        //

        //adds new orders by using button that calls createNewOrder(z); Jeremy Hall
        newOrder.setOnAction((ActionEvent e) -> {
            createNewOrder(z);
            text2.setText("Orders Requested: " + getPatOrders(z.getPatientID()));
        });
        
        //removes orders by using removeOrder button that calls removeOrder(z); Jeremy Hall
        removeOrder.setOnAction((ActionEvent e) -> {
            removeOrder(z);
            text2.setText("Orders Requested: " + getPatOrders(z.getPatientID()));
        });
        
        //goes back to the previous main page Jeremy Hall
        goBack.setOnAction((ActionEvent e) -> {
            appointmentsTable.getColumns().clear();
            main.setCenter(tableContainer);
        });
        
        //removes patient from the doctor's view by using delete button that calls removePatientFromView(z); Jeremy Hall
        delete.setOnAction((ActionEvent e) -> {
            removePatientFromView(z);
        });
        
        //used to update patient information by using updatePatientInformation that calls updatePatient(z); Jeremy Hall
        updatePatientInformation.setOnAction((ActionEvent e) -> {
            updatePatient(z);
        });

    }

    //used to update patient information like email, address, and allergies
    //creates a separate small page to change the information
    //stage x to stage stage Jeremy Hall
    private void updatePatient(Patient z) {
    	
    	//creates the page Jeremy Hall
        VBox container = new VBox();
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Update Patient");
        Scene scene = new Scene(container);
        stage.setScene(scene);
        //stage height increased to 450 to make the items fit with spacing
        stage.setHeight(450);
        stage.setWidth(300);
        scene.getStylesheets().add("file:stylesheet.css");
        
        //creates labels to input new information while showing the old info Jeremy Hall
        Label emailLabel = new Label("Email: ");
        TextField email = new TextField(z.getEmail());
        HBox emailContainer = new HBox(emailLabel, email);

        Label addressLabel = new Label("Address: ");
        TextField address = new TextField(z.getAddress());
        HBox addressContainer = new HBox(addressLabel, address);

        Label insuranceLabel = new Label("Insurance: ");
        TextField insurance = new TextField(z.getInsurance());
        HBox insuranceContainer = new HBox(insuranceLabel, insurance);

        Button submit = new Button("Submit");
        submit.setId("complete");
        //added to center the submit button Jeremy Hall
        HBox button = new HBox();
        button.getChildren().addAll(submit);
        button.setAlignment(Pos.CENTER);
        

        //used to create dropdowns for allergies and if the patient has them 
        ArrayList<PatientAlert> alertsToAddForThisPatient = new ArrayList<PatientAlert>();
        ArrayList<PatientAlert> alertsToRemoveForThisPatient = new ArrayList<PatientAlert>();
        VBox patientAlertContainer = new VBox();
        for (PatientAlert a : paList) {
            Label label = new Label(a.getAlert());
            ComboBox dropdown = new ComboBox();
            dropdown.getItems().addAll("Yes", "No");
            if (allergies.contains(a)) {
                dropdown.setValue("Yes");
            } else {
                dropdown.setValue("No");
            }
            HBox temp = new HBox(label, dropdown);
            temp.setSpacing(10);
            temp.setPadding(new Insets(10));

            patientAlertContainer.getChildren().add(temp);

            //used to update allergies based on if yes or no is chosen for an allergy Jeremy Hall
            dropdown.setOnAction(new EventHandler() {
                @Override
                public void handle(Event eh) {
                    if (dropdown.getValue().toString().equals("Yes")) {
                        alertsToAddForThisPatient.add(a);
                        alertsToRemoveForThisPatient.remove(a);
                    } else if (dropdown.getValue().toString().equals("No")) {
                        alertsToAddForThisPatient.remove(a);
                        alertsToRemoveForThisPatient.add(a);
                    }
                }
            });
        }

        ScrollPane s1 = new ScrollPane(patientAlertContainer);
        s1.setPrefHeight(200);

        container.getChildren().addAll(emailContainer, addressContainer, insuranceContainer, s1, button);
        //spacing added to make the page look better
        container.setSpacing(10);
        stage.show();

        //checks if values inputed are valid if they are then 
        //it submits the information and update the patient info and info inside of sql database Jeremy Hall
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                //Validation
                if (!InputValidation.validateEmail(email.getText())) {
                    return;
                }
                if (!InputValidation.validateAddress(address.getText())) {
                    return;
                }
                if (!InputValidation.validateInsurance(insurance.getText())) {
                    return;
                }
                //End Validation
                z.setAddress(address.getText());
                z.setEmail(email.getText());
                z.setInsurance(insurance.getText());
                String sql = "UPDATE patients SET email = '" + email.getText() + "', address = '" + address.getText() + "', insurance = '" + insurance.getText() + "' WHERE patientID = '" + z.getPatientID() + "';";
                App.executeSQLStatement(sql);
                for (PatientAlert a : alertsToAddForThisPatient) {
                    sql = "INSERT INTO alertsPatientConnector VALUES ( '" + z.getPatientID() + "', '" + a.getAlertID() + "');";
                    App.executeSQLStatement(sql);
                }
                for (PatientAlert a : alertsToRemoveForThisPatient) {
                    sql = "DELETE FROM alertsPatientConnector WHERE patientID = '" + z.getPatientID() + "' AND alertID = '" + a.getAlertID() + "';";
                    App.executeSQLStatement(sql);
                }

                stage.close();
                appointmentsTable.getColumns().clear();
                patientOverviewScreen(z);
            }
        });
    }

    //used remove a patient from doctor view 
    //creates a separate small page to confirm this action
    //stage x to stage stage Jeremy Hall
    private void removePatientFromView(Patient z) {
    	//creates small page to confirm the delete
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Add Order");
        stage.initModality(Modality.WINDOW_MODAL);
        Label text1 = new Label("Are you sure? Type 'CONFIRM' without quotes to continue ");
        //got rid of why in the text field has no reason to be there Jeremy Hall
        TextField text = new TextField("");
        Button killIt = new Button("Confirm Delete");
        killIt.setId("cancel");
        VBox cont = new VBox(text1, text, killIt);
        //added to make the page look nicer and items have spacing between them and the borders Jeremy Hall
        cont.setAlignment(Pos.CENTER);
        cont.setSpacing(10);
        cont.setPadding(new Insets(10));
        
        cont.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(cont));
        
        //validates that CONFIRM was typed and then
        //removes connection of doctor and patient in sql database Jeremy Hall
        killIt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!InputValidation.validateConfirm(text.getText())) {
                    return;
                }
                String sql = "DELETE FROM docPatientConnector WHERE patientID = '" + z.getPatientID() + "' AND referralDocID = '" + App.user.getUserID() + "';";
                App.executeSQLStatement(sql);
                stage.close();
                populateTable();
                appointmentsTable.getColumns().clear();
                main.setCenter(tableContainer);

            }
        });

        stage.showAndWait();

    }

    //gets data to fill in appointmentsTable from sql database 
    //appointment id, time, orders and more Jeremy Hall
    private void populateAppointmentsTable(Patient pat) {
        appointmentsTable.getItems().clear();
        //Connect to database

        String sql = "Select appointments.appt_id, appointments.time, statusCode.status"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID "
                + " WHERE patient_id = '" + pat.getPatientID() + "' AND statusCode < 7"
                + " ORDER BY time ASC;";

        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Appointment> list = new ArrayList<Appointment>();
            while (rs.next()) {
                //What I receieve:  
                Appointment temp = new Appointment(rs.getString("appt_id"), pat.getPatientID(), rs.getString("time"), rs.getString("status"), getPatOrders(pat.getPatientID(), rs.getString("appt_id")));
                list.add(temp);
            }

            for (Appointment x : list) {
                if (x.getStatus().contains(".")) {
                    x.placeholder.setText("View Radiology Report");
                    if (!x.getStatus().contains("Signature")) {
                        x.placeholder.setId("complete");
                    }
                    x.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            viewRadiologyReport(pat, x);
                        }
                    });

                } else {
                    x.placeholder.setText("Radiology Report not created yet");
                    x.placeholder.setId("cancel");
                }
            }

            appointmentsTable.getItems().addAll(list);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //takes a string
    //used to get patient orders like MRI, CT Scan orders from sql database Jeremy Hall
    private String getPatOrders(String patientID) {

        String sql = "Select orderCodes.orders "
                + " FROM patientOrders "
                + " INNER JOIN orderCodes ON patientOrders.orderCodeID = orderCodes.orderID "
                + " WHERE patientID = '" + patientID + "';";
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

    //overloaded method that takes string and int
    //used to get patient orders like MRI, CT Scan orders from sql database Jeremy Hall
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

    //used create a order (CT Scan, MRI) for a patient
    //creates a separate small page to allow the choosing of a modality
    //stage x to stage stage BorderPane y  to BorderPane borderPane  Jeremy Hall
    private void createNewOrder(Patient z) {
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Add Order");
        stage.initModality(Modality.WINDOW_MODAL);
        BorderPane borderPane = new BorderPane();
        Label text1 = new Label("Order: ");
        
        // removed text1.setPrefWidth(200); so it looks better

        ComboBox dropdown = populateOrdersDropdown();
        Button insertOrder = new Button("Create New Order");
        HBox container = new HBox(text1, dropdown, insertOrder);
        //End Hidden Containers
        VBox center = new VBox(container);

        container.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));

        insertOrder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
            	//if the dropdown isn't blank/null
                if (!dropdown.getValue().toString().isBlank()) {
                    //check the patients allergies compared to flags for modalities
                    for (PatientAlert z : allergies) {
                    	//if there is a flag it creates an alert
                    	//alert a to alert alert
                        if (z.getFlags().contains(dropdown.getValue().toString())) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Patient Alert");
                            alert.setHeaderText(dropdown.getValue().toString());
                            alert.setContentText("Patient is allergic to procedure. \n");
                            alert.show();
                            return;
                        }
                    }
                    //if not it makes a new order
                    insertNewOrder(z, dropdown.getValue().toString());
                    patientOverviewScreen(z);
                    stage.close();
                }
            }
        });
        stage.showAndWait();

    }

    //used create a order (CT Scan, MRI) for a patient
    //creates a separate small page to allow the choosing of a modality
    //stage x to stage stage BorderPane y to BorderPane borderPane  Jeremy Ha
    private void removeOrder(Patient z) {
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("Remove Order");
        stage.initModality(Modality.WINDOW_MODAL);
        BorderPane borderPane = new BorderPane();
        Label text1 = new Label("Order to remove: ");
        text1.setPrefWidth(150);
        ComboBox dropdown = populateOrdersDropdown();
        //renamed button insertOrder to removeOrder Jeremy Hall
        Button removeOrder = new Button("Remove Order");
        removeOrder.setId("cancel");
        HBox container = new HBox(text1, dropdown);
        Label confirmTxt = new Label("Type 'CONFIRM' to continue");
        TextField confirm = new TextField();
        HBox cont = new HBox(confirmTxt, confirm);
        //End Hidden Containers
        VBox center = new VBox(container, cont, removeOrder);

        container.setSpacing(10);
        //added to not make items overlap on page Jeremy Hall
        container.setPadding(new Insets(10));
        
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));

        //remove order button is clicked Jeremy Hall
        removeOrder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
            	//if the dropdown for modalities isn't blank Jeremy Hall
                if (!dropdown.getValue().toString().isBlank()) {

                    //Validation
                    if (!InputValidation.validateConfirm(confirm.getText())) {
                        return;
                    }
                    //if the modality choosen isn't a order the patient has make it creates an alert
                    //alert a to alert alert Jeremy Hall
                    if (!getPatOrders(z.getPatientID()).contains(dropdown.getValue().toString())) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText("Try Again");
                        alert.setContentText("Patient does not have that order.\n");
                        alert.show();
                        return;
                    }
                    //removes the chosen order Jeremy Hall
                    removeOrder(z, dropdown.getValue().toString());
                    patientOverviewScreen(z);
                    stage.close();
                }
            }

        });
        stage.showAndWait();
    }

    //used to remove the order from the sql database Jeremy Hall
    private void removeOrder(Patient z, String order) {
        String sql = "DELETE FROM patientOrders "
                + " WHERE patientID = '" + z.getPatientID() + "' AND orderCodeID = (SELECT orderCodes.orderID FROM orderCodes WHERE orderCodes.orders = '" + order + "')"
                + "     AND ROWID = (SELECT ROWID FROM patientOrders WHERE patientID = '" + z.getPatientID() + "' AND orderCodeID = (SELECT orderCodes.orderID FROM orderCodes WHERE orderCodes.orders = '" + order + "') LIMIT 1)"
                + " ;";
        App.executeSQLStatement(sql);
    }

    //used to give dropdowns all the modalities(orders) that are available to be used Jeremy Hall
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

    //inserts new order into the sql database for the patient Jeremy Hall
    private void insertNewOrder(Patient z, String x) {
        String sql = "INSERT INTO  patientOrders VALUES ('" + z.getPatientID() + "', (SELECT orderID FROM orderCodes WHERE orders = '" + x + "'), '1');";
        App.executeSQLStatement(sql);
    }

    //used to show case radiology reports for patients
    //creates a separate page to show the entire report images, text and more
    //stage x to stage stage BorderPane y to BorderPane borderPane  Jeremy Hall
    private void viewRadiologyReport(Patient z, Appointment appt) {
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.setTitle("View Radiology Report");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setMaximized(true);
        BorderPane borderPane = new BorderPane();

        Button confirm = new Button("Confirm Read Receipt");
        confirm.setId("complete");
        //
        VBox imgContainer = new VBox();

        ArrayList<Pair> list = retrieveUploadedImages(appt.getApptID());
        ArrayList<HBox> hbox = new ArrayList<HBox>();

        //gets the images/pictures for the report Jeremy Hall
        if (list.isEmpty()) {
            System.out.println("Error, image list is empty");
        } else {
            int counter = 0;
            int hboxCounter = 0;
            for (int i = 0; i < (list.size() / 2) + 1; i++) {
                hbox.add(new HBox());
            }
            for (Pair i : list) {
                if (counter > 2) {
                    counter++;
                    hboxCounter++;
                }
                ImageView temp = new ImageView(i.getImg());
                temp.setPreserveRatio(true);
                temp.setFitHeight(300);
                Button download = new Button("Download");
                VBox tempBox = new VBox(temp, download);
                tempBox.setId("borderOnHover");
                tempBox.setSpacing(5);
                tempBox.setAlignment(Pos.CENTER);
                tempBox.setPadding(new Insets(10));
                hbox.get(hboxCounter).getChildren().addAll(tempBox);
                
                //allows you to download image/images Jeremy Hall
                download.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        File selectedDirectory = directoryChooser.showDialog(stage);
                        downloadImage(i, selectedDirectory);
                    }

                });
                counter++;
            }
        }

        for (HBox temp : hbox) {
            imgContainer.getChildren().add(temp);
        }
        ScrollPane s1 = new ScrollPane(imgContainer);
        //
        
        //the text part of the report Jeremy Hall
        Label radiologyReport = new Label();
        radiologyReport.setText("Radiology Report: \n" + getRadiologyReport(appt.getApptID()) + "\n\n");
        HBox container = new HBox(s1);
        ScrollPane s2 = new ScrollPane(radiologyReport);
        s2.setPrefHeight(400);
        VBox center = new VBox(container, s2, confirm);
        container.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        borderPane.setCenter(center);
        borderPane.getStylesheets().add("file:stylesheet.css");
        stage.setScene(new Scene(borderPane));

        //the doctor signing the report and understand what it says 
        //this updates the status of the report for the patient Jeremy Hall
        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                updateStatus(appt.getApptID());
                stage.close();
                populateAppointmentsTable(z);
            }

        });
        stage.showAndWait();
    }

    //used to get images for the radiology reports Jeremy Hall
    private ArrayList<Pair> retrieveUploadedImages(String apptId) {
        //Connect to database
        ArrayList<Pair> list = new ArrayList<Pair>();

        String sql = "SELECT *"
                + " FROM images"
                + " WHERE apptID = '" + apptId + "'"
                + " ORDER BY imageID DESC;";

        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            while (rs.next()) {
                //What I receieve:  image
                Pair pair = new Pair(new Image(rs.getBinaryStream("image")), rs.getString("imageID"));
                pair.fis = rs.getBinaryStream("image");
                list.add(pair);
//                System.out.println(rs.getBinaryStream("image"));
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    //used to get the text for the radiology report Jeremy Hall
    private String getRadiologyReport(String apptID) {
        String value = "";

        String sql = "SELECT writtenReport "
                + " FROM report"
                + " WHERE apptID = '" + apptID + "';";
        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            while (rs.next()) {
                //What I receieve:  text
                value = rs.getString("writtenReport");
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

    //used to update a patient status in the sql database Jeremy Hall
    private void updateStatus(String apptID) {
        String sql = "UPDATE appointments "
                + " SET statusCode = 6 "
                + " WHERE appt_id = '" + apptID + "';";
        App.executeSQLStatement(sql);
    }

    //adds alert's name, id and their flags to the paList from sql database Jeremy Hall
    private void populatePaList() {
        paList.clear();

        String sql = "Select patientAlerts.alertID, patientAlerts.alert "
                + " FROM patientAlerts "
                + " "
                + " ;";

        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
                PatientAlert pa = new PatientAlert(rs.getString("alertID"), rs.getString("alert"), getFlagsFromDatabase(rs.getString("alertID")));
                paList.add(pa);
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //gets order(CT Scan, X-Ray) flags from the sql database Jeremy Hall
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

    //gets allergies for a patient from the sql database in the form of patient alerts Jeremy Hall
    private void populateAllergies(Patient z) {
        allergies.clear();

        String sql = "Select patientAlerts.alertID, patientAlerts.alert "
                + " FROM patientAlerts "
                + " INNER JOIN alertsPatientConnector ON patientAlerts.alertID = alertsPatientConnector.alertID "
                + " WHERE alertsPatientConnector.patientID = '" + z.getPatientID() + "'"
                + ";";

        try {

            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
                PatientAlert pa = new PatientAlert(rs.getString("alertID"), rs.getString("alert"), getFlagsFromDatabase(rs.getString("alertID")));
                allergies.add(pa);
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //used to download images from the radiology report Jeremy Hall
    private void downloadImage(Pair img, File selectedDirectory) {
        try {
            String mimeType = URLConnection.guessContentTypeFromStream(img.fis);
            System.out.print(mimeType);
            mimeType = mimeType.replace("image/", "");
            File outputFile = new File(selectedDirectory.getPath() + "/" + img.imgID + "." + mimeType);
            FileUtils.copyInputStreamToFile(img.fis, outputFile);
        } catch (IOException ex) {
            Logger.getLogger(ReferralDoctor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //used to help with the images and the methods using/interacting with them 
    //for the radiology reports Jeremy Hall
    private class Pair {

        Image img;
        String imgID;
        InputStream fis;

        public Pair(Image img, String imgID) {
            this.img = img;
            this.imgID = imgID;
        }

        public Image getImg() {
            return img;
        }

        public void setImg(Image img) {
            this.img = img;
        }

        public String getImgID() {
            return imgID;
        }

        public void setImgID(String imgID) {
            this.imgID = imgID;
        }

    }

}
