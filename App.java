package uk.ac.ed.inf;

import java.sql.SQLException;

/**
 * Hello world!
 */
public class App {
    public static void main( String[] args ) throws SQLException {
        String day =  args[0];
        String month = args[1];
        String year = args[2];
        String webPort = args[3];
        String dataPort = args[4];

        String date = year+"-"+month+"-"+day;

        SearchFlightPath droneFlightPath = new SearchFlightPath(webPort,dataPort,date);

        WriteDataBase writeDataBase = new WriteDataBase(dataPort);

        long startTime=System.currentTimeMillis();
        droneFlightPath.searchPath();
        droneFlightPath.generateGeoJsonFile(day,month,year);

        writeDataBase.writeDeliveriesToDataBase(droneFlightPath.getDeliveries());
        writeDataBase.WriteFlightPathToDataBase(droneFlightPath.getFlightpaths());

        long endTime=System.currentTimeMillis();

        System.out.println("program execution time： "+(endTime-startTime)+"ms");

    }
}
