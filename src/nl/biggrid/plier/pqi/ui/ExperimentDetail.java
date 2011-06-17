/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.plier.pqi.ui;

/**
 *
 * @author Souley
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.jgoodies.looks.Options;
import nl.biggrid.plier.opm.OPMGraph;
import nl.biggrid.plier.pqi.query.*;
import nl.biggrid.plier.pqi.jgoodies.*;

public class ExperimentDetail extends javax.swing.JPanel
        implements ChangeListener {

    static final String TAB_TITLES[] = new String[] {"Events", "Parameters", "Processes", "Graph"};
    static final Font BOLD_FONT = new Font("Tahoma", Font.BOLD, 11);
    static final Font PLAIN_FONT = new Font("Tahoma", Font.PLAIN, 11);

    private OPMGraph theGraph = null;
    private GraphSummary theGraphSummary = null;
    private JTabbedPane tabbedPane = null;
    private EventListEditor eventList = null;
    private ParamListEditor paramList = null;
    private ProcessListEditor processList = null;
    private GVFGraphEditor gvGraphEditor = null;
    private int lastSelectedTab = -1;
    /** Creates new form WFDetail */
    public ExperimentDetail(final GraphSummary aGraphSummary, final OPMGraph aGraph) {
        super(new BorderLayout());
        theGraph = aGraph;
        theGraphSummary = aGraphSummary;
        initComponents();
    }

    void initComponents() {
        tabbedPane = new JTabbedPane(SwingConstants.LEFT) {
            @Override
            public String getTitleAt(int index) {
                if (index == lastSelectedTab) {
                    tabbedPane.setFont(BOLD_FONT);
                    return TAB_TITLES[index];
                }
                tabbedPane.setFont(PLAIN_FONT);
                return TAB_TITLES[index];
            }
        };

        eventList = new EventListEditor(theGraphSummary.getDbId());
        tabbedPane.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        tabbedPane.addTab("Events", Factory.createStrippedScrollPane(eventList));
        //tabbedPane.addTab("Events",  null);
        tabbedPane.addTab("Parameters", null);
        tabbedPane.addTab("Processes", null);
        tabbedPane.addTab("Graph", null);
        lastSelectedTab = 0;
        SimpleInternalFrame sif = new SimpleInternalFrame("<html><b>" + theGraph.getId() + " details</b></html>");
        sif.setPreferredSize(new Dimension(150, 100));
        sif.add(tabbedPane);

        add(sif, BorderLayout.CENTER);
        tabbedPane.addChangeListener(this);
    }

    public void stateChanged(ChangeEvent event) {
        int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
            if (index == 1 && paramList == null) {
                paramList = new ParamListEditor(theGraphSummary.getDbId());
                tabbedPane.setComponentAt(index, Factory.createStrippedScrollPane(paramList));
            }
            if (index == 2 && processList == null) {
                processList = new ProcessListEditor(theGraphSummary.getDbId());
                tabbedPane.setComponentAt(index, Factory.createStrippedScrollPane(processList));
            }
            if (index == 3 && gvGraphEditor == null) {
            //if (index == 3) {
                gvGraphEditor = new GVFGraphEditor(theGraph, theGraphSummary, tabbedPane);
                tabbedPane.setComponentAt(index, gvGraphEditor);
                //tabbedPane.setComponentAt(index, null);
            }
            lastSelectedTab = index;
        }
    }
}
