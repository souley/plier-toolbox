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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import nl.uva.pcc.pqi.util.StringEncrypter;
import nl.uva.pcc.pqi.util.StringEncrypter.EncryptionException;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import nl.uva.pcc.pqi.query.QueryManager;

public class DBSettings extends JDialog
        implements ActionListener, PropertyChangeListener {

    private static final Dimension PREFERRED_SIZE =
            LookUtils.IS_LOW_RESOLUTION
            ? new Dimension(400, 200)
            : new Dimension(400, 200);
    private static final int TEXTFILED_WIDTH = 15;
    public static final String DB_URL = "hibernate.connection.url" ;
    public static final String DB_USER = "hibernate.connection.username";
    public static final String DB_PWD = "hibernate.connection.password" ;

    private final String DB_SETTINGS_FILE = "db.cfg.properties";
    private Properties dbSettings = new Properties();
    private StringEncrypter encrypter = null;
    private String dbURL = "",  dbUser = "",  dbPwd = "";
    private ProgressMonitor progressMonitor;
    private SwingWorker<Void, Void> task;
    private boolean doAbort = false;
    private MainWindow mainWin = null;

    public DBSettings(final MainWindow parent) {
        super(parent, true);
        mainWin = parent;
        initComponents();
        build();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        try {
            encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME, "123456789012345678901234567890");
        } catch (EncryptionException ex) {
            ex.printStackTrace();
        }
        initSettings();
        setSize(PREFERRED_SIZE);
        setLocationRelativeTo(mainWin);
        jbtnOK.addActionListener(this);
        jbtnCancel.addActionListener(this);
    }

    public String getDBHost() {
        return dbURL;
    }

    public String getDBUser() {
        return dbUser;
    }

    public String getDBPwd() {
        return dbPwd;
    }

    public boolean isAborted() {
        return (doAbort || task.isCancelled());
    }

    public Properties getSettgins() {
        return dbSettings;
    }

    void initSettings() {
        try {
            FileInputStream in = new FileInputStream(DB_SETTINGS_FILE);
            dbSettings.load(in);
            in.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println("Cannot load settings file:\n" + fnfe.getMessage());
            return;
        } catch (IOException ioe) {
            System.err.println("IO exception :\n" + ioe.getMessage());
            return;
        }
        jtflHost.setText(dbSettings.getProperty(DB_URL));
        String encryption = dbSettings.getProperty("encryption");
        if (encryption.equalsIgnoreCase("on")) {
            try {
                jtflUser.setText(encrypter.decrypt(dbSettings.getProperty(DB_USER)));
                jpflPwd.setText(encrypter.decrypt(dbSettings.getProperty(DB_PWD)));
            } catch (EncryptionException ex) {
                ex.printStackTrace();
            }
        } else {
            jtflUser.setText(dbSettings.getProperty(DB_USER));
            jpflPwd.setText(dbSettings.getProperty(DB_PWD));
        }
    }

    void saveSettings() {
        dbSettings.setProperty("encryption", "on");
        dbSettings.setProperty(DB_URL, jtflHost.getText());
        try {
            dbSettings.setProperty(DB_USER, encrypter.encrypt(jtflUser.getText()));
            dbSettings.setProperty(DB_PWD, encrypter.encrypt(new String(jpflPwd.getPassword())));
        } catch (EncryptionException ex) {
            ex.printStackTrace();
        }
        try {
            FileOutputStream out = new FileOutputStream(DB_SETTINGS_FILE);
            dbSettings.store(out, "");
            out.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println("Cannot save settings file:\n" + fnfe.getMessage());
            return;
        } catch (IOException ioe) {
            System.err.println("IO exception :\n" + ioe.getMessage());
            return;
        }
    }

    void ConfirmPresets() {
        dbURL = jtflHost.getText();
        dbUser = jtflUser.getText();
        dbPwd = new String(jpflPwd.getPassword());
    }

    void closeWindow() {
        setVisible(false);
        dispose();
    }

    public void actionPerformed(final ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equalsIgnoreCase("ok")) {
            ConfirmPresets();
            showProgressMonitor();
            saveSettings();
            closeWindow();
        } else if (command.equalsIgnoreCase("cancel")) {
            doAbort = true;
            closeWindow();
            //mainWin.shutdownApp();
        }
    }

    private void initComponents() {
        jtflHost = new javax.swing.JTextField();
        jtflUser = new javax.swing.JTextField(TEXTFILED_WIDTH);
        jpflPwd = new javax.swing.JPasswordField(TEXTFILED_WIDTH);
    }

    private void build() {
        setContentPane(buildContentPane());
        setTitle(getWindowTitle());
        setIconImage(readImageIcon("advancedsettings.png").getImage());
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        return panel;
    }

    private Component buildMainPanel() {
        FormLayout layout = new FormLayout(
                "center:pref",
                "p, 10dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.add(buildSettingsPanel(), cc.xy(1, 1));
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
        builder.add(jtflHost, cc.xyw(3, 3, 5));

        builder.addSeparator("User", cc.xyw(1, 5, 7));
        builder.addLabel("Name", cc.xy(1, 7));
        builder.add(jtflUser, cc.xy(3, 7));
        builder.addLabel("Password", cc.xy(5, 7));
        builder.add(jpflPwd, cc.xy(7, 7));


        JPanel panel = builder.getPanel();
        panel.setOpaque(false);

        return panel;
    }

    private Component buildButtonsPanel() {
        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.setOpaque(false);
        jbtnOK = new javax.swing.JButton("OK");
        jbtnOK.setActionCommand("ok");
        jbtnCancel = new javax.swing.JButton("Cancel");
        jbtnCancel.setActionCommand("cancel");
        builder.addButton(new JButton[]{
                    jbtnOK, jbtnCancel});
        return builder.getPanel();
    }

    protected String getWindowTitle() {
        return "DB Settings";
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = DBSettings.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equalsIgnoreCase(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message = String.format("%d%% complete.\n", (progress == 1) ? 0 : progress);
            progressMonitor.setNote(message);
            if (progressMonitor.isCanceled()) {
                task.cancel(true);
            } else if (progress == 100) {
                mainWin.initGUI();
            }
        }
    }

    void showProgressMonitor() {
        progressMonitor = new ProgressMonitor(DBSettings.this,
                "Building internal data ...", "", 0, 100);
        progressMonitor.setProgress(0);
        if (dbURL.matches("(?i:.*oracle.*)")) {
            task = QueryManager.instance().initFinder(QueryManager.SearchEngine.ORACLE, this);
        } if (dbURL.matches("(?i:.*mysql.*)")) {
            task = QueryManager.instance().initFinder(QueryManager.SearchEngine.MYSQL, this);
        }
        if (task != null) {
            task.addPropertyChangeListener(this);
            task.execute();
        }
    }
    
    private javax.swing.JButton jbtnCancel;
    private javax.swing.JButton jbtnOK;
    private javax.swing.JPasswordField jpflPwd;
    private javax.swing.JTextField jtflHost;
    private javax.swing.JTextField jtflUser;
}
