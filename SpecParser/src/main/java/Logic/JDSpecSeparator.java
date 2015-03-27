/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Logic;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import java.util.ArrayList;

/**
 *
 * @author Nina Gonschorreck
 */
public class JDSpecSeparator {
    
    private RegArimaSpecification regSpec_;
    private X11Specification x11Spec_;
    private ArrayList<String> errors;
    
   
    
    //Konstruktor fuer X13Specification
//    public ToWinX13(X13Specification x13){
//    
//        this.regSpec_ = x13.getRegArimaSpecification();
//        this.x11Spec_ = x13.getX11Specification();
//        //Benchmarking doesn't exist in WinX13
//
//    }
    
    //Konstruktor fuer einzelne Specs in X13
    public JDSpecSeparator(RegArimaSpecification regSpec, X11Specification x11spec){
    
        this.regSpec_ = regSpec;
        this.x11Spec_ = x11spec;
    }

    
    //Leerer Konstruktor (fuer die Testklasse)
    public JDSpecSeparator(){
    }

//    public void buildSpec(){
//           
//        //1. X11Specification
//        String x11 = writeX11(x11Spec_);
//        System.out.println(x11);
//        
//        // 2. RegArimaSpecification
//        
//        
//    }
    
    
    public String writeX11(X11Specification x11){
    /*** Result is a string in the following form:
    *    
    *   x11{    mode = ...
    *           seasonalma = (...)
    *           trendma = ...
    *           sigmalim = (...     ...)
    *       }
    *       
    ***/
        
        StringBuilder sb = new StringBuilder("x11{\n");
        
        String tmp;
        
        //1. Mode
        tmp = transformMode(x11.getMode());
        if(!tmp.isEmpty())
            sb.append("\t").append(tmp).append("\n");
       
        //2. Seasonal filters
        tmp = transformSeasonal(x11.getSeasonalFilters());
        if(!tmp.isEmpty())
            sb.append("\t").append(tmp).append("\n");
        
        //3. Trend-/Hendersonfilter
        tmp = transformTrend(x11.getHendersonFilterLength());
        if(!tmp.isEmpty())
            sb.append("\t").append(tmp).append("\n");
        
        //4. Limits of sigma
        tmp = transformSigma(x11.getLowerSigma(), x11.getUpperSigma());
        if(!tmp.isEmpty())
            sb.append("\t").append(tmp).append("\n");
        
        sb.append("\t").append("print = ( D10 )").append("\n");
        
        sb.append("}");
        
        return sb.toString();
    }
    
    private String transformMode(DecompositionMode mode){
        
    // Notiz: Gedanken machen über
                // 1) JD+: DecompositionMode.Undefined
                // 2) WinX13: pseudoadd
                // 3) Default-Einstellungen: mult(winX13), JD+?
        
        StringBuilder sb = new StringBuilder("mode = ");
        
        //There are 4 differnt modi in JD+. 
        switch(mode){
            case Additive:
                sb.append("add");
                break;
            case Multiplicative:
                sb.append("mult");
                break;
            case LogAdditive:
                sb.append("logadd");
                break;
            case Undefined:
                //sinnvoll? Default von Undefined in JD+?
                System.err.println("Warning: Mode=Undefined doesn't exist in WinX13. Solution: mode = mult");
                sb.append("mult");
                break;
            default: 
                /*erzeugt leeren String, in der Spec wird kein mode angegeben*/
                
                // fuer den Fall, dass ein neuer Modus hinzugefuegt wird
                System.err.println("Warning: Mode is unknown. Solution: without mode");
                sb=new StringBuilder();
                //Vermutung: In WinX13 wird mult berechnet(?)
        }
        
        return sb.toString();
    }
    
    private String transformSeasonal(SeasonalFilterOption[] filters){
    //Ueberlegung: was wenn neue Filter hinzugefügt, die nicht in WinX13 existieren? Hier nicht beruecksichtigt
        
        StringBuilder sb = new StringBuilder("seasonalma = ( ");
        
        for(SeasonalFilterOption item : filters){
           if(item.equals(SeasonalFilterOption.Msr)){
               System.err.println("Warning: Msr-Filter doesn't exist in WinX13. Solution: seasonalma = x11default");
               //sinnvoll?: stattdessen Default anhängen
               sb.append(SeasonalFilterOption.X11Default);
           }else{
           //Normalfall: gleiche Filterbezeichnung, daher einfach dranhängen
               sb.append(item);
           }   
        //Leerzeichen einfügen
           sb.append(" ");
        }
        
        sb.append(")");
        
        return sb.toString();
    }    
    
    private String transformTrend(int t){
    
        String rslt;
        
        //Pruefung der Angabe:
        //      1) 1 < t < 101
        //      2) ungerade
        
        if(t>1 && t<101 && t%2!=0){
            rslt = "trendma = "+t;           
        }else{
            System.err.println("Warning: Trendfilter isn't correct. Solution: without trendma");
            rslt="";
        }
        
        return rslt;
    }
    
    private String transformSigma(double s1, double s2){
    
        //reicht Leerzeichen dazwischen? oder tab???
        return "sigmalim = ( "+s1+"\t"+s2 + " )";
    }

    
    public String[] getErrorList() {
        return errors.toArray(new String[errors.size()]);
    }
}
