/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser.Miscellaneous;

import Logic.SpecCollector;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Nina Gonschorreck
 */
public class MyCellRenderer extends JLabel implements ListCellRenderer {

    /**
     * This class will be used for the list of SpecCollectors from the
     * MultiDocument. The method shows which SingleSpec are correctly
     * transformed (green) or not (red).
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        //To change body of generated methods, choose Tools | Templates.
        SpecCollector spec = (SpecCollector) value;
        String s = (index + 1) + "      " + spec.getName();
        setText(s);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            //display red or green logic because of the translation has errors or not
            String[] errorlist = spec.getErrors();
            if (errorlist.length == 0) {
                String[] messagesList = spec.getMessages();
                if (messagesList.length == 0) {
                    setBackground(Color.green);
                } else {
                    setBackground(Color.ORANGE);
                }
            } else {
                setBackground(Color.red);
            }
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;

    }

}
