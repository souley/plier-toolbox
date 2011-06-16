/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.pcc.pqi.ui;

/**
 *
 * @author Souley
 */
import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.JPanel;
import nl.uva.pcc.pqi.query.*;
import nl.uva.pcc.pqi.jgoodies.*;

public class ExperimentSummary extends javax.swing.JPanel {

    private GraphSummary theGraph = null;

    public ExperimentSummary(final GraphSummary aGraph) {
        super(new BorderLayout());
        theGraph = aGraph;
        initComponents();
        fillFields();
    }

    void fillFields() {
        QueryManager searchManager = QueryManager.instance();
        GraphSummary graph = searchManager.getFinder().getDetail(theGraph.getId());
        //jtxaDesc.setText(graph.getDesc());
        jlblSTime.setText(graph.getStart() == null?"":graph.getStart().toString());
        jlblDuration.setText(graph.getDuration());
        jlblEvts.setText(Integer.toString(graph.getNbEvents()));
        jlblProcs.setText(Integer.toString(graph.getNbProcs()));
        jlblParams.setText(Integer.toString(graph.getNbParams()));
    }

    void initComponents() {
        jtxaDesc = new javax.swing.JTextArea();
        jtxaDesc.setEditable(false);
        jtxaDesc.setLineWrap(true);
        jtxaDesc.setWrapStyleWord(true);

        jscpDesc = new JScrollPane();
        jscpDesc.setBorder(null);
        jscpDesc.setViewportView(jtxaDesc);

        jlblSTime = new javax.swing.JLabel();
        jlblDuration = new javax.swing.JLabel();
        jlblEvts = new javax.swing.JLabel();
        jlblProcs = new javax.swing.JLabel();
        jlblParams = new javax.swing.JLabel();

        buildGUI();
    }

    private void buildGUI() {
        //setContentPane(buildContentPane());
        SimpleInternalFrame sif = new SimpleInternalFrame("<html><b>"+theGraph.getId()+" summary</b></html>");
        //sif.setPreferredSize(new Dimension(500, 150));
        sif.add(Factory.createStrippedScrollPane(buildContentPane()));
        //sif.add(builder.getPanel());
        add(sif, BorderLayout.CENTER);

    }

    private JPanel buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(jscpDesc, BorderLayout.NORTH);
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        return panel;
    }

    JPanel buildMainPanel() {
        FormLayout layout = new FormLayout(
                "right:70dlu, 5dlu, 70dlu, 5dlu, 60dlu, 5dlu, 60dlu, 5dlu, right:60dlu",
                "p, 3dlu, p, 3dlu, p, 5dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        //builder.add(jscpDesc, cc.xyw(1, 1, 9));

        builder.addSeparator("Summary", cc.xyw(1, 1, 9));

        builder.addLabel("<html><tt>Start</tt><html>", cc.xyw(1, 3, 2));
        builder.addLabel("<html><tt>Duration</tt><html>", cc.xy(3, 3, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.addLabel("<html><tt>events</tt><html>", cc.xy(5, 3, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.addLabel("<html><tt>Parameters</tt><html>", cc.xy(7, 3, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.addLabel("<html><tt>Processes</tt><html>", cc.xy(9, 3, CellConstraints.CENTER, CellConstraints.BOTTOM));

        builder.add(jlblSTime, cc.xyw(1, 5, 2));
        builder.add(jlblDuration, cc.xy(3, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.add(jlblEvts, cc.xy(5, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.add(jlblParams, cc.xy(7, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.add(jlblProcs, cc.xy(9, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));

//        SimpleInternalFrame sif = new SimpleInternalFrame("<html><b>"+theGraph.getId()+" summary</b></html>");
//        //sif.setPreferredSize(new Dimension(500, 150));
//        sif.add(Factory.createStrippedScrollPane(builder.getPanel()));
//        //sif.add(builder.getPanel());
//        add(sif, BorderLayout.CENTER);
        return builder.getPanel();
    }
//    void buildGUI() {
//        FormLayout layout = new FormLayout(
//                "right:70dlu, 5dlu, 70dlu, 5dlu, 60dlu, 5dlu, 60dlu, 5dlu, right:60dlu",
//                "p, 10dlu, p, 3dlu, p, 3dlu, p, 5dlu, p");
//        PanelBuilder builder = new PanelBuilder(layout);
//        builder.setDefaultDialogBorder();
//        builder.setOpaque(false);
//
//        CellConstraints cc = new CellConstraints();
//        builder.add(jscpDesc, cc.xyw(1, 1, 9));
//
//        builder.addSeparator("Summary", cc.xyw(1, 3, 9));
//
//        builder.addLabel("<html><tt>Start</tt><html>", cc.xyw(1, 5, 2));
//        builder.addLabel("<html><tt>Duration</tt><html>", cc.xy(3, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));
//        builder.addLabel("<html><tt>events</tt><html>", cc.xy(5, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));
//        builder.addLabel("<html><tt>Parameters</tt><html>", cc.xy(7, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));
//        builder.addLabel("<html><tt>Processes</tt><html>", cc.xy(9, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));
//
//        builder.add(jlblSTime, cc.xyw(1, 7, 2));
//        builder.add(jlblDuration, cc.xy(3, 7, CellConstraints.CENTER, CellConstraints.BOTTOM));
//        builder.add(jlblEvts, cc.xy(5, 7, CellConstraints.CENTER, CellConstraints.BOTTOM));
//        builder.add(jlblParams, cc.xy(7, 7, CellConstraints.CENTER, CellConstraints.BOTTOM));
//        builder.add(jlblProcs, cc.xy(9, 7, CellConstraints.CENTER, CellConstraints.BOTTOM));
//
//        SimpleInternalFrame sif = new SimpleInternalFrame("<html><b>"+theGraph.getId()+" summary</b></html>");
//        //sif.setPreferredSize(new Dimension(500, 150));
//        sif.add(Factory.createStrippedScrollPane(builder.getPanel()));
//        //sif.add(builder.getPanel());
//        add(sif, BorderLayout.CENTER);
//    }

    private javax.swing.JLabel jlblDuration;
    private javax.swing.JLabel jlblEvts;
    private javax.swing.JLabel jlblParams;
    private javax.swing.JLabel jlblProcs;
    private javax.swing.JLabel jlblSTime;
    private javax.swing.JScrollPane jscpDesc;
    private javax.swing.JTextArea jtxaDesc;
}
