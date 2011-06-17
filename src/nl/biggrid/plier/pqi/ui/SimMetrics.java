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
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Formatter;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
//import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class SimMetrics extends JDialog {

    static enum QTY_TYPE {

        AREA_MIN, AREA_AVG, AREA_MAX,
        FLOW_MIN, FLOW_AVG, FLOW_MAX,
        PRESSURE_MIN, PRESSURE_AVG, PRESSURE_MAX
    }
    private static final String SIM_UNIT_PREFIX = new String("sol");
    private static final String SIM_UNIT_EXT = new String("dat");
    private String simName;
    String dirName;
    private double aMin = Double.MAX_VALUE, aAvg = 0, aMax = Double.MIN_VALUE;
    private double fMin = Double.MAX_VALUE, fAvg = 0, fMax = Double.MIN_VALUE;
    private double pMin = Double.MAX_VALUE, pAvg = 0, pMax = Double.MIN_VALUE;
    private JFrame parentFrame = null;

    public SimMetrics(final JFrame parent, final String aDirName, final String aSimName, final int x, final int y) {
        super();
        dirName = aDirName;
        simName = aSimName;
        buildGUI();
        pack();
        //setLocation(x, y);
        //this.setl
        //setClosable(true);
        setLocationRelativeTo(parent);
        parentFrame = parent;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        fillFields();
    }

    void closeFrame() {
        setVisible(false);
        dispose();
    }

    private HashMap<QTY_TYPE, Double> getFileMeasurements(final File aFile) {
        final int AREA_COL = 3;
        final int FLOW_COL = 4;
        final int PRESSURE_COL = 5;
        int lineCount = 0;
        double amin = Double.MAX_VALUE, aavg = 0, amax = Double.MIN_VALUE;
        double fmin = Double.MAX_VALUE, favg = 0, fmax = Double.MIN_VALUE;
        double pmin = Double.MAX_VALUE, pavg = 0, pmax = Double.MIN_VALUE;

        HashMap<QTY_TYPE, Double> fmMap = new HashMap<QTY_TYPE, Double>();
        BufferedReader input = null;
        String line = null;
        try {
            input = new BufferedReader(new FileReader(aFile));
            line = input.readLine();
            line = input.readLine();
            while (line != null) {
                String[] items = line.trim().split("\\s+");
                if (AREA_COL < items.length) {
                    try {
                        double acur = Double.parseDouble(items[AREA_COL]);
                        if (amin > acur) {
                            amin = acur;
                        }
                        aavg += acur;
                        if (amax < acur) {
                            amax = acur;
                        }
                    } catch (NumberFormatException nfe) {
                    }
                }
                if (FLOW_COL < items.length) {
                    try {
                        double fcur = Double.parseDouble(items[FLOW_COL]);
                        if (fmin > fcur) {
                            fmin = fcur;
                        }
                        favg += fcur;
                        if (fmax < fcur) {
                            fmax = fcur;
                        }
                    } catch (NumberFormatException nfe) {
                    }
                }
                if (PRESSURE_COL < items.length) {
                    try {
                        double pcur = Double.parseDouble(items[PRESSURE_COL]);

                        if (pmin > pcur) {
                            pmin = pcur;
                        }
                        pavg += pcur;
                        if (pmax < pcur) {
                            pmax = pcur;
                        }
                    } catch (NumberFormatException nfe) {
                    }
                }
                lineCount++;
                line = input.readLine();
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Cannot find file: '" + aFile.getAbsolutePath() + "'");
        } catch (IOException ex) {
            System.err.println("IOException: '" + ex.getMessage() + "'");
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                System.err.println("Cannot close file: '" + aFile.getAbsolutePath() + "'");
            }
        }
        aavg /= (lineCount > 1 ? lineCount : 1);
        favg /= (lineCount > 1 ? lineCount : 1);
        pavg /= (lineCount > 1 ? lineCount : 1);
        fmMap.put(QTY_TYPE.AREA_MIN, Double.valueOf(amin));
        fmMap.put(QTY_TYPE.AREA_AVG, Double.valueOf(aavg));
        fmMap.put(QTY_TYPE.AREA_MAX, Double.valueOf(amax));
        fmMap.put(QTY_TYPE.FLOW_MIN, Double.valueOf(fmin));
        fmMap.put(QTY_TYPE.FLOW_AVG, Double.valueOf(favg));
        fmMap.put(QTY_TYPE.FLOW_MAX, Double.valueOf(fmax));
        fmMap.put(QTY_TYPE.PRESSURE_MIN, Double.valueOf(pmin));
        fmMap.put(QTY_TYPE.PRESSURE_AVG, Double.valueOf(pavg));
        fmMap.put(QTY_TYPE.PRESSURE_MAX, Double.valueOf(pmax));
        return fmMap;
    }

    private void doMeasurements() {
        String simPath = System.getProperty("user.dir");
        simPath += dirName;
        File simDir = new File(simPath);
        FilenameFilter datFilter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.startsWith(SIM_UNIT_PREFIX) && name.endsWith(SIM_UNIT_EXT) && name.indexOf("____") == -1;
            }
        };
        File[] datFiles = simDir.listFiles(datFilter);
        if (datFiles == null) {
            showErrorMsg("No data available for '" + simName + "'");
            return;
        }
        HashMap<QTY_TYPE, Double> mMap;
        for (int i = 0; i < datFiles.length; i++) {
            mMap = getFileMeasurements(datFiles[i]);
            if (aMin > mMap.get(QTY_TYPE.AREA_MIN).doubleValue()) {
                aMin = mMap.get(QTY_TYPE.AREA_MIN).doubleValue();
            }
            if (fMin > mMap.get(QTY_TYPE.FLOW_MIN).doubleValue()) {
                fMin = mMap.get(QTY_TYPE.FLOW_MIN).doubleValue();
            }
            if (pMin > mMap.get(QTY_TYPE.PRESSURE_MIN).doubleValue()) {
                pMin = mMap.get(QTY_TYPE.PRESSURE_MIN).doubleValue();
            }
            
            aAvg += mMap.get(QTY_TYPE.AREA_AVG).doubleValue();
            fAvg += mMap.get(QTY_TYPE.FLOW_AVG).doubleValue();
            pAvg += mMap.get(QTY_TYPE.PRESSURE_AVG).doubleValue();

            if (aMax < mMap.get(QTY_TYPE.AREA_MAX).doubleValue()) {
                aMax = mMap.get(QTY_TYPE.AREA_MAX).doubleValue();
            }
            if (fMax < mMap.get(QTY_TYPE.FLOW_MAX).doubleValue()) {
                fMax = mMap.get(QTY_TYPE.FLOW_MAX).doubleValue();
            }
            if (pMax < mMap.get(QTY_TYPE.PRESSURE_MAX).doubleValue()) {
                pMax = mMap.get(QTY_TYPE.PRESSURE_MAX).doubleValue();
            }
        }
        aAvg /= (datFiles.length > 1 ? datFiles.length : 1);
        fAvg /= (datFiles.length > 1 ? datFiles.length : 1);
        pAvg /= (datFiles.length > 1 ? datFiles.length : 1);
    }

    void fillFields() {
        doMeasurements();

        Formatter amnFormatter = new Formatter();
        amnFormatter.format("%1.5E", aMin);
        jtflAMn.setText(amnFormatter.toString());
        Formatter aaFormatter = new Formatter();
        aaFormatter.format("%1.5E", aAvg);
        jtflAA.setText(aaFormatter.toString());
        Formatter amxFormatter = new Formatter();
        amxFormatter.format("%1.5E", aMax);
        jtflAMx.setText(amxFormatter.toString());

        Formatter fmnFormatter = new Formatter();
        fmnFormatter.format("%1.5E", fMin);
        jtflFMn.setText(fmnFormatter.toString());
        Formatter faFormatter = new Formatter();
        faFormatter.format("%1.5E", fAvg);
        jtflFA.setText(faFormatter.toString());
        Formatter fmxFormatter = new Formatter();
        fmxFormatter.format("%1.5E", fMax);
        jtflFMx.setText(fmxFormatter.toString());

        Formatter pmnFormatter = new Formatter();
        pmnFormatter.format("%1.5E", 0.75 * pMin);
        jtflPMn.setText(pmnFormatter.toString());
        Formatter paFormatter = new Formatter();
        paFormatter.format("%1.5E", 0.75 * pAvg);
        jtflPA.setText(paFormatter.toString());
        Formatter pmxFormatter = new Formatter();
        pmxFormatter.format("%1.5E", 0.75 * pMax);
        jtflPMx.setText(pmxFormatter.toString());
    }

    private void buildGUI() {
        setContentPane(buildContentPane());
        setTitle(simName);
        //setFrameIcon(readImageIcon("settings.gif"));
        setIconImage(readImageIcon("light.gif").getImage());
    }

    /**
     * Builds and answers the content.
     */
    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildCenterPanel(), BorderLayout.CENTER);
        return panel;
    }

    private Component buildCenterPanel() {
        FormLayout layout = new FormLayout(
                "p, $lcgap, p, 10dlu, p, $lcgap, p, 10dlu, p, $lcgap, p",
                "p, 3dlu, p, 10dlu, p, 3dlu, p, 10dlu, p, 3dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        jtflAA = new JTextField(10);
        jtflAA.setEditable(false);
        jtflAA.setBorder(null);
        jtflAA.setHorizontalAlignment(JTextField.CENTER);
        jtflAMn = new JTextField(10);
        jtflAMn.setEditable(false);
        jtflAMn.setBorder(null);
        jtflAMn.setHorizontalAlignment(JTextField.CENTER);
        jtflAMx = new JTextField(10);
        jtflAMx.setEditable(false);
        jtflAMx.setBorder(null);
        jtflAMx.setHorizontalAlignment(JTextField.CENTER);

        jtflFA = new JTextField(10);
        jtflFA.setEditable(false);
        jtflFA.setBorder(null);
        jtflFA.setHorizontalAlignment(JTextField.CENTER);
        jtflFMn = new JTextField(10);
        jtflFMn.setEditable(false);
        jtflFMn.setBorder(null);
        jtflFMn.setHorizontalAlignment(JTextField.CENTER);
        jtflFMx = new JTextField(10);
        jtflFMx.setEditable(false);
        jtflFMx.setBorder(null);
        jtflFMx.setHorizontalAlignment(JTextField.CENTER);

        jtflPA = new JTextField(10);
        jtflPA.setEditable(false);
        jtflPA.setBorder(null);
        jtflPA.setHorizontalAlignment(JTextField.CENTER);
        jtflPMn = new JTextField(10);
        jtflPMn.setEditable(false);
        jtflPMn.setBorder(null);
        jtflPMn.setHorizontalAlignment(JTextField.CENTER);
        jtflPMx = new JTextField(10);
        jtflPMx.setEditable(false);
        jtflPMx.setBorder(null);
        jtflPMx.setHorizontalAlignment(JTextField.CENTER);

        builder.addSeparator("Area", cc.xyw(1, 1, 11));
        builder.addLabel("<html><tt><u>Min</u></tt></html>", cc.xy(1, 3));
        builder.add(jtflAMn, cc.xy(3, 3));
        builder.addLabel("<html><tt><u>Avg</u></tt></html>", cc.xy(5, 3));
        builder.add(jtflAA, cc.xy(7, 3));
        builder.addLabel("<html><tt><u>Max</u></tt></html>", cc.xy(9, 3));
        builder.add(jtflAMx, cc.xy(11, 3));

        builder.addSeparator("Flow", cc.xyw(1, 5, 11));
        builder.addLabel("<html><tt><u>Min</u></tt></html>", cc.xy(1, 7));
        builder.add(jtflFMn, cc.xy(3, 7));
        builder.addLabel("<html><tt><u>Avg</u></tt></html>", cc.xy(5, 7));
        builder.add(jtflFA, cc.xy(7, 7));
        builder.addLabel("<html><tt><u>Max</u></tt></html>", cc.xy(9, 7));
        builder.add(jtflFMx, cc.xy(11, 7));

        builder.addSeparator("Pressure", cc.xyw(1, 9, 11));
        builder.addLabel("<html><tt><u>Min</u></tt></html>", cc.xy(1, 11));
        builder.add(jtflPMn, cc.xy(3, 11));
        builder.addLabel("<html><tt><u>Avg</u></tt></html>", cc.xy(5, 11));
        builder.add(jtflPA, cc.xy(7, 11));
        builder.addLabel("<html><tt><u>Max</u></tt></html>", cc.xy(9, 11));
        builder.add(jtflPMx, cc.xy(11, 11));

        JPanel panel = builder.getPanel();
        return panel;
    }


    // Helper Code **********************************************************************
    /**
     * Looks up and returns an icon for the specified filename suffix.
     */
    void showErrorMsg(final String msg) {
        String decoratedMsg = "<html><font color=\"#ff0000\">" + msg + "</html>";
        JOptionPane.showMessageDialog(
                parentFrame,
                decoratedMsg,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
    protected static ImageIcon readImageIcon(String filename) {
        URL url = SimMetrics.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }
    private JTextField jtflAA;
    private JTextField jtflAMn;
    private JTextField jtflAMx;
    private JTextField jtflFA;
    private JTextField jtflFMn;
    private JTextField jtflFMx;
    private JTextField jtflPA;
    private JTextField jtflPMn;
    private JTextField jtflPMx;
}
