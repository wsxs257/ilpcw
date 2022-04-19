package uk.ac.ed.inf;
import java.lang.Math;
import java.util.InputMismatchException;

/**
 * This class represent the longitude and latitude of one position on the map
 * it also provides the method to calculate the distance between to positions
 * and generate a new position the one moves be done.
 */
public class LongLat{
    double longitude;
    double latitude;

    LongLat (double longitude,double latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /*** check if this longlat position is in the confined area
     *
     * @return true if in the confined area;false if not in the confined area
     */
    public boolean isConfined(){
        if (-3.192473<longitude&&longitude<-3.184319&&latitude< 55.946233&&latitude>55.942617){
            return true;
        }else {
            return false;
        }
    }

    /*** return the distance from current longlat position to a given longlat position
     *
     * @param longLat any longlat position
     * @return calculated distance
     */
    public double distanceTo(LongLat longLat) {
        double distance = Math.sqrt((longitude-longLat.longitude)*(longitude-longLat.longitude)+(latitude-longLat.latitude)*(latitude-longLat.latitude));
        return distance;
    }


    /** check if the distance between the given longlat position and current longlat position is smaller than 0.00015
     *
     * @param longLat any longlat position
     * @return true if distance is smaller than 0.00015; false otherwise;
     */
    public boolean closeTo(LongLat longLat){
        double distance = distanceTo(longLat);
        if (distance<0.00015){
            return true;
        }else {
            return false;
        }
    }

    /**
     * given a input angle ,generate the next position's longlat from current longlat position
     * @param angle should in range 0-360 or -999 means hovering
     * @return next position's longlat
     * @throws IllegalArgumentException when the input angle is not in range of 0-360 or not equal to -999
     */
    public LongLat nextPosition(int angle){
        LongLat longLat = new LongLat(longitude,latitude);
        if (angle>=0&&angle<=360){
            double radians = Math.toRadians(angle);
            longLat.longitude = longitude + 0.00015*Math.cos(radians);
            longLat.latitude = latitude + 0.00015*Math.sin(radians);
            return longLat;
        }else if (angle == -999){
            return longLat;
        }else{
            throw new IllegalArgumentException("wrong angle input!!");
        }
    }
    //getters
    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

}
