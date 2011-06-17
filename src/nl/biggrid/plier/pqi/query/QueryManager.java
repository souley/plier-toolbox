/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.plier.pqi.query;

import java.util.LinkedHashSet;
import java.util.Vector;

import javax.swing.SwingWorker;
import nl.biggrid.plier.opm.Artifact;
import nl.biggrid.plier.opm.OPMGraph;
import nl.biggrid.plier.pqi.ui.DBSettings;
/**
 *
 * @author Souley
 */
public class QueryManager {
    public static enum SearchEngine {JDBC, MYSQL, ORACLE, OTHER};
    static public final String STATUS_UNKNOWN = "UNKNOWN_STATUS";
    static public final String USER_UNKNOWN = "UNKNOWN_USER";
    static private QueryManager _instance = null;
    private IFinder finder = null;

    protected QueryManager() {
    }

    static public QueryManager instance() {
      if(null == _instance) {
         _instance = new QueryManager();
      }
      return _instance;
    }

    public SwingWorker<Void, Void> initFinder(final SearchEngine engine, final DBSettings settings) {
        if (engine == SearchEngine.JDBC) {
            finder = new JDBCFinder(settings.getDBHost(), settings.getDBUser(), settings.getDBPwd());
            return finder.init();
//        } else if (engine == SearchEngine.ORACLE) {
//            finder = new OracleFinder(settings.getSettgins());
//            System.out.println("### QM:if instantiating an Oracle finder");
//            return finder.init();
        } else if (engine == SearchEngine.MYSQL) {
            finder = new MySQLFinder(settings.getSettgins());
            System.out.println("### QM:if instantiating a MySQL finder");
            return finder.init();
        }
        return null;
    }

    public void shutdown() {
//        if (finder instanceof OracleFinder) {
//            ((OracleFinder)finder).shutdown();
//        }
    }

    public IFinder getFinder() {
        return finder;
    }

    public Vector<GraphSummary> searchAll() {
        return finder.searchAll();
    }

    public Vector<GraphSummary> searchName(final String name) {
        return finder.searchName(name);
    }

    public Vector<GraphSummary> searchUser(final String user) {
        return finder.searchUser(user);
    }

    public Vector<GraphSummary> searchTimestamp(final String timestamp) {
        return finder.searchTimestamp(timestamp);
    }

    public Vector<GraphSummary> searchStatus(final String status) {
        return finder.searchStatus(status);
    }

    public Vector<GraphSummary> searchNameUser(final String name, final String user) {
        return finder.searchNameUser(name, user);
    }

    public Vector<GraphSummary> searchNameTime(final String name, final String time) {
        return finder.searchNameTime(name, time);
    }

    public Vector<GraphSummary> searchNameStatus(final String name, final String status) {
        return finder.searchNameStatus(name, status);
    }

    public Vector<GraphSummary> searchUserTime(final String user, final String time) {
        return finder.searchUserTime(user, time);
    }

    public Vector<GraphSummary> searchUserStatus(final String user, final String status) {
        return finder.searchUserStatus(user, status);
    }

    public Vector<GraphSummary> searchTimeStatus(final String timestamp, final String status) {
        return finder.searchTimeStatus(timestamp, status);
    }

    public Vector<GraphSummary> searchNameUserTime(final String name, final String user, final String timestamp) {
        return finder.searchNameUserTime(name, user, timestamp);
    }

    public Vector<GraphSummary> searchNameUserStatus(final String name, final String user, final String status) {
        return finder.searchNameUserStatus(name, user, status);
    }

    public Vector<GraphSummary> searchNameTimeStatus(final String name, final String timestamp, final String status) {
        return finder.searchNameTimeStatus(name, timestamp, status);
    }

    public Vector<GraphSummary> searchUserTimeStatus(final String user, final String timestamp, final String status) {
        return finder.searchUserTimeStatus(user, timestamp, status);
    }

    public Vector<GraphSummary> searchNameUserTimeStatus(final String name, final String user, final String timestamp, final String status) {
        return finder.searchNameUserTimeStatus(name, user, timestamp, status);
    }

    public Vector<String> getUserList() {
        return finder.getUserList();
    }

    public Vector<String> getStatusList() {
        return finder.getStatusList();
    }

//    public OPMGraph getExperiment(final String opmGId) {
//        return finder.getExperiment(opmGId);
//    }

    public OPMGraph getExperiment(final long gid) {
        return finder.getExperiment(gid);
    }

//    public LinkedHashSet<Event> getSortedGraphEvents(final int gid){
//        return finder.getSortedGraphEvents(gid);
//    }

    public LinkedHashSet<Artifact> getSortedGraphProcessIns(final long gid, final long pid){
        return finder.getSortedGraphProcessIns(gid, pid);
    }

}
