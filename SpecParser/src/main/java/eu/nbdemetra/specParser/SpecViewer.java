/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.tss.documents.TsDocument;
import ec.ui.view.tsprocessing.DefaultProcessingViewer;
import Logic.SpecCollector;
import ec.tstoolkit.algorithm.IActiveProcDocument;
import ec.tstoolkit.algorithm.IProcSpecification;
import static ec.ui.view.tsprocessing.DefaultProcessingViewer.BUTTON_APPLY;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * This class shows the transformation of a SingleSpec. The WinX13Spec is in the
 * front, the JD+Spec on the right and on the bottom the error messages because
 * of the translation between the both specs.
 *
 */
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
        setSpecificationsVisible(true);

        winX12Text = new JTextArea();
        errormessage = new JTextArea();

        errormessage.setEditable(false);

        winX12Text.setText("empty");
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
            errormessage.append("\nMESSAGES\n"
                    + "********\n");
            for (String a : this.spec.getMessages()) {
                errormessage.append(a + "\n");
            }

            setDocument(spec.getJDSpec());
        }
        return this;
    }

    public void pressApplyButton() {

        IActiveProcDocument doc = getDocument();
        IProcSpecification pspec = specDescriptor.getCore();
        doc.setSpecification(pspec.clone());
        setDirty(null, false);
        firePropertyChange(BUTTON_APPLY, null, null);
        refreshView();
        if (isHeaderVisible()) {
            refreshHeader();
        }
    }
}
