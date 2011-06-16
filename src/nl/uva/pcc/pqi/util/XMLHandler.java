/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.pcc.pqi.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
//import nl.biggrid.plier.event.Event;
import nu.xom.*;
import nl.biggrid.plier.opm.*;

/**
 *
 * @author Souley
 */
public class XMLHandler {

    static final String OPM_NS = "http://openprovenance.org/model/v1.1.a";
    static final String CHAR_ENCODING = "UTF-8";

    static public String toXML(final OPMGraph graph) {
        Element root = (Element) new Element("opm:OPMGraph", OPM_NS);
        root.addAttribute(new Attribute("id", graph.getId()));
        Element accounts = (Element) new Element("opm:Accounts", OPM_NS);
        root.appendChild(accounts);
        for (Account acc : graph.getAccounts().getAccount()) {
            addAccount(accounts, acc);
        }
        Element agents = (Element) new Element("opm:Agents", OPM_NS);
        root.appendChild(agents);
        for (Agent agt : graph.getAgents().getAgent()) {
            addAgent(agents, agt);
        }
        Element artifacts = (Element) new Element("opm:Artifacts", OPM_NS);
        root.appendChild(artifacts);
        for (Artifact art : graph.getArtifacts().getArtifact()) {
            addArtifact(artifacts, art);
        }
        Element processes = (Element) new Element("opm:Processes", OPM_NS);
        root.appendChild(processes);
        for (nl.biggrid.plier.opm.Process process : graph.getProcesses().getProcess()) {
            addProcess(processes, process);
        }
        Element dependencies = (Element) new Element("opm:CausalDependencies", OPM_NS);
        root.appendChild(dependencies);
        for (CausalDependency dep : graph.getCausalDependencies().getDependency()) {
            addDependency(dependencies, dep);
        }
        return textify(new Document(root));
    }

    static public void addAccount(final Element parent, final Account account) {
        Element accElt = (Element) new Element("opm:Account", OPM_NS);
        accElt.addAttribute(new Attribute("id", account.getId()));
//        Element valElt = (Element) new Element("opm:label", OPM_NS);
//        valElt.addAttribute(new Attribute("value", account.getValue()));
//        accElt.appendChild(valElt);
        parent.appendChild(accElt);
    }

    static public void addAgent(final Element parent, final Agent agent) {
        Element agtElt = (Element) new Element("opm:Agent", OPM_NS);
        agtElt.addAttribute(new Attribute("id", agent.getId()));
//        Element valElt = (Element) new Element("opm:label", OPM_NS);
//        valElt.addAttribute(new Attribute("value", agent.getValue()));
//        agtElt.appendChild(valElt);
        Element accounts = (Element) new Element("opm:Accounts", OPM_NS);
        agtElt.appendChild(accounts);
        for (Account acc : agent.getAccount()) {
            addAccount(accounts, acc);
        }
        parent.appendChild(agtElt);
    }

