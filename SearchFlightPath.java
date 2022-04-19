package uk.ac.ed.inf;


import java.awt.geom.Line2D;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.SQLException;
import java.util.*;
import java.lang.Math;

import com.mapbox.geojson.*;
import com.mapbox.geojson.Point;


public class SearchFlightPath {
    /**
     * web port using to construct menus,words and buildings in the class
     */
    String webPort;
    /**
     * data port using to construct orders in the class
     */
    String dataPort;
    /**
     * which date want to find out the path
     */
    String date;
    /**
     * battery of the drone.Mostly can do 1500 moves
     */
    int moves = 1500;
    /**
     * all buildings on the map
     */
    Buildings map;
    /**
     * where start and end the delivery
     */
    LongLat ApptonTower;
    /**
     * show drones current position
     */
    LongLat drone;
    /**
     * lines around the No-fly zone
     */
    ArrayList<Line2D> NoFlyZone;
    /**
     * list of mapbox point used to record each point drone move to
     */
    ArrayList<Point> RealFlightpath = new ArrayList<>();
    /**
     * list of deliveries class to record the orders has been delivered
     */
    ArrayList<Deliveries> deliveries = new ArrayList<>();
    /**
     * list of flightpath class to record each moves(where from and where to)
     */
    ArrayList<Flightpath> flightpaths = new ArrayList<>();

    /**
     * constructor of this class
     * web port will be used to construct Building class ,Words class and a list of detailed orders on the given date
     * @param webPort where to access the web server
     * @param dataPort where to access the database
     * @param date the date of orders
     */
    SearchFlightPath(String webPort, String dataPort, String date) {
        this.webPort = webPort;
        this.dataPort = dataPort;
        this.date = date;
        this.map = new Buildings(webPort);
        this.ApptonTower = map.getAppleton();
        this.drone = map.getAppleton();
        this.NoFlyZone = map.getLine2DNoFlyZone();
    }

    /**
     * given a what3word version location,return the longitude of this location
     * @param threeWord what3word string
     * @return longitude
     */
    private double getLng(String threeWord) {
        String[] wordList = threeWord.split("[.]");
        //generate a webURl
        String webURL = "http://localhost:" + webPort + "/words/" + wordList[0] + "/" + wordList[1] + "/" + wordList[2] + "/details.json";
        //construct words using webURL
        Words words = new Words(webURL);
        return words.getLng();
    }
    /**
     * given a what3word version location,return the latitude of this location
     * @param threeWord what3word string
     * @return latitude
     */
    private double getLat(String threeWord) {
        String[] wordList = threeWord.split("[.]");
        //generate a webURl
        String webURL = "http://localhost:" + webPort + "/words/" + wordList[0] + "/" + wordList[1] + "/" + wordList[2] + "/details.json";
        //construct words using webURL
        Words words = new Words(webURL);
        return words.getLat();
    }

    /**
     * return the locations the drone should go in the given order
     * @param order an order with it's deliverFrom and deliverTo
     * @return a list of locations of pick-up and deliver to
     */
    private ArrayList<LongLat> getStations(Ordetails order){
        ArrayList<LongLat> path = new ArrayList<>();
        ArrayList<String> deliverFrom = order.getDeliverFrom();
        for (String deliverfrom : deliverFrom) {
            double lngFrom = getLng(deliverfrom);
            double latFrom = getLat(deliverfrom);
            LongLat longLat = new LongLat(lngFrom, latFrom);
            path.add(longLat);
        }
            double lngTo = getLng(order.getDeliverTo());
            double latTO = getLat(order.getDeliverTo());
            LongLat longLat = new LongLat(lngTo, latTO);
            path.add(longLat);
        return path;
    }

    /**
     * get the list of orders on the given date and sort it by each order's cost
     * @return a well sorted list of orders
     * @throws SQLException information on a database access error or other errors.
     */
    private ArrayList<Ordetails> getDetailedOrderList()throws SQLException{
        Ordetails ordetails = new Ordetails();
        //using ordetails method to get a list of detailed orders
        ArrayList<Ordetails> detailedOrderList = ordetails.makeDetailsList(date, dataPort, webPort);
        // sort the order by the cost in pence,the large cost order will be deliver first
        detailedOrderList.sort(new costSorter());
        return detailedOrderList;
    }

