package uk.ac.ed.inf;
import java.sql.*;
import java.util.ArrayList;

/**
 * This is the class to access the database read and save the data from the database in lists.
 */
public class Orders {
    String port;
    String date;

    /**
     * Lists used to save the data after read the database.
     */
    ArrayList<String> orderNumberList = new ArrayList<>();
    ArrayList<String> customerList = new ArrayList<>();
    ArrayList<String> deliverToList = new ArrayList<>();

    Orders(String date,String port){
        this.date = date;
        this.port = port;
    }

    /**
     * This method is aim to access the data base and get all the orders on a given date.
     * Store different data in different list
     * orderNumber store the order numbers.
     * customerList store customer id
     * deliverTolist store where to deliver
     * The date format should be same as the format of deliveryDate in the data base.
     * @throws SQLException
     */
    public void getOrdersFromDataBase() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection("jdbc:derby://localhost:"+port+"/derbyDB");
            final String ordersQuery =
                    "select * from orders where deliveryDate = (?)";
            PreparedStatement psOrdersQuery =
                    conn.prepareStatement(ordersQuery);
            psOrdersQuery.setString(1,date);
// Search for the orders on that date and add them to a list
            ResultSet rs = psOrdersQuery.executeQuery();
            while (rs.next()) {
                String orderNumber = rs.getString("orderNo");
                orderNumberList.add(orderNumber);
                String customer = rs.getString("customer");
                customerList.add(customer);
                String deliverTo = rs.getString("deliverTo");
                deliverToList.add(deliverTo);
            }
        }catch (java.sql.SQLException e){
            e.printStackTrace();
        }
    }


    /**
     * Given a orderNumber then access the database to get the order items in this order
     * @param orderNo order number
     * @return list of items in the order
     * @throws SQLException
     */
    public ArrayList<String> getOrderDetailsFromDataBase(String orderNo)throws SQLException{
        ArrayList<String> itemList = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection("jdbc:derby://localhost:"+port+"/"+"derbyDB");
            final String orderDetailsQuery =
                    "select * from orderDetails where orderNo =(?)";
            PreparedStatement psOrderDetailQuery =
                    conn.prepareStatement(orderDetailsQuery);
            psOrderDetailQuery.setString(1, orderNo);
            ResultSet rs = psOrderDetailQuery.executeQuery();
            while (rs.next()) {
                String item = rs.getString("item");
                itemList.add(item);
            }
        }catch (Exception e){
            throw new java.sql.SQLException();
        }
        return itemList;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<String> getCustomerList() {
        return customerList;
    }

    public ArrayList<String> getDeliverToList() {
        return deliverToList;
    }

    public ArrayList<String> getOrderNumberList() {
        return orderNumberList;
    }

}