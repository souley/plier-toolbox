/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.pcc.pqi.query;

import java.util.LinkedHashSet;
import java.util.Vector;

import javax.swing.SwingWorker;

//import nl.biggrid.plier.event.Event;
import nl.biggrid.plier.opm.Agent;
import nl.biggrid.plier.opm.Artifact;
import nl.biggrid.plier.opm.OPMGraph;
import nl.biggrid.plier.opm.Process;
/**
 *
 * @author Souley
 */
public interface IFinder {
    public SwingWorker<Void, Void> init();

    public Vector<GraphSummary> searchAll();
    public Vector<GraphSummary> searchName(final String name);
    public Vector<GraphSummary> searchUser(final String user);
    public Vector<GraphSummary> searchStatus(final String status);
    public Vector<GraphSummary> searchTimestamp(final String timstamp);
    public Vector<GraphSummary> searchNameUser(final String name, final String user);
    public Vector<GraphSummary> searchNameTime(final String name, final String timstamp);
    public Vector<GraphSummary> searchNameStatus(final String name, final String status);
    public Vector<GraphSummary> searchUserTime(final String user, final String time);
    public Vector<GraphSummary> searchUserStatus(final String user, final String status);
    public Vector<GraphSummary> searchTimeStatus(final String time, final String status);    
    public Vector<GraphSummary> searchNameUserTime(final String name, final String user, final String time);
    public Vector<GraphSummary> searchNameUserStatus(final String name, final String user, final String status);
    public Vector<GraphSummary> searchNameTimeStatus(final String name, final String time, final String status);
    public Vector<GraphSummary> searchUserTimeStatus(final String user, final String time, final String status);
    public Vector<GraphSummary> searchNameUserTimeStatus(final String name, final String user, final String time, final String status);
    
    public Vector<String>   getUserList();
    public Vector<String>   getStatusList();
    public GraphSummary     getDetail(final String graphId);
    //public Vector<Event>    getGraphEvents(final int gid);
    public Vector<Process>  getGraphProcesses(final long gid);
    public Vector<Artifact> getGraphParameters(final long gid);
    public Vector<Artifact> getGraphProcessIns(final long gid, final long pid);
    public Vector<Artifact> getGraphProcessOuts(final long gid, final long pid);
    public Vector<Agent>    getGraphProcessControllers(final long gid, final long pid);
    public Vector<Process>  getGraphArtifactGenerators(final long gid, final long aid);
    public OPMGraph         getExperiment(final String opmGId);

    //public LinkedHashSet<Event> getSortedGraphEvents(final int gid);
    public LinkedHashSet<Artifact> getSortedGraphProcessIns(final long gid, final long pid);
}
