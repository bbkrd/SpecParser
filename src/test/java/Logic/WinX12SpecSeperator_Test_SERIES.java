/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author o35041m
 */
public class WinX12SpecSeperator_Test_SERIES {
    
    private TsData expected;
    private TsPeriod period;
    
    private final String FILEPATH = "'C:\\DATEN\\X13AS\\EXAMPLES\\EXAM1.SER'";
    private final double[] VALUES = getValues();
    
    public WinX12SpecSeperator_Test_SERIES() {
    }

    public TsData createTestFile(String values) {

        WinX12SpecSeparator x12 = new WinX12SpecSeparator();
        x12.buildSpec(values);
        return x12.getTs().getTsData();
    }
    
    @Before
    public void setUp() {    
    }
    
    @Test
    public void test_default(){
        String testname = "test_default";
        Logger.logTestStart(testname);
        
        String spc = 
                "SERIES\n"
                +"{\n"
                +"FILE = " + FILEPATH + "\n"
                +"START = 1980.1\n"
                //+"PERIOD = 12\n"
                +"}";
        
        period = new TsPeriod(TsFrequency.Monthly);
        period.set(1980, 0);
        
        expected = new TsData(period, VALUES, false);
        
        TsData actual = createTestFile(spc);
        
        assertEquals(expected, actual);
        
        Logger.logTestEnd(testname);
    }
    
    
        
    private double[] getValues(){
        double[] values = {
            64.6,                
            65.4,                
            65.7,                
            66.0,                
            66.4,                
            66.5,                
            66.6,                
            66.7,                
            66.7,                
            66.8,                
            67.3,                
            67.8,                
            68.4,                
            69.0,                
            69.5,                
            69.8,                
            70.2,                
            70.5,                
            70.9,                
            71.1,                
            71.4,                
            71.8,                
            72.2,                
            72.3,                
            73.0,                
            73.0,                
            73.0,                
            73.3,                
            73.8,                
            74.6,                
            74.7,                
            74.6,                
            74.9,                
            75.3,                
            75.4,                
            75.6,                
            76.0,                
            76.0,                
            76.0,                
            76.2,                
            76.3,                
            76.6,                
            76.9,                
            77.1,                
            77.3,                
            77.3,                
            77.5,                
            77.7,                
            77.9,                
            78.2,                
            78.3,                
            78.4,                
            78.5,                
            78.7,                
            78.6,                
            78.5,                
            78.6,                
            79.0,                
            79.1,                
            79.2,                
            79.6,                
            80.0,                
            80.3,                
            80.3,                
            80.3,                
            80.3,                
            80.3,                
            80.1,                
            80.2,                
            80.3,                
            80.3,                
            80.4,                
            80.7,                
            80.6,                
            80.3,                
            80.3,                
            80.3,                
            80.3,                
            80.1,                
            79.8,                
            79.9,                
            79.6,                
            79.5,                
            79.6,                
            80.1,                
            80.2,                
            80.2,                
            80.3,                
            80.3,                
            80.4,                
            80.4,                
            80.3,                
            80.3,                
            80.3,                
            80.3,                
            80.4,                
            80.8,                
            81.0,                
            81.0,                
            81.1,                
            81.3,                
            81.4,                
            81.4,                
            81.3,                
            81.4,                
            81.5,                
            81.8,                
            81.9,                
            82.7,                
            82.9,                
            83.0,                
            83.5,                
            83.6,                
            83.7,                
            83.6,                
            83.6,                
            83.7,                
            84.0,                
            84.2,                
            84.4,                
            84.9,                
            85.2,                
            85.2,                
            85.4,                
            85.6,                
            85.7,                
            85.7,                
            85.9,                
            86.2,                
            86.8,                
            86.7,                
            86.7,                
            87.3,                
            87.7,                
            87.7,                
            87.9,                
            88.3,                
            88.7,                
            89.9,                
            89.8,                
            89.8,                
            89.9,                
            90.3,                
            90.4,                
            90.8,                
            91.5,                
            91.7,                
            92.2,                
            92.3,                
            92.7,                
            93.0,                
            93.0,                
            92.9,                
            93.1,                
            93.4,                
            93.5,                
            94.3,                
            95.0,                
            95.3,                
            95.5,                
            95.6,                
            96.0,                
            96.4,                
            96.4,                
            96.3,                
            96.3,                
            96.4,                
            96.6,                
            97.2,                
            97.8,                
            97.9,                
            98.0,                
            98.2,                
            98.5,                
            98.8,                
            98.9,                
            98.8,                
            98.7,                
            98.8,                
            99.0,                
            99.2,                
            99.6,                
            99.7,                
            99.9,                
            99.9,                
           100.2,                
           100.4,                
           100.2,                
           100.2,                
           100.1,                
           100.1,                
           100.4,                
           100.5,                
           101.0,                
           101.1,                
           101.1,                
           101.3,                
           101.4,                
           101.6,                
           101.5,                
           101.5,                
           101.5,                
           101.5,                
           101.8,                
           102.4,                
           102.7,                
           102.7,                
           102.4,                
           102.8,                
           103.0,                
           103.7,                
           103.9,                
           103.6,                
           103.5,                
           103.6,                
           103.8,                
           103.7,                
           103.9,                
           103.8,                
           103.9,                
           104.2,                
           104.4,                
           104.6,                
           104.5,                
           104.2,                
           104.0,                
           104.1,                
           104.1,                
           103.9,                
           104.1,                
           104.3,                
           104.6,                
           104.7,                
           104.8,                
           105.3,                
           105.3,                
           105.0,                
           104.9,                
           105.1,                
           105.5,                
           105.7,                
           106.1,                
           106.3,                
           106.3,                
           106.2,                
           106.8,                
           107.3,                
           107.2,                
           107.6,                
           107.4,                
           107.7,                
           107.7,                
           108.2,                
           108.9,                
           109.0,                
           109.3,                
           109.8,                
           110.0,                
           110.0,                
           109.8,                
           109.8,                
           109.5,                
           109.3,                
           109.4,                
           110.4,                
           110.7,                
           110.9,                
           111.0,                
           111.0,                
           110.9,                
           111.2,                
           111.1,                
           111.0,                
           111.0,                
           110.5,                
           110.7
        };
        
        return values;
    }
}
