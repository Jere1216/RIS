package aasim.ris;

/** EDITED BY JANET MORALES
 *
 * @author 14048
 */
import static aasim.ris.App.ds;
import static aasim.ris.App.url;
import datastorage.User;
import datastorage.Appointment;
import datastorage.InputValidation;
import datastorage.Patient;
import datastorage.PatientAlert;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.postgresql.ds.PGSimpleDataSource;

public class Receptionist extends Stage {

    //<editor-fold>
    //Stage Structure
    HBox navbar = new HBox();
    Button logOut = new Button("Log Out");
    Label username = new Label("Logged In as Receptionist: " + App.user.getFullName());
    ImageView pfp = new ImageView(App.user.getPfp());
    Label receptionist = new Label("Receptionist");
    Label patients = new Label("Patients");

    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);
    //Table Structure
    TableView table = new TableView();
    VBox patientsContainer = new VBox();
    VBox receptionistContainer = new VBox();
    //Search Bar
    FilteredList<Appointment> flAppointment;
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Appointments");

    //Buttons
    Button addAppointment = new Button("Add Appointment");
    Button refreshTable = new Button("Refresh Appointments");
    //Containers
    HBox searchContainer = new HBox(choiceBox, search);
    HBox buttonContainer = new HBox(addAppointment, refreshTable, searchContainer);
    
    private FilteredList<Patient> flPatient;
