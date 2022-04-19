package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;


/**
 * the orders have been delivered by the drone
 * will be saved in this class type
 * help to write the table in database
 */
public class Deliveries {
    /**
     * delivered order's number
     */
    String orderNo;
    /**
     * delivered to where
     */
    String deliveredTo;
    /**
     * delivered order's cost
     */
    int costInPence;

    Deliveries(String orderNo,String deliveredTo,int costInPence){
        this.costInPence = costInPence;
        this.deliveredTo = deliveredTo;
        this.orderNo = orderNo;
    }

    //getters
    public int getCostInPence() {
        return costInPence;
    }

    public String getDeliveredTo() {
        return deliveredTo;
    }

    public String getOrderNo() {
        return orderNo;
    }

}