    static public void addArtifact(final Element parent, final Artifact artifact) {
        Element artElt = (Element) new Element("opm:Artifact", OPM_NS);
        artElt.addAttribute(new Attribute("id", artifact.getId()));
        Element valElt = (Element) new Element("opm:label", OPM_NS);
        valElt.addAttribute(new Attribute("value", artifact.getValue()));
        artElt.appendChild(valElt);
        Element accounts = (Element) new Element("opm:Accounts", OPM_NS);
        artElt.appendChild(accounts);
        for (Account acc : artifact.getAccount()) {
            addAccount(accounts, acc);
        }
        parent.appendChild(artElt);
    }

//    static public void addEvent(final Element parent, final Event evt) {
//        Element evtElt = (Element) new Element("Event");
//        evtElt.addAttribute(new Attribute("id", Integer.toString(evt.getId())));
//        Element idElt = (Element) new Element("identification");
//        idElt.addAttribute(new Attribute("value", evt.getIdentification()));
//        evtElt.appendChild(idElt);
//        Element descElt = (Element) new Element("description");
//        descElt.addAttribute(new Attribute("value", evt.getDescription()));
//        evtElt.appendChild(descElt);
//        Element srcElt = (Element) new Element("source");
//        srcElt.addAttribute(new Attribute("value", evt.getSource()));
//        evtElt.appendChild(srcElt);
//        Element snElt = (Element) new Element("sourceName");
//        snElt.addAttribute(new Attribute("value", evt.getSourceName()));
//        evtElt.appendChild(snElt);
//        Element hostElt = (Element) new Element("host");
//        hostElt.addAttribute(new Attribute("value", evt.getHost()));
//        evtElt.appendChild(hostElt);
//        Element tsElt = (Element) new Element("timestamp");
//        tsElt.addAttribute(new Attribute("value", evt.getTimestamp().toString()));
//        evtElt.appendChild(tsElt);
//        Element levelElt = (Element) new Element("level");
//        levelElt.addAttribute(new Attribute("value", Integer.toString(evt.getLevel())));
//        evtElt.appendChild(levelElt);
//        Element codeElt = (Element) new Element("code");
//        codeElt.addAttribute(new Attribute("value", Integer.toString(evt.getCode())));
//        evtElt.appendChild(codeElt);
//        Element catElt = (Element) new Element("category");
//        catElt.addAttribute(new Attribute("value", Integer.toString(evt.getCategory())));
//        evtElt.appendChild(catElt);
//        parent.appendChild(evtElt);
//    }

    static public void addProcess(final Element parent, final nl.biggrid.plier.opm.Process process) {
        Element proElt = (Element) new Element("opm:Process", OPM_NS);
        proElt.addAttribute(new Attribute("id", process.getId()));
//        Element valElt = (Element) new Element("opm:label", OPM_NS);
//        valElt.addAttribute(new Attribute("value", process.getValue()));
//        proElt.appendChild(valElt);
        Element accounts = (Element) new Element("opm:Accounts", OPM_NS);
        proElt.appendChild(accounts);
        for (Account acc : process.getAccount()) {
            addAccount(accounts, acc);
        }
//        Element events = (Element) new Element("events");
//        proElt.appendChild(events);
//        for (Event evt : process.getEvents()) {
//            addEvent(events, evt);
//        }
        parent.appendChild(proElt);
    }

    static public void addDependency(final Element parent, final CausalDependency dep) {
        if (dep instanceof Used) {
            addUsed(parent, (Used) dep);
        } else if (dep instanceof WasControlledBy) {
            addWasControlledBy(parent, (WasControlledBy) dep);
        } else if (dep instanceof WasGeneratedBy) {
            addWasGeneratedBy(parent, (WasGeneratedBy) dep);
        } else if (dep instanceof WasDerivedFrom) {
            addWasDerivedFrom(parent, (WasDerivedFrom) dep);
        } else if (dep instanceof WasTriggeredBy) {
            addWasTriggeredBy(parent, (WasTriggeredBy) dep);
        }
    }

    static public void addUsed(final Element parent, final Used used) {
        Element uElt = (Element) new Element("opm:Used", OPM_NS);
        Element effElt = (Element) new Element("opm:effect", OPM_NS);
        effElt.addAttribute(new Attribute("ref", used.getEffect().getId()));
        Element cozElt = (Element) new Element("opm:cause", OPM_NS);
        cozElt.addAttribute(new Attribute("ref", used.getCause().getId()));
        Element rolElt = (Element) new Element("opm:role", OPM_NS);
        rolElt.addAttribute(new Attribute("value", used.getRole()));
        uElt.appendChild(effElt);
        uElt.appendChild(cozElt);
        uElt.appendChild(rolElt);
        Element accounts = (Element) new Element("opm:Accounts", OPM_NS);
        uElt.appendChild(accounts);
        for (Account acc : used.getAccount()) {
            addAccount(accounts, acc);
        }
        parent.appendChild(uElt);
    }