//</editor-fold>
    //Populate the stage
    Receptionist() {
    	
        this.setTitle("RIS- Radiology Information System (Reception)"); //sets title for the program
        //Navbar
        pfp.setPreserveRatio(true);
        pfp.setFitHeight(38);
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        username.setId("navbar");
        username.setOnMouseClicked(eh -> userInfo());
        patients.setId("navbar");
        patients.setOnMouseClicked(eh -> patientsPageView());
        receptionist.setId("navbar");
        receptionist.setOnMouseClicked(eh -> receptionistView());
        HBox navButtons = new HBox(receptionist, patients);
        navButtons.setAlignment(Pos.TOP_LEFT);
        
        HBox.setHgrow(navButtons, Priority.ALWAYS);
        navbar.getChildren().addAll(navButtons, username, pfp, logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        //End navbar

        //Buttons
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });
 
        receptionistView();
        // Scene Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
        table.setMinHeight(600);
        // End scene

      

    }

    //creates the patient page to show all patients and their information Jeremy Hall
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

        receptionist.setId("navbar");
        patients.setId("selected");
        
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
        TableColumn appointment = new TableColumn("Make Appointment");

        //And all of the Value setting
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        DOBCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
        insuranceCol.setCellValueFactory(new PropertyValueFactory<>("insurance"));
        appointment.setCellValueFactory(new PropertyValueFactory<>("Placeholder2"));

        //Couldn't put the table
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.09));
        fullNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        emailCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        DOBCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        insuranceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        appointment.prefWidthProperty().bind(table.widthProperty().multiply(0.2));

        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //back together again
        table.getColumns().addAll(patientIDCol, fullNameCol, emailCol, DOBCol, insuranceCol, appointment);
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
            
            for (Patient z : list) {
                z.placeholder2.setText("Create appointment");
                z.placeholder2.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                    	addAppointment(z.getFullName(), z.getEmail());
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
    
    private void receptionistView() {
	    receptionistContainer.getChildren().clear();
	    main.setCenter(receptionistContainer);
		table.getColumns().clear();	
		//loads the table and buttons and then adds them to receptionistContainer Jeremy hall
		loadCenter();
		receptionistContainer.getChildren().addAll(table, buttonContainer);
		
		//Buttons
		addAppointment.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent e) {
		    addAppointment();
		    }
		});
		refreshTable.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent e) {
		        populateTable();
		    }
		});
		    
		//updates the navbar Jeremy Hall
		receptionist.setId("selected");
	        patients.setId("navbar");
		
		//Searchbar Structure
		searchContainer.setAlignment(Pos.TOP_RIGHT);
		HBox.setHgrow(searchContainer, Priority.ALWAYS);
		choiceBox.setPrefHeight(40);
		search.setPrefHeight(40);
		choiceBox.getItems().addAll("Appointment ID", "Patient ID", "Full Name", "Date/Time", "Status");
		choiceBox.setValue("Appointment ID");
		search.textProperty().addListener((obs, oldValue, newValue) -> {
		    if (choiceBox.getValue().equals("Appointment ID")) {
		        flAppointment.setPredicate(p -> new String(p.getApptID() + "").contains(newValue));//filter table by Appt ID
		    }
		    if (choiceBox.getValue().equals("Patient ID")) {
		        flAppointment.setPredicate(p -> new String(p.getPatientID() + "").contains(newValue));//filter table by Patient Id
		    }
		    if (choiceBox.getValue().equals("Full Name")) {
		        flAppointment.setPredicate(p -> p.getFullName().toLowerCase().contains(newValue.toLowerCase()));//filter table by Full name
		    }
		    if (choiceBox.getValue().equals("Date/Time")) {
		        flAppointment.setPredicate(p -> p.getTime().contains(newValue));//filter table by Date/Time
		    }
		    if (choiceBox.getValue().equals("Status")) {
		        flAppointment.setPredicate(p -> p.getStatus().toLowerCase().contains(newValue.toLowerCase()));//filter table by Status
		    }
		    table.getItems().clear();
		    table.getItems().addAll(flAppointment);
		    });
		// End Searchbar Structure
		
		// This function populates the table, making sure all NONCOMPLETED appointments are viewable
	        populateTable();   
	}
    
    // Add stuff to the center, and make it look good.
    private void loadCenter() {
        // Vbox to hold the table
    	receptionistContainer.setAlignment(Pos.TOP_CENTER);
    	receptionistContainer.setPadding(new Insets(20));
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.setSpacing(10);
        
        TableColumn apptIDCol = new TableColumn("Appointment ID");
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn firstNameCol = new TableColumn("Full Name");
        TableColumn timeCol = new TableColumn("Time of Appt.");
        TableColumn orderCol = new TableColumn("Orders Requested");
        TableColumn status = new TableColumn("Status");
        TableColumn updateAppt = new TableColumn("Update Appointment");
        // Allow Table to read Appointment class
        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        orderCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        status.setCellValueFactory(new PropertyValueFactory<>("statusAsLabel"));
        updateAppt.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        //Set Column Widths
        //added widths for each column so they can properly fit information Jeremy Hall
        apptIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        firstNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        orderCol.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        status.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        updateAppt.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        //Add columns to table
        table.getColumns().addAll(apptIDCol, patientIDCol, firstNameCol, timeCol, orderCol, status, updateAppt);
        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
    }
    
    //Clear the table, query the database, populate table based on results.
    //DOES NOT SHOW 'COMPLETED' APPOINTMENTS. 
    private void populateTable() {
        table.getItems().clear();
        //Connect to database

        String sql = "Select appt_id, patient_id, patients.full_name, time, statusCode.status"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID "
                + " INNER JOIN patients ON patients.patientID = appointments.patient_id"
                + " WHERE statusCode < 3"
                + " ORDER BY time ASC;";

        try {

            ds.setUrl(Optional.ofNullable(url).orElseThrow(() -> new IllegalArgumentException("JDBC_DATABASE_URL is not set.")));
            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<Appointment> list = new ArrayList<Appointment>();

            while (rs.next()) {
                // What I receieve:  apptId, patientID, fullname, time, address, insurance, referral, status, order
                Appointment appt = new Appointment(rs.getString("appt_id"), rs.getString("patient_id"), rs.getString("time"), rs.getString("status"), getPatOrders(rs.getString("patient_id"), rs.getString("appt_id")));
                appt.setFullName(rs.getString("full_name"));
                list.add(appt);
            }

            for (Appointment x : list) {
                x.placeholder.setText("Update Appointment");
                x.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        updateAppointment(x);
                    }
                });
            }
            flAppointment = new FilteredList(FXCollections.observableList(list), p -> true);
            table.getItems().addAll(flAppointment);
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private String getPatOrders(String patientID, String aInt) {

        String sql = "Select orderCodes.orders "
                + " FROM appointmentsOrdersConnector "
                + " INNER JOIN orderCodes ON appointmentsOrdersConnector.orderCodeID = orderCodes.orderID "
                + " WHERE apptID = '" + aInt + "';";

        String value = "";
        try {

            ds.setUrl(Optional.ofNullable(url).orElseThrow(() -> new IllegalArgumentException("JDBC_DATABASE_URL is not set.")));
            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                value += rs.getString("orders") + ", ";
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return value;
    }
    
    // On button press, log out
    private void logOut() {
        App.user = new User();
        Stage x = new Login();
        x.show();
        x.setMaximized(true);
        this.hide();
    }

    private void userInfo() {
        Stage x = new UserInfo();
        x.show();
        x.setMaximized(true);
        this.close();
    }

    // On button press, open up a new stage (calls private nested class)
    private void addAppointment() {
        Stage x = new AddAppointment();
        x.setTitle("Add Appointment");
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.showAndWait();
        populateTable();
    }
    
    private void addAppointment(String name, String email) {
        Stage x = new AddAppointment(name, email);
        x.setTitle("Add Appointment");
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.showAndWait();
    }

    // On button press, open up a new stage
    private void updateAppointment(Appointment appt) {
        Stage x = new Stage();
        x.setTitle("Update Appointment");
        x.initOwner(this);
        x.setHeight(250);
        x.initModality(Modality.WINDOW_MODAL);
        
        //Creates Button Objects and Displays Them
        Button updateTime = new Button("Reschedule Appointment");
        Button updateStatus = new Button("Change Appointment Status");
        Button updatePatient = new Button("Update Patient Information");
        Button addPatientNote = new Button ("Set Patient Note");
        HBox display = new HBox(updateStatus, addPatientNote, updateTime, updatePatient);
        display.setAlignment(Pos.CENTER);
        display.setSpacing(25);
        
        
        //Sets Up VBox For Update Appointment Stage
        VBox container = new VBox(display);
        Scene scene = new Scene(container);
        x.setScene(scene);
        x.setWidth(750);
        x.setHeight(250);
        container.setAlignment(Pos.CENTER);
        scene.getStylesheets().add("file:stylesheet.css");

        //Update Time Button Action Event Handler
        updateTime.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                container.getChildren().clear();

                DatePicker datePicker = new DatePicker();
                Text text = new Text("Insert Date: ");
                Text text1 = new Text("Insert Time (HH:MM): ");
                ComboBox hours = new ComboBox();
                for (int i = 0; i < 24; i++) {
                    String temp = "";
                    if (i < 10) {
                        temp = "0" + i;
                    } else {
                        temp = "" + i;
                    }
                    hours.getItems().add(temp);
                }
                ComboBox minutes = new ComboBox();
                for (int i = 0; i < 60; i += 15) {
                    String temp = "";
                    if (i < 10) {
                        temp = "0" + i;
                    } else {
                        temp = "" + i;
                    }
                    minutes.getItems().add(temp);
                }
                Text colon = new Text(":");
                HBox time = new HBox(hours, colon, minutes);
                Button submit = new Button("Submit");
                submit.setPrefWidth(100);
                submit.setId("complete");

                HBox hidden = new HBox(text, datePicker, text1, time, submit);
                hidden.setSpacing(15);
                container.getChildren().add(hidden);
                submit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        String full = hours.getValue().toString() + ":" + minutes.getValue().toString();
                        //validation here
                        if (!InputValidation.validateFuture(datePicker.getValue().toString())) {
                            return;
                        }
                        if (!InputValidation.validateFutureTime(datePicker.getValue().toString(), full)) {
                            return;
                        }
                        //end validation
                        updateTime(datePicker.getValue().toString() + " " + full, appt.getApptID());
                        x.close();
                    }

                });
            }
        });
        //Update Status Button Action Event Handler
        updateStatus.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                container.getChildren().clear();

                ComboBox dropdown = new ComboBox();
                dropdown.getItems().addAll("Patient Did Not Show", "Appointment Scheduled", "Patient Checked In", 
                "Patient received by Technician", "Patient Cancelled", "Faculty Cancelled");

                dropdown.setValue(appt.getStatus());
                Button submit = new Button("Submit");
                submit.setId("complete");

                HBox hidden = new HBox(dropdown, submit);
                hidden.setSpacing(15);
                container.getChildren().add(hidden);
                submit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        //validation here
                        //end validation
                        if (!dropdown.getValue().toString().isBlank()) {
                            updateStatus(dropdown.getValue().toString(), appt);
                            x.close();
                        }
                    }
                });
            }
        });
        //Update Patient Button Action Event Handler
        updatePatient.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                Patient pat = pullPatientInfo(appt.getPatientID());
                x.close();
                updatePatient(pat);
            }
        });
        //Patient Note Button Action Event Handler
        addPatientNote.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                String patID = appt.getPatientID();
                x.close();
                addPatNote(patID);
            }
        });        

        x.showAndWait();
        populateTable();
    }
    
    // updates any information that is new, on the patient
    private void updatePatient(Patient z) {
    	ArrayList<PatientAlert> paList = populatePaList();
    	ArrayList<PatientAlert> allergies = populateAllergies(z);
    	
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
            }
        });
    }

    private ArrayList<PatientAlert> populatePaList() {
        ArrayList<PatientAlert> paList = new ArrayList<>();

        String sql = "Select patientAlerts.alertID, patientAlerts.alert "
                + " FROM patientAlerts "
                + " "
                + " ;";

        try {

            ds.setUrl(Optional.ofNullable(url).orElseThrow(() -> new IllegalArgumentException("JDBC_DATABASE_URL is not set.")));
            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                PatientAlert pa = new PatientAlert(rs.getString("alertID"), rs.getString("alert"), getFlagsFromDatabase(rs.getString("alertID")));
                paList.add(pa);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return paList;
    }

    private ArrayList<PatientAlert> populateAllergies(Patient z) {
        ArrayList<PatientAlert> allergies = new ArrayList<>();

        String sql = "Select patientAlerts.alertID, patientAlerts.alert "
                + " FROM patientAlerts "
                + " INNER JOIN alertsPatientConnector ON patientAlerts.alertID = alertsPatientConnector.alertID "
                + " WHERE alertsPatientConnector.patientID = '" + z.getPatientID() + "'"
                + ";";

        try {

            ds.setUrl(Optional.ofNullable(url).orElseThrow(() -> new IllegalArgumentException("JDBC_DATABASE_URL is not set.")));
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
        return allergies;
    }

    private String getFlagsFromDatabase(String aInt) {

        String val = "";
        String sql = "Select orderCodes.orders "
                + " FROM flags "
                + " INNER JOIN orderCodes ON flags.orderID = orderCodes.orderID "
                + " WHERE alertID = '" + aInt + "' "
                + ";";

        try {

            ds.setUrl(Optional.ofNullable(url).orElseThrow(() -> new IllegalArgumentException("JDBC_DATABASE_URL is not set.")));
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

    private Patient pullPatientInfo(String patID) {
        Patient temp = null;

        String sql = "Select * "
                + " FROM patients"
                + " WHERE patientID = '" + patID + "';";

        try {

            ds.setUrl(Optional.ofNullable(url).orElseThrow(() -> new IllegalArgumentException("JDBC_DATABASE_URL is not set.")));
            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // What I receieve:  patientID, email, full_name, dob, address, insurance
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

    private void updateTime(String string, String apptID) {
        String sql = "UPDATE appointments "
                + " SET time = '" + string + "' "
                + " WHERE appt_id = '" + apptID + "';";
        App.executeSQLStatement(sql);
    }

    private void updateStatus(String status, Appointment appt) {
        String sql = "UPDATE appointments "
                + " SET statusCode = "
                + "     (SELECT statusID FROM statusCode WHERE status = '" + status + "') "
                + " WHERE appt_id = '" + appt.getApptID() + "';";
        App.executeSQLStatement(sql);

        if (status.contains("Cancelled")) {
            String sql1 = "INSERT INTO  patientOrders VALUES ('" + appt.getPatientID() + "', (SELECT orderCodeID FROM appointmentsOrdersConnector WHERE apptID = '" + appt.getApptID() + "'), '1');";
            App.executeSQLStatement(sql1);
        }
    }

    //Adds/Sets a Patient Note
    private void addPatNote(String patID) {

        Stage x = new Stage();
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.setMaximized(false);
        BorderPane y = new BorderPane();
        Label label = new Label("Set User Note (Please Include Current Date And Time In Note): ");
        Button confirm = new Button("Confirm");
        confirm.setId("complete");

        TextArea noteText = new TextArea();
        noteText.getText();

        Button cancel = new Button("Cancel");
        cancel.setId("cancel");
        HBox btnContainer = new HBox(cancel, confirm);
        btnContainer.setSpacing(25);
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setPadding(new Insets(20));
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                x.close();
            }
        });
        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (noteText.getText().isBlank()) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Error");
                    a.setHeaderText("Try Again");
                    a.setContentText("Please enter a valid note.\n");
                    a.show();
                    return;
                }
                addRecNoteToDatabase(noteText.getText(), patID);
                x.close();
                populateTable();
            }
        });

        VBox container = new VBox(label, noteText, btnContainer);
        y.setCenter(container);
        x.show();
    }
    
    //Adds/Sets Patient Note To Database
    private void addRecNoteToDatabase(String note, String patID) {
        String sql = "INSERT INTO recnote (noteID, writtenRecNote) VALUES ('" + patID + "', ?);";
        try {
            Connection conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, note);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        //If The Index Is Taken, This Will Update The Note:
        } catch (SQLException f) {
            String sql2 = "UPDATE recnote SET writtenRecNote='"+note+"' WHERE noteID='"+patID+"';";
            try {
            Connection conn = ds.getConnection();
            App.executeSQLStatement(sql2);
            conn.close();
            //The Error Isn't Fixed By Updating The Table:
            } catch (SQLException e) {
            System.out.println(e.getMessage());
            }
        }
    }
    /* 
    // Private Nested Classes Below.
    //
    //
    //
     */
    //Private Nested Class 1
    //For the Add Appointment
    private class AddAppointment extends Stage {

        Patient pat = null;
        ArrayList<String> orders = new ArrayList<String>();
        DatePicker datePicker = new DatePicker();

        // Class Variables
        AddAppointment() {
            TextField patFullName = new TextField("Full Name");
            TextField patEmail = new TextField("Email");
            Button check = new Button("Pull Patient Information");
            // the time and order
            Text text = new Text("Insert Date: ");
            Text text1 = new Text("Insert Time (HH:MM): ");
            ComboBox hours = new ComboBox();

            for (int i = 0; i < 24; i++) {
                String temp = "";
                if (i < 10) {
                    temp = "0" + i;
                } else {
                    temp = "" + i;
                }
                hours.getItems().add(temp);
            }
            ComboBox minutes = new ComboBox();
            for (int i = 0; i < 60; i += 15) {
                String temp = "";
                if (i < 10) {
                    temp = "0" + i;
                } else {
                    temp = "" + i;
                }
                minutes.getItems().add(temp);
            }
            hours.setMinWidth(75);
            minutes.setMinWidth(75);

            Text colon = new Text(":");
            HBox time = new HBox(hours, colon, minutes);


            Text tutorial = new Text("Click to remove: ");

            Button submit = new Button("Submit");
            submit.setId("complete");
            //
            HBox initialContainer = new HBox(patFullName, patEmail, check);
            initialContainer.setSpacing(10);
            HBox hiddenContainer = new HBox(text, datePicker, text1, time);
            hiddenContainer.setSpacing(10);
            HBox hiddenOrderContainer = new HBox();
            hiddenOrderContainer.setSpacing(10);

            HBox hiddenContainer1 = new HBox(submit);
            hiddenContainer1.setSpacing(10);
            VBox container = new VBox(initialContainer, hiddenContainer, hiddenOrderContainer, hiddenContainer1);
            container.setAlignment(Pos.CENTER);
            initialContainer.setAlignment(Pos.CENTER);
            hiddenContainer.setAlignment(Pos.CENTER);
            hiddenOrderContainer.setAlignment(Pos.CENTER);
            hiddenContainer1.setAlignment(Pos.CENTER);
            container.setPadding(new Insets(10));
            initialContainer.setPadding(new Insets(10));
            hiddenContainer.setPadding(new Insets(10));
            hiddenOrderContainer.setPadding(new Insets(10));
            hiddenContainer1.setPadding(new Insets(10));
            hiddenContainer.setVisible(false);
            hiddenOrderContainer.setVisible(false);
            hiddenContainer1.setVisible(false);
            Scene newScene = new Scene(container);
            newScene.getStylesheets().add("file:stylesheet.css");
            this.setScene(newScene);
            this.setWidth(750);
            hiddenOrderContainer.getChildren().add(tutorial);
            check.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    if (!InputValidation.validateName(patFullName.getText())) {
                        return;
                    }
                    if (!InputValidation.validateEmail(patEmail.getText())) {
                        return;
                    }
                    pat = pullPatientInfo(patFullName.getText(), patEmail.getText());
                    if (pat != null) {
                        check.setVisible(false);
                        Text request = new Text("Orders Requested: ");
                        ComboBox dropdown = getPatOrders(pat.getPatientID());
                        dropdown.setPrefWidth(150);

                        hiddenContainer.getChildren().addAll(request, dropdown);
                        hiddenContainer.setVisible(true);
                        hiddenOrderContainer.setVisible(true);
                        hiddenContainer1.setVisible(true);

                        dropdown.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                                orders.add(dropdown.getValue().toString());
                                Button temp = new Button(dropdown.getValue().toString());
                                hiddenOrderContainer.getChildren().add(temp);
                                temp.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent t) {
                                        if (!dropdown.getValue().toString().isBlank()) {
                                            orders.remove(temp.getText());
                                            hiddenOrderContainer.getChildren().remove(temp);
                                        }
                                    }
                                });
                            }
                        });
