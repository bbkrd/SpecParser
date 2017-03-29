/* 
 * Copyright 2016 Deutsche Bundesbank
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.nbdemetra.specParser.Miscellaneous;

import Logic.SpecCollector;
import java.awt.Color;
import java.awt.Component;
import java.util.Collection;
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
            Collection<TranslationInfo> error_types = spec.getTranslation_messages().values();
            if (error_types.contains(TranslationInfo.ERROR)) {
                setBackground(Color.red);
            } else if (error_types.contains(TranslationInfo.WARNING1)) {
                setBackground(Color.ORANGE);
            } else if (error_types.contains(TranslationInfo.WARNING2)) {
                setBackground(Color.YELLOW);
            } else {
                setBackground(Color.GREEN);
            }
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
        
    }
    
}
