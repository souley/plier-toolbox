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

public class DurationComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        if (o1 instanceof String && o2 instanceof String) {
            String[] theItems = ((String) o1).split(":");
            String[] theOtherItems = ((String) o2).split(":");
            if (!(theItems.length == 3 && theOtherItems.length == 3)) {
                return 0;
            }
            int theHours = Integer.parseInt(theItems[0]);
            int theMins = Integer.parseInt(theItems[1]);
            int theSecs = Integer.parseInt(theItems[2]);
            int theOtherHours = Integer.parseInt(theOtherItems[0]);
            int theOtherMins = Integer.parseInt(theOtherItems[1]);
            int theOtherSecs = Integer.parseInt(theOtherItems[2]);
            if (theHours == theOtherHours) {
                if (theMins == theOtherMins) {
                    return Integer.valueOf(theSecs).compareTo(Integer.valueOf(theOtherSecs));
                } else {
                    return Integer.valueOf(theMins).compareTo(Integer.valueOf(theOtherMins));
                }
            }
            return Integer.valueOf(theHours).compareTo(Integer.valueOf(theOtherHours));
        }
        return 0;
    }
}

