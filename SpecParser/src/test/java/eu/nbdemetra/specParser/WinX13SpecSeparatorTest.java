/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import static junit.framework.TestCase.assertEquals;
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

//    @Test
    public void testeX11() {

        String winX11text = "x11{ mode = add\n"
                + "trendma = 13\n"
                + "sigmalim =(1.25  2.75)\n" 
                + "seasonalma = s3x9\n"
                + "title = \"3x9 moving average, mad\"\n"
                + "appendfcst = yes\n"
                + "appendbcst = no\n"
                + "final = user\n"
                + "print = ( brief +b2)\n"
                + "save = ( d10 d11 )\n"
                + "savelog = ( m7 q )}";

        System.out.println(winX11text);
        WinX13SpecSeparator sep = new WinX13SpecSeparator();
        sep.buildSpec(winX11text);
        X13Specification erg = sep.getResult();

        for (String s : sep.getErrorList()) {
            System.out.println(s);
        }

        X11Specification x11 = new X11Specification();
        x11.setMode(DecompositionMode.Additive);
        x11.setSeasonalFilter(SeasonalFilterOption.S3X9);
        x11.setHendersonFilterLength(13);
        x11.setSigma(1.25, 2.75);

        X13Specification x13 = new X13Specification();
        x13.setX11Specification(x11);

        assertEquals(x13, erg);
    }

//    @Test
    public void teste_sigmalim() {

        double lowerSigma = 0.6;
        double upperSigma = 2.45;

        String winX13Text = " ( " + lowerSigma + "   " + upperSigma + " ) ;";
        WinX13SpecSeparator sep = new WinX13SpecSeparator();
        sep.read_sigmalim(SpecificationPart.X11, winX13Text);
        X13Specification erg = sep.getResult();

        X13Specification x13 = new X13Specification();
        x13.getX11Specification().setLowerSigma(lowerSigma);
        x13.getX11Specification().setUpperSigma(upperSigma);

        assertEquals(erg, x13);
    }

//    @Test
    public void teste_mode() {

        String winX13Text = " mult;";
        WinX13SpecSeparator sep = new WinX13SpecSeparator();
        sep.read_mode(SpecificationPart.X11, winX13Text);
        X13Specification erg = sep.getResult();

        X13Specification x13 = new X13Specification();
        x13.getX11Specification().setMode(DecompositionMode.Multiplicative);

        assertEquals(erg, x13);
    }

//    @Test
    public void teste_sesonalma() {

        SeasonalFilterOption[] filter = {SeasonalFilterOption.S3X15, SeasonalFilterOption.S3X15, SeasonalFilterOption.Stable};

        StringBuilder sb = new StringBuilder("(");
        for (SeasonalFilterOption s : filter) {
            sb.append(s.toString()).append("\t");
        }
        sb.append(")");
        String winX13Text = sb.toString();
        WinX13SpecSeparator sep = new WinX13SpecSeparator();
        sep.read_seasonalma(SpecificationPart.X11, winX13Text);
        X13Specification erg = sep.getResult();

        X13Specification x13 = new X13Specification();
        x13.getX11Specification().setSeasonalFilters(filter);

        assertEquals(x13, erg);
    }

//    @Test
    public void teste_trendma() {

        int trend = 13;

        String winX13Text = " " + trend + ";";
        WinX13SpecSeparator sep = new WinX13SpecSeparator();
        sep.read_trendma(SpecificationPart.X11, winX13Text);
        X13Specification erg = sep.getResult();

        X13Specification x13 = new X13Specification();
        x13.getX11Specification().setHendersonFilterLength(trend);

        assertEquals(x13, erg);
    }

    @Test
    public void teste_model(){
        
        String model = " (2,1,[3])(1 1 1);";
        String ar = "(, 0.7f,)";
        String ma ="(0.2f,);";
        WinX13SpecSeparator sep = new WinX13SpecSeparator();
        sep.read_model(SpecificationPart.ARIMA, model);
        sep.read_ar(SpecificationPart.ARIMA, ar);
        sep.read_ma(SpecificationPart.ARIMA, ma);
        
        for(Parameter p : sep.getResult().getRegArimaSpecification().getArima().getTheta()){
            System.out.println("p: "+p.getValue()+" "+p.getType());
        }
        
    }
}
