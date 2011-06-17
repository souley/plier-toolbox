/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.plier.pqi.query;

import java.sql.Timestamp;

/**
 *
 * @author Souley
 */
public class Event {
    private String name;
    private String source;
    private Timestamp timestamp;

    public Event() {
    }

    public String getName() {
        return name;
    }

    public void setName(String aName) {
        name = aName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String src) {
        source = src;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp time) {
        timestamp = time;
    }
}
