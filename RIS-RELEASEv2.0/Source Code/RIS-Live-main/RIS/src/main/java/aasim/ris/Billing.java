package aasim.ris;

/**
 *
 * @author 14048
 */
import static aasim.ris.App.ds;
import datastorage.User;
import datastorage.Appointment;
import datastorage.InputValidation;
import datastorage.Order;
import datastorage.Patient;
import datastorage.Payment;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Billing extends Stage {

    //<editor-fold>
    //Stage Structure
    HBox navbar = new HBox();
    Button logOut = new Button("Log Out");
    Label username = new Label("Logged In as Biller: " + App.user.getFullName());
    ImageView pfp = new ImageView(App.user.getPfp());

    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);
    //Table Structure
    TableView table = new TableView();

    //Search Bar
    FilteredList<Appointment> flAppointment;
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Appointments");

    //Buttons
    Button refreshTable = new Button("Refresh Appointments");
    //Containers
    HBox searchContainer = new HBox(choiceBox, search);
    HBox buttonContainer = new HBox(refreshTable, searchContainer);
    VBox tableContainer = new VBox(table, buttonContainer);
    
    //used to track the totalBill added by Jeremy Hall
    //removed totalCost and makePayment columns and commented out sections
    //Stage x changed multiple times to Stage stage
    private Float billDue = 0.00F;
    //added to ensure only valid payments are processed Jeremy Hall
    private boolean validPayment = true;

