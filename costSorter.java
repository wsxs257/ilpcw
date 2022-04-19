package uk.ac.ed.inf;
import java.util.Comparator;

/**
 * a comparator class help to sort the orders
 */
public class costSorter implements Comparator<Ordetails>{
    @Override
    public int compare(Ordetails o1, Ordetails o2) {
        return (o2.cost)-(o1.cost);
    }
}
