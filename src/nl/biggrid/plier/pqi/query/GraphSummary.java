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
public class GraphSummary {
    private long dbId;
    private String id;
    private Timestamp start;
    private Timestamp end;
    private String duration;
    private int nbEvents;
    private int nbProcs;
    private String status;
    private int nbParams;

    public void setDbId(final long nDbId) {
        dbId = nDbId;
    }

    public void setId(final String nId) {
        id = nId;
    }

    public void setStart(final Timestamp nStart) {
        start = nStart;
    }

    public void setEnd(final Timestamp nEnd) {
        end = nEnd;
    }

    public void setDuration(final String nDuration) {
        duration = nDuration;
    }

    public void setStatus(final String aStatus) {
        status = aStatus;
    }

    public long getDbId() {
        return dbId;
    }

    public void setNbEvents(final int nne) {
        nbEvents = nne;
    }

    public void setNbProcs(final int nnp) {
        nbProcs = nnp;
    }

    public void setNbParams(final int nnp) {
        nbParams = nnp;
    }

    public int getNbEvents() {
        return nbEvents;
    }

    public int getNbProcs() {
        return nbProcs;
    }

    public String getId() {
        return id;
    }

    public Timestamp getStart() {
        return start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public String getDuration() {
        return duration;
    }

    public String getStatus() {
        return status;
    }

    public int getNbParams() {
        return nbParams;
    }

}
