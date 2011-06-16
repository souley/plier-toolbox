package nl.uva.pcc.pqi.util;

import nl.uva.pcc.pqi.jgoodies.Settings;
import nl.uva.pcc.pqi.jgoodies.SimpleInternalFrame;
import nl.uva.pcc.pqi.ui.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.*;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import nl.uva.pcc.pqi.jgoodies.*;

public class DemoFrame extends JDialog {

    protected static final Dimension PREFERRED_SIZE =
            LookUtils.IS_LOW_RESOLUTION
            ? new Dimension(580, 210)
            : new Dimension(580, 210);
    private static final String COPYRIGHT =
            "\u00a9 2001-2009 JGoodies Karsten Lentzsch. All Rights Reserved.";
    /** Describes optional settings of the JGoodies Looks. */
    private final Settings settings;

    /**
     * Constructs a <code>DemoFrame</code>, configures the UI,
     * and builds the content.
     */
    protected DemoFrame(Settings settings) {
        this.settings = settings;
        configureUI();
        //buildDC();
        buildET();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) {
        Settings settings = createDefaultSettings();
        String lafClassName = Options.PLASTICXP_NAME;
        settings.setSelectedLookAndFeel(lafClassName);
        DemoFrame instance = new DemoFrame(settings);
        instance.setSize(PREFERRED_SIZE);
        instance.locateOnScreen(instance);
        instance.setVisible(true);
    }

    private static Settings createDefaultSettings() {
        Settings settings = Settings.createDefault();

        // Configure the settings here.

        return settings;
    }

    /**
     * Configures the user interface; requests Swing settings and
     * JGoodies Looks options from the launcher.
     */
    private void configureUI() {
        // UIManager.put("ToolTip.hideAccelerator", Boolean.FALSE);

        Options.setDefaultIconSize(new Dimension(18, 18));

        Options.setUseNarrowButtons(settings.isUseNarrowButtons());

        // Global options
        Options.setTabIconsEnabled(settings.isTabIconsEnabled());
        UIManager.put(Options.POPUP_DROP_SHADOW_ENABLED_KEY,
                settings.isPopupDropShadowEnabled());

        // Swing Settings
        LookAndFeel selectedLaf = settings.getSelectedLookAndFeel();
        if (selectedLaf instanceof PlasticLookAndFeel) {
            PlasticLookAndFeel.setPlasticTheme(settings.getSelectedTheme());
            PlasticLookAndFeel.setTabStyle(settings.getPlasticTabStyle());
            PlasticLookAndFeel.setHighContrastFocusColorsEnabled(
                    settings.isPlasticHighContrastFocusEnabled());
        } else if (selectedLaf.getClass() == MetalLookAndFeel.class) {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        }

        // Work around caching in MetalRadioButtonUI
        JRadioButton radio = new JRadioButton();
        radio.getUI().uninstallUI(radio);
        JCheckBox checkBox = new JCheckBox();
        checkBox.getUI().uninstallUI(checkBox);

        try {
            UIManager.setLookAndFeel(selectedLaf);
        } catch (Exception e) {
            System.out.println("Can't change L&F: " + e);
        }

    }

    /**
     * Builds the <code>DemoFrame</code> using Options from the Launcher.
     */
    private void buildDB() {
        setContentPane(buildContentPane());
        setTitle(getWindowTitle());
        setIconImage(readImageIcon("settings.gif").getImage());
    }

    private void buildDC() {
        setContentPane(buildContentPane());
        setTitle(getWindowTitle());
        setIconImage(readImageIcon("eye_16x16.gif").getImage());
    }

    private void buildSP() {
        setContentPane(buildContentPane());
        setTitle(getWindowTitle());
        setIconImage(readImageIcon("eye_16x16.gif").getImage());
    }

    private void buildET() {
        setContentPane(buildContentPane());
        setTitle(getWindowTitle());
        setIconImage(readImageIcon("settings.gif").getImage());
    }

    /**
     * Builds and answers the content.
     */
    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        //panel.add(buildMainPanel(), BorderLayout.CENTER);
        //panel.add(buildDetailPanel(), BorderLayout.CENTER);
        panel.add(buildDetailPanel2(), BorderLayout.CENTER);
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

        //builder.addLabel("Last: ", cc.xy(1, 1));
        builder.addSeparator("Last time unit", cc.xyw(1, 1, 9));
        builder.add(new JRadioButton("hour"), cc.xy(1, 3));
        builder.add(new JRadioButton("day"), cc.xy(3, 3));
        builder.add(new JRadioButton("week"), cc.xy(5, 3));
        builder.add(new JRadioButton("month"), cc.xy(7, 3));
        builder.add(new JRadioButton("year"), cc.xy(9, 3));

        builder.addSeparator("Specific date", cc.xyw(1, 5, 9));
        builder.add(new JCheckBox("Enter a date"), cc.xy(1, 7));
        builder.add(new JTextField(), cc.xyw(3, 7, 7));

