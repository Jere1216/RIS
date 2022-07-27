package aasim.ris;

import static aasim.ris.App.ds;
import static aasim.ris.App.url;
import datastorage.InputValidation;
import datastorage.User;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author 14048
 */
public class Login extends Stage {
//Creating all individual elements in scene
    //Create Username/Password Label and Textbox 

    private Label textUsername = new Label("Enter your Username:");
    //removed here text useless and adds unnecessary step to login Jeremy Hall
    private TextField inputUsername = new TextField();
    private Label textPassword = new Label("Enter your Password:");
    private PasswordField inputPassword = new PasswordField();
    //Create Login Button. Logic for Button at End.
    private Button btnLogin = new Button("Login");
    private GridPane grid = new GridPane();
    VBox center = new VBox();
    Scene scene = new Scene(center, 1000, 1000);
    
    //added loginAttempts to limit amount of tries to login as a security measure
    //while it may be annoying medical information must be protected and this will help 
    //stop brute force attacks and ensure HIPAA is followed Jeremy Hall
    private int loginAttempts = 5;

  
    //Constructor. Displays view
    Login() {
        //Setting the Title
        this.setTitle("RIS- Radiology Information System (Logging In)");
        //edit gridPane to look better
        changeGridPane();
        
        //ability to hit the enter button to login Janet
    
        btnLogin.defaultButtonProperty().bind(inputPassword.focusedProperty());
        
        //ON button click
        btnLogin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
            	//more than 5 login attempts stops any login attempt even if it is correct for security purposes 
            	//Alert a to Alert alert Jeremy Hall
            	if(loginAttempts  < 1) {
            		 Alert alert = new Alert(Alert.AlertType.INFORMATION);
            		 alert.setTitle("Error");
            		 alert.setHeaderText("Try Again");
            		 alert.setContentText("You ran out of login attempts.\nYou need to restart the application to be able to login.");
            		 alert.show();
            	}
            	else
            		loginCheck();
            }

        });
        
       
       
        
        inputUsername.setId("textfield");
        inputPassword.setId("textfield");
        center.setId("loginpage");
        center.setSpacing(10);
        try {
            //Setting the logo
            FileInputStream file = new FileInputStream("logo.png");
            Image logo = new Image(file);
            ImageView logoDisplay = new ImageView(logo);
            center.setAlignment(Pos.CENTER);
            center.getChildren().addAll(logoDisplay, grid);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Setting scene appropriately
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
        this.setMaximized(true);
        this.show();
        connectToDatabase();
    }

    //sets up the textboxes and button on the screen Jeremy Hall
    private void changeGridPane() {
        //Gridpane does what Gridpane does best
        //Everything's on a grid. 
        //Follows-> Column (x), then Row (y)
        grid.setAlignment(Pos.CENTER);
        GridPane.setConstraints(textUsername, 0, 0);
        GridPane.setConstraints(inputUsername, 2, 0);
        GridPane.setConstraints(textPassword, 0, 2);
        GridPane.setConstraints(inputPassword, 2, 2);
        GridPane.setConstraints(btnLogin, 1, 3, 3, 1);
        grid.setPadding(new Insets(5));
        grid.setHgap(5);
        grid.setVgap(5);
        grid.getChildren().addAll(textUsername, inputUsername, textPassword, inputPassword, btnLogin);
        //

    }

