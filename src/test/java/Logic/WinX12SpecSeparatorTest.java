/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x13.X13Specification;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author s4504gn
 */
public class WinX12SpecSeparatorTest {
    
    public WinX12SpecSeparatorTest() {
    }

    @Test
    public void test_setX11Defaults() {
        String spc = "X11{"
                + "}";
        
        WinX12SpecSeparator x12 = new WinX12SpecSeparator();
        x12.buildSpec(spc);
        X13Specification spec = x12.getCurrentSpec();
        
        X13Specification expected = new X13Specification();
        expected.getRegArimaSpecification().getBasic().setPreprocessing(false);
        expected.getX11Specification().setMode(DecompositionMode.Multiplicative);
        expected.getX11Specification().setSeasonal(true);
        expected.getX11Specification().setSeasonalFilter(SeasonalFilterOption.Msr);
        expected.getX11Specification().setSigma(1.5, 2.5);
        expected.getX11Specification().setForecastHorizon(0);
        
        assertEquals(expected, spec);
    }
    
     @Test
    public void test_read_trendma() {
        String spc = "X11{"
                + "trendma = 17"
                + "}";
        
        WinX12SpecSeparator x12 = new WinX12SpecSeparator();
        x12.buildSpec(spc);
        X13Specification spec = x12.getCurrentSpec();
        
        X13Specification expected = new X13Specification();
        expected.getRegArimaSpecification().getBasic().setPreprocessing(false);
        expected.getX11Specification().setMode(DecompositionMode.Multiplicative);
        expected.getX11Specification().setSeasonal(true);
        expected.getX11Specification().setSeasonalFilter(SeasonalFilterOption.Msr);
        expected.getX11Specification().setSigma(1.5, 2.5);
        expected.getX11Specification().setForecastHorizon(0);
        expected.getX11Specification().setHendersonFilterLength(17);
        
        assertEquals(expected, spec);
    }
}