    static public void addWasControlledBy(final Element parent, final WasControlledBy wcb) {
        Element wcbElt = (Element) new Element("opm:WasControlledBy", OPM_NS);
        Element effElt = (Element) new Element("opm:effect", OPM_NS);
        effElt.addAttribute(new Attribute("ref", wcb.getEffect().getId()));
        Element cozElt = (Element) new Element("opm:cause", OPM_NS);
        cozElt.addAttribute(new Attribute("ref", wcb.getCause().getId()));
        Element rolElt = (Element) new Element("opm:role", OPM_NS);
        rolElt.addAttribute(new Attribute("value", wcb.getRole()));
        wcbElt.appendChild(effElt);
        wcbElt.appendChild(cozElt);
        wcbElt.appendChild(rolElt);
        Element accounts = (Element) new Element("opm:Accounts", OPM_NS);
        wcbElt.appendChild(accounts);
        for (Account acc : wcb.getAccount()) {
            addAccount(accounts, acc);
        }
        parent.appendChild(wcbElt);
    }

    static public void addWasGeneratedBy(final Element parent, final WasGeneratedBy wgb) {
        Element wgbElt = (Element) new Element("opm:WasGeneratedBy", OPM_NS);
        Element effElt = (Element) new Element("opm:effect", OPM_NS);
        effElt.addAttribute(new Attribute("ref", wgb.getEffect().getId()));
        Element cozElt = (Element) new Element("opm:cause", OPM_NS);
        cozElt.addAttribute(new Attribute("ref", wgb.getCause().getId()));
        Element rolElt = (Element) new Element("opm:role", OPM_NS);
        rolElt.addAttribute(new Attribute("value", wgb.getRole()));
        wgbElt.appendChild(effElt);
        wgbElt.appendChild(cozElt);
        Element accounts = (Element) new Element("opm:Accounts", OPM_NS);
        wgbElt.appendChild(accounts);
        for (Account acc : wgb.getAccount()) {
            addAccount(accounts, acc);
        }
        wgbElt.appendChild(rolElt);
        parent.appendChild(wgbElt);
    }

    static public void addWasDerivedFrom(final Element parent, final WasDerivedFrom wdf) {
        Element wdfElt = (Element) new Element("opm:WasDerivedFrom", OPM_NS);
        Element effElt = (Element) new Element("opm:effect", OPM_NS);
        effElt.addAttribute(new Attribute("ref", wdf.getEffect().getId()));
        Element cozElt = (Element) new Element("opm:cause", OPM_NS);
        cozElt.addAttribute(new Attribute("ref", wdf.getCause().getId()));
        wdfElt.appendChild(effElt);
        wdfElt.appendChild(cozElt);
        Element accounts = (Element) new Element("opm:Accounts", OPM_NS);
        wdfElt.appendChild(accounts);
        for (Account acc : wdf.getAccount()) {
            addAccount(accounts, acc);
        }
        parent.appendChild(wdfElt);
    }

    static public void addWasTriggeredBy(final Element parent, final WasTriggeredBy wtb) {
        Element wtbElt = (Element) new Element("opm:WasTriggeredBy", OPM_NS);
        Element effElt = (Element) new Element("opm:effect", OPM_NS);
        effElt.addAttribute(new Attribute("ref", wtb.getEffect().getId()));
        Element cozElt = (Element) new Element("opm:cause", OPM_NS);
        cozElt.addAttribute(new Attribute("ref", wtb.getCause().getId()));
        wtbElt.appendChild(effElt);
        wtbElt.appendChild(cozElt);
        Element accounts = (Element) new Element("opm:Accounts", OPM_NS);
        wtbElt.appendChild(accounts);
        for (Account acc : wtb.getAccount()) {
            addAccount(accounts, acc);
        }
        parent.appendChild(wtbElt);
    }

    static public String textify(final Document doc) {
        String xml = "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Serializer serializer = new Serializer(baos, CHAR_ENCODING);
            serializer.setIndent(4);
            serializer.write(doc);
            xml = baos.toString(CHAR_ENCODING);
        } catch (UnsupportedEncodingException uce) {
            System.err.println(uce);
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return xml;
    }
}