//  loginCheck()
//    Checks user inputted username/password
//    Gets user, sets local user
//    Opens new stage for user's role
//    
    private void loginCheck() {

        String username = inputUsername.getText();
        String password = inputPassword.getText();
        //decremented towards 0 for every attempt to log in Jeremy Hall
        loginAttempts--;

        //checks if username and password are valid username and passwords 
        //not if they are correct Jeremy Hall 
        if (!InputValidation.validateUsername(username)) {
            return;
        }

        if (!InputValidation.validatePassword(password)) {
            return;
        }

        //gets username and password from sql database and sets up user Jeremy Hall 
        String sql = "Select * FROM users WHERE username = '" + username + "' AND password = '" + password + "' AND enabled = true;";

        try {
            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String userId = rs.getString("user_id");
                String fullName = rs.getString("full_name");
                int role = rs.getInt("role");
                App.user = new User(userId, fullName, role);
                App.user.setEmail(rs.getString("email"));
                App.user.setUsername(rs.getString("username"));
                try {
                    App.user.setPfp(new Image(new FileInputStream(App.imagePathDirectory + rs.getString("pfp"))));
                } catch (FileNotFoundException ex) {
                    App.user.setPfp(null);
                }
            }

//
            //closes sql related connections Jeremy Hall
            rs.close();
            stmt.close();
            conn.close();
            //

            //replace if else chain with switch statement
            //brackets used to ensure local scope Jeremy Hall
            //Stage x changed to Stage stage
            switch(App.user.getRole()) {
            case 1: {   //goes to admin page
            	  Stage stage = new Administrator();
            	  stage.show();
            	  stage.setMaximized(true);
                  this.hide();
            }
            break;
            case 2:{  //goes to receptionist page
            	 Stage stage = new Receptionist();
            	 stage.show();
            	 stage.setMaximized(true);
                 this.hide();
            }
            break;
            case 3:{  //goes to technician page
                Stage stage = new Technician();
                stage.show();
                stage.setMaximized(true);
                this.hide();
            }
            break;
            case 4:{  //goes to radiologist page
                Stage stage = new Rad();
                stage.show();
                stage.setMaximized(true);
                this.hide();
            }
            break;
            case 5:{   //goes to Referral Doctor page
                Stage stage = new ReferralDoctor();
                stage.show();
                stage.setMaximized(true);
                this.hide();
            }
            break;
            case 6:{  //goes to billing page
            	 Stage stage = new Billing();
            	 stage.show();
            	 stage.setMaximized(true);
                 this.hide();
            }
            break;
            default: throw new SQLException("Invalid Username / Password");
            }
            //runs if you logout and try to login again Jeremy Hall
        } catch (SQLException e) {
            System.out.println(e.getMessage());

            //alert a to alert alert  Jeremy Hall
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            if (e.getMessage().contains("password authentication failed")) {
            	alert.setHeaderText("Try Again");
            	alert.setContentText("URL Username/Password incorrect. Please correct the url.\n(Restart the Program)");
                File credentials = new File("../credentials.ris");
                credentials.delete();
            } else {
            	alert.setHeaderText("Try Again");
                //tracks the amount of login attempts left Jeremy Hall
            	   if(loginAttempts > 0) {
	       	            alert.setContentText("Username / Password not found. \nYou have "+(loginAttempts)+" login attempts left. \nPlease contact an administrator if problem persists. ");
	       	            alert.show();
                   }
                   else {
	                    alert.setContentText("You ran out of login attempts.\nYou need to restart the application to be able to login.");
	              	    alert.show();
                   }
            }
            alert.show();
            //runs on initial login  Jeremy
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            //tracks the amount of login attempts left Jeremy Hall
            if(loginAttempts > 0) {
	            alert.setContentText("Username / Password not found. \nYou have "+(loginAttempts)+" login attempts left. \nPlease contact an administrator if problem persists. ");
	            alert.show();
            }
            else {
            	alert.setContentText("You ran out of login attempts.\nYou need to restart the application to be able to login.");
       		 	alert.show();
            }
        }

    }

    //Checks for valid database connection 
    //stage x to stage stage BorderPane y to BorderPane borderPane 
    // scene z to scene scene Jeremy Hall
    private void connectToDatabase() {
        try {

            File credentials = new File("../credentials.ris");
            //if the file doesn't exist it gets the user to input the url to the database 
            //if it does exist (else clause) it starts reading the file Jeremy Hall
            if (!credentials.exists()) {
                credentials.createNewFile();
                Stage stage = new Stage();
                stage.initOwner(this);
                stage.initModality(Modality.WINDOW_MODAL);
                BorderPane borderPane = new BorderPane();
                Scene scene = new Scene(borderPane);
                scene.getStylesheets().add("file:stylesheet.css");
                stage.setScene(scene);
                Text text = new Text("Insert URL");
                TextArea area = new TextArea();
                Button submit = new Button("Submit");
                HBox container = new HBox(text, area, submit);
                borderPane.setCenter(container);
                stage.show();
                submit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent eh) {
                        if (!area.getText().isEmpty()) {
                            FileOutputStream outputStream = null;
                            try {
                                outputStream = new FileOutputStream(credentials);
                                byte[] strToBytes = area.getText().getBytes();
                                outputStream.write(strToBytes);
                                App.url = area.getText();
                                ds.setUrl(Optional.ofNullable(url).orElseThrow(() -> new IllegalArgumentException("JDBC_DATABASE_URL is not set.")));
                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IllegalArgumentException ex) {
                                credentials.delete();
                                //alert a to alert alert Jeremy Hall
                                Alert alert = new Alert(AlertType.INFORMATION);
                                alert.setTitle("Error");
                                alert.setHeaderText("URL Invalid");
                                alert.setContentText("URL Invalid. Please contact your Administrator. (Restart Application)");
                                alert.showAndWait();

                            } finally {
                                try {
                                    outputStream.close();
                                } catch (IOException ex) {
                                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            stage.close();

                        }
                    }
                });
            } else {
                FileReader fr = new FileReader(credentials);   //reads the file  
                BufferedReader br = new BufferedReader(fr);  //creates a buffering character input stream  
                StringBuffer sb = new StringBuffer();    //constructs a string buffer with no characters  
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);      //appends line to string buffer  
                }
                fr.close();    //closes the stream and release the resources
                try {
                    App.url = sb.toString();
                    ds.setUrl(Optional.ofNullable(url).orElseThrow(() -> new IllegalArgumentException("JDBC_DATABASE_URL is not set.")));
                } catch (IllegalArgumentException ex) {
                    credentials.delete();
                  //alert a to alert alert Jeremy Hall
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText("URL Invalid");
                    alert.setContentText("URL Invalid. Please contact your Administrator. (Restart Application)");
                    alert.showAndWait();
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
