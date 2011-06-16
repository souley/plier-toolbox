/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.pcc.pqi.ui;

/**
 *
 * @author Souley
 */
import att.grappa.Graph;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaSupport;
import att.grappa.Parser;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JComponent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import nl.biggrid.plier.opm.Account;
import nl.biggrid.plier.opm.Agent;
import nl.biggrid.plier.opm.Artifact;
import nl.biggrid.plier.opm.CausalDependency;
import nl.biggrid.plier.opm.EmbeddedAnnotation;
import nl.biggrid.plier.opm.OPMGraph;
import nl.biggrid.plier.opm.Property;
import nl.biggrid.plier.opm.Used;
import nl.biggrid.plier.opm.WasControlledBy;
import nl.biggrid.plier.opm.WasDerivedFrom;
import nl.biggrid.plier.opm.WasGeneratedBy;
import nl.biggrid.plier.opm.WasTriggeredBy;
import nl.uva.pcc.pqi.query.QueryManager;
import nl.uva.pcc.pqi.query.GraphSummary;

public class GVFGraphEditor extends JScrollPane {

    private OPMGraph myGraph = null;
    private GraphSummary myGraphSummary = null;

    public GVFGraphEditor(final OPMGraph aGraph, final GraphSummary aGraphSummary, JComponent aContainer) {
        myGraph = aGraph;
        myGraphSummary = aGraphSummary;
        drawGraph();
    }

    public Map<Artifact, String> getProcessIns(final String pid) {
        Map<Artifact, String> matches = new HashMap<Artifact, String>();
        Collection<Artifact> params = myGraph.getArtifacts().getArtifact();
        for (Artifact art : params) {
            Collection<CausalDependency> deps = myGraph.getCausalDependencies().getDependency();
            for (CausalDependency dep : deps) {
                if (dep instanceof Used) {
                    Used used = (Used)dep;
                    if (used.getEffect().getId().equalsIgnoreCase(pid) && used.getCause().getId().equalsIgnoreCase(art.getId())) {
                        matches.put(art, used.getRole());
                    }
                }
            }
        }
        return matches;
    }

    nl.biggrid.plier.opm.Process getProcessByName(final String pid) {
        for (nl.biggrid.plier.opm.Process p : myGraph.getProcesses().getProcess()) {
            if (pid.equalsIgnoreCase(p.getId())) {
                return p;
            }
        }
        return null;
    }

    class HandyPair{
        Date timestamp = null;
        String input = "";
        public HandyPair(final Date time, final String someInput) {
            timestamp = time;
            input = someInput;
        }
    }

//    String getProcessInputString(final String pid) {
//        String inputStr = "";
//        Collection<Artifact> inputs = QueryManager.instance().getSortedGraphProcessIns(myGraphSummary.getDbId(), pid);
//        for (Artifact input : inputs) {
//            inputStr += (input.getValue() + ",");
//        }
//        if (inputStr.indexOf(",") != -1) {
//            inputStr = inputStr.substring(0, inputStr.lastIndexOf(","));
//        }
//        return inputStr;
//    }
//
//    LinkedHashSet<Event> getSortedFilteredGraphEvents() {
//        LinkedHashSet<Event> filteredEvents = new LinkedHashSet<Event>();
//        LinkedHashSet<Event> events = QueryManager.instance().getSortedGraphEvents(myGraphSummary.getDbId());
//        String prevEvtSrc = "";
//        for (Event event : events) {
//            if (!prevEvtSrc.isEmpty()) {
//                if (!prevEvtSrc.equalsIgnoreCase(event.getSource())) {
//                    prevEvtSrc = event.getSource();
//                    filteredEvents.add(event);
//                }
//            } else {
//                prevEvtSrc = event.getSource();
//                filteredEvents.add(event);
//            }
//        }
//        return filteredEvents;
//    }
//
//    Map<nl.biggrid.plier.opm.Process, HandyPair> getSortedProcesses() {
//        Map<nl.biggrid.plier.opm.Process, HandyPair> matches = new LinkedHashMap<nl.biggrid.plier.opm.Process, HandyPair>();
//        LinkedHashSet<Event> events = getSortedFilteredGraphEvents();
//        for (Event event : events) {
//            nl.biggrid.plier.opm.Process p = getProcessByName(event.getSource());
//            if (p != null) {
//                matches.put(p, new HandyPair(event.getTimestamp(), getProcessInputString(event.getSource())));
//            }
//        }
//        return matches;
//    }

//    public Map<Artifact, String> getProcessOuts(final String pid) {
//        Map<Artifact, String> matches = new HashMap<Artifact, String>();
//        LinkedHashSet<Artifact> params = myGraph.getArtifacts();
//        for (Artifact art : params) {
//            LinkedHashSet<Dependency> deps = myGraph.getDependencies();
//            for (Dependency dep : deps) {
//                if ((dep instanceof WasGeneratedBy) && dep.getCause().equalsIgnoreCase(pid) && dep.getEffect().equalsIgnoreCase(art.getId())) {
//                    matches.put(art, ((WasGeneratedBy) dep).getRole());
//                }
//            }
//        }
//        return matches;
//    }

//    public Map<Agent, String> geProcessControllers(final String pid) {
//        Map<Agent, String> matches = new HashMap<Agent, String>();
//        LinkedHashSet<Agent> ctrls = myGraph.getAgents();
//        for (Agent agt : ctrls) {
//            LinkedHashSet<Dependency> deps = myGraph.getDependencies();
//            for (Dependency dep : deps) {
//                if ((dep instanceof WasControlledBy) && dep.getEffect().equalsIgnoreCase(pid) && dep.getCause().equalsIgnoreCase(agt.getId())) {
//                    matches.put(agt, ((WasControlledBy) dep).getRole());
//                }
//            }
//        }
//        return matches;
//    }

