/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.plier.pqi.query;

import java.util.Vector;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.LinkedHashSet;
import javax.swing.SwingWorker;

import nl.biggrid.plier.opm.Artifact;
import nl.biggrid.plier.opm.Process;
import nl.biggrid.plier.opm.Agent;
//import nl.biggrid.plier.event.Event;
import nl.biggrid.plier.opm.OPMGraph;
import nl.biggrid.plier.pqi.query.Event;
/**
 *
 * @author Souley
 */
public class JDBCFinder implements IFinder {

    static final String SNAPSHOT_NAME = "opm_summary";

    class Experiment {

        int dbId = 0;
        String name = "";
        String user = "";
        String status = "";
        String duration = "";

        public Experiment(final int anId, final String aName, final String aUser, final String aStatus) {
            dbId = anId;
            name = aName;
            user = aUser;
            status = aStatus;
        }
    }

    class Inventory {

        Vector<Experiment> experiments = null;

        public Inventory() {
            experiments = new Vector<Experiment>();
        }

        public Inventory(final Vector<Experiment> someExperiments) {
            experiments = someExperiments;
        }

        public void add(final Experiment experiment) {
            experiments.add(experiment);
        }

        public String getStatusById(final int anId) {
            for (Experiment e : experiments) {
                if (anId == e.dbId) {
                    return e.status;
                }
            }
            return "";
        }

        public String getStatusByName(final String aName) {
            for (Experiment e : experiments) {
                if (e.name.matches("(?i:.*" + aName + ".*)")) {
                    return e.status;
                }
            }
            return "";
        }

        public String getStatusByUser(final String aUser) {
            for (Experiment e : experiments) {
                if (aUser.equalsIgnoreCase(e.user)) {
                    return e.status;
                }
            }
            return "";
        }

        public boolean statusExists(final String aStatus) {
            for (Experiment e : experiments) {
                if (aStatus.equalsIgnoreCase(e.status)) {
                    return true;
                }
            }
            return false;
        }

        public Vector<String> validStatus() {
            Vector<String> buffer = new Vector<String>();
            for (Experiment e : experiments) {
                if (!buffer.contains(e.status)) {
                    buffer.add(e.status);
                }
            }
            return buffer;
        }

        public void setExperimentDuration(final int expKey, final String duration) {
            for (Experiment e : experiments) {
                if (e.dbId == expKey) {
                    e.duration = duration;
                    break;
                }
            }
        }

        public String getExperimentDuration(final int expKey) {
            for (Experiment e : experiments) {
                if (e.dbId == expKey) {
                    return e.duration;
                }
            }
            return null;
        }
    }

    class InitializationTask extends SwingWorker<Void, Void> {

