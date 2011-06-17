/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.plier.pqi.ui;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

/**
 *
 * @author Souley
 */
public class XMLPane extends JScrollPane {

    private JTextPane xmlPane = null;
    static private Font XML_FONT = new Font("Tahoma", Font.BOLD, 12);

    class XmlEditorKit extends StyledEditorKit {

        private ViewFactory xmlViewFactory;

        public XmlEditorKit() {
            xmlViewFactory = new XmlViewFactory();
        }

        @Override
        public ViewFactory getViewFactory() {
            return xmlViewFactory;
        }

        @Override
        public String getContentType() {
            return "text/xml";
        }
    }

    public XMLPane() {
        // Set editor kit
        xmlPane = new JTextPane();
        xmlPane.setEditorKitForContentType("text/xml", new XmlEditorKit());
        xmlPane.setContentType("text/xml");
        xmlPane.setFont(XML_FONT);
    }

    public void setText(final String text) {
        xmlPane.setText(text);
        setViewportView(xmlPane);
    }
}
