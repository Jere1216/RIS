/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datastorage;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 *
 * @author 14048
 */
public class InputValidation {

	//validates if the name entered is  normal name with regular characters otherwise it generates an alert
	//alert a to alert alert Jeremy Hall
    public static boolean validateName(String name) {
        if (name == null || name.isBlank() || !name.matches("^[a-zA-Z]+ [a-zA-Z]+$")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Please enter a valid full name. \n");
            alert.show();
            return false;
        }
        return true;
    }

    //checks if the username is not blank and has a ' if it does it generate an alert
  	//alert a to alert alert Jeremy Hall
    public static boolean validateUsername(String name) {
        if (name.isBlank() || name.contains("'")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Please enter a valid username.");
            alert.show();
            return false;
        }
        return true;
    }

    //checks if the password is not blank and has a ' if it does it generate an alert
  	//alert a to alert alert Jeremy Hall
    public static boolean validatePassword(String pass) {
        if (pass.isBlank() || pass.contains("'")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Please enter a valid password.");
            alert.show();
            return false;
        }
        return true;
    }
    //checks if the email is not blank or null
    //it also checks the regular characters an email would contain like a @ character
    //if it doesn't it generates and alert
    //alert a to alert alert Jeremy Hall
    public static boolean validateEmail(String email) {
        if (email == null || email.isBlank() || !email.matches("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Error");
            a.setHeaderText("Try Again");
            a.setContentText("Please enter a valid Email. \n");
            a.show();
            return false;
        }
        return true;
    }

    //checks if the user entered CONFIRM used for admin page to confirm changes 
    //alert a to alert alert Jeremy Hall
    public static boolean validateConfirm(String confirm) {
        if (!confirm.equals("CONFIRM")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Please enter 'CONFIRM'.\n");
            alert.show();
            return false;
        }
        return true;
    }

    //checks if the date is not null or blank
    //checks if the user added a actual date 
    //alert a to alert alert Jeremy Hall
    public static boolean validateDate(String date) {
        if (date == null || date.isBlank() || !date.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}$")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Please enter a valid date.\n");
            alert.show();
            return false;
        }
        return true;
    }

    //calls validateDate to see if a date was entered
    //makes sure the date entered isn't before the current date
    //alert a to alert alert Jeremy Hall
    public static boolean validateFuture(String date) {
        if (!validateDate(date)) {
            return false;
        }
        if (LocalDate.parse(date).isBefore(LocalDate.now())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Date has already past.\nPlease insert a valid date.\n");
            alert.show();
            return false;
        }
        return true;
    }

    //checks if the address is not blank and has a ' if it does it generate an alert
  	//alert a to alert alert Jeremy Hall
    public static boolean validateAddress(String address) {
        if (address.isBlank() || address.contains("'")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Please enter a valid Address.\n");
            alert.show();
            return false;
        }
        return true;
    }

    //checks if the insurance is not blank and has a ' if it does it generate an alert
  	//alert a to alert alert Jeremy Hall
    public static boolean validateInsurance(String insurance) {
        if (insurance.isBlank() || insurance.contains("'")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Please enter a valid Insurance.\n");
            alert.show();
            return false;
        }
        return true;
    }

    //checks if the time is not null or blank
    //checks if the user added a actual time 
    //alert a to alert alert Jeremy Hall
    public static boolean validateTime(String time) {
        if (time == null || time.isBlank() || !time.matches("(^([0-9]|[0-1][0-9]|[2][0-3]):([0-5][0-9])$)")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Please enter a valid Time.\n");
            alert.show();
            return false;
        }
        return true;
    }

    //calls validateTime to see if a time was entered
    //makes sure the time entered isn't before the current time
    //just like validateDate and validateFuture
    //alert a to alert alert Jeremy Hall
    public static boolean validateFutureTime(String date, String time) {
        if (!validateTime(time)) {
            return false;
        }
        if (LocalDate.parse(date).isEqual(LocalDate.now())) {
            if (LocalTime.parse(time).isBefore(LocalTime.now())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Try Again");
                alert.setContentText("Time Inputted has already past.\nPlease insert a valid time.\n");
                alert.show();
                return false;
            }

        }
        return true;
    }

    //checks payments to billing by seeing if they can be a floating point number
    //it also makes sure the payment is not greater than the totalCost (bill due from billing)
    //alert a to alert alert Jeremy Hall
    public static boolean validatePayment(String payment, Float totalCost) {
        try {
            Float.parseFloat(payment);
            
            //added to check if the person over pays the amount they owe Jeremy Hall 
            if(Float.parseFloat(payment) > totalCost) {
            	Alert alert = new Alert(Alert.AlertType.INFORMATION);
            	alert.setTitle("Error");
            	alert.setHeaderText("You over payed");
            	alert.setContentText("Please enter amounts less than or equal to "+totalCost+". \n");
   
               if(alert.showAndWait().get() == ButtonType.OK) {
                	return false;
                }
          	}
            
        } catch (Exception b) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Please enter a valid payment.\n");
            alert.show();
            return false;
        }
        return true;
    }
    
    //checks new modalities cost by seeing if they can be a floating point number
    //if not an alert it created 
    //alert a to alert alert Jeremy Hall
     public static boolean validateCost(String payment) {
        try {
            Float.parseFloat(payment);
            
        } catch (Exception b) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            //changed to say valid cost as this is for a modality and valid payment doens't make any sense under this context
            alert.setContentText("Please enter a valid cost.\n");
            alert.show();
            return false;
        }
        return true;
    }
    
    
    //checks to see if a valid date of birth was entered by using validateDate
    //if not an alert is generated
    //alert a to alert alert Jeremy Hall
    public static boolean validateDOB(String date) {
        if (!validateDate(date)) {
            return false;
        }
        if (LocalDate.now().isBefore(LocalDate.parse(date))) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Try Again");
            alert.setContentText("Please enter a valid Date of Birth.\n");
            alert.show();
            return false;
        }
        return true;
    }

}