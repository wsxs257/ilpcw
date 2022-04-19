package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;

/**
 * each moves in the algorithm will be save as this type of class
 * help to write the database
 */
public class Flightpath {
    /**
     * the order's order number which is being delivered
     */
    String orderNo;
    double fromLongitude;
    double fromLatitude;
    /**
     * The angle drone moves
     */
    int angle;
    double toLongitude;
    double toLatitude;

    Flightpath(String orderNo,double fromLongitude,double fromLatitude,int angle,double toLongitude,double toLatitude){
        this.orderNo = orderNo;
        this.fromLongitude = fromLongitude;
        this.fromLatitude = fromLatitude;
        this.angle = angle;
        this.toLongitude = toLongitude;
        this.toLatitude = toLatitude;
    }

    //getters
    String getOrderNo(){
        return orderNo;
    }

    double getFromLongitude(){
        return fromLongitude;
    }

    double getFromLatitude(){
        return fromLatitude;
    }

    int getAngle(){
        return angle;
    }

    double getToLongitude(){
        return toLongitude;
    }

    double getToLatitude(){
        return toLatitude;
    }

}
