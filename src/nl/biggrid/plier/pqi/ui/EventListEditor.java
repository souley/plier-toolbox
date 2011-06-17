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

import nl.biggrid.plier.pqi.query.*;
import nl.biggrid.plier.pqi.query.Event;

public class EventListEditor extends JTable {

    static final String[] EVENT_COLUMNS = new String[]{"Event", "Process Name", "Timestamp"};

    /** Creates new form EventListEditor */
    public EventListEditor(final long gid) {
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
        Vector<Event> events = searchManager.getFinder().getGraphEvents(gid);
        DefaultTableModel model = new DefaultTableModel(EVENT_COLUMNS, 0);
        for (Event event : events) {
            model.addRow(new Object[]{event.getName(),
                        event.getSource(),
                        event.getTimestamp()
                    });
        }
        setModel(model);
    }
}
