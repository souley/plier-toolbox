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
import java.awt.Font;

import java.util.Vector;
import java.util.Arrays;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import java.net.URL;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import nl.biggrid.plier.pqi.query.*;
import nl.biggrid.plier.pqi.jgoodies.*;

public class MainWindow extends JFrame
        implements ActionListener {

    class TimeHandlerAdapter extends FocusAdapter {
        JFrame owner = null;
        public TimeHandlerAdapter(JFrame anOwner) {
            owner = anOwner;
        }
        @Override
        public void focusGained(final FocusEvent event) {
            if (null == event.getOppositeComponent()) {
                return;
            }
            java.awt.EventQueue.invokeLater(new Runnable() {
                Object focusLoser = event.getOppositeComponent();
                public void run() {
                    if (dateChooser == null) {
                        dateChooser = new DateSelector(owner);
                    }
                    dateChooser.setVisible(true);

                    dateChooser.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosed(final WindowEvent evt) {
                            String selectedDate = dateChooser.getSelectedDate();
                            if (selectedDate != null) {
                                jtflTime.setText(selectedDate);
                            }
                        }
                    });
                }
            });
        }
    }
    protected static final int TEXTFIELD_WIDTH = 20;
    protected static final Dimension PREFERRED_SIZE =
            LookUtils.IS_LOW_RESOLUTION
            ? new Dimension(800, 700)
            : new Dimension(800, 700);
    private static final String COPYRIGHT =
            "\u00a9 2010-2011 PQI Benabdelkader & Madougou PCC-UVA. All Rights Reserved.";
    static final String TAB_TITLES[] = new String[]{"SUMMARY", "XML", "GRAPH", "DOT"};
    static final Font BOLD_FONT = new Font("Tahoma", Font.BOLD, 11);
    static final Font PLAIN_FONT = new Font("Tahoma", Font.PLAIN, 11);

    /** Describes optional settings of the JGoodies Looks. */
    private final Settings settings;    
    public static enum SearchType {

        NONE,
        NAME, USER, TIME, STATUS,
        NAME_USER, NAME_TIME, NAME_STATUS, USER_TIME, USER_STATUS, TIME_STATUS,
        NAME_USER_TIME, NAME_USER_STATUS, NAME_TIME_STATUS, USER_TIME_STATUS,
        NAME_USER_TIME_STATUS
    };
    private QueryManager searchManager = QueryManager.instance();
    private SearchType searchType = SearchType.NONE;
    private DBSettings dbProperties = null;
    private SimListEditor simList = null;
    private Vector<GraphSummary> currentMatches = new Vector<GraphSummary>();
    private DateSelector dateChooser = null;

    protected MainWindow(Settings settings) {
        this.settings = settings;
        configureUI();
        initComponents();
        buildGUI();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setupListeners();
        showDBSettings();
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent event) {
                shutdownEngine();
            }
        });
    }

    void shutdownEngine() {
        searchManager.shutdown();
    }

    public void shutdownApp() {
        shutdownEngine();
        setVisible(false);
        dispose();
        System.exit(0);
    }

    void initUsers(final Vector<String> users) {
        jcbxUser.setModel(new DefaultComboBoxModel(users));
    }

    void initStatus(final Vector<String> statuses) {
        jcbxStatus.setModel(new DefaultComboBoxModel(statuses));
    }

    void initTimeDivision() {
        if (jtflTime.getText().isEmpty()) {
            jtflTime.setText("ALL");
        }
    }

    void showErrorMsg(final String msg) {
        String decoratedMsg = "<html><font color=\"#ff0000\">" + msg + "</html>";
        JOptionPane.showMessageDialog(
                this,
                decoratedMsg,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    void showDBSettings() {
        if (dbProperties == null) {
            dbProperties = new DBSettings(this);
        }
        dbProperties.setVisible(true);
    }

    public void initGUI() {
//        Vector<String> users = new Vector<String>(Arrays.asList("ALL", QueryManager.USER_UNKNOWN));
        Vector<String> users = new Vector<String>(Arrays.asList("ALL"));
        users.addAll(searchManager.getUserList());
        initUsers(users);
        //Vector<String> statuses = new Vector<String>(Arrays.asList("ALL", QueryManager.STATUS_UNKNOWN));
        Vector<String> statuses = new Vector<String>(Arrays.asList("ALL"));
        statuses.addAll(searchManager.getStatusList());
        initStatus(statuses);
        initTimeDivision();
        jbtnFind.setEnabled(true);
    }

    void clearFields() {
        jtflName.setText("");
        jcbxUser.setSelectedItem("ALL");
        //if (jtflTime.getText().isEmpty()) {
            jtflTime.setText("ALL");
        //}
        jcbxStatus.setSelectedItem("ALL");
    }

    void setSearchType() {
        String wfName = jtflName.getText();
        String wfUser = (String) jcbxUser.getSelectedItem();
        String wfTimestamp = jtflTime.getText();
        String wfStatus = (String) jcbxStatus.getSelectedItem();
        if (wfName.isEmpty() && wfUser.equals("ALL") && wfTimestamp.equals("ALL") && wfStatus.equals("ALL")) {
            searchType = SearchType.NONE;
        } else if (!wfName.isEmpty() && wfUser.equals("ALL") && wfTimestamp.equals("ALL") && wfStatus.equals("ALL")) {
            searchType = SearchType.NAME;
        } else if (wfName.isEmpty() && !wfUser.equals("ALL") && wfTimestamp.equals("ALL") && wfStatus.equals("ALL")) {
            searchType = SearchType.USER;
        } else if (wfName.isEmpty() && wfUser.equals("ALL") && !wfTimestamp.equals("ALL") && wfStatus.equals("ALL")) {
            searchType = SearchType.TIME;
        } else if (wfName.isEmpty() && wfUser.equals("ALL") && wfTimestamp.equals("ALL") && !wfStatus.equals("ALL")) {
            searchType = SearchType.STATUS;
        } else if (!wfName.isEmpty() && !wfUser.equals("ALL") && wfTimestamp.equals("ALL") && wfStatus.equals("ALL")) {
            searchType = SearchType.NAME_USER;
        } else if (!wfName.isEmpty() && wfUser.equals("ALL") && !wfTimestamp.equals("ALL") && wfStatus.equals("ALL")) {
            searchType = SearchType.NAME_TIME;
        } else if (!wfName.isEmpty() && wfUser.equals("ALL") && wfTimestamp.equals("ALL") && !wfStatus.equals("ALL")) {
            searchType = SearchType.NAME_STATUS;
        } else if (wfName.isEmpty() && !wfUser.equals("ALL") && !wfTimestamp.equals("ALL") && wfStatus.equals("ALL")) {
            searchType = SearchType.USER_TIME;
        } else if (wfName.isEmpty() && !wfUser.equals("ALL") && wfTimestamp.equals("ALL") && !wfStatus.equals("ALL")) {
            searchType = SearchType.USER_STATUS;
        } else if (wfName.isEmpty() && wfUser.equals("ALL") && !wfTimestamp.equals("ALL") && !wfStatus.equals("ALL")) {
            searchType = SearchType.TIME_STATUS;
        } else if (!wfName.isEmpty() && !wfUser.equals("ALL") && !wfTimestamp.equals("ALL") && wfStatus.equals("ALL")) {
            searchType = SearchType.NAME_USER_TIME;
        } else if (!wfName.isEmpty() && !wfUser.equals("ALL") && wfTimestamp.equals("ALL") && !wfStatus.equals("ALL")) {
            searchType = SearchType.NAME_USER_STATUS;
        } else if (!wfName.isEmpty() && wfUser.equals("ALL") && !wfTimestamp.equals("ALL") && !wfStatus.equals("ALL")) {
            searchType = SearchType.NAME_TIME_STATUS;
        } else if (wfName.isEmpty() && !wfUser.equals("ALL") && !wfTimestamp.equals("ALL") && !wfStatus.equals("ALL")) {
            searchType = SearchType.USER_TIME_STATUS;
        } else if (!wfName.isEmpty() && !wfUser.equals("ALL") && !wfTimestamp.equals("ALL") && !wfStatus.equals("ALL")) {
            searchType = SearchType.NAME_USER_TIME_STATUS;
        }
    }

    void clearResults() {
        if (simList != null) {
            simList.invalidateSelections();
        }
    }

    Vector<GraphSummary> doSearch() {
        clearResults();
        Vector<GraphSummary> matches = new Vector<GraphSummary>();
        //long start = System.currentTimeMillis();
        if (searchType == SearchType.NONE) {
            matches = searchManager.searchAll();
        } else if (searchType == SearchType.NAME) {
            matches = searchManager.searchName(jtflName.getText());
        } else if (searchType == SearchType.USER) {
            matches = searchManager.searchUser((String) jcbxUser.getSelectedItem());
        } else if (searchType == SearchType.TIME) {
            matches = searchManager.searchTimestamp(jtflTime.getText());
        } else if (searchType == SearchType.STATUS) {
            matches = searchManager.searchStatus((String) jcbxStatus.getSelectedItem());
        } else if (searchType == SearchType.NAME_USER) {
            matches = searchManager.searchNameUser(jtflName.getText(), (String) jcbxUser.getSelectedItem());
        } else if (searchType == SearchType.NAME_TIME) {
            matches = searchManager.searchNameTime(jtflName.getText(), jtflTime.getText());
        } else if (searchType == SearchType.NAME_STATUS) {
            matches = searchManager.searchNameStatus(jtflName.getText(), (String) jcbxStatus.getSelectedItem());
        } else if (searchType == SearchType.USER_TIME) {
            matches = searchManager.searchUserTime((String) jcbxUser.getSelectedItem(), jtflTime.getText());
        } else if (searchType == SearchType.USER_STATUS) {
            matches = searchManager.searchUserStatus((String) jcbxUser.getSelectedItem(), (String) jcbxStatus.getSelectedItem());
        } else if (searchType == SearchType.TIME_STATUS) {
            matches = searchManager.searchTimeStatus(jtflTime.getText(), (String) jcbxStatus.getSelectedItem());
        } else if (searchType == SearchType.NAME_USER_TIME) {
            matches = searchManager.searchNameUserTime(jtflName.getText(), (String) jcbxUser.getSelectedItem(), jtflTime.getText());
        } else if (searchType == SearchType.NAME_USER_STATUS) {
            matches = searchManager.searchNameUserStatus(jtflName.getText(), (String) jcbxUser.getSelectedItem(), (String) jcbxStatus.getSelectedItem());
        } else if (searchType == SearchType.NAME_TIME_STATUS) {
            matches = searchManager.searchNameTimeStatus(jtflName.getText(), jtflTime.getText(), (String) jcbxStatus.getSelectedItem());
        } else if (searchType == SearchType.USER_TIME_STATUS) {
            matches = searchManager.searchUserTimeStatus((String) jcbxUser.getSelectedItem(), jtflTime.getText(), (String) jcbxStatus.getSelectedItem());
        } else if (searchType == SearchType.NAME_USER_TIME_STATUS) {
            matches = searchManager.searchNameUserTimeStatus(jtflName.getText(), (String) jcbxUser.getSelectedItem(), jtflTime.getText(), (String) jcbxStatus.getSelectedItem());
        }
        //System.out.println("Search Time = " + (System.currentTimeMillis() - start));
        return matches;
    }

    void showResults(final Vector<GraphSummary> matches) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                simList = new SimListEditor(matches, jtpTabs);
                jscpMatches.setViewportView(simList);
            }
        });
    }

    void doSearchAction() {
        if (searchManager.getFinder() == null) {
            showErrorMsg("You are currently not connected to any provenance store!");
            return;
        }
        setSearchType();
        currentMatches = doSearch();
        showResults(currentMatches);
    }

    public void setupListeners() {
        jbtnSettings.addActionListener(this);
        jbtnStats.addActionListener(this);

        jbtnClear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                clearFields();
            }
        });
        jbtnFind.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                doSearchAction();
            }
        });
        jtflTime.addFocusListener(new TimeHandlerAdapter(this));
    }

    void showStats(final JFrame owner) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                (new Metrics(currentMatches, owner)).setVisible(true);
            }
        });

    }

    private void initComponents() {
        jcbxUser = new javax.swing.JComboBox();
        jcbxStatus = new javax.swing.JComboBox();
        jtflTime = new javax.swing.JTextField(TEXTFIELD_WIDTH);
        jtflName = new javax.swing.JTextField(TEXTFIELD_WIDTH);

        jscpMatches = new javax.swing.JScrollPane();
    }

    private static Settings createDefaultSettings() {
        Settings settings = Settings.createDefault();
        return settings;
    }

    private void configureUI() {
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

    private void buildGUI() {
        setContentPane(buildContentPane());
        setTitle(getWindowTitle());
        setJMenuBar(
                createMenuBuilder().buildMenuBar(
                settings,
                createHelpActionListener(),
                createAboutActionListener()));
        setIconImage(readImageIcon("opm.png").getImage());
    }

    protected JGMenuBar createMenuBuilder() {
        return new JGMenuBar();
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildTopPanel(), BorderLayout.NORTH);
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        return panel;
    }

    private Component buildMainPanel() {
        JSplitPane mainSplit = UIFSplitPane.createStrippedSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                jscpMatches,
                buildTabbedPane());
        mainSplit.setDividerLocation(200);
        mainSplit.setPreferredSize(PREFERRED_SIZE);
        mainSplit.setOpaque(false);

        return mainSplit;
    }

    private Component buildTabbedPane() {
        jtpTabs = new JTabbedPane(SwingConstants.BOTTOM) {

            @Override
            public String getTitleAt(int index) {
                if (index == jtpTabs.getSelectedIndex()) {
                    jtpTabs.setFont(BOLD_FONT);
                    return TAB_TITLES[index];
                }
                jtpTabs.setFont(PLAIN_FONT);
                return TAB_TITLES[index];
            }
        };
        jtpTabs.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        jtpTabs.addTab(TAB_TITLES[0], null);
        jtpTabs.addTab(TAB_TITLES[1], null);
        jtpTabs.addTab(TAB_TITLES[2], null);
        jtpTabs.addTab(TAB_TITLES[3], null);

        return jtpTabs;
    }

    private JComponent buildTopPanel() {
        JPanel topPanel = new JPanel();

        FormLayout layout = new FormLayout(
                "right:pref, 40dlu, center:pref",
                "pref");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.add(buildCriteriaPanel(), cc.xy(1, 1));
        builder.add(buildButtonPanel(), cc.xy(3, 1));

        topPanel.add(builder.getPanel());
        return topPanel;
    }

    private JComponent buildCriteriaPanel() {
        FormLayout layout = new FormLayout(
                "right:pref, $lcgap, pref, 40dlu, right:pref, $lcgap, pref",
                "p, 3dlu, p, 15dlu, p, 3dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.addSeparator("Experiment", cc.xyw(1, 1, 3));
        builder.addLabel("Keyword", cc.xy(1, 3));
        builder.add(jtflName, cc.xy(3, 3));

        builder.addSeparator("Timestamp", cc.xyw(5, 1, 3));
        builder.addLabel("Value", cc.xy(5, 3));
        builder.add(jtflTime, cc.xy(7, 3));

        builder.addSeparator("Experimenter", cc.xyw(1, 5, 3));
        builder.addLabel("Name", cc.xy(1, 7));
        builder.add(jcbxUser, cc.xy(3, 7));

        builder.addSeparator("Experiment status", cc.xyw(5, 5, 3));
        builder.addLabel("Status", cc.xy(5, 7));
        builder.add(jcbxStatus, cc.xy(7, 7));

        JPanel panel = builder.getPanel();
        panel.setOpaque(false);

        return panel;
    }

    private JButton[] createButtons() {
        jbtnFind = new javax.swing.JButton("Search");
        jbtnFind.setEnabled(false);
        jbtnClear = new javax.swing.JButton("Reset");
        jbtnSettings = new javax.swing.JButton("Settings");
        jbtnStats = new javax.swing.JButton("Metrics");
        return new JButton[]{
                    jbtnFind,
                    jbtnClear,
                    jbtnSettings,
                    jbtnStats};
    }

    private JComponent buildButtonPanel() {
        ButtonStackBuilder builder = new ButtonStackBuilder();
        builder.addButton(createButtons());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(builder.getPanel(), BorderLayout.CENTER);

        panel.setOpaque(false);
        return panel;
    }

    protected String getWindowTitle() {
        return "Provenance Query Interface";
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = MainWindow.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    protected void locateOnScreen(Component component) {
        Dimension paneSize = component.getSize();
        Dimension screenSize = component.getToolkit().getScreenSize();
        component.setLocation(
                (screenSize.width - paneSize.width) / 2,
                (screenSize.height - paneSize.height) / 2);
    }

    protected ActionListener createHelpActionListener() {
        return null;
    }

    protected ActionListener createAboutActionListener() {
        return new AboutActionListener();
    }

    private final class AboutActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(
                    MainWindow.this,
                    "Provenance Query Interface\n\n" + COPYRIGHT + "\n\n");
        }
    }

    public void actionPerformed(final ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(jbtnSettings)) {
            showDBSettings();
        } else if (source.equals(jbtnStats)) {
            showStats(this);
        }
    }

    public static void main(String[] args) {
        Settings settings = createDefaultSettings();
//        String osName = System.getProperty("os.name");
//        String lafClassName = "";
//        if (osName.indexOf("Windows") != -1) {
//            lafClassName = Options.JGOODIES_WINDOWS_NAME;
//        } else {
//            lafClassName = Options.PLASTIC_NAME;
//        }
//        settings.setSelectedLookAndFeel(lafClassName);
        settings.setSelectedLookAndFeel(Options.PLASTIC_NAME);
        MainWindow instance = new MainWindow(settings);
        instance.setSize(PREFERRED_SIZE);
        instance.locateOnScreen(instance);
        instance.setVisible(true);
    }

    private javax.swing.JButton jbtnClear;
    private javax.swing.JButton jbtnFind;
    private javax.swing.JButton jbtnSettings;
    private javax.swing.JButton jbtnStats;
    private javax.swing.JComboBox jcbxStatus;
    private javax.swing.JComboBox jcbxUser;
    private javax.swing.JScrollPane jscpMatches;
    private javax.swing.JTextField jtflName;
    private javax.swing.JTextField jtflTime;
    private javax.swing.JTabbedPane jtpTabs;
}
