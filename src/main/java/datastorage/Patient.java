/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datastorage;

import javafx.scene.control.Button;

/**
 *
 * @author 14048
 */
public class Patient {

    String patientID;
    String email, fullName, dob, address, insurance;
    public Button placeholder = new Button("Placeholder");
    public Button placeholder2 = new Button("Placeholder2");

    public Patient() {
    }

    public Patient(String patientID, String email, String fullName, String dob, String address, String insurance) {
        this.patientID = patientID;
        this.email = email;
        this.fullName = fullName;
        this.dob = dob;
        this.address = address;
        this.insurance = insurance;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInsurance() {
        return insurance;
    }

    public Button getPlaceholder() {
        return placeholder;
    }
    
    public Button getPlaceholder2() {
        return placeholder2;
    }

    public void setInsurance(String insurance) {
        this.insurance = insurance;
    }
}
