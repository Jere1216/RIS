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
public class Order {

    private String orderID;
    private String order;
    public Button placeholder = new Button("placeholder");
    public Button placeholder2 = new Button("placeholder2");
    private float cost;

    public Order(String orderID, String order) {
        this.orderID = orderID;
        this.order = order;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public Button getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(Button placeholder) {
        this.placeholder = placeholder;
    }
    
    public Button getPlaceholder2() {
        return placeholder2;
    }

    public void setPlaceholder2(Button placeholder) {
        this.placeholder2 = placeholder;
    }

}
