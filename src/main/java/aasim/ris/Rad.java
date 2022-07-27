package aasim.ris;

import static aasim.ris.App.ds;
import datastorage.Appointment;
import datastorage.User;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

// Sets up and runs the Radiologist user profile
public class Rad extends Stage {
    // Creates visual elements for Radiologist profile
    
    //Displays navbar
    HBox navbar = new HBox();
    Label username = new Label("Logged In as Radiologist: " + App.user.getFullName());
    ImageView pfp = new ImageView(App.user.getPfp());

    Button logOut = new Button("Log Out");
    
    //Displays table
    TableView appointmentsTable = new TableView();
    VBox tableContainer = new VBox(appointmentsTable);

    //Displays scene
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);
    
    //Displays Search
    private FilteredList<Appointment> flAppointment;
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Appointments");
    HBox searchContainer = new HBox(choiceBox, search);
    
    //Creates FileChooser object
    private final FileChooser fileChooser = new FileChooser();
    
    //Radiology Department Class
    public Rad() {
        this.setTitle("RIS - Radiology Information System (Radiologist)");
        //Navigation Bar Setup
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
        navbar.getChildren().addAll(username, pfp, logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);

        //Center
        main.setCenter(tableContainer);
        createTableAppointments();
        populateTable();
        
        //Searchbar Structure
        tableContainer.getChildren().add(searchContainer);
        tableContainer.setPadding(new Insets(20));
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("Patient ID", "Full Name", "Date/Time", "Order", "Status");
        choiceBox.setValue("Patient ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> { 
        switch (choiceBox.getValue()) {
         case "Patient ID":
            flAppointment.setPredicate(p -> new String(p.getPatientID() + "").contains(newValue));//filter table by Patient Id
            break;
         case "Full Name":
            flAppointment.setPredicate(p -> p.getFullName().toLowerCase().contains(newValue.toLowerCase()));//filter table by Full name
            break;
         case "Date/Time":
            flAppointment.setPredicate(p -> p.getTime().contains(newValue));//filter table by Date/Time
            break;
         case "Order":
            flAppointment.setPredicate(p -> p.getOrder().toLowerCase().contains(newValue.toLowerCase()));//filter table by Date/Time
            break;
         case "Status":
            flAppointment.setPredicate(p -> p.getStatus().toLowerCase().contains(newValue.toLowerCase()));//filter table by Status
            break;
         default:
            break;
        }
            appointmentsTable.getItems().clear();
            appointmentsTable.getItems().addAll(flAppointment);
        });

        appointmentsTable.setMinHeight(600);
        //Set Scene with stylesheet file
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
    }
    
    //Creates Appointment Table (Need to optimize across users)
    private void createTableAppointments() {
        //Sets up Table Columns
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn fullNameCol = new TableColumn("Name");
        TableColumn timeCol = new TableColumn("Time");
        TableColumn orderIDCol = new TableColumn("Orders");
        TableColumn statusCol = new TableColumn("Appointment Status");
        TableColumn reportCol = new TableColumn("Report");

        //Sets Column Values 
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        orderIDCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusAsLabel"));
        reportCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        //Set Styling for Table 
        patientIDCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.15));
        fullNameCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.15));
        timeCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.1));
        orderIDCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.3));
        statusCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.2));
        reportCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.1));
        appointmentsTable.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        
        //Compile the Table 
        appointmentsTable.getColumns().addAll(patientIDCol, fullNameCol, timeCol, orderIDCol, statusCol, reportCol);
    }
    
    //Populates Appointment Table
    private void populateTable() {
        appointmentsTable.getItems().clear();
        
        //Connects to SQL database
        String sql = "Select appt_id, patient_id, patients.full_name, time, statusCode.status"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID"
                + " INNER JOIN patients ON appointments.patient_id = patients.patientID"
                + " WHERE statusCode = 4"
                + " ORDER BY time ASC;";
        try {

            Connection conn = ds.getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //Sets up Array List for Appointments
            List<Appointment> list = new ArrayList<Appointment>();

            while (rs.next()) {
                //What I receieve:  apptId, patientID, fullname, time, address, insurance, referral, status, order
                Appointment appt = new Appointment(rs.getString("appt_id"), rs.getString("patient_id"), rs.getString("time"), rs.getString("status"), getPatOrders(rs.getString("patient_id"), rs.getString("appt_id")));
                appt.setFullName(rs.getString("full_name"));
                list.add(appt);
            }
            for (Appointment z : list) {
                z.placeholder.setText("Create Report");
                z.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        radPageTwo(z.getPatientID(), z.getApptID(), z.getFullName(), z.getOrder());
                    }
                });
            }
            flAppointment = new FilteredList(FXCollections.observableList(list), p -> true);
            appointmentsTable.getItems().addAll(flAppointment);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Gets Patient Orders 
    private String getPatOrders(String patientID, String aInt) {
        //Connects to SQL Database for String Output
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
            //Close Connections
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return value;
    }
    
    //Handles Logout Request
    private void logOut() {
        App.user = new User();
        Stage x = new Login();
        x.show();
        x.setMaximized(true);
        this.close();
    }
    
    //Handles UserInfo for NavBar
    private void userInfo() {
        Stage x = new UserInfo();
        x.show();
        x.setMaximized(true);
        this.close();
    }

    //Sets up Appointment Table for First Page of Radiologist User
    private void radOne() {
        populateTable();
        main.setCenter(tableContainer);
    }

    //Sets up Second Page of Radiologist User
    private void radPageTwo(String patID, String apptId, String fullname, String order) {
        VBox container = new VBox();
        container.setSpacing(10);
        container.setAlignment(Pos.CENTER);
        Label patInfo = new Label("Patient: " + fullname + "\t Order/s Requested: " + order + "\n");
        Label imgInfo = new Label("Images Uploaded: " + fullname + "\t Order/s Requested: " + order);
        Label noteInfo = new Label ("Note From Technician: \n" + getTechNote(patID));
        //Sets up ScrollPane for Tech Notes 
        ScrollPane noteSP = new ScrollPane();
        noteSP.setPrefHeight(700);
        noteSP.setPrefWidth(10);
        noteSP.setFitToWidth(true);
        noteSP.setContent(noteInfo);        
        
        fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png")
        );
        
        //Buttons for cancel and Upload Report
        Button cancel = new Button("Go Back");
        cancel.setId("cancel");
        Button addReport = new Button("Upload Report");
        HBox buttonContainer = new HBox(cancel, addReport);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(25);
        container.setPadding(new Insets(20));
        container.getChildren().addAll(patInfo, noteSP, buttonContainer);
        main.setCenter(container);
        
        //Cancel on Event Cancel
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                populateTable();
                main.setCenter(tableContainer);
            }
        });
        
        
        addReport.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                openFile(patID, apptId);

            }
        });

    }

    //Opens File Type Modality
    private void openFile(String patID, String apptId) {

        Stage x = new Stage();
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.setMaximized(true);
        BorderPane y = new BorderPane();
        Label label = new Label("Report");
        Button confirm = new Button("Confirm");
        confirm.setId("complete");
        VBox imgContainer = new VBox();
        ArrayList<Pair> list = retrieveUploadedImages(apptId);
        ArrayList<HBox> hbox = new ArrayList<HBox>();
        int counter = 0;
        int hboxCounter = 0;
        if (list.isEmpty()) {
            System.out.println("Error, image list is empty");
        } else {
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
                download.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        File selectedDirectory = directoryChooser.showDialog(x);
                        downloadImage(i, selectedDirectory);
                    }

                });
                counter++;
            }
        }
        for (HBox i : hbox) {
            imgContainer.getChildren().add(i);
        }
        imgContainer.setSpacing(10);
        imgContainer.setPadding(new Insets(10));
        ScrollPane s1 = new ScrollPane();
        s1.setContent(imgContainer);

        TextArea reportText = new TextArea();
        reportText.getText();

        Button cancel = new Button("Cancel");
        cancel.setId("cancel");
        HBox btnContainer = new HBox(cancel, confirm, s1);
        btnContainer.setSpacing(25);
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
                if (reportText.getText().isBlank()) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Error");
                    a.setHeaderText("Try Again");
                    a.setContentText("Please enter a valid report.\n");
                    a.show();
                    return;
                }
                addReportToDatabase(reportText.getText(), apptId);
                updateAppointmentStatus(patID, apptId);
                x.close();
                populateTable();
                main.setCenter(tableContainer);
            }
        });

        VBox container = new VBox(s1, label, reportText, btnContainer);
        y.setCenter(container);
        x.show();
    }

    //Submit SQL Report to Database
    private void addReportToDatabase(String report, String apptId) {
        String sql = "INSERT INTO report (apptID, writtenreport) VALUES ('" + apptId + "', ?);";
        try {
            Connection conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, report);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Paired ArrayList For Uploaded Images
    private ArrayList<Pair> retrieveUploadedImages(String apptId) {
        //Connect To Database
        ArrayList<Pair> list = new ArrayList<Pair>();

        String sql = "SELECT *"
                + " FROM images"
                + " WHERE apptID = '" + apptId + "'"
                + " ORDER BY imageID DESC;";

        try {
            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //Gets Available Images and Pairs Them
            while (rs.next()) {
                //Recieves Image
                Pair pair = new Pair(new Image(rs.getBinaryStream("image")), rs.getString("imageID"));
                pair.fis = rs.getBinaryStream("image");
                list.add(pair);
                /*Debug*/ //System.out.println(rs.getBinaryStream("image"));
            }
            //Closes Connections
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    //Method to Set Appointment Status In Appointment Tab
    private void updateAppointmentStatus(String patID, String apptId) {

        String sql = "UPDATE appointments"
                + " SET statusCode = 5"
                + " WHERE appt_id = '" + apptId + "';";
        try {
            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Method to Download the Image using FileUtils, Translates to Output
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
    
    //Method to Get Written Tech Notes
    private String getTechNote(String patID) {
        String sql = "SELECT writtenTechNote FROM technote WHERE noteID='" + patID + "';";
        String recNote = "";
        try {

            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
            		recNote = rs.getString(1);
            	
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        if(recNote.isEmpty()){
            String noNote = "There are no patient notes.";
            return noNote;
        }else{
            return recNote;  
        }  
    }
    
    //Class to Pair Images and Image IDs
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