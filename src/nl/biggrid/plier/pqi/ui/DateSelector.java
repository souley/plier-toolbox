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
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.toedter.calendar.JDateChooser;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.LookUtils;

public class DateSelector extends javax.swing.JDialog
        implements ActionListener, ItemListener {

    protected static final Dimension PREFERRED_SIZE =
            LookUtils.IS_LOW_RESOLUTION
            ? new Dimension(330, 210)
            : new Dimension(330, 210);

    String selectedDate = null;
    JDateChooser dateChooser = null;

    /** Creates new form DateSelector */
    public DateSelector(final JFrame owner) {
        super(owner, true);
        initComponents();
        syncSelection();

        setLocationRelativeTo(owner);
        setSize(PREFERRED_SIZE);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        jrbNone.addItemListener(this);
        jrbHour.addItemListener(this);
        jrbDay.addItemListener(this);
        jrbWeek.addItemListener(this);
        jrbMonth.addItemListener(this);
        jrbYear.addItemListener(this);

        jbtnOK.addActionListener(this);
        jbtnKO.addActionListener(this);
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    void initComponents() {
        bgTimeIntervals = new javax.swing.ButtonGroup();
        jrbHour = new javax.swing.JRadioButton("Hour");
        jrbDay = new javax.swing.JRadioButton("Day");
        jrbWeek = new javax.swing.JRadioButton("Week");
        jrbMonth = new javax.swing.JRadioButton("Month");
        jrbYear = new javax.swing.JRadioButton("Year");
        jrbNone = new javax.swing.JRadioButton("Choose date");

        setTitle("Date Chooser");

        bgTimeIntervals.add(jrbHour);
        bgTimeIntervals.add(jrbDay);
        bgTimeIntervals.add(jrbWeek);
        bgTimeIntervals.add(jrbMonth);
        bgTimeIntervals.add(jrbYear);
        bgTimeIntervals.add(jrbNone);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-d");

        jrbHour.setSelected(true);
        build();
    }

    private void build() {
        setContentPane(buildContentPane());
        setIconImage(readImageIcon("calendar.gif").getImage());
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        return panel;
    }

    private Component buildChoicePanel() {
        FormLayout layout = new FormLayout(
                "right:pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref",
                "p, 5dlu, p, 10dlu, p, 5dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.addSeparator("Last time unit", cc.xyw(1, 1, 9));
        builder.add(jrbHour, cc.xy(1, 3));
        builder.add(jrbDay, cc.xy(3, 3));
        builder.add(jrbWeek, cc.xy(5, 3));
        builder.add(jrbMonth, cc.xy(7, 3));
        builder.add(jrbYear, cc.xy(9, 3));

        builder.addSeparator("Specific date", cc.xyw(1, 5, 9));
        builder.add(jrbNone, cc.xy(1, 7));
        builder.add(dateChooser, cc.xyw(3, 7, 7));

        return builder.getPanel();
    }

    private Component buildMainPanel() {
        FormLayout layout = new FormLayout(
                "center:pref",
                "p, 10dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.add(buildChoicePanel(), cc.xy(1, 1));
        builder.add(buildButtonsPanel(), cc.xy(1, 3));

        return builder.getPanel();
    }

    private Component buildButtonsPanel() {
        jbtnOK = new javax.swing.JButton("Validate");
        jbtnKO = new javax.swing.JButton("Cancel");

        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.setOpaque(false);
        builder.addButton(new JButton[]{
                    jbtnOK, jbtnKO});
        return builder.getPanel();
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = DateSelector.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    void syncSelection() {
        if (jrbNone.isSelected()) {
            dateChooser.setEnabled(true);
        } else {
            dateChooser.setEnabled(false);
        }
    }

    void validateDate() {
        if (jrbNone.isSelected()) {
            JComponent uiComp = dateChooser.getDateEditor().getUiComponent();
            if (uiComp instanceof JTextField) {
                selectedDate = ((JTextField) uiComp).getText();
            } else {
                selectedDate = dateChooser.getDate().toString();
            }
        } else if (jrbHour.isSelected()) {
            selectedDate = new String("Last hour");
        } else if (jrbDay.isSelected()) {
            selectedDate = new String("Last day");
        } else if (jrbWeek.isSelected()) {
            selectedDate = new String("Last week");
        } else if (jrbMonth.isSelected()) {
            selectedDate = new String("Last month");
        } else if (jrbYear.isSelected()) {
            selectedDate = new String("Last year");
        }
    }

    void invalidateDate() {
        selectedDate = null;
    }

    void closeWindow() {
        setVisible(false);
        dispose();
    }

    public void actionPerformed(final ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equalsIgnoreCase("Validate")) {
            validateDate();
            closeWindow();
        } else if (command.equalsIgnoreCase("Cancel")) {
            invalidateDate();
            closeWindow();
        }

    }

    public void itemStateChanged(final ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            syncSelection();
        }
    }
    private javax.swing.ButtonGroup bgTimeIntervals;
    private javax.swing.JButton jbtnKO;
    private javax.swing.JButton jbtnOK;
    private javax.swing.JRadioButton jrbDay;
    private javax.swing.JRadioButton jrbHour;
    private javax.swing.JRadioButton jrbMonth;
    private javax.swing.JRadioButton jrbNone;
    private javax.swing.JRadioButton jrbWeek;
    private javax.swing.JRadioButton jrbYear;
}
