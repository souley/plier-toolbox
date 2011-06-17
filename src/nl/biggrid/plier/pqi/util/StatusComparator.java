/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.plier.pqi.util;

/**
 *
 * @author Souley
 */
import java.util.Comparator;

public class StatusComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        if (o1 instanceof String && o2 instanceof String) {
            String theStatus = ((String) o1);
            String theOtherStatus = ((String) o2);
            if (!theStatus.equalsIgnoreCase(theOtherStatus)) {
                if (!(theStatus.equalsIgnoreCase("FINISHED") || theStatus.equalsIgnoreCase("RUNNING"))) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
        return 0;
    }
}