        int getItemCount() {
            int itemCount = 0;
            String statement = "select count(*) as total  " +
                    "from OPM_GRAPH ";
            if (dbConnection != null) {
                try {
                    Statement stmt = dbConnection.createStatement();
                    ResultSet rs = stmt.executeQuery(statement);
                    if (rs.next()) {
                        itemCount = rs.getInt("total");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Exception: connection null!!!");
            }
            return itemCount;
        }

        void setDurations() {
            String statement = "select opm_dbid_graph, concat(timediff(max(event_timestamp), min(event_timestamp)), '') duration " +
                    "from PLIER_EVENT " +
                    "group by opm_dbid_graph";
            if (dbConnection != null) {
                try {
                    Statement stmt = dbConnection.createStatement();
                    ResultSet rs = stmt.executeQuery(statement);
                    while (rs.next()) {
                        inventory.setExperimentDuration(rs.getInt("opm_dbid_graph"),
                                rs.getString("duration"));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Exception: connection null!!!");
            }
        }

        boolean containsSimilar(Experiment anExperiment) {
            for (Experiment experiment : inventory.experiments) {
                if (experiment.dbId == anExperiment.dbId && experiment.name.equalsIgnoreCase(anExperiment.name)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Void doInBackground() {
            int progress = 0;
            String statement = "select * from (" +
                    "select g.opm_dbid_graph, g.opm_id, substring_index(substring_index(a.opm_value, '/CN=', -2), '/CN=', 1) username, event_description status, event_timestamp start " +
                    "from OPM_GRAPH g, OPM_AGENT a, PLIER_EVENT e " +
                    "where g.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and e.OPM_DBID_GRAPH=g.OPM_DBID_GRAPH and event_source_name='Moteur/Diane' " +
                    "union " +
                    "select g.opm_dbid_graph, g.opm_id, substring_index(substring_index(a.opm_value, '/CN=', -2), '/CN=', 1) username, NULL, NULL " +
                    "from OPM_GRAPH g, OPM_AGENT a " +
                    "where g.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%'" +
                    ") as graphs";
            setProgress(0);
            try {
                Thread.sleep(400);
            } catch (InterruptedException ignore) {
            }
            firePropertyChange("progress", new Integer(progress), new Integer(1));
            int itemCount = getItemCount();
            inventory = new Inventory();
            Vector<Experiment> rawMatches = new Vector<Experiment>();
            if (dbConnection != null) {
                try {
                    Statement stmt = dbConnection.createStatement();
                    ResultSet rs = stmt.executeQuery(statement);
                    while (rs.next()) {
                        int dbId = rs.getInt("opm_dbid_graph");
                        String name = rs.getString("opm_id");
                        String user = rs.getString("username");
                        String status = rs.getString("status");
                        rawMatches.add(new Experiment(dbId, name, user, status));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Exception: connection null!!!");
            }
            ListIterator<Experiment> iterator = rawMatches.listIterator();
            try {
                while (iterator.hasNext() && !isCancelled()) {
                    Experiment anExperiment = iterator.next();
                    if (!containsSimilar(anExperiment)) {
                        Experiment anotherExperiment = null;
                        if (iterator.hasNext()) {
                            anotherExperiment = iterator.next();
                        }
                        while (iterator.hasNext() && anExperiment.dbId == anotherExperiment.dbId && anExperiment.name.equalsIgnoreCase(anotherExperiment.name)) {
                            anExperiment = anotherExperiment;
                            anotherExperiment = iterator.next();
                        }
                        if (iterator.hasNext()) {
                            inventory.add(anExperiment);
                        } else {
                            if (!inventory.experiments.contains(anExperiment)) {
                                inventory.add(anExperiment);
                            }
                            if (anotherExperiment != null) {
                                inventory.add(anotherExperiment);
                            }
                        }
                        firePropertyChange("progress", new Integer(progress), new Integer(100 * inventory.experiments.size() / itemCount));
                        Thread.sleep(5);
                        progress = 100 * inventory.experiments.size() / itemCount;
                    }
                }
            } catch (InterruptedException ignore) {
            }
            setDurations();
            return null;
        }
    }
    Inventory inventory = null;
    Connection dbConnection = null;
    HashMap<Integer, String> graphStatus = new HashMap<Integer, String>();

    public JDBCFinder(final String dbHost, final String dbUser, final String dbPwd) {
        dbConnection = getConnection(dbHost, dbUser, dbPwd);
    }

    Connection getConnection(final String dbHost, final String dbUser, final String dbPwd) {
        Connection con = null;
        try {
            con = DriverManager.getConnection(dbHost, dbUser, dbPwd);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return con;
    }

    void executeStatement(final String statement) {
        if (dbConnection != null) {
            try {
                Statement stmt = dbConnection.createStatement();
                ResultSet rs = stmt.executeQuery(statement);
                while (rs.next()) {
                    int graph = rs.getInt("OPM_Graph");
                    String opmId = rs.getString("opm_id");
                    System.out.println("Graph=" + graph + " OPM ID=" + opmId);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Exception: connection null!!!");
        }
    }

    ResultSet runQuery(final String query) {
        ResultSet rs = null;
        if (dbConnection != null) {
            try {
                Statement stmt = dbConnection.createStatement();
                rs = stmt.executeQuery(query);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return rs;
    }

    Vector<GraphSummary> buildGraphFromQuery(final String statement) {
        Vector<GraphSummary> matches = new Vector<GraphSummary>();
        if (dbConnection != null) {
            try {
                Statement stmt = dbConnection.createStatement();
                ResultSet rs = stmt.executeQuery(statement);
                while (rs.next()) {
                    GraphSummary aMatch = new GraphSummary();
                    int mKey = rs.getInt("opm_dbid_graph");
                    aMatch.setDbId(mKey);
                    aMatch.setId(rs.getString("opm_id"));
                    aMatch.setStatus(rs.getString("status"));
                    aMatch.setDuration(inventory.getExperimentDuration(mKey));
                    matches.add(aMatch);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Exception: connection null!!!");
        }
        return matches;
    }

    void makeSnapshot() {
        runQuery("create view " + SNAPSHOT_NAME + " as (select pe.opm_dbid_graph OPM_Graph, og.opm_id id, og.opm_description, min(event_timestamp) start, max(event_timestamp) end, (timediff(max(event_timestamp),min(event_timestamp))) duration, count(distinct pe.plier_id) events, count(distinct op.opm_dbid_process) processes " + "from PLIER_EVENT pe, OPM_GRAPH og, OPM_PROCESS op" + "where og.opm_dbid_graph=pe.opm_dbid_graph and og.opm_dbid_graph=op.opm_dbid_graph and pe.opm_dbid_graph=op.opm_dbid_graph " + "group by pe.opm_dbid_graph, op.opm_dbid_graph");
    }

    public SwingWorker<Void, Void> init() {
        return new InitializationTask();
    }

    public Vector<GraphSummary> searchAll() {
        Vector<GraphSummary> matches = new Vector<GraphSummary>();
        for (Experiment e : inventory.experiments) {
            GraphSummary aMatch = new GraphSummary();
            aMatch.setDbId(e.dbId);
            aMatch.setId(e.name);
            aMatch.setStatus(e.status);
            aMatch.setDuration(e.duration);
            matches.add(aMatch);
        }
        return matches;
    }

    public Vector<GraphSummary> searchName(final String name) {
        String fname = name.replaceAll("%", "");
        String statement = "select g.opm_dbid_graph, opm_id, event_description status " +
                "from OPM_GRAPH g, PLIER_EVENT e " +
                "where opm_id like '%" + fname + "%' and g.opm_dbid_graph=e.opm_dbid_graph and event_source_name='Moteur/Diane' and event_description='" + inventory.getStatusByName(fname) + "'";

        return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchUser(final String user) {
        System.out.println("jf.su ...");
//        String statement = "select distinct g.opm_id, g.opm_dbid_graph, event_description status " +
//                "from OPM_GRAPH g, OPM_AGENT a, PLIER_EVENT e " +
//                "where g.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and substring_index(substring_index(opm_value, '/CN=', -2), '/CN=', 1) like '%" + user + "%' and g.opm_dbid_graph=e.opm_dbid_graph and event_source_name='Moteur/Diane' and event_description='" + inventory.getStatusByUser(user) + "'";
        String statement = "select distinct g.opm_id, g.opm_dbid_graph, event_description status " +
                "from OPM_GRAPH g, OPM_AGENT a, PLIER_EVENT e " +
                "where g.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and substring_index(substring_index(opm_value, '/CN=', -2), '/CN=', 1) like '%" + user + "%' and g.opm_dbid_graph=e.opm_dbid_graph and event_source_name='Moteur/Diane' and event_description='" + inventory.getStatusByUser(user) + "'" +
                "union " +
                "select distinct g.opm_id, g.opm_dbid_graph, '' " +
                "from OPM_GRAPH g, OPM_AGENT a " +
                "where g.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and substring_index(substring_index(opm_value, '/CN=', -2), '/CN=', 1) like '%" + user + "%' and g.opm_dbid_graph not in (select distinct opm_dbid_graph from PLIER_EVENT)";
        Vector<GraphSummary> result = buildGraphFromQuery(statement);
        System.out.println("jf.su result size = " + result.size());
        return result;
    }

    String getTimeStatement(final String timestamp, final String validStatus) {
        SimpleTimeZone stz = new SimpleTimeZone(3600000,
                "Europe/Amsterdam",
                Calendar.MARCH, -1, Calendar.SUNDAY,
                3600000, SimpleTimeZone.UTC_TIME,
                Calendar.OCTOBER, -1, Calendar.SUNDAY,
                3600000, SimpleTimeZone.UTC_TIME,
                3600000);

        Calendar calendar = new GregorianCalendar(stz);
        String statement = "";
        if (timestamp.equalsIgnoreCase("Last hour")) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph and event_source_name='Moteur/Diane' and event_description in " + validStatus + " " +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  timediff(now(),min(event_timestamp)) <= '01:00:00' " +
                    "order by og.opm_dbid_graph, status desc";
        } else if (timestamp.equalsIgnoreCase("Last day")) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph and event_source_name='Moteur/Diane' and event_description in " + validStatus + " " +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  timediff(now(),min(event_timestamp)) <= '24:00:00' " +
                    "order by og.opm_dbid_graph, status desc";
        } else if (timestamp.equalsIgnoreCase("Last week")) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph and event_source_name='Moteur/Diane' and event_description in " + validStatus + " " +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  timediff(now(),min(event_timestamp)) <= '" + calendar.get(GregorianCalendar.DAY_OF_WEEK - 1) * 24 + ":00:00' " +
                    "order by og.opm_dbid_graph, status desc";
        } else if (timestamp.equalsIgnoreCase("Last month")) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph and event_source_name='Moteur/Diane' and event_description in " + validStatus + " " +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  timediff(now(),min(event_timestamp)) <= '" + calendar.get(GregorianCalendar.DAY_OF_MONTH) * 24 + ":00:00' " +
                    "order by og.opm_dbid_graph, status desc";
        } else if (timestamp.equalsIgnoreCase("Last year")) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph and event_source_name='Moteur/Diane' and event_description in " + validStatus + " " +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  datediff(now(), min(event_timestamp)) * 24 + extract(hour from now()) - extract(hour from min(event_timestamp)) <= '" + calendar.get(GregorianCalendar.DAY_OF_YEAR) * 24 + ":00:00' " +
                    "order by og.opm_dbid_graph, status desc";
        } else if (!timestamp.isEmpty()) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph  and event_source_name='Moteur/Diane' and event_description in " + validStatus + " " +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  datediff('" + timestamp + "', min(event_timestamp)) = 0 " +
                    "order by og.opm_dbid_graph, status desc";
        }
        return statement;
    }

    public Vector<GraphSummary> searchTimestamp(final String timestamp) {
        Vector<String> validStatus = inventory.validStatus();
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        for (String status : validStatus) {
            buffer.append("'" + status + "',");
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        buffer.append(")");
        return buildGraphFromQuery(getTimeStatement(timestamp, buffer.toString()));
    }

    String getTimeStatement(final String timestamp) {
        SimpleTimeZone stz = new SimpleTimeZone(3600000,
                "Europe/Amsterdam",
                Calendar.MARCH, -1, Calendar.SUNDAY,
                3600000, SimpleTimeZone.UTC_TIME,
                Calendar.OCTOBER, -1, Calendar.SUNDAY,
                3600000, SimpleTimeZone.UTC_TIME,
                3600000);

        Calendar calendar = new GregorianCalendar(stz);
        String statement = "";
        if (timestamp.equalsIgnoreCase("Last hour")) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph " +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  timediff(now(),min(event_timestamp)) <= '01:00:00' " +
                    "order by og.opm_dbid_graph, status desc";
        } else if (timestamp.equalsIgnoreCase("Last day")) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph " +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  timediff(now(),min(event_timestamp)) <= '24:00:00' " +
                    "order by og.opm_dbid_graph, status desc";
        } else if (timestamp.equalsIgnoreCase("Last week")) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph " +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  timediff(now(),min(event_timestamp)) <= '" + calendar.get(GregorianCalendar.DAY_OF_WEEK - 1) * 24 + ":00:00' " +
                    "order by og.opm_dbid_graph, status desc";
        } else if (timestamp.equalsIgnoreCase("Last month")) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph " +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  timediff(now(),min(event_timestamp)) <= '" + calendar.get(GregorianCalendar.DAY_OF_MONTH) * 24 + ":00:00' " +
                    "order by og.opm_dbid_graph, status desc";
        } else if (timestamp.equalsIgnoreCase("Last year")) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph  and event_source_name='Moteur/Diane'" +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  datediff(now(), min(event_timestamp)) * 24 + extract(hour from now()) - extract(hour from min(event_timestamp)) <= '" + calendar.get(GregorianCalendar.DAY_OF_YEAR) * 24 + ":00:00' " +
                    "order by og.opm_dbid_graph, status desc";
        } else if (!timestamp.isEmpty()) {
            statement = "select og.opm_id, og.opm_dbid_graph, event_description status " +
                    "from PLIER_EVENT pe, OPM_GRAPH og " +
                    "where og.opm_dbid_graph=pe.opm_dbid_graph  and event_source_name='Moteur/Diane'" +
                    "group by pe.opm_dbid_graph, pe.event_description " +
                    "having  datediff('" + timestamp + "', min(event_timestamp)) = 0 " +
                    "order by og.opm_dbid_graph, status desc";
        }
        return statement;
    }

    public Vector<GraphSummary> searchStatus(final String status) {
        if (inventory.statusExists(status)) {
            String statement = "select distinct g.opm_id, g.opm_dbid_graph, event_description status " +
                    "from OPM_GRAPH g, PLIER_EVENT e " +
                    "where g.OPM_DBID_GRAPH=e.OPM_DBID_GRAPH and e.event_source_name='Moteur/Diane' and e.event_description='" + status + "'";
            return buildGraphFromQuery(statement);
        } else {
            return new Vector<GraphSummary>();
        }
    }

    public Vector<GraphSummary> searchNameUser(final String name, final String user) {
        String wname = name.replaceAll("%", "");
        String statement = "select g.opm_id, g.opm_dbid_graph, event_description status " +
                "from OPM_GRAPH g, OPM_AGENT a, PLIER_EVENT e " +
                "where g.opm_id like '%" + wname + "%' and g.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and substring(a.opm_value,instr(a.opm_value,\"/CN=\")+4, 40) like '%" + user + "%' and g.OPM_DBID_GRAPH=e.OPM_DBID_GRAPH and e.event_source_name='Moteur/Diane' and event_description='" + inventory.getStatusByUser(user) + "'";
        return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchNameTime(final String name, final String timestamp) {
        String fname = name.replaceAll("%", "");
        String statement = getTimeStatement(timestamp);
        String nameClause = "og.opm_id like '%" + fname + "%' and event_description='" + inventory.getStatusByName(fname) + "'";
        statement = statement.replaceFirst("where", "where " + nameClause + " and ");
        return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchNameStatus(final String name, final String status) {
        if (inventory.statusExists(status)) {
            String statement = "select distinct g.opm_id, g.opm_dbid_graph, event_description status " +
                    "from OPM_GRAPH g, PLIER_EVENT e " +
                    "where g.opm_id like '%" + name + "%' and g.OPM_DBID_GRAPH=e.OPM_DBID_GRAPH and e.event_source_name='Moteur/Diane' and e.event_description='" + status + "'";
            return buildGraphFromQuery(statement);
        } else {
            return new Vector<GraphSummary>();
        }
    }

    public Vector<GraphSummary> searchUserTime(final String user, final String timestamp) {
        String statement = getTimeStatement(timestamp);
        String userClause = "og.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and substring(a.opm_value,instr(a.opm_value,\"/CN=\")+4, 40) like '%" + user + "%' and event_description='" + inventory.getStatusByUser(user) + "'";
        statement = statement.replaceFirst("from", "from OPM_AGENT a, ");
        statement = statement.replaceFirst("where", "where " + userClause + " and ");
        return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchUserStatus(final String user, final String status) {
        if (inventory.statusExists(status)) {
            String statement = "select distinct g.opm_id, g.opm_dbid_graph, event_description status " +
                    "from OPM_AGENT a, OPM_GRAPH g, PLIER_EVENT e " +
                    "where g.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and substring(a.opm_value,instr(a.opm_value,\"/CN=\")+4, 40) like '%" + user + "%' and g.OPM_DBID_GRAPH=e.OPM_DBID_GRAPH and e.event_source_name='Moteur/Diane' and e.event_description='" + status + "'";
            return buildGraphFromQuery(statement);
        } else {
            return new Vector<GraphSummary>();
        }
    }

    public Vector<GraphSummary> searchTimeStatus(final String timestamp, final String status) {
        if (inventory.statusExists(status)) {
            String statement = getTimeStatement(timestamp);
            String statusClause = "pe.event_source_name='Moteur/Diane' and pe.event_description='" + status + "'";
            statement = statement.replaceFirst("where", "where " + statusClause + " and ");
            return buildGraphFromQuery(statement);
        } else {
            return new Vector<GraphSummary>();
        }
    }

    public Vector<GraphSummary> searchNameUserTime(final String name, final String user, final String timestamp) {
        String statement = getTimeStatement(timestamp);
        String fname = name.replaceAll("%", "");
        String userClause = "og.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and substring(a.opm_value,instr(a.opm_value,\"/CN=\")+4, 40) like '%" + user + "%'";
        String nameClause = "og.opm_id like '%" + fname + "%' and event_description='" + inventory.getStatusByName(fname) + "'";
        statement = statement.replaceFirst("from", "from OPM_AGENT a, ");
        statement = statement.replaceFirst("where", "where " + userClause + " and ");
        statement = statement.replaceFirst("where", "where " + nameClause + " and ");
        return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchNameUserStatus(final String name, final String user, final String status) {
        if (inventory.statusExists(status)) {
            String statement = "select distinct g.opm_id, g.opm_dbid_graph, event_description status " +
                    "from OPM_AGENT a, OPM_GRAPH g, PLIER_EVENT e " +
                    "where g.opm_id like '%" + name + "%' and g.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and substring(a.opm_value,instr(a.opm_value,\"/CN=\")+4, 40) like '%" + user + "%' and g.OPM_DBID_GRAPH=e.OPM_DBID_GRAPH and e.event_source_name='Moteur/Diane' and e.event_description='" + status + "'";
            return buildGraphFromQuery(statement);
        } else {
            return new Vector<GraphSummary>();
        }
    }

    public Vector<GraphSummary> searchNameTimeStatus(final String name, final String timestamp, final String status) {
        if (inventory.statusExists(status)) {
            String statement = getTimeStatement(timestamp);
            String nameClause = "og.opm_id like '%" + name + "%'";
            String statusClause = "pe.event_source_name='Moteur/Diane' and pe.event_description='" + status + "'";
            statement = statement.replaceFirst("where", "where " + statusClause + " and ");
            statement = statement.replaceFirst("where", "where " + nameClause + " and ");
            return buildGraphFromQuery(statement);
        } else {
            return new Vector<GraphSummary>();
        }
    }

    public Vector<GraphSummary> searchUserTimeStatus(final String user, final String timestamp, final String status) {
        if (inventory.statusExists(status)) {
            String statement = getTimeStatement(timestamp);
            String userClause = "og.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and substring(a.opm_value,instr(a.opm_value,\"/CN=\")+4, 40) like '%" + user + "%'";
            String statusClause = "pe.event_source_name='Moteur/Diane' and pe.event_description='" + status + "'";
            statement = statement.replaceFirst("from", "from OPM_AGENT a, ");
            statement = statement.replaceFirst("where", "where " + statusClause + " and ");
            statement = statement.replaceFirst("where", "where " + userClause + " and ");
            return buildGraphFromQuery(statement);
        } else {
            return new Vector<GraphSummary>();
        }
    }

    public Vector<GraphSummary> searchNameUserTimeStatus(final String name, final String user, final String timestamp, final String status) {
        if (inventory.statusExists(status)) {
            String statement = getTimeStatement(timestamp);
            String nameClause = "og.opm_id like '%" + name + "%'";
            String userClause = "og.opm_dbid_graph=a.opm_dbid_graph and a.opm_id like '%User%' and substring(a.opm_value,instr(a.opm_value,\"/CN=\")+4, 40) like '%" + user + "%'";
            String statusClause = "pe.event_source_name='Moteur/Diane' and pe.event_description='" + status + "'";
            statement = statement.replaceFirst("from", "from OPM_AGENT a, ");
            statement = statement.replaceFirst("where", "where " + statusClause + " and ");
            statement = statement.replaceFirst("where", "where " + userClause + " and ");
            statement = statement.replaceFirst("where", "where " + nameClause + " and ");
            return buildGraphFromQuery(statement);
        } else {
            return new Vector<GraphSummary>();
        }
    }

    public Vector<String> getUserList() {
        Vector<String> users = new Vector<String>();
        ResultSet rs = runQuery("select distinct substring_index(substring_index(opm_value, '/CN=', -2), '/CN=', 1) User " +
                "from OPM_AGENT " +
                "where opm_id like '%User%'");
        try {
            while (rs.next()) {
                users.add(rs.getString("User"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return users;
    }

    public Vector<String> getStatusList() {
        Vector<String> vStatus = new Vector<String>();
        ResultSet rs = runQuery("select distinct event_description " +
                "from PLIER_EVENT " +
                "where event_source_name='Moteur/Diane'");
        try {
            while (rs.next()) {
                vStatus.add(rs.getString("event_description"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return vStatus;
    }

    public GraphSummary getDetail(final String graphId) {
        GraphSummary aMatch = new GraphSummary();
        aMatch.setId(graphId);
        ResultSet rs = runQuery("select og.opm_dbid_graph, og.opm_description, min(event_timestamp) start, max(event_timestamp) end, concat(timediff(max(event_timestamp), min(event_timestamp)), '') duration, count(distinct pe.plier_id) events, count(distinct op.opm_dbid_process) processes, count(distinct a.opm_dbid_artifact) parameters " + " from PLIER_EVENT pe, OPM_GRAPH og, OPM_PROCESS op, OPM_ARTIFACT a where og.opm_dbid_graph=pe.opm_dbid_graph and og.opm_dbid_graph=a.opm_dbid_graph and og.opm_dbid_graph=op.opm_dbid_graph and pe.opm_dbid_graph=op.opm_dbid_graph and og.opm_id='" + graphId + "'");
        try {
            while (rs.next()) {
                aMatch.setDbId(rs.getInt("og.opm_dbid_graph"));
                aMatch.setStart(rs.getTimestamp("start"));
                aMatch.setEnd(rs.getTimestamp("end"));
                aMatch.setDuration(rs.getString("duration"));
                aMatch.setNbEvents(rs.getInt("events"));
                if (aMatch.getNbEvents() == 0) {
                    aMatch.setNbProcs(getGraphProcesses(aMatch.getDbId()).size());
                    aMatch.setNbParams(getGraphParameters(aMatch.getDbId()).size());
                } else {
                    aMatch.setNbProcs(rs.getInt("processes"));
                    aMatch.setNbParams(rs.getInt("parameters"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return aMatch;
    }

    public GraphSummary getDetail(final long graphId) {
        GraphSummary aMatch = new GraphSummary();
        return aMatch;
    }

    public Vector<Event> getGraphEvents(final long gid) {
        Vector<Event> matches = new Vector<Event>();
//        ResultSet rs = runQuery("select event_description, opm_value, event_timestamp " +
//                "from OPM_PROCESS p, PLIER_EVENT e " +
//                "where p.OPM_DBID_GRAPH=e.OPM_DBID_GRAPH and p.OPM_DBID_GRAPH=" + gid + " and e.event_source_name='Moteur/Diane' and event_identification like '%STATUS%'");
//        try {
//            while (rs.next()) {
//                Event aMatch = new Event();
//                aMatch.setDescription(rs.getString("event_description"));
//                aMatch.setSource(rs.getString("opm_value"));
//                aMatch.setTimestamp(rs.getTimestamp("event_timestamp"));
//                matches.add(aMatch);
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
        return matches;
    }

    public Vector<Process> getGraphProcesses(final long gid) {
        Vector<Process> matches = new Vector<Process>();
        ResultSet rs = runQuery("select opm_id, opm_value from OPM_PROCESS where opm_dbid_graph=" + gid);
        try {
            while (rs.next()) {
                Process aMatch = new Process();
                aMatch.setId(rs.getString("opm_id"));
                //aMatch.setValue(rs.getString("opm_value"));
                matches.add(aMatch);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return matches;
    }

    public Vector<Artifact> getGraphParameters(final long gid) {
        Vector<Artifact> matches = new Vector<Artifact>();
        ResultSet rs = runQuery("select opm_id, opm_value from OPM_ARTIFACT where opm_dbid_graph=" + gid);
        try {
            while (rs.next()) {
                Artifact aMatch = new Artifact();
                aMatch.setId(rs.getString("opm_id"));
                aMatch.setValue(rs.getString("opm_value"));
                matches.add(aMatch);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return matches;
    }

    public Vector<Agent> getGraphAgents(final long gid) {
        Vector<Agent> matches = new Vector<Agent>();
        ResultSet rs = runQuery("select opm_id, opm_value from OPM_PROCESS where opm_dbid_graph=" + gid);
        try {
            while (rs.next()) {
                Agent aMatch = new Agent();
                aMatch.setId(rs.getString("opm_id"));
                //aMatch.setValue(rs.getString("opm_value"));
                matches.add(aMatch);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return matches;
    }

    public Vector<Artifact> getGraphProcessIns(final long gid, final long pid) {
        Vector<Artifact> matches = new Vector<Artifact>();
        ResultSet rs = runQuery("select a.OPM_ID, a.OPM_VALUE " +
                "from OPM_ARTIFACT a, OPM_CAUSALDEPENDENCY cd, OPM_USED u " +
                "where a.OPM_DBID_GRAPH=" + gid + " and cd.OPM_DBID_GRAPH=a.OPM_DBID_GRAPH and cd.OPM_DBID_USED=u.OPM_DBID_USED and u.OPM_EFFECT like '" + pid + "' and a.OPM_ID=u.OPM_CAUSE");
        try {
            while (rs.next()) {
                Artifact aMatch = new Artifact();
                aMatch.setId(rs.getString("a.opm_id"));
                aMatch.setValue(rs.getString("a.opm_value"));
                matches.add(aMatch);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return matches;
    }

    public Vector<Artifact> getGraphProcessOuts(final long gid, final long pid) {
        Vector<Artifact> matches = new Vector<Artifact>();
        ResultSet rs = runQuery("select a.OPM_ID, a.OPM_VALUE " +
                "from OPM_ARTIFACT a, OPM_CAUSALDEPENDENCY cd, OPM_WASGENERATEDBY wgb " +
                "where a.OPM_DBID_GRAPH=" + gid + " and cd.OPM_DBID_GRAPH=a.OPM_DBID_GRAPH and cd.OPM_DBID_WASGENERATEDBY=wgb.OPM_DBID_WASGENERATEDBY and wgb.OPM_CAUSE like '" + pid + "' and a.OPM_ID=wgb.OPM_EFFECT");
        try {
            while (rs.next()) {
                Artifact aMatch = new Artifact();
                aMatch.setId(rs.getString("a.opm_id"));
                aMatch.setValue(rs.getString("a.opm_value"));
                matches.add(aMatch);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return matches;
    }

    public Vector<Agent> getGraphProcessControllers(final long gid, final long pid) {
        Vector<Agent> matches = new Vector<Agent>();
        ResultSet rs = runQuery("select distinct a.OPM_ID, a.OPM_VALUE " +
                "from OPM_AGENT a, OPM_CAUSALDEPENDENCY cd, OPM_WASCONTROLLEDBY wcb " +
                "where a.OPM_DBID_GRAPH=" + gid + " and cd.OPM_DBID_GRAPH=a.OPM_DBID_GRAPH " +
                "and cd.OPM_DBID_WASCONTROLLEDBY=wcb.OPM_DBID_WASCONTROLLEDBY and " +
                "wcb.OPM_EFFECT like '" + pid + "' and a.OPM_ID=wcb.OPM_CAUSE and (a.OPM_ID like 'filename%'" +
                "or a.OPM_ID like 'host%')");
        try {
            while (rs.next()) {
                Agent aMatch = new Agent();
                aMatch.setId(rs.getString("a.opm_id"));
                //aMatch.setValue(rs.getString("a.opm_value"));
                matches.add(aMatch);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return matches;
    }

    public Vector<Process> getGraphArtifactGenerators(final long gid, final long aid) {
        Vector<Process> matches = new Vector<Process>();
        ResultSet rs = runQuery("select p.opm_id, p.OPM_VALUE " +
                "from OPM_PROCESS p, OPM_CAUSALDEPENDENCY cd, OPM_WASGENERATEDBY wgb " +
                "where p.OPM_DBID_GRAPH=" + gid + " and p.OPM_ID=wgb.OPM_CAUSE and cd.OPM_DBID_GRAPH=p.OPM_DBID_GRAPH and wgb.OPM_EFFECT='" + aid + "' and cd.OPM_DBID_WASgeneratEDBY=wgb.OPM_DBID_WASgeneratEDBY");
        try {
            while (rs.next()) {
                Process aMatch = new Process();
                aMatch.setId(rs.getString("p.opm_id"));
                //aMatch.setValue(rs.getString("p.opm_value"));
                matches.add(aMatch);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return matches;
    }

//    public OPMGraph getExperiment(final String opmGId) {
//        return null;
//    }

    public OPMGraph getExperiment(final long gid) {
        return null;
    }
    
//    public LinkedHashSet<Event> getSortedGraphEvents(final int gid){
//        return new LinkedHashSet<Event>();
//    }
    public LinkedHashSet<Artifact> getSortedGraphProcessIns(final long gid, final long pid){
        return new LinkedHashSet<Artifact>();
    }

}