// will recieve an error on the user-end if the patient is not in the system
                    } else {
                        Alert a = new Alert(Alert.AlertType.INFORMATION);
                        a.setTitle("Error");
                        a.setHeaderText("Try Again");
                        a.setContentText("Patient not found\nPlease verify all information or contact the patient's Referral Doctor\n");
                        a.show();
                        return;
                    }
                }

            });

            submit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    String full = hours.getValue().toString() + ":" + minutes.getValue().toString();
                    if (!InputValidation.validateFuture(datePicker.getValue().toString())) {
                        return;
                    }
                    if (!InputValidation.validateFutureTime(datePicker.getValue().toString(), full)) {
                        return;
                    }
                    insertAppointment(pat.getPatientID(), orders, datePicker.getValue().toString() + " " + full);
                }
            });

        }
        
        //overloaded and is called by the patient view
        AddAppointment(String name, String email) {
        TextField patFullName = new TextField(name);
        patFullName.setEditable(false);
        TextField patEmail = new TextField(email);
        patEmail.setEditable(false);

        Button check = new Button("Pull Patient Information");
        // the time and order
        Text text = new Text("Insert Date: ");
        Text text1 = new Text("Insert Time (HH:MM): ");
        ComboBox hours = new ComboBox();

        for (int i = 0; i < 24; i++) {
            String temp = "";
            if (i < 10) {
                temp = "0" + i;
            } else {
                temp = "" + i;
            }
            hours.getItems().add(temp);
        }
        ComboBox minutes = new ComboBox();
        for (int i = 0; i < 60; i += 15) {
            String temp = "";
            if (i < 10) {
                temp = "0" + i;
            } else {
                temp = "" + i;
            }
            minutes.getItems().add(temp);
        }
        hours.setMinWidth(75);
        minutes.setMinWidth(75);

        Text colon = new Text(":");
        HBox time = new HBox(hours, colon, minutes);


        Text tutorial = new Text("Click to remove: ");

        Button submit = new Button("Submit");
        submit.setId("complete");
        //
        HBox initialContainer = new HBox(patFullName, patEmail, check);
        initialContainer.setSpacing(10);
        HBox hiddenContainer = new HBox(text, datePicker, text1, time);
        hiddenContainer.setSpacing(10);
        HBox hiddenOrderContainer = new HBox();
        hiddenOrderContainer.setSpacing(10);

        HBox hiddenContainer1 = new HBox(submit);
        hiddenContainer1.setSpacing(10);
        VBox container = new VBox(initialContainer, hiddenContainer, hiddenOrderContainer, hiddenContainer1);
        container.setAlignment(Pos.CENTER);
        initialContainer.setAlignment(Pos.CENTER);
        hiddenContainer.setAlignment(Pos.CENTER);
        hiddenOrderContainer.setAlignment(Pos.CENTER);
        hiddenContainer1.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));
        initialContainer.setPadding(new Insets(10));
        hiddenContainer.setPadding(new Insets(10));
        hiddenOrderContainer.setPadding(new Insets(10));
        hiddenContainer1.setPadding(new Insets(10));
        hiddenContainer.setVisible(false);
        hiddenOrderContainer.setVisible(false);
        hiddenContainer1.setVisible(false);
        Scene newScene = new Scene(container);
        newScene.getStylesheets().add("file:stylesheet.css");
        this.setScene(newScene);
        this.setWidth(750);
        hiddenOrderContainer.getChildren().add(tutorial);
        check.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!InputValidation.validateName(patFullName.getText())) {
                    return;
                }
                if (!InputValidation.validateEmail(patEmail.getText())) {
                    return;
                }
                pat = pullPatientInfo(patFullName.getText(), patEmail.getText());
                if (pat != null) {
                    check.setVisible(false);
                    Text request = new Text("Orders Requested: ");
                    ComboBox dropdown = getPatOrders(pat.getPatientID());
                    dropdown.setPrefWidth(100);

                    hiddenContainer.getChildren().addAll(request, dropdown);
                    hiddenContainer.setVisible(true);
                    hiddenOrderContainer.setVisible(true);
                    hiddenContainer1.setVisible(true);

                    dropdown.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            orders.add(dropdown.getValue().toString());
                            Button temp = new Button(dropdown.getValue().toString());
                            hiddenOrderContainer.getChildren().add(temp);
                            temp.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent t) {
                                    if (!dropdown.getValue().toString().isBlank()) {
                                        orders.remove(temp.getText());
                                        hiddenOrderContainer.getChildren().remove(temp);
                                    }
                                }
                            });
                        }
                    });
