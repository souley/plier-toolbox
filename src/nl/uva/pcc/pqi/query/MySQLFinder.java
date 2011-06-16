/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.pcc.pqi.query;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.SimpleTimeZone;
import java.util.Vector;
import javax.swing.SwingWorker;
import nl.biggrid.plier.opm.Agent;
import nl.biggrid.plier.opm.Artifact;
import nl.biggrid.plier.opm.OPMGraph;
import nl.biggrid.plier.tools.PersistenceManager;
import nl.uva.pcc.pqi.ui.DBSettings;
import nl.uva.pcc.pqi.util.StringEncrypter;
import nl.uva.pcc.pqi.util.StringEncrypter.EncryptionException;

/**
 *
 * @author Souley
 */
public class MySQLFinder implements IFinder {

    //static final String DN_SQL = "javax.jdo.query.SQL";
    static final String TWO_DIGIT_FORMAT = "%02d";
    static final int SECONDS_IN_HOUR = 3600;
    static final int SECONDS_IN_MINUTE = 60;

    private final String HIBERNATE_SETTINGS_FILE = "hibernate.cfg.xml";
    class Experiment {

        int dbId = 0;
        String name = "";
        String user = "";
        String status = "";
        String duration = "";
        Timestamp debut = null;

        public Experiment(final int anId, final String aName, final String aStatus, final String aDuration) {
            dbId = anId;
            name = aName;
            duration = aDuration;
            status = aStatus;
        }

