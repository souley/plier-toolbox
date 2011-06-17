/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.plier.pqi.ui;

/**
 *
 * @author Souley
 */
import java.util.Vector;
import java.util.HashMap;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.Font;

import java.net.URL;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableRowSorter;

import nl.biggrid.plier.opm.OPMGraph;
import nl.biggrid.plier.pqi.query.*;
import nl.biggrid.plier.pqi.util.*;
import nl.biggrid.plier.tools.PersistenceManager;

public class SimListEditor extends javax.swing.JTable implements ChangeListener {

    static final String[] columnNames = {"Name", "Stat", "Time"};

    class SimTableModel extends AbstractTableModel {

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getRowCount() {
            return matches.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row < matches.size()) {
                switch (col) {
                    case 0:
                        return matches.elementAt(row).getId();
                    case 1:
                        return matches.elementAt(row).getStatus();
                    case 2:
                        return matches.elementAt(row).getDuration();
                }
            }
            return null;
        }
    }

    class StatusCellRenderer extends JLabel
            implements TableCellRenderer {

        JTable theTable = null;

        public StatusCellRenderer(JTable aTable) {
            theTable = aTable;
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
            setBackground(Color.white);
        }

        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isCellSelected,
                boolean cellHasFocus,
                int row,
                int col) {
            if (value instanceof String) {
                String status = (String) value;
                String iconName;
                if (status.equalsIgnoreCase("SUCCEEDED")) {
                    iconName = "succeed";
                } else if (status.equalsIgnoreCase("SUCCEEDED(f)")) {
                    iconName = "succeed2";
                } else if (status.equalsIgnoreCase("FAILED")) {
                    iconName = "failure";
                } else if (status.equalsIgnoreCase("FAILED(s)")) {
                    iconName = "failure2";
                } else if (status.equalsIgnoreCase("RUNNING")) {
                    iconName = "running";
                } else {
                    iconName = "unknown";
                }
                ImageIcon icon = imageMap.get(iconName);
                setIcon(icon);
                if (isCellSelected) {
                    setBackground(theTable.getSelectionBackground());
                } else {
                    setBackground(Color.white);
                }
            }
            return this;
        }
    }

    class SimRowSorter extends TableRowSorter<SimTableModel> {

        public SimRowSorter(final SimTableModel simTableModel) {
            super(simTableModel);
            setComparator(1, new StatusComparator());
            setComparator(2, new DurationComparator());
        }
    }
    private Vector<GraphSummary> matches = new Vector<GraphSummary>();
    private HashMap<String, ImageIcon> imageMap;
    private String[] statusStrings = {"succeed", "succeed2", "failure", "failure2", "running", "unknown"};
    // test
    JTabbedPane tabs = null;
    private XMLPane xmlTab = null;
    private XMLPane dotTab = null;
    private GraphView graphTab = null;
    private JPanel summaryTab = new JPanel();
    private int lastSelectedTab = 0;
    private OPMGraph selectedGraph = null;
    private HashMap<String, String> graphDot;
    private GraphSummary selectedGraphSummary = null;
    //end test
    /** Creates new form SimListEditor */
    public SimListEditor(final Vector<GraphSummary> someMatches, final JTabbedPane aTabbedPane) {
        super();
        matches = someMatches;
        tabs = aTabbedPane;
        tabs.addChangeListener(this);
        initComponents();
        initOtherThings();
        TableColumn statusColumn = getColumnModel().getColumn(1);
        if (statusColumn != null) {
            statusColumn.setCellRenderer(new StatusCellRenderer(this));
        }
        getSelectionModel().addListSelectionListener(new TableSelectionHandler());
        setRowSorter(new SimRowSorter(new SimTableModel()));
        graphDot = new HashMap<String, String>();
    }

    public int getLastSelectedTab() {
        return lastSelectedTab;
    }

    void initComponents() {
        setModel(new SimTableModel());
        setRowSelectionAllowed(true);
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        getColumnModel().getColumn(1).setPreferredWidth(30);
        getColumnModel().getColumn(2).setPreferredWidth(45);
    }

    void initOtherThings() {
        imageMap = new HashMap<String, ImageIcon>(statusStrings.length);
        for (int i = 0; i < statusStrings.length; i++) {
            imageMap.put(statusStrings[i], createImageIcon("/resources/images/" + statusStrings[i] + ".png"));
            if (imageMap.get(statusStrings[i]) != null) {
                imageMap.get(statusStrings[i]).setDescription(statusStrings[i]);
            }
        }
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            @Override
            public Font getFont() {
                return new Font(Font.MONOSPACED, Font.BOLD, 11);
            }
        };
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        String tooltip = "";
        int row = rowAtPoint(event.getPoint());
        int col = columnAtPoint(event.getPoint());
        if (row != -1 && col != -1) {
            Object value = getValueAt(row, col);
            if (value instanceof String) {
                tooltip = (String) value;
            }
        }
        return tooltip;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        URL imgURL = SimListEditor.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("SimListEditor.createImageIcon() Couldn't find file: " + path);
            return null;
        }
    }

    void updateTabs() {
        if (selectedGraph != null) {
            if (lastSelectedTab == 2) {
                String dotOut = graphDot.get(selectedGraph.getId());
                if (dotOut != null) {
                    graphTab = new GraphView(selectedGraph, dotOut);
                } else {
                    graphTab = new GraphView(selectedGraph);
                    graphDot.put(selectedGraph.getId(), graphTab.getDotOut());
                }
                tabs.setComponentAt(lastSelectedTab, graphTab);
            } else if (lastSelectedTab == 1) {
                xmlTab = new XMLPane();
                //xmlTab.setText(XMLHandler.toXML(selectedGraph));
                xmlTab.setText(PersistenceManager.toXML(selectedGraph));
                tabs.setComponentAt(lastSelectedTab, xmlTab);
            } else if (lastSelectedTab == 3) {
                dotTab = new XMLPane();
                if (graphTab == null) {
                    graphTab = new GraphView(selectedGraph);
                    graphDot.put(selectedGraph.getId(), graphTab.getDotOut());
                }
                dotTab.setText(graphTab.getDotOut());
                tabs.setComponentAt(lastSelectedTab, dotTab);
            } else {
                summaryTab = new GraphPanel(selectedGraphSummary, selectedGraph);
                tabs.setComponentAt(lastSelectedTab, summaryTab);
            }
            tabs.setSelectedIndex(lastSelectedTab);
        }
    }

    public void invalidateSelections() {
        selectedGraphSummary = null;
        selectedGraph = null;
    }

    public void stateChanged(ChangeEvent event) {
        lastSelectedTab = tabs.getSelectedIndex();
        updateTabs();
    }

    class TableSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                String graphId = (String) getValueAt(getSelectedRow(), 0);
                if (graphId != null) {
                    for (GraphSummary graph : matches) {
                        if (graphId.equalsIgnoreCase(graph.getId())) {
                            //selectedGraph = QueryManager.instance().getExperiment(graphId);
                            selectedGraphSummary = graph;
                            selectedGraph = QueryManager.instance().getExperiment(graph.getDbId());
                            updateTabs();
                            break;
                        }
                    }
                }
            }
        }
    }
}
