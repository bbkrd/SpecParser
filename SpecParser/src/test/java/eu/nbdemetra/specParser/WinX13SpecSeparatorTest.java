/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 *
 * @author Nina Gonschorreck
 */
public class WinX13SpecSeparatorTest {

    public WinX13SpecSeparatorTest() {
    }

//    @Test
    public void testSpecBuild() {

        String winX13Text = "x11 { mode = add\n\t#nix\n\t seasonalma = 3x15 }\n arima{ model=nix\n\t}\nregression{ \n}\n";

        WinX13SpecSeparator sep = new WinX13SpecSeparator();
        sep.buildSpec(winX13Text);
        System.out.println("Fehlerliste: ");
        for (String error : sep.getErrorList()) {
            System.out.println(error);
        }
    }

    @Test
    public void teste_sigmalim() {

        String test = "(a, b);";
        WinX13SpecSeparator sep = new WinX13SpecSeparator();

        sep.read_sigmalim(SpecificationPart.X11, test);
        for(String t: sep.getErrorList()){
            System.out.println(t);
        }
    }
}