    static String cuteName(String input) {
        String stringId = input.trim();

        stringId = stringId.replace("://", "_");
        stringId = stringId.replace(":", "_");
        stringId = stringId.replace(";", "_");
        stringId = stringId.replace("/", "_");
        stringId = stringId.replace("-", "_");
        stringId = stringId.replace(".", "_");
        stringId = stringId.replace(" ", "_");
        stringId = stringId.replace("@", "_");
        stringId = stringId.replace("[", "_");
        stringId = stringId.replace("]", "_");
        stringId = stringId.replace("|", "_");
        stringId = stringId.replace("=", "_");
        //As requested by Ammar
        stringId = stringId.replace("(", "_");
        stringId = stringId.replace(")", "_");
        //stringId = "_" + stringId;

        return stringId;
    }

//    Event getEventById(final LinkedHashSet<Event> events, final int evId) {
//        for (Event event : events) {
//            if (event.getId() == evId) {
//                return event;
//            }
//        }
//        return null;
//    }

//    boolean processHasAccount(final nl.biggrid.plier.opm.Process process, final Account account) {
//        return process.getAccounts().contains(account);
//    }
//
//    boolean artifactHasAccount(final Artifact artifact, final Account account) {
//        return artifact.getAccounts().contains(account);
//    }
//
//    boolean agentHasAccount(final Agent agent, final Account account) {
//        return agent.getAccounts().contains(account);
//    }

    nl.biggrid.plier.opm.Process isRetryOf(final Map<nl.biggrid.plier.opm.Process, HandyPair> piMap, final nl.biggrid.plier.opm.Process p, final String procIn, final int end) {
        if (end >= 1) {
            Set<nl.biggrid.plier.opm.Process> procs = piMap.keySet();
            //Collection<String> inputs = piMap.values();
            nl.biggrid.plier.opm.Process[] procArray = procs.toArray(new nl.biggrid.plier.opm.Process[procs.size()]);
            for (int i=end-1; i>=0; i--) {
                if (procIn.equalsIgnoreCase(piMap.get(procArray[i]).input) && !p.equals(procArray[i])) {
                    return procArray[i];
                }
            }
        }
        return null;
    }

//    public String createDot() {
//        String header = "digraph OPMGraph { \n";
//        header = header + "rankdir=\"LR\";\n";
//        String body = "";
//        String footer = "}\n";
//
//        Map<nl.biggrid.plier.opm.Process, HandyPair> piMap = getSortedProcesses();
//        for (Map.Entry<nl.biggrid.plier.opm.Process, HandyPair> entry : piMap.entrySet()) {
//            nl.biggrid.plier.opm.Process p = entry.getKey();
//            body = body + cuteName(p.getValue()) + " [shape=box];" + "\n";
//        }
//        int index = 0;
//        nl.biggrid.plier.opm.Process prevProc = null;
//        String subgraph = "";
//
//        for (Map.Entry<nl.biggrid.plier.opm.Process, HandyPair> entry : piMap.entrySet()) {
//            nl.biggrid.plier.opm.Process p = entry.getKey();
//            if (prevProc != null) {
//                body = body + cuteName(prevProc.getValue()) +
//                        " -> " +
//                        cuteName(p.getValue()) +
//                        " [label=\""+entry.getValue().timestamp+"\"]\n";
//                nl.biggrid.plier.opm.Process retriedProc = isRetryOf(piMap, p, entry.getValue().input, index);
//                if (retriedProc != null) {
//                    body = body + cuteName(p.getValue()) +
//                        " -> " +
//                        cuteName(retriedProc.getValue()) +
//                        " [style=dotted,label=\"RetryOf\"]\n";
//                }
//            }
//            prevProc = p;
//            index++;
//        }
//        body += subgraph;
//        String toReturn = header + body + footer;
//        return toReturn;
//    }

