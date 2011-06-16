/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.pcc.pqi.ui;

/**
 *
 * @author Souley
 */
import nl.uva.pcc.pqi.jgoodies.Settings;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;

public class JGMenuBar {

	/**
	 * Builds, configures, and returns the menubar. Requests HeaderStyle,
	 * look-specific BorderStyles, and Plastic 3D hint from Launcher.
	 */
	JMenuBar buildMenuBar(
        Settings settings,
        ActionListener helpActionListener,
        ActionListener aboutActionListener) {

		JMenuBar bar = new JMenuBar();
		bar.putClientProperty(Options.HEADER_STYLE_KEY,
							  settings.getMenuBarHeaderStyle());
		bar.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY,
							  settings.getMenuBarPlasticBorderStyle());
		bar.putClientProperty(WindowsLookAndFeel.BORDER_STYLE_KEY,
							  settings.getMenuBarWindowsBorderStyle());
		bar.putClientProperty(PlasticLookAndFeel.IS_3D_KEY,
							  settings.getMenuBar3DHint());

		bar.add(buildHelpMenu(helpActionListener, aboutActionListener));
		return bar;
	}

	/**
	 * Builds and and returns the help menu.
	 */
	private JMenu buildHelpMenu(
        ActionListener helpActionListener,
        ActionListener aboutActionListener) {

		JMenu menu = createMenu("Help", 'H');

		JMenuItem item;
        item = createMenuItem("Help Contents", readImageIcon("help.gif"), 'H');
        if (helpActionListener != null) {
    		item.addActionListener(helpActionListener);
        }
        menu.add(item);
        if (!isAboutInOSMenu()) {
            menu.addSeparator();
            item = createMenuItem("About", 'a');
            item.addActionListener(aboutActionListener);
            menu.add(item);
        }

		return menu;
	}

    // Factory Methods ********************************************************

    protected JMenu createMenu(String text, char mnemonic) {
        JMenu menu = new JMenu(text);
        menu.setMnemonic(mnemonic);
        return menu;
    }


    protected JMenuItem createMenuItem(String text) {
        return new JMenuItem(text);
    }


    protected JMenuItem createMenuItem(String text, char mnemonic) {
        return new JMenuItem(text, mnemonic);
    }

    protected JMenuItem createMenuItem(String text, char mnemonic, KeyStroke key) {
        JMenuItem menuItem = new JMenuItem(text, mnemonic);
        menuItem.setAccelerator(key);
        return menuItem;
    }


    protected JMenuItem createMenuItem(String text, Icon icon) {
        return new JMenuItem(text, icon);
    }


    protected JMenuItem createMenuItem(String text, Icon icon, char mnemonic) {
        JMenuItem menuItem = new JMenuItem(text, icon);
        menuItem.setMnemonic(mnemonic);
        return menuItem;
    }


    protected JMenuItem createMenuItem(String text, Icon icon, char mnemonic, KeyStroke key) {
        JMenuItem menuItem = createMenuItem(text, icon, mnemonic);
        menuItem.setAccelerator(key);
        return menuItem;
    }

    /**
     * Checks and answers whether the about action has been moved to an
     * operating system specific menu, e.g. the OS X application menu.
     *
     * @return true if the about action is in an OS-specific menu
     */
    protected boolean isAboutInOSMenu() {
        return false;
    }

    /**
     * Looks up and returns an icon for the specified filename suffix.
     */
    private ImageIcon readImageIcon(String filename) {
        URL url = getClass().getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

}
