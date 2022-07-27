package aasim.ris;

import datastorage.User;
import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * JavaFX App
 */
public class App extends Application {

    public static User user;
    public static String fileName = "risDirectory";
    public static String imagePathDirectory = "Favicons/";
//    public static String url = System.getenv("JDBC_DATABASE_URL");
    public static String url = "";
    public static PGSimpleDataSource ds = new PGSimpleDataSource();

    /**
     * Primary beginning of application Launches the Login Page
     */
    @Override
    public void start(Stage stage) {

        //Creating, Editing, Adding stuff to the scene
        //Add stuff to the Stage
        stage = new Login();
        //

    }

    /**
     * Finds the root certificate to connect to the database.
     *
     */
    public static void main1(String[] args) {
        ds.setSslRootCert("root.crt");
        launch();
             
        //String sql1 = "DROP TABLE adnote;";
        //executeSQLStatement(sql1);
        
        //String sql2 = "DROP TABLE technote;";
        //executeSQLStatement(sql2);        
        
        //String sql3 = "DROP TABLE recnote;";
        //executeSQLStatement(sql3);
        
        //createAdNoteTable(fileName);
        //createRecNoteTable(fileName);
        //createTechNoteTable(fileName);
        
    }

    /*
      Creates tables Users, Roles Populates Roles Table 
        1: Administrator 2: Receptionist 3: Technician 4: Radiologist 5: Referral Doctor 6: Biller
     */
    public static void createAndPopulateTables(String fileName) {
        String sql = "CREATE TABLE users (\n"
                + "	user_id INT PRIMARY KEY DEFAULT unique_rowid(),\n"
                + "	email VARCHAR(45) UNIQUE NOT NULL,\n"
                + "	full_name VARCHAR(45) NOT NULL,\n"
                + "	username VARCHAR(25) UNIQUE NOT NULL,\n"
                + "	password VARCHAR(64) NOT NULL,\n"
                + "     role INT NOT NULL,\n"
                + "     pfp STRING ,\n"
                + "	enabled BOOL NOT NULL DEFAULT true\n"
                + ");";
        executeSQLStatement(sql);

        sql = "CREATE TABLE roles (\n"
                + "	roleID INT PRIMARY KEY,\n"
                + "     role VARCHAR(25),\n"
                + "	UNIQUE(roleID, role) \n"
                + ");";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('1', 'Administrator');\n";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('2', 'Receptionist');\n";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('3', 'Technician');\n";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('4', 'Radiologist');\n";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('5', 'Referral Doctor');\n";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('6', 'Biller');\n";
        executeSQLStatement(sql);
    }

    /*
        Creates tables Appointments, AppointmentsOrdersConnector
     */
    public static void createAppointmentTable(String fileName) {
        //apptId, patientID, fullname, time, address, insurance, referral, status, order
        String sql = "CREATE TABLE appointments (\n"
                + "	appt_id INT PRIMARY KEY UNIQUE DEFAULT unique_rowid(),\n"
                + "	patient_id INT NOT NULL,\n"
                + "	time VARCHAR(25) NOT NULL,\n"
                + "     statusCode INT NOT NULL, "
                + "     viewable INT DEFAULT 1, "
                + "     UNIQUE(patient_id, time) "
                + ");";
        executeSQLStatement(sql);
        String sql1 = "CREATE TABLE appointmentsOrdersConnector ( "
                + "     apptID INT,"
                + "     orderCodeID INT, "
                + "     UNIQUE(apptID, orderCodeID) "
                + ");";
        executeSQLStatement(sql1);
    }

    /*
        Creates table DocPatientConnector
     */
    public static void createDocPatientConnectorTable(String fileName) {
        String sql = "CREATE TABLE docPatientConnector (\n"
                + "	referralDocID INT,\n"
                + "	patientID INT, \n"
                + "     UNIQUE(referralDocID, patientID)"
                + ");";

        executeSQLStatement(sql);
    }

    /*
        Creates tables Patients, PatientAlerts, Flags, AlertsPatientConnector, RadPatientConnector
     */
    public static void createPatientTable(String fileName) {
        String sql = "CREATE TABLE patients (\n"
                + "	patientID INT PRIMARY KEY DEFAULT unique_rowid(),\n"
                + "	email VARCHAR(45) NOT NULL,\n"
                + "	full_name VARCHAR(45) NOT NULL,\n"
                + "	dob VARCHAR(45) NOT NULL,\n"
                + "	address VARCHAR(64) NOT NULL,\n"
                + "     insurance VARCHAR(64) NOT NULL, \n"
                + "     UNIQUE(email, full_name)"
                + ");";
        executeSQLStatement(sql);

        sql = "CREATE TABLE patientAlerts ("
                + " alertID INT PRIMARY KEY DEFAULT unique_rowid(), "
                + " alert STRING  UNIQUE NOT NULL "
                + ");";
        executeSQLStatement(sql);

        sql = "CREATE TABLE flags ("
                + " alertID INT, "
                + " orderID INT, "
                + " UNIQUE(alertID, orderID) "
                + ");";
        executeSQLStatement(sql);

        sql = "CREATE TABLE alertsPatientConnector ("
                + " patientID INT, "
                + " alertID INT, "
                + " UNIQUE(patientID, alertID) "
                + ");";
        executeSQLStatement(sql);

        sql = "CREATE TABLE radPatientConnector ("
                + " patientID INT UNIQUE, "
                + " userID INT, "
                + " UNIQUE(patientID, userID) "
                + ");";
        executeSQLStatement(sql);
    }

