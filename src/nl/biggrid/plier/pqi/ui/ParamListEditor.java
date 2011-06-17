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

import nl.biggrid.plier.opm.Artifact;

import nl.biggrid.plier.pqi.query.*;

public class ParamListEditor extends JTable {

    static final String[] PARAM_COLUMNS = new String[]{"Parameter Id", "Value"};

    public ParamListEditor(final long gid) {
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
        Vector<Artifact> params = searchManager.getFinder().getGraphParameters(gid);
        DefaultTableModel model = new DefaultTableModel(PARAM_COLUMNS, 0);
        for (Artifact param : params) {
            model.addRow(new Object[]{  param.getId(),
                                        param.getValue()
                                     }
            );
        }
        setModel(model);
    }
}