//</editor-fold>
    ArrayList<Order> varName = new ArrayList<>();
    //Populate the stage

    //main page for the billing section Jeremy Hall
    Billing() {
    	
        this.setTitle("RIS- Radiology Information System (Billing)");
        //Navbar
        pfp.setPreserveRatio(true);
        pfp.setFitHeight(38);
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        username.setId("navbar");
        username.setOnMouseClicked(eh -> userInfo());
        navbar.getChildren().addAll(username, pfp, logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        //End navbar

        //Putting center code here as to not clutter stuff
        loadCenter();
        varName = populateOrders();
        //Buttons
        
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });

        refreshTable.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                populateTable();
            }
        });

        //Searchbar Structure
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("Appointment ID", "Patient ID", "Full Name", "Date/Time", "Status");
        choiceBox.setValue("Appointment ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
        	//make into a switch jeremy hall
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
        //End Searchbar Structure
        //Scene Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
        //End scene
        
        //added to increase the table size and not waste as much space as before Jeremy Hall
        table.setMinHeight(600);

        //This function populates the table, making sure all NONCOMPLETED appointments are viewable
        populateTable();

    }

    //Add stuff to the center, and make it look good.
    private void loadCenter() {
        table.getColumns().clear();
        //Vbox to hold the table
        tableContainer.setAlignment(Pos.TOP_CENTER);
        tableContainer.setPadding(new Insets(20));
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.setSpacing(10);
        TableColumn apptIDCol = new TableColumn("Appointment ID");
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn firstNameCol = new TableColumn("Full Name");
        TableColumn timeCol = new TableColumn("Time of Appt.");
        TableColumn orderCol = new TableColumn("Orders Requested");
        TableColumn status = new TableColumn("Status");
        TableColumn updateAppt = new TableColumn("Update Billing");
        TableColumn delAppt = new TableColumn("Remove Appointment");
        
        //Allow Table to read Appointment class
        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        orderCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        updateAppt.setCellValueFactory(new PropertyValueFactory<>("placeholder"));
        delAppt.setCellValueFactory(new PropertyValueFactory<>("placeholder1"));
        
        //Set Column Widths
        //changed column widths to fit better and removed totalcost and makepayment columns Jeremy
        apptIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        firstNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        orderCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        updateAppt.prefWidthProperty().bind(table.widthProperty().multiply(0.08));
        status.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        delAppt.prefWidthProperty().bind(table.widthProperty().multiply(0.1));

        //Add columns to table
        table.getColumns().addAll(apptIDCol, patientIDCol, firstNameCol, timeCol, orderCol, status, updateAppt, delAppt);
        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        main.setCenter(tableContainer);
    }

    //adds information to the table from database like patient id, appoint id etc.. Jeremy Hall
    private void populateTable() {
        table.getItems().clear();
        //Connect to database

        String sql = "Select appt_id, patient_id, patients.full_name, time, statusCode.status"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID "
                + " INNER JOIN patients ON patients.patientID = appointments.patient_id"
                + " WHERE statusCode > 3 AND viewable != 0 "
                + " ORDER BY time ASC;";

        try {
            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Appointment> list = new ArrayList<Appointment>();

            while (rs.next()) {
                //What I receieve:  apptId, patientID, fullname, time, address, insurance, referral, status, order
            	//removed total cost and make payment as the columns no longer exist Jeremy Hall
                Appointment appt = new Appointment(rs.getString("appt_id"), rs.getString("patient_id"), rs.getString("time"), rs.getString("status"), getPatOrders(rs.getString("patient_id"), rs.getString("appt_id")));
                appt.setFullName(rs.getString("full_name"));
                appt.placeholder.setText("View Bill");
                appt.placeholder.setOnAction(eh -> viewBill(appt));
                appt.placeholder1.setText("Remove Appointment");
                appt.placeholder1.setId("cancel");
                appt.placeholder1.setOnAction(eh -> removeAppointment(appt));
                list.add(appt);
            }

            for (Appointment x : list) {
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

    //gets orders of modalities for each patient like MRI CT Scan etc... Jeremy Hall
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

    //On button press, log out
    // Stage x chnaged to Stage stage Jeremy Hall
    private void logOut() {
        App.user = new User();
        Stage stage = new Login();
        stage.show();
        stage.setMaximized(true);
        this.hide();
    }

    //removed getTotal method Jeremy Hall

//        float value = -1;
//        return value;
    //used as a separate page to show a patient's bill, 
    //how much they owe and how much they have paid 
    //stage x to stage stage Jeremy Hall
    private void viewBill(Appointment appt) {
        Stage stage = new Stage();
        stage.setTitle("View Bill");
        stage.setMaximized(true);
        stage.setResizable(false);
        BorderPane bp = new BorderPane();
        //reformat where the bill is shown
       // bp.setPadding(new Insets(20));
        Scene sc = new Scene(bp);
        stage.setScene(sc);
        sc.getStylesheets().add("file:stylesheet.css");
// code goes here
//header
        HBox header = new HBox();
        Label patientName = new Label("Patient Name:\n" + appt.getFullName());
        Label patientEmail = new Label("Email:\n" + getEmail(appt.getPatientID()));
        Label patientAddress = new Label("Address:\n" + getAddress(appt.getPatientID()));
        Label patientInsurance = new Label("Insurance:\n" + getInsurance(appt.getPatientID()));
        header.getChildren().addAll(patientName, patientEmail, patientAddress, patientInsurance);
        header.setAlignment(Pos.CENTER);
        bp.setTop(header);
//end header
//center
        //billDue is reset to ensure it is correct Jeremy Hall
        billDue = 0.00F;
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);

        //gets orders of the patient to get the bill due before any patient payment Jeremy Hall
        VBox center = new VBox(grid);
        //used to center the bill to the middle of the new page Jeremy Hall
        center.setPadding(new Insets(0,0,0,600));
        ScrollPane sp = new ScrollPane(center);
        String order[] = appt.getOrder().split(",");
        int counter = 0;
        for (int i = 0; i < order.length - 1; i++) {
            Label tempOrder = new Label(order[i].trim());
            Label tempCost = new Label("Hello");
            for (Order a : varName) {
                if (a.getOrder().equals(order[i].trim())) {
                    tempCost.setText(a.getCost() + "");
                    billDue += a.getCost();
                }
            }
            Label apptDate = new Label(appt.getTime().split(" ")[0]);
            grid.add(apptDate, 1, i);
            grid.add(tempOrder, 0, i);
            grid.add(tempCost, 2, i);
            counter = i;
        }
        //adds payments made by the patient or insurance and updates the bill that is due Jeremy Hall
        counter++;
        ArrayList<Payment> payment = populatePayment(appt.getApptID());
        for (Payment p : payment) {
            Label byWhom = new Label("Patient Paid");
            if (p.getByPatient() == 0) {
                byWhom.setText("Insurance Paid");
            }
            Label tempPaymentDate = new Label(p.getTime());
            float num = -1 * p.getPayment();
            String positive = "";
            if (num > 0) {
                positive = "+";
                //added to make charges say Added Charge instead of incorrectly saying either patient paid or insurance paid Jeremy Hall
                byWhom.setText("Added Charge");
            }
            Label tempPayment = new Label(positive + num);
            //red means it increased the bill due Jeremy Hall
            if (num > 0) {
                byWhom.setId("shadeRed");
                tempPaymentDate.setId("shadeRed");
                tempPayment.setId("shadeRed");
            } else {
            //green means the bill due was reduced Jeremy Hall
                byWhom.setId("shadeGreen");
                tempPaymentDate.setId("shadeGreen");
                tempPayment.setId("shadeGreen");

            }
            
            //adds new payments unto the page Jeremy Hall
            grid.add(byWhom, 0, counter);
            grid.add(tempPaymentDate, 1, counter);
            grid.add(tempPayment, 2, counter);
            billDue -= p.getPayment();
            counter++;
        }
        bp.setCenter(sp);
        
//end center
//footer
        //moved to allow updates with payments updater used to ensure correct place on the page Jeremy Hall
        HBox footer = new HBox();
        Label blank = new Label("Total Bill Remaining: ");
        Label tc = new Label("" + billDue);
        
        Button btn = new Button("Go Back");
        btn.setId("cancel");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                stage.close();
            }
        });
        Button btn1 = new Button("Make Payment");
        btn1.setId("complete");
        btn1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
            	//all added by Jeremy Hall to update values with every payment instead of reloading the page
                makePayment(appt);
                //make sure values are only added to the bill if the payment is valid Jeremy Hall
                if(validPayment == true) {
	                ArrayList<Payment> payment = populatePayment(appt.getApptID());
	                Label byWhom = new Label("Patient Paid");
	                if (payment.get(payment.size()-1).getByPatient() == 0) {
	                    byWhom.setText("Insurance Paid");
	                }
	                Label tempPaymentDate = new Label(payment.get(payment.size()-1).getTime());
	                float num = -1 * payment.get(payment.size()-1).getPayment();
	                String positive = "";
	                if (num > 0) {
	                    positive = "+";
	                    //added to make charges say Added Charge instead of incorrectly saying either patient paid or insurance paid Jeremy Hall
	                    byWhom.setText("Added Charge");
	                }
	                Label tempPayment = new Label(positive + num);
	                if (num > 0) {
	                    byWhom.setId("shadeRed");
	                    tempPaymentDate.setId("shadeRed");
	                    tempPayment.setId("shadeRed");
	                } else {
	                    byWhom.setId("shadeGreen");
	                    tempPaymentDate.setId("shadeGreen");
	                    tempPayment.setId("shadeGreen");
	
	                }
	
	                //uses the size of the ArrayList and order array to increment the rows the ArrayList size + array length - 1 = number of rows
	                //if there are 2 orders and 1 payment then the row = 2 + 3 - 1 = 4 so the next thing goes on row 4 
	                //Updates the bill due with the new payment Jeremy Hall
	                grid.add(byWhom, 0, payment.size() + order.length -1);
	                grid.add(tempPaymentDate, 1, payment.size() + order.length -1);
	                grid.add(tempPayment, 2, payment.size() + order.length -1);
	                billDue -= payment.get(payment.size()-1).getPayment();
	                tc.setText("" + billDue);
	            }
            }
        });

        footer.getChildren().addAll(btn, blank, tc, btn1);
        bp.setBottom(footer);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20));