    /*
        Creates table StatusCode
     */
    public static void createStatusCodesTable(String fileName) {
        String sql = "CREATE TABLE statusCode (\n"
                + "	statusID INT PRIMARY KEY,\n"
                + "	status VARCHAR(45)\n"
                + ");";
        executeSQLStatement(sql);
    }

    /*
        Creates table OrderCodes
     */
    public static void createOrderCodesTable(String fileName) {
        String sql = "CREATE TABLE orderCodes (\n"
                + "	orderID INT PRIMARY KEY DEFAULT unique_rowid(),\n"
                + "	orders VARCHAR(45) UNIQUE, \n"
                + "     cost REAL "
                + ");";
        executeSQLStatement(sql);
    }

    /*
        Creates table patientOrders
     */
    public static void createOrdersTable(String fileName) {
        String sql = "CREATE TABLE patientOrders (\n"
                + "	patientID INT ,\n"
                + "     orderCodeID INT NOT NULL ,\n"
                + "     enabled INT DEFAULT 1\n" //1 = YES, 0 = FALSE
                + ");";
        executeSQLStatement(sql);
    }

    /*
        Creates table Images
     */
    public static void createImageTable(String fileName) {
        String sql = "CREATE TABLE images (\n"
                + "	imageID INT PRIMARY KEY DEFAULT unique_rowid(),\n"
                + "	patientID INT,\n"
                + "	apptID INT,\n"
                + "	image BLOB\n"
                + ");";
        executeSQLStatement(sql);
    }
               
    /*
        Creates table Report
     */
    public static void createRadReportTable(String fileName) {
        String sql = "CREATE TABLE report(\n"
                + "apptID INT UNIQUE, \n"
                + "writtenReport STRING "
                + ");";
        executeSQLStatement(sql);
    }
    
    /*
        Creates table rnotes for Receptionist Notes
     */
    public static void createRecNoteTable(String fileName) {
        String sql = "CREATE TABLE recnote(\n"
                + "noteID INT UNIQUE, \n"                
                + "writtenRecNote STRING "
                + ");";
        executeSQLStatement(sql);
    }
    
    /*
        Creates table rnotes for Admin Notes
     */
    public static void createAdNoteTable(String fileName) {
        String sql = "CREATE TABLE adnote(\n"
                + "noteID INT UNIQUE, \n"                
                + "writtenAdNote STRING "
                + ");";
        executeSQLStatement(sql);
    }
    /*
        Creates table rnotes for Tech Notes
     */
    public static void createTechNoteTable(String fileName) {
        String sql = "CREATE TABLE technote(\n"
                + "noteID INT UNIQUE, \n"                
                + "writtenTechNote STRING "
                + ");";
        executeSQLStatement(sql);
    }
     
    
    /*
        Method which allows insertions / updates / deletes to the database.
        Does not return anything.
        All changes are reflected in the database.
     */
    public static void executeSQLStatement(String sql) {
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

    /*
        Populates statusCode table with all statuses.
     */
    public static void populateTablesStatus(String fileName) {
        String sql = "INSERT INTO statusCode VALUES ('0', 'Patient Did Not Show');\n";
        String sql1 = "INSERT INTO statusCode VALUES ('1', 'Appointment Scheduled');\n";
        String sql2 = "INSERT INTO statusCode VALUES ('2', 'Patient Checked In');\n";
        String sql3 = "INSERT INTO statusCode VALUES ('3', 'Patient received by Technician');\n";
        String sql4 = "INSERT INTO statusCode VALUES ('4', 'Images Uploaded');\n";
        String sql5 = "INSERT INTO statusCode VALUES ('5', 'Radiology Report Uploaded.');\n";
        String sql6 = "INSERT INTO statusCode VALUES ('6', 'Referral Doctor Signature Completed.');\n";
        String sql7 = "INSERT INTO statusCode VALUES ('7', 'Patient Cancelled');\n";
        String sql8 = "INSERT INTO statusCode VALUES ('8', 'Faculty Cancelled');\n";

        executeSQLStatement(sql);
        executeSQLStatement(sql1);
        executeSQLStatement(sql2);
        executeSQLStatement(sql3);
        executeSQLStatement(sql4);
        executeSQLStatement(sql5);
        executeSQLStatement(sql6);
        executeSQLStatement(sql7);
        executeSQLStatement(sql8);
    }

    /*
        Creates table PatientPayments.
     */
    public static void createPatientPayment() {
        //String sql2 = "DROP TABLE patientPayments;";
        //executeSQLStatement(sql2);
        String sql = "CREATE TABLE patientPayments(\n"
                + "apptID INTEGER, \n"
                + "time TEXT, \n"
                + "patientPayment REAL, "
                + "byPatient INTEGER" // when byPatient will be one and when not will be zero
                + ");";
        executeSQLStatement(sql);
    }

    /*
        Populates the Admin table
     */
    public static void populateTablesAdmin(String fileName) {
        String sql = "INSERT INTO users(email, full_name, username, password, role) VALUES ('god@gmail.com', 'Administrator Dave', 'admin', 'admin', '1');\n";
        executeSQLStatement(sql);
    }

}