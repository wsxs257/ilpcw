package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;

/**
 * this class is used to write the data into the database
 */
public class WriteDataBase {
    String dataPort;
    WriteDataBase(String dataPort){
        this.dataPort = dataPort;
    }

    /**
     * Creat a flightpath table in the database write the input list of flightpaths on the table
     * @param flightpaths a list of flightpaths
     * @throws SQLException
     */
    public void WriteFlightPathToDataBase(ArrayList<Flightpath> flightpaths) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:derby://localhost:"+dataPort+"/"+"derbyDB");
        Statement statement = conn.createStatement();

        DatabaseMetaData databaseMetadata = conn.getMetaData();
        ResultSet resultSet =
                databaseMetadata.getTables(null, null, "FLIGHTPATH", null);

        if (resultSet.next()) {
            statement.execute("drop table flightpath");
        }
        statement.execute(
                "create table flightpath("+
                        "orderNo char(8), " +
                        "fromLongitude double, " +
                        "fromLatitude double,"+
                        "angle integer,"+
                        "toLongitude double,"+
                        "toLatitude double)");

        PreparedStatement psFlightPath = conn.prepareStatement(
                "insert into flightpath values (?, ?, ?, ?, ?, ?)");
        for(Flightpath flightpath:flightpaths){
            psFlightPath.setString(1, flightpath.getOrderNo());

            psFlightPath.setDouble(2, flightpath.getFromLongitude());

            psFlightPath.setDouble(3, flightpath.getFromLatitude());

            psFlightPath.setInt(4, flightpath.getAngle());

            psFlightPath.setDouble(5, flightpath.getToLongitude());

            psFlightPath.setDouble(6, flightpath.getToLatitude());

            psFlightPath.execute();
        }
        System.out.println("Successful to create flightpath table!!");
    }

    /**
     * Creat a deliveries table in the database write the input list of deliveries on the table
     * @param deliveries a list of deliveries
     * @throws SQLException
     */
    public void writeDeliveriesToDataBase(ArrayList<Deliveries> deliveries) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:derby://localhost:"+dataPort+"/"+"derbyDB");

        Statement statement = conn.createStatement();

        DatabaseMetaData databaseMetadata = conn.getMetaData();
        ResultSet resultSet =
                databaseMetadata.getTables(null, null, "DELIVERIES", null);

        if (resultSet.next()) {
            statement.execute("drop table deliveries");
        }

        statement.execute(
                "create table deliveries(" +
                        "orderNo char(8), " +
                        "deliveredTo varchar(19), " +
                        "costInPence int)");

        PreparedStatement psDeliveries = conn.prepareStatement(
                "insert into deliveries values (?, ?, ?)");

        for(Deliveries delivery:deliveries){
            psDeliveries.setString(1, delivery.getOrderNo());
            psDeliveries.setString(2, delivery.getDeliveredTo());
            psDeliveries.setInt(3, delivery.getCostInPence());
            psDeliveries.execute();
        }
        System.out.println("Successful to create deliveries table!!");
    }

}
