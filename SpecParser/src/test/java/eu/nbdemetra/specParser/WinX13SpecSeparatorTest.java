/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import Logic.WinX12SpecSeparator;
import eu.nbdemetra.specParser.Miscellaneous.SpecificationPart;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import static junit.framework.TestCase.assertEquals;

/**
 *
 * @author Nina Gonschorreck
 */
public class WinX13SpecSeparatorTest {

    public WinX13SpecSeparatorTest() {
    }

//    @Test
    public void testSpecBuild() {

        String winX13Text = "x11 {sigmalim = (1.6\n 2.1)}";

        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.buildSpec(winX13Text);
        System.out.println("Fehlerliste: ");
        for (String error : sep.getErrorList()) {
            System.out.println(error);
        }
    }
    
//    @Test
    public void teste_sigmalim() {

        double lowerSigma = 0.6;
        double upperSigma = 2.5;

        String winX13Text = " ( " + lowerSigma + "   " + upperSigma + " ) ;";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_sigmalim(SpecificationPart.X11, winX13Text);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getX11Specification().setLowerSigma(lowerSigma);
        x13.getX11Specification().setUpperSigma(upperSigma);

        assertEquals(erg, x13);
    }

//    @Test
    public void teste_mode() {

        String winX13Text = " mult;";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_mode(SpecificationPart.X11, winX13Text);
        X13Specification erg = sep.getResult().getSpecification();

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
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_seasonalma(SpecificationPart.X11, winX13Text);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getX11Specification().setSeasonalFilters(filter);

        assertEquals(x13, erg);
    }

//    @Test
    public void teste_trendma() {

        int trend = 13;

        String winX13Text = " " + trend + ";";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_trendma(SpecificationPart.X11, winX13Text);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getX11Specification().setHendersonFilterLength(trend);

        assertEquals(x13, erg);
    }

//    @Test
    public void teste_model() {

        String model = " (2,1,[3])(1 1 1);";
        String ar = "(, 0.7f,);";
        String ma = "(0.2f,);";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_model(SpecificationPart.ARIMA, model);
        sep.read_ar(SpecificationPart.ARIMA, ar);
        sep.read_ma(SpecificationPart.ARIMA, ma);
        X13Specification erg = sep.getResult().getSpecification();

        Parameter[] phi = {new Parameter(-0.1, ParameterType.Undefined),
            new Parameter(-0.7, ParameterType.Fixed)};

        Parameter[] theta = {new Parameter(0.0, ParameterType.Undefined),
            new Parameter(0.0, ParameterType.Undefined),
            new Parameter(-0.2, ParameterType.Fixed)};

        Parameter[] bphi = {new Parameter(-0.1, ParameterType.Undefined)};

        Parameter[] btheta = {new Parameter(-0.1, ParameterType.Undefined)};

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getArima().setP(2);
        x13.getRegArimaSpecification().getArima().setPhi(phi);
        x13.getRegArimaSpecification().getArima().setD(1);
        x13.getRegArimaSpecification().getArima().setQ(3);
        x13.getRegArimaSpecification().getArima().setTheta(theta);
        x13.getRegArimaSpecification().getArima().setBP(1);
        x13.getRegArimaSpecification().getArima().setBPhi(bphi);
        x13.getRegArimaSpecification().getArima().setBD(1);
        x13.getRegArimaSpecification().getArima().setBQ(1);
        x13.getRegArimaSpecification().getArima().setBTheta(btheta);

//        assertEquals(x13.getRegArimaSpecification().getArima().getPhi(), erg.getRegArimaSpecification().getArima().getPhi());
        assertEquals(x13, erg);
    }

//    @Test
    public void teste_acceptdefault() {
        String s = "no";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_acceptdefault(SpecificationPart.AUTOMDL, s);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getAutoModel().setAcceptDefault(false);

        assertEquals(x13, erg);

        s = "yes";
        sep.read_acceptdefault(SpecificationPart.AUTOMDL, s);
        erg = sep.getResult().getSpecification();

        x13.getRegArimaSpecification().getAutoModel().setAcceptDefault(true);

        assertEquals(x13, erg);
    }

//    @Test
    public void teste_checkmu() {
        String s = "no";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_checkmu(SpecificationPart.AUTOMDL, s);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getAutoModel().setCheckMu(false);

        assertEquals(x13, erg);

        s = "yes";
        sep.read_checkmu(SpecificationPart.AUTOMDL, s);
        erg = sep.getResult().getSpecification();

        x13.getRegArimaSpecification().getAutoModel().setCheckMu(true);

        assertEquals(x13, erg);
    }

    public void teste_mixed() {

        String s = " no;";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_mixed(SpecificationPart.AUTOMDL, s);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getAutoModel().setMixed(false);

        assertEquals(x13, erg);

        s = "yes;";
        sep.read_mixed(SpecificationPart.AUTOMDL, s);
        erg = sep.getResult().getSpecification();

        x13.getRegArimaSpecification().getAutoModel().setMixed(true);

        assertEquals(x13, erg);
    }

//    @Test
    public void teste_ljungboxlimit() {

        String s = " 0.95;";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_ljungboxlimit(SpecificationPart.AUTOMDL, s);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getAutoModel().setLjungBoxLimit(0.95);

        assertEquals(x13, erg);
    }

//    @Test
    public void teste_armalimit() {

        String s = " 0.95;";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_armalimit(SpecificationPart.AUTOMDL, s);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getAutoModel().setArmaSignificance(0.95);

        assertEquals(x13, erg);
    }

//    @Test
    public void teste_balanced() {
        String s = "no";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_balanced(SpecificationPart.AUTOMDL, s);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getAutoModel().setBalanced(false);

        assertEquals(x13, erg);

        s = "yes";
        sep.read_balanced(SpecificationPart.AUTOMDL, s);
        erg = sep.getResult().getSpecification();

        x13.getRegArimaSpecification().getAutoModel().setBalanced(true);

        assertEquals(x13, erg);
    }
//    @Test
    public void teste_bhrinitial() {
        String s = "no";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_hrinitial(SpecificationPart.AUTOMDL, s);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getAutoModel().setHannanRissanen(false);

        assertEquals(x13, erg);

        s = "yes";
        sep.read_hrinitial(SpecificationPart.AUTOMDL, s);
        erg = sep.getResult().getSpecification();

        x13.getRegArimaSpecification().getAutoModel().setHannanRissanen(true);

        assertEquals(x13, erg);
    }
    
//     @Test
    public void teste_reducecv() {

        String s = " 0.06;";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_reducecv(SpecificationPart.AUTOMDL, s);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getAutoModel().setPercentReductionCV(0.06);

        assertEquals(x13, erg);
    }
//     @Test
    public void teste_urfinal() {

        String s = "1;";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_urfinal(SpecificationPart.AUTOMDL, s);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getAutoModel().setUnitRootLimit(1);

        assertEquals(x13, erg);
    }
    
//     @Test
    public void teste_tol() {

        String s = "1e-5;";
        WinX12SpecSeparator sep = new WinX12SpecSeparator();
        sep.read_tol(SpecificationPart.ESTIMATE, s);
        X13Specification erg = sep.getResult().getSpecification();

        X13Specification x13 = new X13Specification();
        x13.getRegArimaSpecification().getEstimate().setTol(1e-5);

        assertEquals(x13, erg);
    }
}
