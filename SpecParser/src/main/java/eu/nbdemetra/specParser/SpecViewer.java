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
package eu.nbdemetra.specParser;

import ec.tss.documents.TsDocument;
import ec.ui.view.tsprocessing.DefaultProcessingViewer;
import Logic.SpecCollector;
import ec.tstoolkit.algorithm.IActiveProcDocument;
import ec.tstoolkit.algorithm.IProcSpecification;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * This class shows the transformation of a SingleSpec. The WinX12Spec is in the
 * front, the JD+Spec on the right and on the bottom the error messages because
 * of the translation between the both specs.
 *
 */
//public class SpecViewer extends DefaultProcessingViewer<TsDocument> {
public class SpecViewer extends DefaultProcessingViewer<TsDocument> {

    private JTextArea winX12Text;
    private JTextArea errormessage;
    private JScrollPane scrollText;
    private JScrollPane scrollError;
    private JSplitPane split;

    private SpecCollector spec;

    // FACTORY METHODS >
    public static SpecViewer create(SpecCollector spec) {
        SpecViewer viewer = new SpecViewer(Type.APPLY, spec);
        return viewer;
    }

    public SpecViewer(Type type, final SpecCollector spec) {
        super(type);
        this.spec = spec;
        remove(splitter);

//        doc =  spec.getJDSpec();
        winX12Text = new JTextArea();
        errormessage = new JTextArea();

        errormessage.setEditable(false);

        winX12Text.setText("Please load a file");
        errormessage.setText("error messages");

        scrollText = new JScrollPane(winX12Text);
        scrollError = new JScrollPane(errormessage);

        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollText, scrollError);
        split.setDividerLocation(0.5);
        split.setResizeWeight(0.5);

        add(split);

        if (spec.getJDSpec() != null) {
            setDocument(spec.getJDSpec());
        }

//        setSpecificationsVisible(true);
//        specPanel.setEnabled(false);
    }

    public SpecCollector getSpecCollector() {
        return spec;
    }

    public String getWinX12Text() {

        return winX12Text.getText();
    }

    public Document getWinDoc() {
        return winX12Text.getDocument();
    }

    public SpecViewer refresh(SpecCollector spec) {

        if (spec != null) {
            this.spec = spec;
            winX12Text.setText(this.spec.getWinX12Spec());
            errormessage.setText("ERRORS\n"
                    + "******\n");
            for (String a : this.spec.getErrors()) {
                errormessage.append(a + "\n");
            }
            errormessage.append("\nWARNINGS\n"
                    + "********\n");
            for (String a : this.spec.getWarnings()) {
                errormessage.append(a + "\n");
            }
            errormessage.append("\nMESSAGES\n"
                    + "********\n");
            for (String a : this.spec.getMessages()) {
                errormessage.append(a + "\n");
            }

//            errormessage.append("\nTESTS\n"
//                    + "********\n");
//            for (String a : this.spec.getTests()) {
//                errormessage.append(a + "\n");
//            }
            setDocument(this.spec.getJDSpec());
           
            Component c = ((BorderLayout) specPanel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
            if (c != null) {
                specPanel.remove(c);
            }
            specPanel.revalidate();
        }
        return this;
    }

//    public void pressApplyButton() {
//
//        IActiveProcDocument doc = getDocument();
//        IProcSpecification pspec = specDescriptor.getCore();
//        doc.setSpecification(pspec.clone());
//        setDirty(false);
//
//        firePropertyChange(BUTTON_APPLY, null, null);
//        refreshView();
//        if (isHeaderVisible()) {
//            refreshHeader();
//        }
//
//    }
}
