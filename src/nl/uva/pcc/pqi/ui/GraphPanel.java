/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.pcc.pqi.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import nl.biggrid.plier.opm.OPMGraph;
import nl.uva.pcc.pqi.jgoodies.UIFSplitPane;
import nl.uva.pcc.pqi.query.GraphSummary;

/**
 *
 * @author Souley
 */
public class GraphPanel extends JPanel {

    OPMGraph theGraph = null;
    GraphSummary theGraphSummary = null;
    private JPanel summaryContainer = null; // Workflow detail container
    private JScrollPane detailContainer = null; // Workflow component detail container

    public GraphPanel(final GraphSummary graphSummary, final OPMGraph graph) {
        theGraphSummary = graphSummary;
        theGraph = graph;
        initComponents();
        buildGUI();
    }

    void initComponents() {
        summaryContainer = new ExperimentSummary(theGraphSummary);
        detailContainer = new JScrollPane();
    }

    void buildGUI() {
        setLayout(new BorderLayout());
        add(buildMainPanel(), BorderLayout.CENTER);
        detailContainer.setViewportView(new ExperimentDetail(theGraphSummary, theGraph));
    }

    private Component buildMainPanel() {
        JSplitPane verticalSplit = UIFSplitPane.createStrippedSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                summaryContainer,
                detailContainer);
        verticalSplit.setDividerLocation(150);
        verticalSplit.setOpaque(false);
        return verticalSplit;
    }
}