    String simpleValue(String string) {
        if (string.indexOf("/") >= 0) {
            return string.substring(string.lastIndexOf("/") + 1, string.length());
        } else {
            return string;
        }
    }

    boolean compare(String s1, String s2) {
        java.text.DateFormat df = new java.text.SimpleDateFormat("dd-MM-yy HH:mm:ss");


        try {
            java.util.Date d1 = df.parse(s1);
            java.util.Date d2 = df.parse(s2);
            if (d1.getTime() >= d2.getTime()) {
                return true;
            } else {
                return false;
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String createDot2() {
        String[][] processes = new String[1000][9]; //process, input, flag, start, end, status, retryof, event, out
        String host = "";
        String header = "digraph OPMGraph { \n";
        String graphId = myGraph.getId();
        String artifactId = "";
        if (graphId.indexOf("__") >= 0) {
            graphId = graphId.substring(graphId.indexOf("__") + 2, graphId.length());
        }
        header = header + "rankdir=\"BT\";\n" + "ratio=fill;fontsize=20;fontcolor=violetred;\n" + "workflowLabel [label=\"" + myGraph.getId() + "\",color=white,fontsize=24,fontcolor=violetred" + ",URL=\"" + host + graphId + "/html/" + graphId + ".html\"];\n" + "compound=true;\n";
        String body = "";
        String footer = "}\n";
        String finalStatus = ""; //used to check the final status of a process for coloring the process box
        String p_cluster = "";

        try {
            String[] possibilities = new String[100];
            int i = 0;
            for (Account accountn : myGraph.getAccounts().getAccount()) {
                possibilities[i] = accountn.getId();
                i++;
            }
            int l = 0;
            String lastProc = "";
            String lastProperty = "";
            for (nl.biggrid.plier.opm.Process process : myGraph.getProcesses().getProcess()) {
                processes[l][0] = process.getId();
                processes[l][3] = "";
                processes[l][2] = "FREE";
                processes[l][4] = "";
                processes[l][6] = "0";
                processes[l][7] = "";
                processes[l][8] = "";
                finalStatus = "";
//                body = body + cuteName(process.getId()) + "[shape=box,label=\"" + process.getValue() + "\"];" + "\n";
                String events = "";
                String temp = "";
//                System.out.println("last event1" + lastEvent + ": ");
//                System.out.print("\n" + process.getId() + ": ");
                if (lastProperty.length() > 0 && !lastProperty.equals("COMPLETED")) {
//                    p_cluster = p_cluster + cuteName(process.getId()) + " -> " + cuteName(lastProc) + " [style=dotted,label=RetryOf];\n";
                }
                i = 1;
                for (EmbeddedAnnotation relatedAnn : process.getAnnotation()) {
                    System.out.println((i++) + "- " + relatedAnn.getId() + "-> " );
                for (Property property : relatedAnn.getPropertyList()) {
                    System.out.println("\t"+ property.getUri() + ": " + property.getValue());
//                }
//                    //Event realEvent = opmGraph.getEvents().getEventById(relatedEvent.getId());
//                    Event realEvent = getEventById(opmGraph.getEvents(), relatedEvent.getId());
                    finalStatus = property.getUri();
//                    events = events + "<e" + realEvent.getId() + "> " + realEvent.getDescription().split(" ")[0].split("\n")[0] + "|";
                    events = events + "<e" + i + "> " + property.getUri() + "|";
//                }
                    Date d = new Date(Long.parseLong(property.getValue()));
//                    d = realEvent.getTimestamp();
                    temp = temp + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(d) + "|";
//
////                    if (finalStatus.equals("QUEUED") && !lastEvent.equals("COMPLETED"))
////                        p_cluster = p_cluster + cuteName(process.getId()) + " -> " + cuteName(lastProc) + " [style=dotted,label=RetryOf];\n";
                    if (finalStatus.equals("QUEUED")) {
                        processes[l][3] = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(d);
                    }
                    if (processes[l][3].equals("") && finalStatus.equals("RUNNING")) {
                        processes[l][3] = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(d);
                    }
                    if (finalStatus.equals("COMPLETED") || finalStatus.equals("ERROR") || finalStatus.equals("TIMEOUT")) {
                        processes[l][4] = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(d);
                    }
                }
//                break;
                }
//                System.out.println("last event2" + lastEvent + ": ");
                lastProperty = finalStatus;
                if (finalStatus.equals("COMPLETED")) {
                    finalStatus = "color=greenyellow";
                    processes[l][5] = "SUCCEED";
                } else {
                    finalStatus = "color=red";
                    processes[l][5] = "FAILED";
                }


                if (!events.isEmpty()) {
                    System.out.println("Events: "+ events);
                    System.out.println("temp: "+ temp);
                    events = events.substring(0, events.lastIndexOf("|")).trim();
                    temp = temp.substring(0, temp.lastIndexOf("|")).trim();
                    events = "{{" + events + "}|{" + temp + "}}";

//                    body = body + "eventList" + cuteName(process.getId()) + " [shape=record,style=rounded,fontsize=10,label=\"" + events + "\"];\n";
//                    body = body + "eventList" + cuteName(process.getId()) + " -> " + cuteName(process.getId()) + " [style=dotted,color=orange]\n";
                    processes[l][7] = cuteName(process.getId()) + " [shape=box," + finalStatus + ",style=filled,label=\"" + process.getId() + "\"" + ",URL=\"" + host + graphId + "/out/" + process.getId() + ".std.out\"];\n";
                    processes[l][7] = processes[l][7] + "eventList" + cuteName(process.getId()) + " [shape=record,style=rounded,fontsize=10,label=\"" + events + "\"];\n";
                    processes[l][7] = processes[l][7] + "eventList" + cuteName(process.getId()) + " -> " + cuteName(process.getId()) + " [style=dotted,color=orange]\n";
                    if (finalStatus.equals("color=red")) {
                        processes[l][8] = "_STD_OUT_" + cuteName(process.getId()) + " [shape=ellipse,color=orangered,style=filled,label=Error" + ",URL=\"" + host + graphId + "/err/" + process.getId() + ".std.err\"];\n";
                        processes[l][8] = processes[l][8] + "_STD_OUT_" + cuteName(process.getId()) + " -> eventList" + cuteName(process.getId()) + " [style=dotted,color=red]\n";
                    }
                    if (finalStatus.equals("color=greenyellow")) {
                        processes[l][8] = "_STD_OUT_" + cuteName(process.getId()) + " [shape=ellipse,color=greenyellow,style=filled,label=Completed];\n";
                        processes[l][8] = processes[l][8] + "_STD_OUT_" + cuteName(process.getId()) + " -> eventList" + cuteName(process.getId()) + " [style=dotted,color=green]\n";
                    }

                } else {
                    finalStatus = "color=lightblue";
                    body = body + cuteName(process.getId()) + " [shape=box," + finalStatus + ",style=filled,label=\"" + process.getId() + "\"];" + "\n";
                }
                int input = 0;
                for (CausalDependency cd : myGraph.getCausalDependencies().getDependency()) {
                    if (cd instanceof Used) {
                        Used used = (Used) cd;
                        if (used.getEffect().equals(process)) {
                            input++;
                        }
                    }
                }
                processes[l][1] = Integer.toString(input);

                lastProc = process.getId();
                l++;

//                body = body + cuteName(process.getId()) + " [shape=box," + finalStatus + ",style=filled,label=\"" + process.getValue() + "\"];" + "\n";
            }
            for (i = 0; i < l; i++) {
//                    System.out.println(processes[i][0] + "\t " + processes[i][1] + "\t '" + processes[i][2] + "\t '" + processes[i][3] + "'\t '" + processes[i][4] + "'\t " + processes[i][5] + processes[i][6] + "'\t '" + processes[i][7] + "'\t " + processes[i][8]);
//                System.out.println(processes[i][0]);
                if (processes[i][1].equals("0")) {
//                    System.out.print("\t ---> This is a re-trial of ... ");
                    for (int j = 0; j < l; j++) {
                        if (j != i && processes[j][4].length() > 2 && processes[i][3].length() > 2 && processes[j][2].equals("FREE") && processes[j][5].equals("FAILED") && compare(processes[i][3], processes[j][4])) {
//                            System.out.println(processes[i][0] + ": " + processes[j][0]);
                            processes[j][2] = "Taken";
                            processes[i][6] = String.valueOf(j);
                            p_cluster = p_cluster + cuteName(processes[i][0]) + " -> " + "_STD_OUT_" + cuteName(processes[j][0]) + " [style=dotted,label=RetryOf];\n";
                            break;
                        }

                    }
                }
//                    System.out.println(processes[i][0] + "\t " + processes[i][1] + "\t '" + processes[i][2] + "\t '" + processes[i][3] + "'\t '" + processes[i][4] + "'\t " + processes[i][5] + "'\t " + processes[i][6]);
            }
            //p_cluster = p_cluster + "}\n";
            for (Artifact artifact : myGraph.getArtifacts().getArtifact()) {
//                System.out.println("artifact: " + artifact.getValue());
                artifactId = simpleValue(artifact.getValue());
//                if (artifact.getValue().indexOf(":")>=0 && artifact.getValue().indexOf("/")>=0)
                if (artifact.getValue().indexOf("/") >= 0) {
                    body = body + cuteName(artifact.getId()) + " [shape=ellipse,label=\"" + artifactId + "\",URL=\"" + artifact.getValue() + "\"];\n";
                } else {
                    body = body + cuteName(artifact.getId()) + " [shape=ellipse,label=\"" + artifactId + "\"];\n";
                }
            }
//            header = header + p_cluster+ "}\n" ;
            for (Agent agent : myGraph.getAgents().getAgent()) {
                body = body + cuteName(agent.getId()) + " [shape=octagon,color=lightcyan,style=filled,label=\"" + agent.getId() + "\"];\n";
            }

            body = body + "\n";
            for (CausalDependency cd : myGraph.getCausalDependencies().getDependency()) {
                if (cd instanceof Used) {
                    Used used = (Used) cd;
                    body = body + cuteName(used.getEffect().getId()) +
                            " -> " +
                            cuteName(used.getCause().getId()) +
                            " [style=dotted,color=brown,label=\"" +
                            used.getRole() + "\"]\n";
                }
                if (cd instanceof WasGeneratedBy) {
                    WasGeneratedBy wgb = (WasGeneratedBy) cd;
                    if (wgb.getRole().equals("Output")) {
                        body = body + cuteName(wgb.getEffect().getId()) +
                                " -> " +
                                cuteName(wgb.getCause().getId()) +
                                " [style=dotted,color=blue,label=\"" +
                                wgb.getRole() + "\"]\n";
                    } else if (wgb.getRole().indexOf("Output_")<0) {//if ((wgb.getRole().substring(wgb.getRole().lastIndexOf("_")+1,wgb.getRole().length())).length()<4){
                        System.out.println(wgb.getCause() + " - " + wgb.getEffect() + " - " + wgb.getRole() + " - '" + wgb.getRole().substring(wgb.getRole().lastIndexOf("_")+1,wgb.getRole().length()) + "'");
//                        System.out.println(wgb.getCause() + " - " + wgb.getEffect() + " - " + wgb.getRole() + " - " + Integer.decode(wgb.getRole().substring(wgb.getRole().lastIndexOf("_")+1,wgb.getRole().length())));
                        body = body + cuteName(wgb.getEffect().getId()) +
                                " -> " + "_STD_OUT_" +
                                cuteName(wgb.getCause().getId()) +
                                " [style=dotted,color=green,label=\"" +
                                wgb.getRole() + "\"]\n";
                    }

                }
                if (cd instanceof WasControlledBy) {
                    WasControlledBy wcb = (WasControlledBy) cd;
                    body = body + cuteName(wcb.getEffect().getId()) +
                            " -> " +
                            cuteName(wcb.getCause().getId()) +
                            " [style=dotted,label=\"" +
                            wcb.getRole() + "\"]\n";
                }
                if (cd instanceof WasDerivedFrom) {
                    WasDerivedFrom wdf = (WasDerivedFrom) cd;
                    body = body + cuteName(wdf.getEffect().getId()) +
                            " -> " +
                            cuteName(wdf.getCause().getId()) +
                            " [style=solid]\n";
                }
                if (cd instanceof WasTriggeredBy) {
                    WasTriggeredBy wtb = (WasTriggeredBy) cd;
                    body = body + cuteName(wtb.getEffect().getId()) +
                            " -> " +
                            cuteName(wtb.getCause().getId()) +
                            " [style=solid]\n";
                }
            }
            i = 1;

                String v1, v2;
            for (i = 0; i < l; i++) {
                processes[i][2] = "FREE";
                v1 = String.valueOf(i);
                v2 = String.valueOf(i);
                for (int j = i; j < l; j++) {
                    if (processes[j][6].equals(v2)) {
                        processes[j][6] = v1;
                        v2 = String.valueOf(j);
                    }
                }
            }
            for (i = 0; i < l; i++) {
                int idx = Integer.parseInt(processes[i][6]);
//                System.out.println(i + "- " + processes[i][0] + "\t " + processes[i][1] + "\t '" + processes[i][2] + "\t '" + processes[i][3] + "'\t '" + processes[i][4] + "'\t " + processes[i][5] + "'\t " + processes[i][6]);
//                    System.out.println(processes[i][0]);
                if (processes[i][2].equals("FREE") && processes[i][3].length() > 2 && processes[i][4].length() > 2 && idx==0) {
                    processes[i][2] = "Taken";
                    processes[i][7] = "\nsubgraph cluster" + (i) + "{color=white;\n" + processes[i][7];
                }
                if (idx!=0){
                    processes[idx][7] = processes[idx][7] + processes[i][7];
                    processes[idx][8] = processes[idx][8] + processes[i][8];
                    processes[i][7] = ""; processes[i][8] = "";
                }

            }
            for (i = 0; i < l; i++) {
//                System.out.println("Process: " + i + ": " + processes[i][6] + " - " + processes[i][7].length());
                if (processes[i][7].length()>2){
//                    System.out.println("Adding subgraph: " + i);
                    p_cluster = p_cluster + processes[i][7] + processes[i][8] + "}\n";
                }
            }



        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String toReturn = header + body + p_cluster + footer;
//        String toReturn = header + body.substring(0,body.length()-2) + p_cluster + "\n}\n}" + footer;
//        String toReturn = header + footer;
        return toReturn;
    }

    void drawGraph() {
        try {
            // Create temporary file.
            File tempfile = File.createTempFile("plier", ".dot");

            // Delete temp file when program exits.
            tempfile.deleteOnExit();

            // Write to temp file
            BufferedWriter out = new BufferedWriter(new FileWriter(tempfile));

            String dotOut = createDot2();
            out.write(dotOut);
            out.close();

            // Parse the .dot file (diagram)
            InputStream inputDot = new FileInputStream(tempfile.getAbsolutePath());
            Parser parser = new Parser(inputDot, System.err);

            try {
                parser.parse();
            } catch (Exception ex) {
                System.err.println("Exception: " + ex.getMessage());
                JOptionPane.showMessageDialog(
                        this,
                        "PlierExchange: Cannot create diagram.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(System.err);
            //container.getViewport().setVisible(false);
            }

            // Create the Graph
            Graph graph = parser.getGraph();
            graph.setEditable(true);
            graph.setErrorWriter(new PrintWriter(System.err, true));

            // Create the Graph Pannel
            GrappaPanel grappaPanel = new GrappaPanel(graph);
            grappaPanel.addGrappaListener(new GrappaAdapter());
//            grappaPanel.setScaleToFit(true);

            setViewportView(grappaPanel);

            String layout = "";
            String[] dotArgs = {"dot", layout}; //neato
            try {
                java.lang.Process dotProcess = Runtime.getRuntime().exec(dotArgs, null, null);
                if (!GrappaSupport.filterGraph(graph, dotProcess)) {
                    System.err.println("ERROR: Processing the function 'filterGraph'");
                }
            } catch (Exception ex) {
                System.err.println("Exception while setting up 'dot': " + ex.getMessage() + "\nLayout not performed.");
                JOptionPane.showMessageDialog(
                        null,
                        "PlierExchange: The application cannot find the 'dot' program.\n" +
                        "Layout not performed.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            graph.repaint();

            tempfile.delete();
        } catch (Exception e) {
            System.err.println("Exception while creating diagram: " + e.getMessage());
            JOptionPane.showMessageDialog(
                    this,
                    "Cannot create diagram.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        //container.getViewport().setVisible(false);
        }
    }
}
