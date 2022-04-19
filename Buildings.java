package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to create HttpClient
 * And use the method to Send HTTP request to access the building folder in the
 * Webserver.And it also provide the method to parsing the response
 *
 */
public class Buildings {
    String port;
    /**
     * final variables Appleton tower on the map.
     */
    final LongLat Appleton = new LongLat(-3.186874, 55.944494);
    /**
     * Constructor of this class
     * @param port the port where to access the web server
     */
    Buildings(String port){
        this.port = port;
    }
    //creat the client;
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Construct a HTTP request to access the buildings/no-fly-zone files
     * @return a constructed HTTP request
     */
    private HttpRequest getNoFlyZoneRequest(){
        return HttpRequest.newBuilder().uri(URI.create("http://localhost:"+port+"/buildings/no-fly-zones.geojson.")).build();
    }

    /**
     * Send the HTTP request to the web server and get the response body
     * @return the response body
     */
    private String getNoFlyZoneResponse(){
        String response_body = " ";
        try {
            HttpResponse<String> response = client.send(getNoFlyZoneRequest(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                response_body = response.body();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response_body;
    }
    /**
     * Construct a HTTP request to access the buildings/landmarks files
     * @return a constructed HTTP request
     */
    private HttpRequest getLandmarkRequest(){
        return HttpRequest.newBuilder().uri(URI.create("http://localhost:"+port+"/buildings/landmarks.geojson.")).build();
    }

    /**
     * Send the HTTP request to the web server and get the response body
     * @return the response body
     */
    private String getLandmarkResponse(){
        String response_body = " ";
        try {
            HttpResponse<String> response = client.send(getLandmarkRequest(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                response_body = response.body();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response_body;
    }

    /**
     * After get the response of no-fly-zone file from Webserver
     * Parsing the geojson response and convert the Polygon type No fly zone
     * to the lines around the no-fly zone in Line2D type.
     *
     * @return list of Line2D line around the no-fly zone
     */
    public ArrayList<Line2D> getLine2DNoFlyZone(){
        String source = getNoFlyZoneResponse();
        FeatureCollection featureCollection = FeatureCollection.fromJson(source);
        List<Feature> features = featureCollection.features();
        ArrayList<Line2D> line2DArrayList = new ArrayList<>();
        //iterate all polygon in the feature collection
        if (features != null) {
            for(Feature feature:features){
                Polygon polygon = (Polygon)feature.geometry();
                // iterate all list of point in the polygon
                if (polygon != null) {
                    for(List<Point> listPoint:polygon.coordinates()){
                        ArrayList<Point2D> point2DArrayList = new ArrayList<>();
                        //iterate all point and add creat new point in point2D type
                        for (Point point:listPoint){
                            Point2D point2D = new Point2D.Double();
                            point2D.setLocation(point.coordinates().get(0),point.coordinates().get(1));
                            point2DArrayList.add(point2D);
                        }
                        //connect each point2D type point to a line around the no-fly-zone
                        for (int i = 0;i<point2DArrayList.size();i++){
                            if (i == point2DArrayList.size()-1){
                                Line2D line2D = new Line2D.Double();
                                line2D.setLine(point2DArrayList.get(i),point2DArrayList.get(0));
                                line2DArrayList.add(line2D);
                            }else {
                                Line2D line2D = new Line2D.Double();
                                line2D.setLine(point2DArrayList.get(i),point2DArrayList.get(i+1));
                                line2DArrayList.add(line2D);
                            }
                        }
                    }
                }
            }
        }
        return line2DArrayList;
    }

    /**
     * Parsing the geojson response and get the first landmark on the map
     *
     * @return LongLat type landmark1
     */
    private LongLat getLandMarkOne() {
        String source = getLandmarkResponse();
        FeatureCollection featureCollection = FeatureCollection.fromJson(source);
        List<Feature> features = featureCollection.features();
        Feature feature = features.get(0);
        Point point = (Point) feature.geometry();
        return new LongLat(point.coordinates().get(0),point.coordinates().get(1));
    }

    /**
     * Parsing the geojson response and get the second landmark on the map
     *
     * @return LongLat type landmark2
     */
    private LongLat getLandMarkTwo() {
        String source = getLandmarkResponse();
        FeatureCollection featureCollection = FeatureCollection.fromJson(source);
        List<Feature> features = featureCollection.features();
        Feature feature = features.get(1);
        Point point = (Point) feature.geometry();
        return new LongLat(point.coordinates().get(0),point.coordinates().get(1));
    }

    private ArrayList<LongLat> getLandMark(){
        String source = getLandmarkResponse();
        FeatureCollection featureCollection = FeatureCollection.fromJson(source);
        List<Feature> features = featureCollection.features();
        ArrayList<LongLat> landmarks = new ArrayList<>();
        for (Feature feature:features){
            Point point = (Point) feature.geometry();
            landmarks.add(new LongLat(point.coordinates().get(0),point.coordinates().get(1)));
        }
        return landmarks;
    }

    /**
     * Check if the line between two LongLat position
     * interact with the line around the no-fly-zone
     * @param current any Longlat position
     * @param destination any Longlat position
     * @return true if the line interact with the no-fly-zone
     * @return false if the line does not interact with the no-fly-zone
     */
    private boolean checkInNoFlyZone(LongLat current, LongLat destination) {
        Line2D flightpath = new Line2D.Double();
        flightpath.setLine(current.longitude, current.latitude, destination.longitude, destination.latitude);
        ArrayList<Line2D> NoFlyZone = getLine2DNoFlyZone();
        for (Line2D noflyline : NoFlyZone) {
            if (noflyline.intersectsLine(flightpath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a longlat position get the available and closest landmark
     *
     * @param current any Longlat position
     * @param station any Longlat position
     * @return the closest landmark on the map
     */
    public LongLat getClosedLandMark(LongLat current,LongLat station){
        ArrayList<LongLat> landmarks = getLandMark();
        ArrayList<LongLat> availLandmarks = new ArrayList<>();
        for (LongLat landmark:landmarks){
            if (!checkInNoFlyZone(current,landmark)&&!checkInNoFlyZone(station,landmark)){
                availLandmarks.add(landmark);
            }
        }
        double minDistance = 0.0;
        LongLat closest = new LongLat(0.0,0.0);
        for (LongLat landmark:availLandmarks){
            if (minDistance==0.0){
                minDistance = current.distanceTo(landmark)+landmark.distanceTo(station);
                closest = landmark;
            }
            if (current.distanceTo(landmark)+landmark.distanceTo(station)<minDistance){
                minDistance = current.distanceTo(landmark)+landmark.distanceTo(station);
                closest = landmark;
            }
        }
        return closest;
    }

    //getters
    public LongLat getAppleton() {
        return Appleton;
    }
}