    /**
     * calculate the angle between two given positions
     * The angle return should in range of 0-360 and is a multiple of 10
     * @param start the location of drone current position
     * @param end the location drone want to fly to
     * @return angle which drone should go to get to the end position
     */
    private int calAnagle(LongLat start, LongLat end) {

        if (end.getLongitude() > start.getLongitude() && end.getLatitude() == start.getLatitude()) {
            return 0;
        } else if (end.getLongitude() < start.getLongitude() && end.getLatitude() == start.getLatitude()) {
            return 180;
        } else if (end.getLongitude() == start.getLongitude() && end.getLatitude() > start.getLatitude()) {
            return 90;
        } else if (end.getLongitude() == start.getLongitude() && end.getLatitude() < start.getLatitude()) {
            return 270;
        }
        int tan = (int) Math.round(Math.atan(Math.abs((end.getLatitude() - start.getLatitude()) / (end.getLongitude() - start.getLongitude()))) * 180 / Math.PI);
        // >=5 return 10 for example 165 = 170
        if (tan % 10 >= 5) {
            tan = tan / 10 * 10 + 10;
        } else {
            tan = tan / 10 * 10;
        }
        //Quadrant
        if (end.getLongitude() > start.getLongitude() && end.getLatitude() > start.getLatitude())//first quadrant
        {
            return tan;
        } else if (end.getLongitude() < start.getLongitude() && end.getLatitude() > start.getLatitude())//second quadrant
        {
            return 180 - tan;
        } else if (end.getLongitude() < start.getLongitude() && end.getLatitude() < start.getLatitude())//third quadrant
        {
            return 180 + tan;

        } else {//forth quadrant
            return 360 - tan;
        }
    }