//end footer
        stage.show();
    }

    //gets the patient address from sql database Jeremy Hall
    private String getAddress(String patientID) {

        String sql = "SELECT address FROM patients WHERE patientID = '" + patientID + "';";

        String value = "";
        try {
            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {

                value += rs.getString("address");
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

    //gets patient email from sql database Jeremy Hall
    private String getEmail(String patientID) {

        String sql = "SELECT email FROM patients WHERE patientID = '" + patientID + "';";

        String value = "";
        try {
            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {

                value += rs.getString("email");
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

    //gets the patient insurance from sql database Jeremy Hall
    private String getInsurance(String patientID) { 

        String sql = "SELECT insurance FROM patients WHERE patientID = '" + patientID + "';";

        String value = "";
        try {
            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {

                value += rs.getString("insurance");
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

    //gets orders MRI, CT Scan from sql database Jeremy Hall
    private ArrayList<Order> populateOrders() {

        String sql = "SELECT * FROM orderCodes;";

        ArrayList<Order> value = new ArrayList<>();
        try {
            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
                Order order = new Order(rs.getString("orderID"), rs.getString("orders"));
                order.setCost(rs.getFloat("cost"));
                value.add(order);
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

    //gets payments saved from the sql database Jeremy Hall
    private ArrayList<Payment> populatePayment(String apptID) {

        String sql = "SELECT * FROM patientPayments WHERE apptID ='" + apptID + "';";

        ArrayList<Payment> value = new ArrayList<>();
        try {
            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
                Payment payment = new Payment(rs.getString("apptID"), rs.getString("time"), rs.getFloat("patientPayment"));
                payment.setByPatient(rs.getInt("byPatient"));
                value.add(payment);
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

    //Separate small page used to enter payments Jeremy Hall 
    //stage x to stage stage 
    //button b to submitButton
    private void makePayment(Appointment appt) {
    	//to ensure payments are only added if the submit button is pressed Jeremy Hall
    	validPayment = false;
        Stage stage = new Stage();
        VBox container = new VBox();
        container.setPadding(new Insets(20));
        container.setSpacing(10);
        Scene scene = new Scene(container);
        scene.getStylesheets().add("file:stylesheet.css");
        stage.setScene(scene);
        stage.setHeight(200);
        stage.setWidth(500);

        HBox hello = new HBox();
        Label enterpay = new Label("Enter Payment Here");
        TextField ep = new TextField();
        HBox hello2 = new HBox();
        ComboBox dropdown = new ComboBox();
        dropdown.getItems().addAll("Patient", "Insurance");
        dropdown.setValue("Patient");
        Button submitButton = new Button("Submit");
        hello2.setAlignment(Pos.CENTER);
        hello.getChildren().addAll(enterpay, ep, dropdown);
        hello.setSpacing(10);
        hello2.getChildren().addAll(submitButton);
        
        container.getChildren().addAll(hello,hello2);
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
            	//to reset the value if the previous payment was invalid Jeremy Hall
            	validPayment = true;
            	//added billDue as a global variable to ensure consistency in the bill that is due and to check if someone over pays Jeremy Hall 
                if (!InputValidation.validatePayment(ep.getText(),billDue)){
                	validPayment = false;
                    return;
                }
                String sql = "";
                if (dropdown.getValue().toString().equals("Patient")) {
                    sql = "INSERT INTO patientPayments(apptID, time, patientPayment, byPatient) VALUES ('" + appt.getApptID() + "', '" + LocalDate.now() + "' , '" + ep.getText() + "', '1' )";
                } else {
                    sql = "INSERT INTO patientPayments(apptID, time, patientPayment, byPatient) VALUES ('" + appt.getApptID() + "', '" + LocalDate.now() + "' , '" + ep.getText() + "', '0' )";
                }
                App.executeSQLStatement(sql);
                stage.close();
            }
        });
        stage.showAndWait();
    }
    
    //removes an appointment and updates the table 
    //added the need to confirm the deletion to stop it occuring by accident  Jeremy Hall
    private void removeAppointment(Appointment appt) {
    	Stage stage = new Stage();
   	    stage.initOwner(this);
        stage.setTitle("Remove Appointment");
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
           	 
           	 	String sql = "UPDATE appointments SET viewable = 0 WHERE appt_id = '" + appt.getApptID() + "';";
             	App.executeSQLStatement(sql);
             	populateTable();
             	stage.close();
            }
        });
    }

    //sends user to userInfo page Jeremy Hall
    private void userInfo() {
        Stage stage = new UserInfo();
        stage.show();
        stage.setMaximized(true);
        this.close();
    }
}
