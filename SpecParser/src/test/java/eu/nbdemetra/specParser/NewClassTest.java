package eu.nbdemetra.specParser;

import eu.nbdemetra.specParser.Miscellaneous.SpecificationPart;
import Logic.WinX12SpecSeparator;
import Logic.JDSpecSeparator;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.junit.Test;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Nina Gonschorreck
 */
public class NewClassTest {

    public NewClassTest() {
    }

//    @Test
    public void testMethode(){
        
        WinX12SpecSeparator jd = new WinX12SpecSeparator();
        jd.read_sigmalim(SpecificationPart.X11, "(, 2.5)");
        
    }
//    @Test
    public void testWriteX11() {
        //create Specification        
        X11Specification x11 = new X11Specification();
        x11.setMode(DecompositionMode.Additive);
        SeasonalFilterOption[] seasonal = {SeasonalFilterOption.Stable,
            SeasonalFilterOption.S3X1,
            SeasonalFilterOption.Msr,
            SeasonalFilterOption.X11Default};

        x11.setSeasonalFilters(seasonal);
        x11.setHendersonFilterLength(13);

        //test method writeX11
//        JDSpecSeparator winx13 = new JDSpecSeparator();
//        String erg = winx13.writeX11(x11);
//        System.out.println(erg);
    }

//    @Test
    public void testAlles(){
        //create Specification        
        X11Specification x11 = new X11Specification();
        x11.setMode(DecompositionMode.Additive);
        SeasonalFilterOption[] seasonal = {SeasonalFilterOption.Stable,
            SeasonalFilterOption.S3X1,
            SeasonalFilterOption.Msr,
            SeasonalFilterOption.X11Default
        };

        x11.setSeasonalFilters(seasonal);
        x11.setHendersonFilterLength(13);

//        JDSpecSeparator winx13 = new JDSpecSeparator();
//        String erg = winx13.writeX11(x11);
//        System.out.println(erg);

//Rueckrichtung
//        StringBuilder sb = new StringBuilder(erg);
//        sb.append("Arima{\n}");

//        erg = sb.toString();

        // erste Teilung: Trennung der Teilspezifikationen
//        String[] textSplit = erg.split("}");

        //Bezeichner: Inhalt
//        HashMap<String, String> singleSpec = new HashMap();
//
//        String[] tmp;
//
//        for (String item : textSplit) {
//
//            //zweite Teilung: Trennung nach Bezeichner und Inhalt
//            tmp = item.split("\\{");
//            singleSpec.put(tmp[0], tmp[1]);
////            tmp=null;
//        }

        WinX12SpecSeparator jd = new WinX12SpecSeparator();      
//        jd.buildSpec(erg);
//        jd.x11ToString();
 
    }  

    @Test
    public void testtest(){
    
        String test = "   }\n";
        String [] tmp = test.split("\\}");
        System.out.println("tmp[0]:"+tmp[0]);
        System.out.println("tmp[1]:"+tmp[1]);
    }
}