//will recieve an error on the user-end if the patient is not in the system
                } else {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Error");
                    a.setHeaderText("Try Again");
                    a.setContentText("Patient not found\nPlease verify all information or contact the patient's Referral Doctor\n");
                    a.show();
                    return;
                }
            }

        });

        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String full = hours.getValue().toString() + ":" + minutes.getValue().toString();
                if (!InputValidation.validateFuture(datePicker.getValue().toString())) {
                    return;
                }
                if (!InputValidation.validateFutureTime(datePicker.getValue().toString(), full)) {
                    return;
                }
                insertAppointment(pat.getPatientID(), orders, datePicker.getValue().toString() + " " + full);
                receptionistView();
            }
        });

    }
// appointment scheduling for the patient
        private void insertAppointment(String patientID, ArrayList<String> orders, String time) {
            String sql = "INSERT INTO appointments(patient_id, time, statusCode)"
                    + " VALUES ('" + patientID + "', '" + time + "', '1');\n";
            App.executeSQLStatement(sql);
            for (String x : orders) {
                String sql1 = "INSERT INTO appointmentsOrdersConnector(apptID, orderCodeID)"
                        + " VALUES ("
                        + " (SELECT appt_id FROM appointments WHERE patient_id = '" + patientID + "' AND time = '" + time + "') , "
                        + " (SELECT orderID FROM orderCodes WHERE orders = '" + x + "') "
                        + ");\n";

                App.executeSQLStatement(sql1);
                String sql2 = "DELETE FROM patientOrders WHERE patientID = '" + patientID + "' AND orderCodeID = (SELECT orderID FROM orderCodes WHERE orders = '" + x + "')";
                App.executeSQLStatement(sql2);
            }
            this.close();
        }
// retireves the patient orders from the sql database
        private ComboBox getPatOrders(String patientID) {

            String sql = "Select orderCodes.orders "
                    + " FROM patientOrders "
                    + " INNER JOIN orderCodes ON patientOrders.orderCodeID = orderCodes.orderID "
                    + " WHERE patientID = '" + patientID + "';";
            ComboBox value = new ComboBox();
            try {

                Connection conn = ds.getConnection();

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    value.getItems().add(rs.getString("orders"));
                }
                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return value;
        }
// retireves the information from the patient stored in the sql database
        private Patient pullPatientInfo(String patFullName, String patEmail) {
            Patient temp = null;

            String sql = "Select * "
                    + " FROM patients"
                    + " WHERE email = '" + patEmail + "' AND full_name = '" + patFullName + "';";

            try {

                Connection conn = ds.getConnection();

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    // What I receieve:  patientID, email, full_name, dob, address, insurance
                    temp = new Patient(rs.getString("patientID"), rs.getString("email"), rs.getString("full_name"), 
                    rs.getString("dob"), rs.getString("address"), rs.getString("insurance"));
                }
                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return temp;
        }
    }
}