        return builder.getPanel();
    }

    private Component buildDetailPanel() {
        FormLayout layout = new FormLayout(
                "right:60dlu, 5dlu, 60dlu, 5dlu, 60dlu, 5dlu, 60dlu, 5dlu, right:60dlu",
                "p, 20dlu, p, 3dlu, p, 3dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        //builder.addSeparator("Experiment XXX", cc.xyw(1, 1, 9));
        builder.add(new JTextArea(), cc.xyw(1, 1, 9));

        builder.addSeparator("Summary", cc.xyw(1, 3, 9));

        builder.addLabel("Start", cc.xyw(1, 5, 2));
        builder.addLabel("Duration", cc.xy(3, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.addLabel("Events", cc.xy(5, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.addLabel("Processes", cc.xy(7, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.addLabel("Parameters", cc.xy(9, 5, CellConstraints.CENTER, CellConstraints.BOTTOM));

        builder.addLabel("Today", cc.xyw(1, 7, 2));
        builder.addLabel("22:22", cc.xy(3, 7, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.addLabel("23", cc.xy(5, 7, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.addLabel("2", cc.xy(7, 7, CellConstraints.CENTER, CellConstraints.BOTTOM));
        builder.addLabel("6", cc.xy(9, 7, CellConstraints.CENTER, CellConstraints.BOTTOM));

        //builder.addSeparator("Experiment graph", cc.xyw(1, 9, 9));
        //builder.add(new JRadioButton("events"), cc.xy(1, 11));
        //builder.add(new JRadioButton("processes"), cc.xy(3, 11));
        //builder.add(new JRadioButton("parameters"), cc.xy(5, 11));
        //builder.add(new JRadioButton("draw"), cc.xy(9, 11));
        //builder.add(new JButton("D&raw graph"), cc.xyw(1, 9, 2));

        JPanel panel = builder.getPanel();
//        panel.setBorder(new CompoundBorder(
//                new TitledBorder("Experiment XXX"),
//                new EmptyBorder(5, 5, 5, 5)));
        return panel;
    }

    private Component buildDetailPanel2() {
        //JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.BOTTOM);
        //tabbedPane.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        //tabbedPane.addTab("", Factory.createStrippedScrollPane(buildDetailPanel()));
        //tabbedPane.addTab("Tree", Factory.createStrippedScrollPane(buildTree()));
        //tabbedPane.addTab("Help", Factory.createStrippedScrollPane(buildHelp()));

        SimpleInternalFrame sif = new SimpleInternalFrame("Experiment XXX");
        sif.setPreferredSize(new Dimension(150, 100));
        //sif.add(tabbedPane);
        sif.add(Factory.createStrippedScrollPane(buildDetailPanel()));
        return sif;
    }

    // Tabbed Pane **********************************************************
    /**
     * Builds and answers the tabbed pane.
     */
    private Component buildMainPanel() {
        FormLayout layout = new FormLayout(
                "center:pref",
                "p, 10dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

//        builder.add(buildSettingsPanel(), cc.xy(1, 1));
        builder.add(buildChoicePanel(), cc.xy(1, 1));
        builder.add(buildButtonsPanel(), cc.xy(1, 3));

        return builder.getPanel();
    }

    private Component buildSettingsPanel() {
        FormLayout layout = new FormLayout(
                "right:pref, $lcgap, pref, 10dlu, right:pref, $lcgap, pref",
                "p, 3dlu, p, 7dlu, p, 3dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.addSeparator("Host", cc.xyw(1, 1, 7));
        builder.addLabel("URI", cc.xy(1, 3));
        builder.add(new JTextField(), cc.xyw(3, 3, 5));

        builder.addSeparator("User", cc.xyw(1, 5, 7));
        builder.addLabel("Name", cc.xy(1, 7));
        builder.add(new JTextField(15), cc.xy(3, 7));
        builder.addLabel("Password", cc.xy(5, 7));
        builder.add(new JPasswordField(15), cc.xy(7, 7));


        JPanel panel = builder.getPanel();
        panel.setOpaque(false);

        return panel;
    }

    private Component buildButtonsPanel() {
        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.setOpaque(false);
        builder.addButton(new JButton[]{
                    new JButton("OK"), new JButton("Cancel")});
        return builder.getPanel();
    }

    protected String getWindowTitle() {
        return "Simple Looks Demo";
    }


    // Helper Code **********************************************************************
    /**
     * Looks up and returns an icon for the specified filename suffix.
     */
    protected static ImageIcon readImageIcon(String filename) {
        URL url = DemoFrame.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    /**
     * Locates the given component on the screen's center.
     */
    protected void locateOnScreen(Component component) {
        Dimension paneSize = component.getSize();
        Dimension screenSize = component.getToolkit().getScreenSize();
        component.setLocation(
                (screenSize.width - paneSize.width) / 2,
                (screenSize.height - paneSize.height) / 2);
    }
}