    /**
     * Check if the line between two LongLat position
     * interact with the line around the no-fly-zone
     * @param drone current Longlat position
     * @param destination pick up or deliver to Longlat position
     * @return true if the line interact with the no-fly-zone;false if the line does not interact with the no-fly-zone
     */
    private boolean checkInNoFlyZone(LongLat drone, LongLat destination) {
        Line2D flightpath = new Line2D.Double();
        flightpath.setLine(drone.getLongitude(), drone.getLatitude(), destination.getLongitude(), destination.getLatitude());
        for (Line2D noflyline : NoFlyZone) {
            if (noflyline.intersectsLine(flightpath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * when the next move of drone will cross the no-fly-zone.
     * Use this function to shift the angles that drone will move
     * return this shifted angle
     * @param current current position of drone
     * @param angle the angle which will cause the move of drone cross the no-fly-zone
     * @return the angle that drone can move and wont cross the no-fly-zone
     */
    private int findBestNextMoveAngle(LongLat current,int angle){
        int extraAngle = 10;
        //Keep adding or subtracting 10 degrees around the angle that cannot be passed at first,
        //until to find a passable angle drone can move
        while (angle-extraAngle>angle-180&&angle+extraAngle<angle+180){
            LongLat next1;
            LongLat next2;
            int angle1;
            int angle2;
            //Correction angle is between 0-360 degrees
            if (angle+extraAngle>360){
                 next1 = current.nextPosition((angle+extraAngle)-360);
                 angle1 = (angle+extraAngle)-360;
            }else {
                 next1 = current.nextPosition(angle+extraAngle);
                 angle1 = angle+extraAngle;
            }
            if (angle-extraAngle<0){
                next2 = current.nextPosition(360+(angle-extraAngle));
                angle2 = 360+(angle-extraAngle);
            }else {
                next2 = current.nextPosition(angle-extraAngle);
                angle2 = angle-extraAngle;
            }
            if (!checkInNoFlyZone(drone,next1)&& next1.isConfined()){
                return angle1;
            }
            if (!checkInNoFlyZone(drone,next2)&& next2.isConfined()){
                return angle2;
            }
            extraAngle = extraAngle+10;
        }
        return 0;
    }

    /**
     * when the next move of drone will go out from the confined area,
     * return the shift the angle which next move of drone is in the confined area
     * @param current current position of drone
     * @param angle the angle which will cause the move of drone leave the confined area
     * @return the angle after shifted
     */
    private int findAngleInConfined(LongLat current,int angle){
        int extraAngle = 10;
        while (angle-extraAngle>angle-180&&angle+extraAngle<angle+180){
            LongLat next1;
            LongLat next2;
            int angle1;
            int angle2;
            if (angle+extraAngle>360){
                next1 = current.nextPosition((angle+extraAngle)-360);
                angle1 = (angle+extraAngle)-360;
            }else {
                next1 = current.nextPosition(angle+extraAngle);
                angle1 = angle+extraAngle;
            }
            if (angle-extraAngle<0){
                next2 = current.nextPosition(360+(angle-extraAngle));
                angle2 = 360+(angle-extraAngle);
            }else {
                next2 = current.nextPosition(angle-extraAngle);
                angle2 = angle-extraAngle;
            }
            if (next1.isConfined()){
                return angle1;
            }
            if (next2.isConfined()){
                return angle2;
            }
            extraAngle = extraAngle+10;
        }
        return 0;
    }

    /**
     * main part of algorithm
     * given a list of locations to go
     * search the path and add each point in the path in the RealFlightPath list
     * add each moves in the flightPaths list
     * @param order the order which drone is delivering
     * @param stations the positions where drone should go
     */
    private void getFlypath(Ordetails order,ArrayList<LongLat> stations){
        for (LongLat station:stations){
            //check if the from drone current position will pass the no-fly zone when flies directly to the target point
            //If drone can directly arrive ,skip the current loop and go to the next loop
            LongLat landmark = map.getClosedLandMark(drone,station);
            while (checkInNoFlyZone(drone,station)){
                int nextAngle = calAnagle(drone,landmark);
                LongLat next = drone.nextPosition(nextAngle);

                //if the next move is not in the confined area shift the input angle of next move
                if (!next.isConfined()){
                    nextAngle = findAngleInConfined(drone,calAnagle(drone,landmark));
                    next = drone.nextPosition(nextAngle);
                }
                //if the next move will cross the no-fly-zone,shift the input angle of next move
                if (checkInNoFlyZone(drone,next)){
                    nextAngle = findBestNextMoveAngle(drone,calAnagle(drone,landmark));
                    next = drone.nextPosition(nextAngle);
                    //record each move
                    Flightpath flightpath = new Flightpath(order.getOrderNo(),drone.getLongitude(),drone.getLatitude(),nextAngle,next.getLongitude(),next.getLatitude());
                    setDrone(next);
                    moves = moves - 1;
                    Point point = Point.fromLngLat(drone.getLongitude(), drone.getLatitude());
                    RealFlightpath.add(point);
                    flightpaths.add(flightpath);
                }else{
                    Flightpath flightpath = new Flightpath(order.getOrderNo(),drone.getLongitude(),drone.getLatitude(),nextAngle,next.getLongitude(),next.getLatitude());
                    setDrone(next);
                    moves = moves - 1;
                    Point point = Point.fromLngLat(drone.getLongitude(), drone.getLatitude());
                    RealFlightpath.add(point);
                    flightpaths.add(flightpath);
                }
                //When the drone has completely reached the landmark,break this loop go to next
                if (drone.closeTo(landmark)){
                    break;
                }
            }

            //When the drone is not near the target point, keep the drone moving along the two-point angle
            while (!drone.closeTo(station)){
                int nextAngle = calAnagle(drone,station);
                LongLat next = drone.nextPosition(nextAngle);
                //if the next move is not in the confined area shift the input angle of next move
                if (!next.isConfined()){
                    nextAngle = findAngleInConfined(drone,calAnagle(drone,station));
                    next = drone.nextPosition(nextAngle);
                }
                //if the next move will cross the no-fly-zone,shift the input angle of next move
                if (checkInNoFlyZone(drone,next)){
                    nextAngle = findBestNextMoveAngle(drone,calAnagle(drone,station));
                    next = drone.nextPosition(nextAngle);
                    Flightpath flightpath = new Flightpath(order.getOrderNo(),drone.getLongitude(),drone.getLatitude(),nextAngle,next.getLongitude(),next.getLatitude());
                    setDrone(next);
                    moves = moves - 1;
                    Point point = Point.fromLngLat(drone.getLongitude(), drone.getLatitude());
                    RealFlightpath.add(point);
                    flightpaths.add(flightpath);
                }else{
                    Flightpath flightpath = new Flightpath(order.getOrderNo(),drone.getLongitude(),drone.getLatitude(),nextAngle,next.getLongitude(),next.getLatitude());
                    setDrone(next);
                    moves = moves - 1;
                    Point point = Point.fromLngLat(drone.getLongitude(), drone.getLatitude());
                    RealFlightpath.add(point);
                    flightpaths.add(flightpath);
                }
            }
            //when close to the destination hovering
            LongLat next = drone.nextPosition(-999);
            int nextAngle = -999;
            Flightpath flightpath = new Flightpath(order.getOrderNo(),drone.getLongitude(),drone.getLatitude(),nextAngle,next.getLongitude(),next.getLatitude());
            setDrone(next);
            moves = moves - 1;
            flightpaths.add(flightpath);
            //When you reach a target point and find that the moves are not enough
            //stop the whole cycle
        }
    }

    /**
     * Algorithms to get drones back.add each moves in the path in the RealFlightPath list
     * add each moves in the flightPaths list
     */
    private void backToApp(){
        //check if the from drone current position will pass the no-fly zone when flies directly to the target point
        //If drone can directly arrive ,skip the current loop and go to the next loop
        LongLat landmark = map.getClosedLandMark(drone,getApptonTower());
        while (checkInNoFlyZone(drone,getApptonTower())){

            int nextAngle = calAnagle(drone,landmark);
            LongLat next = drone.nextPosition(nextAngle);
//if the next move is not in the confined area shift the input angle of next move
            if (!next.isConfined()){
                nextAngle = findAngleInConfined(drone,calAnagle(drone,landmark));
                next = drone.nextPosition(nextAngle);
            }
//if the next move will cross the no-fly-zone,shift the input angle of next move
            if (checkInNoFlyZone(drone,next)){
                nextAngle = findBestNextMoveAngle(drone,calAnagle(drone,landmark));
                next = drone.nextPosition(nextAngle);
                Flightpath flightpath = new Flightpath("AppTower",
                        drone.getLongitude(),drone.getLatitude(),nextAngle,next.getLongitude(),next.getLatitude());
                setDrone(next);
                moves = moves - 1;
                Point point = Point.fromLngLat(drone.getLongitude(), drone.getLatitude());
                RealFlightpath.add(point);
                flightpaths.add(flightpath);
            }else {
                Flightpath flightpath = new Flightpath("AppTower",
                        drone.getLongitude(),drone.getLatitude(),nextAngle,next.getLongitude(),next.getLatitude());
                setDrone(next);
                moves = moves - 1;
                Point point = Point.fromLngLat(drone.getLongitude(), drone.getLatitude());
                RealFlightpath.add(point);
                flightpaths.add(flightpath);
            }
            //When the drone has completely reached the landmark,break this loop go to next
            if (drone.closeTo(landmark)){
                break;
            }
        }
        //When the drone is not near the target point, keep the drone moving along the two-point angle
        while (!drone.closeTo(getApptonTower())){
            int nextAngle = calAnagle(drone,getApptonTower());
            LongLat next = drone.nextPosition(calAnagle(drone,getApptonTower()));

            if (!next.isConfined()){
                nextAngle = findAngleInConfined(drone,calAnagle(drone,getApptonTower()));
                next = drone.nextPosition(nextAngle);
            }

            if (checkInNoFlyZone(drone,next)){
                nextAngle = findBestNextMoveAngle(drone,calAnagle(drone,getApptonTower()));
                Flightpath flightpath = new Flightpath("AppTower",
                        drone.getLongitude(),drone.getLatitude(),nextAngle,next.getLongitude(),next.getLatitude());
                setDrone(next);
                moves = moves - 1;
                Point point = Point.fromLngLat(drone.getLongitude(), drone.getLatitude());
                RealFlightpath.add(point);
                flightpaths.add(flightpath);
            }else{
                Flightpath flightpath = new Flightpath("AppTower",
                        drone.getLongitude(),drone.getLatitude(),nextAngle,next.getLongitude(),next.getLatitude());
                setDrone(next);
                moves = moves - 1;
                Point point = Point.fromLngLat(drone.getLongitude(), drone.getLatitude());
                RealFlightpath.add(point);
                flightpaths.add(flightpath);
            }
        }
    }

    /**
     * function to start to search the flight path
     * @throws SQLException information on a database access error or other errors.
     */

    public void searchPath() throws SQLException {
        //All sorted orders on the given day
        ArrayList<Ordetails> orders = getDetailedOrderList();
        System.out.println("total orders number "+orders.size());
        int costOfAll=0;
        int costSend =0;
        int numberOfdelivers = 0;

        for (Ordetails ordetails:orders){
            costOfAll = costOfAll+ordetails.getCost();
        }
        System.out.println("total cost "+costOfAll);
        //add start point to the flight path record
        Point StartLocation = Point.fromLngLat(ApptonTower.getLongitude(), ApptonTower.getLatitude());
        RealFlightpath.add(StartLocation);
        //deliver each order in the orders list
        for (Ordetails order:orders){
            System.out.println(order.getOrderNo());
            ArrayList<LongLat> path = getStations(order);
            //sort the shop
            // Choose a shorter route
            //0-1-2-3
            //or0-2-1-3
            if (path.size()==3){
                LongLat deliverFrom1 = path.get(0);
                LongLat deliverFrom2 = path.get(1);
                LongLat deliverTo = path.get(2);
                if (drone.distanceTo(deliverFrom1)+deliverFrom1.distanceTo(deliverFrom2)+deliverFrom2.distanceTo(deliverTo)>
                        drone.distanceTo(deliverFrom2)+deliverFrom2.distanceTo(deliverFrom1)+deliverFrom1.distanceTo(deliverTo)){
                    path.set(0,deliverFrom2);
                    path.set(1,deliverFrom1);
                }
            }
            //start path search
            getFlypath(order,path);
            //save the deliveries have complete
            Deliveries delivery = new Deliveries(order.getOrderNo(),order.getDeliverTo(),order.getCost());
            costSend = costSend+order.getCost();
            deliveries.add(delivery);
            numberOfdelivers = numberOfdelivers+1;
            // when move is not enough ,stop to deliver
            if (moves<50){
                break;
            }

        }
        //back to appleton tower
        backToApp();

        System.out.println("number of orders be delivered "+numberOfdelivers);
        System.out.println("total cost be delivered "+costSend);
        System.out.println(moves);
        System.out.println(flightpaths.size());
    }

    /**
     * generate a local geojson file about the drone route on given a date,
     * @param day day
     * @param month month
     * @param year year
     */
    public void generateGeoJsonFile(String day,String month,String year){
        LineString line = LineString.fromLngLats(getRealFlightpath());
        Geometry geometry = (Geometry) line;
        Feature feature = Feature.fromGeometry(geometry);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        String jsonString = featureCollection.toJson();
        try {
            FileWriter fileWriter = new FileWriter("drone-"+day+"-"+month+"-"+year+".geojson");
            fileWriter.write(jsonString);
            fileWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    //getter and setters
    public void setDrone(LongLat drone) {
        this.drone = drone;
    }

    public ArrayList<Deliveries> getDeliveries() {
        return deliveries;
    }

    public ArrayList<Flightpath> getFlightpaths() {
        return flightpaths;
    }

    public ArrayList<Point> getRealFlightpath() {
        return RealFlightpath;
    }

    public LongLat getApptonTower() {
        return ApptonTower;
    }

}










