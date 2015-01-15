/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.tss.sa.documents.SaDocument;

/**
 *
 * @author Nina Gonschorreck
 */

public class SpecCollector {
/**
 *   This class colletcs both specifications (WinX13, JD+) of a SingleSpec. 
 *
 *   @param winX13SpecText saves the content of a WinX13Specification in a form of *.SPC file 
 *   @param jdSpec saves the JDemetra+ specification as an object of SaDocument
 *   @param errors collects the errors by translation of specifications
 */
    
    private String winX13SpecText;
    private SaDocument jdSpec;
    private String[] errors;

    public void setWinX13Spec(String text) {
        this.winX13SpecText = text;
    }

    public String getWinX13Spec() {
        return winX13SpecText;
    }

    public void setJDSpec(SaDocument x13) {
        this.jdSpec = x13;
    }

    public SaDocument getJDSpec() {
        return jdSpec;
    }

    public String[] getErrors() {
        if(errors==null){
         errors = new String[]{}; 
        }
        return errors;
    }

    public void translate(TranslationTo_Type type) {

        if (type == TranslationTo_Type.JDSpec) {
            // Translation from WinX13Spec to JDemetra+Spec
            WinX13SpecSeparator separator = new WinX13SpecSeparator();
            separator.buildSpec(winX13SpecText);
            jdSpec=separator.getResult();
            errors = separator.getErrorList();
        } else {
            //Translation from JDemetra+Spec to WinX13Spec
            JDSpecSeparator separator = new JDSpecSeparator();
            //...
        }

    }
}
