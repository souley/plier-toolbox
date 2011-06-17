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
import java.awt.Color;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import nl.biggrid.plier.opm.Process;

import nl.biggrid.plier.pqi.query.*;

public class ProcessListEditor extends JTable {
    static final String[] PROCESS_COLUMNS = new String[]{"Process Id"};

    public ProcessListEditor(final long gid) {
        super();
        setFillsViewportHeight(true);
        setBackground(new Color(255, 255, 204));
        fillByQuery(gid);
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            @Override
            public Font getFont() {
                return new Font(Font.MONOSPACED, Font.ITALIC, 11);
            }
        };
    }

    void fillByQuery(final long gid) {
        QueryManager searchManager = QueryManager.instance();
        Vector<Process> processes = searchManager.getFinder().getGraphProcesses(gid);
        DefaultTableModel model = new DefaultTableModel(PROCESS_COLUMNS, 0);
        for (Process process : processes) {
            model.addRow(new Object[]{  process.getId() }
            );
        }
        setModel(model);
    }
}
