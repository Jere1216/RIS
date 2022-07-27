/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datastorage;

import java.util.Objects;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 *
 * @author 14048
 */
public class PatientAlert {

    String alertID;
    String alert, flags;
    public Button placeholder = new Button("placeholder");
    public Button placeholder1 = new Button("placeholder");
    public Button placeholder2 = new Button("placeholder");
    

    public Button getPlaceholder() {
        return placeholder;
    }

    public Button getPlaceholder1() {
        return placeholder1;
    }
    
    public Button getPlaceholder2() {
        return placeholder2;
    }

    public PatientAlert(String orderID, String alert, String flags) {
        this.alertID = orderID;
        this.alert = alert;
        this.flags = flags;
    }

    public String getAlertID() {
        return alertID;
    }

    public void setAlertID(String alertID) {
        this.alertID = alertID;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.alert);
        hash = 41 * hash + Objects.hashCode(this.flags);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PatientAlert other = (PatientAlert) obj;
        if (this.alertID != other.alertID) {
            return false;
        }
        if (!Objects.equals(this.alert, other.alert)) {
            return false;
        }
        return Objects.equals(this.flags, other.flags);
    }

}
