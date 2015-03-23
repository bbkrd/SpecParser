/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.tss.documents.TsDocument;
import ec.tss.sa.documents.X13Document;
import ec.ui.view.tsprocessing.DefaultProcessingViewer;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

/**
 * This class shows the transformation of a SingleSpec. The WinX13Spec is in the
 * front, the JD+Spec on the right and on the bottom the error messages because
 * of the translation between the both specs.
 *
 */
public class SpecViewer extends DefaultProcessingViewer<TsDocument> {

    private JTextArea winX13Text;
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

        winX13Text = new JTextArea();
        errormessage = new JTextArea();

        errormessage.setFocusable(false);

        winX13Text.setText("empty");
        errormessage.setText("error messages");

        scrollText = new JScrollPane(winX13Text);
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

    public String getWinX13Text() {

        return winX13Text.getText();
    }

    public SpecViewer refresh(SpecCollector spec) {

        this.spec = spec;
        winX13Text.setText(this.spec.getWinX13Spec());
        errormessage.setText("ERRORS\n"
                + "******\n");
        for (String a : this.spec.getErrors()) {
            errormessage.append(a + "\n");
        }
        
        setDocument(spec.getJDSpec());
        
        return this;
    }
}
