package uk.ac.ed.inf;

import java.sql.SQLException;
import java.util.ArrayList;


/**This class is used to save the date get from the web server and database in this class type.
 * combine the data get from web serve menus file and the data from database together
 * help to be call from other class
 * Also provide the method to generate a list contains all orders on a given day
 */
public class Ordetails {
    /**
     * orderNo is Order number
     */
    String orderNo;

    String deliveryDate;
    /**
     * customers id
     */
    String customer;
    /**
     * pick-up positions
     */
    ArrayList<String> deliverFrom;
    /**
     * deliver to where
     */
    String deliverTo;
    /**
     * items in the order
     */
    ArrayList<String> items;

    int cost;

    /**
     * return a list of orders on a given date in Ordetails type after input the web port and data port where to get the data
     * <p>
     * each ordetails combine the order's order number which get from database with the cost and pick-up location of this order which get from web server
     * @param date date of deliveries
     * @param dataPort the port where to get access the database
     * @param webPort the port where to get access the web server
     * @return return a list of detailed order
     * @throws SQLException database port access failed wrong data port
     */
    public ArrayList<Ordetails> makeDetailsList(String date,String dataPort,String webPort) throws SQLException {
        ArrayList<Ordetails> details = new ArrayList<>();
        // construct a menus using the given webport
        Menus menus = new Menus(webPort);
        // construct a orders using the given dataport
        Orders orders = new Orders(date,dataPort);
        orders.getOrdersFromDataBase();
        // add each order to a detailed order list
        for(int i = 0;i <orders.getOrderNumberList().size(); i ++){
            Ordetails ordetails = new Ordetails();
            ordetails.setOrderNo(orders.getOrderNumberList().get(i));
            ordetails.setDeliveryDate(orders.getDate());
            ordetails.setCustomer(orders.getCustomerList().get(i));
            ordetails.setDeliverTo(orders.getDeliverToList().get(i));
            ordetails.setItems(orders.getOrderDetailsFromDataBase(orders.getOrderNumberList().get(i)));
            ordetails.setCost(menus.getDeliveryCost(orders.getOrderDetailsFromDataBase(orders.getOrderNumberList().get(i))));
            ordetails.setDeliverFrom(menus.get3wordLocation(orders.getOrderDetailsFromDataBase(orders.getOrderNumberList().get(i))));
            details.add(ordetails);
        }
        return details;
    }

    //getters and setters
    public String getOrderNo(){
        return orderNo;
    }

    public String getDeliveryDate(){
        return deliveryDate;
    }

    public String getCustomer(){
        return customer;
    }

    public ArrayList<String> getDeliverFrom(){
        return deliverFrom;
    }

    public String getDeliverTo(){
        return deliverTo;
    }

    public ArrayList<String> getItems(){
        return items;
    }

    public int getCost(){
        return cost;
    }

    private void setOrderNo(String orderNo){
        this.orderNo = orderNo;
    }

    private void setCost(int cost) {
        this.cost = cost;
    }

    private void setCustomer(String customer) {
        this.customer = customer;
    }

    private void setDeliverFrom(ArrayList<String> deliverFrom) {
        this.deliverFrom = deliverFrom;
    }

    private void setDeliverTo(String deliverTo) {
        this.deliverTo = deliverTo;
    }

    private void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    private void setItems(ArrayList<String> items) {
        this.items = items;
    }

}
