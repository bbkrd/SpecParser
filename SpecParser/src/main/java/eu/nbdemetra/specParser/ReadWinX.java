/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.nbdemetra.specParser;

import java.util.HashMap;

/**
 *
 * @author Nina Gonschorreck
 */
public class ReadWinX {
    
    
    public HashMap<String, String> generateSingleSpecList(String s){
    
        //result is a list with an indicator and the content of the single Specification
        HashMap<String, String> singleSpec = new HashMap<String, String>();
        
        // first split: split the whole spec in the single specification
        String[] textSplit = s.split("}");
        
        //second split: split all single specs into indicator and content
        String[] tmp;
        for (String item: textSplit){
            tmp = item.split("\\{");
            singleSpec.put(tmp[0], tmp[1]);
            tmp=null;
        }
                
       return singleSpec;
    }
    
//    Methode zum Entfernen von kommentaren
}
