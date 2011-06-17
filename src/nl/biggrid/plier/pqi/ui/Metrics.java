/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.plier.pqi.ui;

/**
 *
 * @author Souley
 */
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.LookUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;
import java.sql.Time;
import nl.biggrid.plier.pqi.query.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import nl.biggrid.plier.pqi.jgoodies.Factory;
import nl.biggrid.plier.pqi.util.*;

public class Metrics extends javax.swing.JDialog
        implements ItemListener {

    private static final Dimension PREFERRED_SIZE =
            LookUtils.IS_LOW_RESOLUTION
            ? new Dimension(580, 190)
            : new Dimension(580, 190);
    Vector<GraphSummary> matches;
    DurationComparator comparator = new DurationComparator();
    String strAvg = "";
    String strMin = "";
    String strMax = "";
    long avgTime = 0;

    public Metrics(final Vector<GraphSummary> someMatches, final JFrame parent) {
        super(parent, true);
        matches = someMatches;
        buildGUI();
        setSize(PREFERRED_SIZE);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        strAvg = getAvg();
        strMin = getMin();
        strMax = getMax();
        fillFields();
        jrbAvg.addItemListener(this);
        jrbMin.addItemListener(this);
        jrbMax.addItemListener(this);
        manageList();
    }

    String getAvg() {
        long milliSum = 0;
        for (GraphSummary aMatch : matches) {
            Time mTime = Time.valueOf(aMatch.getDuration());
            if (mTime != null) {
                milliSum += mTime.getTime();
            }
        }
        avgTime = milliSum / (matches.size() != 0 ? matches.size() : 1);
        return new Time(avgTime).toString();
    }

    String getMin() {
        String min = "99:99:99";
        for (GraphSummary aMatch : matches) {
            String mDuration = aMatch.getDuration();
            if (comparator.compare(min, mDuration) > 0) {
                min = mDuration;
            }
        }
        return min;
    }

    String getMax() {
        String max = "00:00:00";
        for (GraphSummary aMatch : matches) {
            String mDuration = aMatch.getDuration();
            if (comparator.compare(max, mDuration) < 0) {
                max = mDuration;
            }
        }
        return max;
    }

    void fillFields() {
        jlblSimCount.setText(Integer.toString(matches.size()));
        jlblAvgV.setText(strAvg);
        jlblMinV.setText(strMin);
        jlblMaxV.setText(strMax);
    }

    Vector<String> getSimMatchingTime(final String aDuration) {
        Vector<String> tMatches = new Vector<String>();
        for (GraphSummary aMatch : matches) {
            if (comparator.compare(aDuration, aMatch.getDuration()) == 0) {
                tMatches.add(aMatch.getId());
            }
        }
        return tMatches;
    }

    Vector<String> getSimClose2Avg() {
        Vector<String> close2Avg = new Vector<String>();
        int valWin = 1000;
        while (close2Avg.size() < 1) {
            for (GraphSummary aMatch : matches) {
                if (Math.abs(avgTime - Time.valueOf(aMatch.getDuration()).getTime()) <= valWin) {
                    if (!close2Avg.contains(aMatch.getId())) {
                        close2Avg.add(aMatch.getId());
                    }
                }
            }
            valWin *= 2;
        }
        return close2Avg;
    }

    void manageList() {
        if (jrbAvg.isSelected()) {
            jlSims.setListData(getSimClose2Avg());
        } else if (jrbMin.isSelected()) {
            jlSims.setListData(getSimMatchingTime(strMin));
        } else if (jrbMax.isSelected()) {
            jlSims.setListData(getSimMatchingTime(strMax));
        }
    }

    public void itemStateChanged(final ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            manageList();
        }
    }

    private void buildGUI() {
        bgRadios = new javax.swing.ButtonGroup();
        jlblSimCount = new javax.swing.JLabel();
        jlblSimCount.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        jlSims = new javax.swing.JList();
        jlSims.setBackground(new java.awt.Color(255, 255, 224));
        jlblAvgV = new javax.swing.JLabel();
        jrbAvg = new javax.swing.JRadioButton("<html><tt>Show averages</tt></html>");
        jlblMinV = new javax.swing.JLabel();
        jrbMin = new javax.swing.JRadioButton("<html><tt>Show minima</tt></html>");
        jlblMaxV = new javax.swing.JLabel();
        jrbMax = new javax.swing.JRadioButton("<html><tt>Show maxima</tt></html>");

        bgRadios.add(jrbAvg);
        bgRadios.add(jrbMin);
        bgRadios.add(jrbMax);

        setLayout(new BorderLayout());
        add(buildCenterPanel(), BorderLayout.CENTER);
        //jrbMin.setSelected(true);
    }

    private Component buildCenterPanel() {
        FormLayout layout = new FormLayout(
                "p, 5dlu, p, 10dlu, p, $lcgap, p",
                "p, 10dlu, p, 5dlu, p, 3dlu, p, 3dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.addLabel("<html><tt>Simulation Count</tt></html>", cc.xy(1, 1));
        builder.add(jlblSimCount, cc.xy(3, 1));

        builder.addSeparator("Time metrics", cc.xyw(1, 3, 7));

        builder.addLabel("<html><tt>Minimum duration</tt></html>", cc.xy(1, 5));
        builder.add(jlblMinV, cc.xy(3, 5));
        builder.add(jrbMin, cc.xy(5, 5));

        builder.add(Factory.createStrippedScrollPane(jlSims), cc.xywh(7, 5, 1, 5));

        builder.addLabel("<html><tt>Average duration</tt></html>", cc.xy(1, 7));
        builder.add(jlblAvgV, cc.xy(3, 7));
        builder.add(jrbAvg, cc.xy(5, 7));

        builder.addLabel("<html><tt>Maximum duration</tt></html>", cc.xy(1, 9));
        builder.add(jlblMaxV, cc.xy(3, 9));
        builder.add(jrbMax, cc.xy(5, 9));

        return builder.getPanel();
    }
    private javax.swing.ButtonGroup bgRadios;
    private javax.swing.JList jlSims;
    private javax.swing.JLabel jlblSimCount;
    private javax.swing.JLabel jlblAvgV;
    private javax.swing.JLabel jlblMaxV;
    private javax.swing.JLabel jlblMinV;
    private javax.swing.JRadioButton jrbAvg;
    private javax.swing.JRadioButton jrbMax;
    private javax.swing.JRadioButton jrbMin;
}