        public Experiment(final int anId, final String aName, final String aUser, final String aStatus, final String aDuration) {
            this(anId, aName, aStatus, aDuration);
            user = aUser;
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

        public void setExperimentUser(final int expKey, final String user) {
            for (Experiment e : experiments) {
                if (e.dbId == expKey) {
                    e.user = user;
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

        String format(final long number) {
            StringBuilder formattedNumber = new StringBuilder();
            Formatter formatter = new Formatter(formattedNumber);
            formatter.format(TWO_DIGIT_FORMAT, number);
            return formattedNumber.toString();
        }

        int getItemCount() {
            System.out.println("###MSF::gic ... ###");
            List results = pm.executeSQL("select count(*) from OPMGraph");
            System.out.println("###MSF::gic items="+results.size());
            System.out.println("###MSF::gic results[0] type -> "+results.get(0).getClass().getName());
            //Iterator<Object[]> iterator = results.iterator();
            //if (iterator.hasNext()) {
                //Object[] items = iterator.next();
                Object item = results.get(0);
//                System.out.println("###MSF::gic ....");
//                System.out.println("###MSF::gic items[0].length="+items.length);
//                System.out.println("###MSF::gic items[0] type -> "+items[0].getClass().getName());
                //System.out.println("###MSF::gic items[0].length="+items.length);
                return (((BigInteger) item).intValue());
            //}
//            for (Object[] items : results) {
//                System.out.println("###MSF::gic items[0] type -> "+items[0].getClass().getName());
//                return (((Long) items[0]).intValue());
//            }
            //return 0;
        }

        @Override
        public Void doInBackground() {
            int progress = 0;
            String statement = "select g.GraphKey, g.GraphId, g.GraphStatus, g.GraphUser, timestampdiff(SECOND, g.StartsAt, g.EndsAt), g.StartsAt " +
                                "from OPMGraph as g";
            setProgress(0);
            try {
                Thread.sleep(400);
            } catch (InterruptedException ignore) {
            }
            firePropertyChange("progress", new Integer(progress), new Integer(1));
            int itemCount = getItemCount();
            //int itemCount = pm.count("OPMGraph");
            System.out.println("###MSF::gic items: "+itemCount);
            inventory = new Inventory();
            Vector<Experiment> rawMatches = new Vector<Experiment>();
//            Query query = pm.newQuery(DN_SQL, statement);
//            List<Object[]> results = (List<Object[]>) query.execute();
            List results = pm.executeSQL(statement);
            System.out.println("###MSF::gic results -> "+results.size());
            //for (Object[] items : results) {
            for (int i=0; i<results.size(); i++) {
                Object[] items = (Object[])results.get(i);
                //System.out.println("###MSF::gic items["+i+"] -> "+items.length);
                if (items != null) {
                    //System.out.println("###MSF::gic (items[0] != null) -> "+(items[0] != null));
                    int gid = ((BigInteger) items[0]).intValue();
                    //System.out.println("###MSF::gic (items[1] != null) -> "+(items[1] != null));
                    String name = (items[1] != null) ? items[1].toString() : "";
                    String status = (items[2] != null) ? items[2].toString() : "";
                    String user = (items[3] != null) ? items[3].toString() : "";
                    String duration = "";
                    //System.out.println("###MSF::gic (items[4] != null) -> "+(items[4] != null));
                    long durationInSecs = (items[4] != null) ? ((BigInteger) items[4]).longValue() : 0;
                    long hours = durationInSecs / SECONDS_IN_HOUR;
                    long minutes = (durationInSecs % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE;
                    long secs = durationInSecs % SECONDS_IN_MINUTE;
                    duration = format(hours) + ":" + format(minutes) + ":" + format(secs);
                    Experiment e = new Experiment(gid, name, user, status, duration);
                    //System.out.println("###MSF::gic (items[5] != null) -> "+(items[5] != null));
                    Timestamp debut = (items[5] != null) ? Timestamp.valueOf(items[5].toString()) : null;
                    e.debut = debut;
                    rawMatches.add(e);
               }
            }
            System.out.println("###MSF::gic raw matches: "+rawMatches.size());
            try {
                for (Experiment exp : rawMatches) {
                    if (exp.status.isEmpty()) {
                        exp.status = QueryManager.STATUS_UNKNOWN;
                    }
                    inventory.add(exp);
                    firePropertyChange("progress", new Integer(progress), new Integer(100 * inventory.experiments.size() / itemCount));
                    Thread.sleep(5);
                    progress = 100 * inventory.experiments.size() / itemCount;
                }
            } catch (InterruptedException ignore) {
            }
            return null;
        }
    }
    private PersistenceManager pm = null;
    Inventory inventory = null;

    public MySQLFinder(final Properties dbSettings) {
        if ("on".equalsIgnoreCase(dbSettings.getProperty("encryption"))) {
            StringEncrypter crypto = null;
            try {
                crypto = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME, "123456789012345678901234567890");
                String encodedUser = dbSettings.getProperty(DBSettings.DB_USER);
                String encodedPWD = dbSettings.getProperty(DBSettings.DB_PWD);
                dbSettings.put(DBSettings.DB_USER, crypto.decrypt(encodedUser));
                dbSettings.put(DBSettings.DB_PWD, crypto.decrypt(encodedPWD));
            } catch (EncryptionException ex) {
                ex.printStackTrace();
            }
        }
        //PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(jdoSettings);
        //pm = pmf.getPersistenceManager();
        pm = new PersistenceManager();
        pm.init(HIBERNATE_SETTINGS_FILE, dbSettings);
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

    Vector<GraphSummary> buildGraphFromQuery(final String statement) {
        Vector<GraphSummary> matches = new Vector<GraphSummary>();
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
        List<Object[]> results = pm.executeSQL(statement);
        Iterator iterator = results.iterator();
        while (iterator.hasNext()) {
            Object[] items = (Object[]) iterator.next();
            Long gid = (Long) items[1];
            GraphSummary aMatch = new GraphSummary();
            int mKey = gid.intValue();
            aMatch.setDbId(mKey);
            aMatch.setId(items[0].toString());
            String finalStatus = inventory.getStatusByName(aMatch.getId());
            if (items.length > 2) {
                if (!finalStatus.equalsIgnoreCase(items[2].toString())) {
                    continue;
                }
            }
            aMatch.setStatus(finalStatus);
            aMatch.setDuration(inventory.getExperimentDuration(mKey));
            matches.add(aMatch);
        }
        return matches;
    }

    public Vector<GraphSummary> searchName(final String name) {
        String fname = name.replaceAll("%", "");
        String statement = "select GraphId, GraphKey " +
                "from OPMGraph " +
                "where GraphId like '%" + fname + "%'";
        return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchUser(final String user) {
        Vector<GraphSummary> res = new Vector<GraphSummary>();
            String statement = "select GraphId, GraphKey " +
                            "from OPMGraph " +
                            "where GraphUser like '%" + user + "%'";
            res = buildGraphFromQuery(statement);
        return res;
    }

    public Vector<GraphSummary> searchStatus(final String status) {
        Vector<GraphSummary> res = new Vector<GraphSummary>();
        String statement = "select distinct GraphId, GraphKey, GraphStatus " +
                            "from OPMGraph " +
                            "where GraphStatus='" + status + "'";
        res = buildGraphFromQuery(statement);
        return res;
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
        return buildGraphFromQuery(getTimeStatement(timestamp));
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
            statement = "select GraphId, g.GraphKey, GraphStatus " +
                        "from OPMGraph g " +
                        "where timediff(now(), StartsAt) <= '01:00:00' ";
        } else if (timestamp.equalsIgnoreCase("Last day")) {
            statement = "select GraphId, g.GraphKey, GraphStatus " +
                        "from OPMGraph g " +
                        "where timediff(now(), StartsAt) <= '24:00:00' ";
        } else if (timestamp.equalsIgnoreCase("Last week")) {
            statement = "select GraphId, g.GraphKey, GraphStatus " +
                        "from OPMGraph g " +
                        "where  timediff(now(), StartsAt) <= '" + calendar.get(GregorianCalendar.DAY_OF_WEEK-1) * 24 + ":00:00'";
        } else if (timestamp.equalsIgnoreCase("Last month")) {
            statement = "select GraphId, g.GraphKey, GraphStatus " +
                        "from OPMGraph g " +
                        "where  timediff(now(), StartsAt) <= '" + calendar.get(GregorianCalendar.DAY_OF_MONTH) * 24 + ":00:00'";
        } else if (timestamp.equalsIgnoreCase("Last year")) {
            statement = "select GraphId, g.GraphKey, GraphStatus " +
                        "from OPMGraph g " +
                        "where  datediff(now(), StartsAt) * 24 + extract(hour from now()) - extract(hour from StartsAt) <= '" + calendar.get(GregorianCalendar.DAY_OF_YEAR) * 24 + ":00:00'";
        } else if (!timestamp.isEmpty()) {
            statement = "select GraphId, g.GraphKey, GraphStatus "+
                        "from OPMGraph g "+
                        "where datediff('" + timestamp + "', StartsAt) = 0";
        }
        return statement;
    }

    public Vector<GraphSummary> searchNameUser(final String name, final String user) {
        String wname = name.replaceAll("%", "");
        String statement = "select g.GraphId, g.GraphKey " +
                "from OPMGraph g " +
                "where g.GraphId like '%" + wname + "%' and g.GraphUser like '%" + user + "%'";
        return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchNameTime(final String name, final String timestamp) {
        String fname = name.replaceAll("%", "");
        String statement = getTimeStatement(timestamp);
        String nameClause = "g.GraphId like '%" + fname + "%'";
        statement = statement.replaceFirst("where", "where " + nameClause + " and ");
        return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchNameStatus(final String name, final String status) {
            String statement = "select distinct GraphId, GraphKey, GraphStatus " +
                    "from OPMGraph g " +
                    "where GraphId like '%" + name + "%' and GraphStatus='" + status + "'";
            return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchUserTime(final String user, final String timestamp) {
        String statement = getTimeStatement(timestamp);
        String userClause = "GraphUser like '%" + user + "%'";
        statement = statement.replaceFirst("where", "where " + userClause + " and ");
        return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchUserStatus(final String user, final String status) {
            String statement = "select distinct GraphId, GraphKey, GraphStatus " +
                    "from OPMGraph " +
                    "where GraphUser='" + user + "' and GraphStatus='" + status + "'";
            return buildGraphFromQuery(statement);
    }

//    private boolean inTimespan(final Timestamp estart, final String timestamp) {
//        if (timestamp.isEmpty()) {
//            return false;
//        }
//        SimpleTimeZone stz = new SimpleTimeZone(3600000,
//                "Europe/Amsterdam",
//                Calendar.MARCH, -1, Calendar.SUNDAY,
//                3600000, SimpleTimeZone.UTC_TIME,
//                Calendar.OCTOBER, -1, Calendar.SUNDAY,
//                3600000, SimpleTimeZone.UTC_TIME,
//                3600000);
//
//        Calendar now = new GregorianCalendar(stz);
//        Calendar start = new GregorianCalendar(stz);
//        start.setTime(estart);
//
//        if (timestamp.equalsIgnoreCase("Last hour")) {
//            return (now.get(GregorianCalendar.YEAR) == start.get(GregorianCalendar.YEAR) &&
//                    now.get(GregorianCalendar.MONTH) == start.get(GregorianCalendar.MONTH) &&
//                    now.get(GregorianCalendar.DAY_OF_MONTH) == start.get(GregorianCalendar.DAY_OF_MONTH) &&
//                    now.get(GregorianCalendar.HOUR_OF_DAY) - start.get(GregorianCalendar.HOUR_OF_DAY) <= 1);
//        } else if (timestamp.equalsIgnoreCase("Last day")) {
//            return (now.get(GregorianCalendar.YEAR) == start.get(GregorianCalendar.YEAR) &&
//                    now.get(GregorianCalendar.MONTH) == start.get(GregorianCalendar.MONTH) &&
//                    now.get(GregorianCalendar.DAY_OF_MONTH) - start.get(GregorianCalendar.DAY_OF_MONTH)  <= 1);
//        } else if (timestamp.equalsIgnoreCase("Last week")) {
//            return (now.get(GregorianCalendar.YEAR) == start.get(GregorianCalendar.YEAR) &&
//                    now.get(GregorianCalendar.MONTH) == start.get(GregorianCalendar.MONTH) &&
//                    now.get(GregorianCalendar.DAY_OF_MONTH) - start.get(GregorianCalendar.DAY_OF_MONTH)  <= 7);
//        } else if (timestamp.equalsIgnoreCase("Last month")) {
//            return (now.get(GregorianCalendar.YEAR) == start.get(GregorianCalendar.YEAR) &&
//                    now.get(GregorianCalendar.MONTH) - start.get(GregorianCalendar.MONTH) <= 1);
//        } else if (timestamp.equalsIgnoreCase("Last year")) {
//            return (now.get(GregorianCalendar.YEAR) - start.get(GregorianCalendar.YEAR) <= 1);
//        } else {
//            Date ts = Date.valueOf(timestamp);
//            if (ts == null) {
//                return false;
//            }
//            Calendar timeThen = new GregorianCalendar(stz);
//            timeThen.setTime(ts);
//            return (timeThen.get(GregorianCalendar.YEAR) == start.get(GregorianCalendar.YEAR) &&
//                    timeThen.get(GregorianCalendar.MONTH) == start.get(GregorianCalendar.MONTH) &&
//                    timeThen.get(GregorianCalendar.DAY_OF_MONTH) == start.get(GregorianCalendar.DAY_OF_MONTH));
//        }
//    }

    public Vector<GraphSummary> searchTimeStatus(final String timestamp, final String status) {
            String statement = getTimeStatement(timestamp);
            String statusClause = "GraphStatus='" + status + "'";
            statement = statement.replaceFirst("where", "where " + statusClause + " and ");
            return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchNameUserTime(final String name, final String user, final String timestamp) {
        String statement = getTimeStatement(timestamp);
        String fname = name.replaceAll("%", "");
        String userClause = "g.GraphUser='" + user + "'";
        String nameClause = "g.GraphId like '%" + fname + "%'";
        statement = statement.replaceFirst("where", "where " + userClause + " and ");
        statement = statement.replaceFirst("where", "where " + nameClause + " and ");
        return buildGraphFromQuery(statement);
    }


    public Vector<GraphSummary> searchNameUserStatus(final String name, final String user, final String status) {
            String statement = "select GraphId, GraphKey, GraphStatus " +
                    "from OPMGraph " +
                    "where GraphId like '%" + name + "%' and GraphUser='" + user + "' and GraphStatus='" + status + "'";
            return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchNameTimeStatus(final String name, final String timestamp, final String status) {
            String statement = getTimeStatement(timestamp);
            String nameClause = "g.GraphId like '%" + name + "%'";
            String statusClause = "GraphStatus='" + status + "'";
            statement = statement.replaceFirst("where", "where " + statusClause + " and ");
            statement = statement.replaceFirst("where", "where " + nameClause + " and ");
            return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchUserTimeStatus(final String user, final String timestamp, final String status) {
            String statement = getTimeStatement(timestamp);
            String userClause = "g.GraphUser='" + user + "'";
            String statusClause = "GraphStatus='" + status + "'";
            statement = statement.replaceFirst("where", "where " + statusClause + " and ");
            statement = statement.replaceFirst("where", "where " + userClause + " and ");
            return buildGraphFromQuery(statement);
    }

    public Vector<GraphSummary> searchNameUserTimeStatus(final String name, final String user, final String timestamp, final String status) {
            String statement = getTimeStatement(timestamp);
            String nameClause = "GraphId like '%" + name + "%'";
            String userClause = "GraphUser='" + user + "'";
            String statusClause = "GraphStatus='" + status + "'";
            statement = statement.replaceFirst("where", "where " + statusClause + " and ");
            statement = statement.replaceFirst("where", "where " + userClause + " and ");
            statement = statement.replaceFirst("where", "where " + nameClause + " and ");
            return buildGraphFromQuery(statement);
    }

    public Vector<String> getUserList() {
        Vector<String> users = new Vector<String>();
        String statement = "select distinct g.GraphUser from OPMGraph g";
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
        List results = pm.executeSQL(statement);
        System.out.println("###MSF::gic users: "+results.size());
        Iterator iterator = results.iterator();
        while (iterator.hasNext()) {
            //Object[] items = iterator.next();
            users.add(iterator.next().toString().trim());
        }
        return users;
    }

    public Vector<String> getStatusList() {
        Vector<String> vStatus = new Vector<String>();
        String statement = "select distinct GraphStatus from OPMGraph";
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
        List results = pm.executeSQL(statement);
        System.out.println("###MSF::gic statuses: "+results.size());
        Iterator iterator = results.iterator();
        while (iterator.hasNext()) {
            //Object[] items = iterator.next();
            vStatus.add(iterator.next().toString().trim());
        }
        return vStatus;
    }

    public GraphSummary getDetail(final String graphId) {
        GraphSummary aMatch = new GraphSummary();
        aMatch.setId(graphId);
//        String statement = "select og.GraphKey, og.StartsAt, og.EndsAt, count(distinct ann.AnnotationKey), count(distinct op.ProcessKey), count(distinct a.ArtifactKey) " +
//                " from OPMAnnotation an, OPMGraph og, OPMProcess op, OPMArtifact a " +
//                "where og.GraphKey=an.GraphKey and og.GraphKey=a.GraphKey and og.GraphKey=op.GraphKey and og.GraphId='" + graphId + "'";
        String statement = "select og.GraphKey, og.StartsAt, og.EndsAt, count(distinct op.ProcessKey), count(distinct a.ArtifactKey) " +
                " from OPMGraph og, OPMProcess op, OPMArtifact a " +
                "where og.GraphKey=a.GraphKey and og.GraphKey=op.GraphKey and og.GraphId='" + graphId + "'";
        List results = pm.executeSQL(statement);
        Iterator iterator = results.iterator();
        while (iterator.hasNext()) {
            Object[] items = (Object[]) iterator.next();
            aMatch.setDbId(((BigInteger) items[0]).intValue());
            aMatch.setStart((items[1] != null) ? Timestamp.valueOf(items[1].toString()) : null);
            aMatch.setEnd((items[2] != null) ? Timestamp.valueOf(items[2].toString()) : null);
            aMatch.setDuration(inventory.getExperimentDuration(aMatch.getDbId()));
//            aMatch.setNbEvents(((Long) items[3]).intValue());
            aMatch.setNbEvents(0);
//            if (aMatch.getNbEvents() == 0) {
//                aMatch.setNbProcs(getGraphProcesses(aMatch.getDbId()).size());
//                aMatch.setNbParams(getGraphParameters(aMatch.getDbId()).size());
//            } else {
//                aMatch.setNbProcs(((Long) items[4]).intValue());
//                aMatch.setNbParams(((Long) items[5]).intValue());
//            }
                aMatch.setNbProcs(((BigInteger) items[3]).intValue());
                aMatch.setNbParams(((BigInteger) items[4]).intValue());
        }
        return aMatch;
    }

//    public Vector<Event> getGraphEvents(final int gid) {
//        Vector<Event> matches = new Vector<Event>();
//        String statement = "select EVENT_DESC, PROCESS_VALUE, EVENT_TIMESTAMP " +
//                "from OPM_PROCESS p, PLIER_EVENT e " +
//                "where p.GraphKey=e.GraphKey and p.GraphKey=" + gid + " and e.EVENT_SRC=p.PROCESS_ID and EVENT_TYPE like '%STATUS%'";
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
//        Iterator iterator = results.iterator();
//        while (iterator.hasNext()) {
//            Event aMatch = new Event();
//            Object[] items = (Object[]) iterator.next();
//            aMatch.setDescription(items[0].toString());
//            aMatch.setSource(items[1].toString());
//            aMatch.setTimestamp(Timestamp.valueOf(items[2].toString()));
//            matches.add(aMatch);
//        }
//        return matches;
//    }

    public Vector<nl.biggrid.plier.opm.Process> getGraphProcesses(final long gid) {
        Vector<nl.biggrid.plier.opm.Process> matches = new Vector<nl.biggrid.plier.opm.Process>();
//        String statement = "select PROCESS_ID, PROCESS_VALUE from OPM_PROCESS where GraphKey=" + gid;
        String statement = "select ProcessId from OPMProcess where GraphKey=" + gid;
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
//        Iterator iterator = results.iterator();
        List results = pm.executeSQL(statement);
        Iterator iterator = results.iterator();
        while (iterator.hasNext()) {
            nl.biggrid.plier.opm.Process aMatch = new nl.biggrid.plier.opm.Process();
            //Object[] items = (Object[]) iterator.next();
            aMatch.setId(iterator.next().toString());
            //aMatch.setValue(items[1].toString());
            matches.add(aMatch);
        }
        return matches;
    }

//    public LinkedHashSet<Event> getSortedGraphEvents(final int gid) {
//        LinkedHashSet<Event> matches = new LinkedHashSet<Event>();
//        String statement = "select EVENT_SRC, EVENT_DESC, EVENT_TIMESTAMP " +
//                            "from PLIER_EVENT " +
//                            "where GraphKey=" + gid + " and EVENT_TYPE='STATUS' order by EVENT_SRC, EVENT_TIMESTAMP";
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
//        Iterator iterator = results.iterator();
//        while (iterator.hasNext()) {
//            Event aMatch = new Event();
//            Object[] items = (Object[]) iterator.next();
//            aMatch.setSource(items[0].toString());
//            aMatch.setDescription(items[1].toString());
//            aMatch.setTimestamp(Timestamp.valueOf(items[2].toString()));
//            matches.add(aMatch);
//        }
//        return matches;
//    }

    public Vector<Artifact> getGraphParameters(final long gid) {
        Vector<Artifact> matches = new Vector<Artifact>();
        String statement = "select ArtifactId, ArtifactValue from OPMArtifact where GraphKey=" + gid;
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
//        Iterator iterator = results.iterator();
        List results = pm.executeSQL(statement);
        Iterator iterator = results.iterator();
        while (iterator.hasNext()) {
            Object[] items = (Object[]) iterator.next();
            if (items != null) {
                Artifact aMatch = new Artifact();
                aMatch.setId((items[0] != null)?items[0].toString():"");
                aMatch.setValue((items[1] != null)?items[1].toString():"");
                matches.add(aMatch);
            }
        }
        return matches;
    }

    public Vector<Artifact> getGraphProcessIns(final long gid, final long pid) {
        Vector<Artifact> matches = new Vector<Artifact>();
        String statement = "select a.ArtifactId, a.ArtifactValue " +
                "from OPMArtifact a, OPMDependency dep " +
                "where a.GraphKey=" + gid + " and dep.GraphKey=a.GraphKey and dep.DependencyType='nl.biggrid.plier.opm.Used' and dep.DependencyEffect=" + pid + " and a.ArtifactKey=dep.DependencyCause";
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
//        Iterator iterator = results.iterator();
        List<Object[]> results = pm.executeSQL(statement);
        Iterator<Object[]> iterator = results.iterator();
        while (iterator.hasNext()) {
            Artifact aMatch = new Artifact();
            Object[] items = (Object[]) iterator.next();
            aMatch.setId(items[0].toString());
            aMatch.setValue(items[1].toString());
            matches.add(aMatch);
        }
        return matches;
    }

    public LinkedHashSet<Artifact> getSortedGraphProcessIns(final long gid, final long pid) {
        LinkedHashSet<Artifact> matches = new LinkedHashSet<Artifact>();
        String statement = "select distinct DependencyCause, ArtifactValue Value " +
                            "from OPMDependency d, OPMArtifact a " +
                            "where DependencyCause=ArtifactKey and d.GraphKey=a.GraphKey and d.GraphKey=" + gid + " and DependencyEffect=" + pid + " and DependencyType='nl.biggrid.plier.opm.Used' order by DependencyKey, DependencyEffect";
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
//        Iterator iterator = results.iterator();
        List<Object[]> results = pm.executeSQL(statement);
        Iterator<Object[]> iterator = results.iterator();
        while (iterator.hasNext()) {
            Artifact aMatch = new Artifact();
            Object[] items = (Object[]) iterator.next();
            aMatch.setId(items[0].toString());
            aMatch.setValue(items[1].toString());
            matches.add(aMatch);
        }
        return matches;
    }

    public Vector<Artifact> getGraphProcessOuts(final long gid, final long pid) {
        Vector<Artifact> matches = new Vector<Artifact>();
        String statement = "select a.ArtifactId, a.ArtifactValue " +
                "from OPMArtifact a, OPMDependency dep " +
                "where a.GraphKey=" + gid + " and dep.GraphKey=a.GraphKey and dep.DependencyType='nl.biggrid.plier.opm.WasGeneratedBy' and dep.DependencyCause=" + pid + " and a.ArtifactKey=dep.DependencyEffect";
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
//        Iterator iterator = results.iterator();
        List<Object[]> results = pm.executeSQL(statement);
        Iterator<Object[]> iterator = results.iterator();
        while (iterator.hasNext()) {
            Artifact aMatch = new Artifact();
            Object[] items = (Object[]) iterator.next();
            aMatch.setId(items[0].toString());
            aMatch.setValue(items[1].toString());
            matches.add(aMatch);
        }
        return matches;
    }

    public Vector<Agent> getGraphProcessControllers(final long gid, final long pid) {
        Vector<Agent> matches = new Vector<Agent>();
        String statement = "select distinct a.AgentId " +
                "from OPMAgent a, OPMDependency dep " +
                "where a.GraphKey=" + gid + " and dep.GraphKey=a.GraphKey " +
                "and dep.DependencyType='nl.biggrid.plier.opm.WasControlledBy' and " +
                "dep.DependencyEffect=" + pid + " and a.AgentKey=dep.DependencyCause";
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
//        Iterator iterator = results.iterator();
        List<Object[]> results = pm.executeSQL(statement);
        Iterator<Object[]> iterator = results.iterator();
        while (iterator.hasNext()) {
            Agent aMatch = new Agent();
            Object[] items = (Object[]) iterator.next();
            aMatch.setId(items[0].toString());
            //aMatch.setValue(items[1].toString());
            matches.add(aMatch);
        }
        return matches;
    }

    public Vector<nl.biggrid.plier.opm.Process> getGraphArtifactGenerators(final long gid, final long aid) {
        Vector<nl.biggrid.plier.opm.Process> matches = new Vector<nl.biggrid.plier.opm.Process>();
        String statement = "select p.ProcessId " +
                "from OPMProcess p, OPMDependency dep " +
                "where p.GraphKey=" + gid + " and p.ProcessKey=wgb.DependencyCause and dep.GraphKey=p.GraphKey and dep.DependencyType='nl.biggrid.plier.opm.WasGeneratedBy' and dep.DependencyEffect=" + aid + "";
//        Query query = pm.newQuery(DN_SQL, statement);
//        List results = (List) query.execute();
//        Iterator iterator = results.iterator();
        List<Object[]> results = pm.executeSQL(statement);
        Iterator<Object[]> iterator = results.iterator();
        while (iterator.hasNext()) {
            nl.biggrid.plier.opm.Process aMatch = new nl.biggrid.plier.opm.Process();
            Object[] items = (Object[]) iterator.next();
            aMatch.setId(items[0].toString());
            //aMatch.setValue(items[1].toString());
            matches.add(aMatch);
        }
        return matches;
    }

    public void shutdown() {
//        if (pm != null) {
//            pm.close();
//        }
    }

    public OPMGraph getExperiment(final String gid) {
//        List experiments = pm.executeSQL("select * from OPMGraph where GraphId = ?", OPMGraph.class, gid);
//        System.out.println("###MSF::ge matching experiment: "+experiments.size());
//        return (OPMGraph)experiments.get(0);
        OPMGraph graph = (OPMGraph)pm.get(OPMGraph.class, new Long(2));
        System.out.println("### ACCOUNTS -> "+graph.getAccounts().getAccount().size());
        return graph;
    }

}
