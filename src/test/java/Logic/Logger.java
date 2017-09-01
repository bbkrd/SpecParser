/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

/**
 *
 * @author o35041m
 */
public class Logger {
    
    public static void logTestStart(String testname){
        System.out.println("start of testmethod: " + testname);
    }
  
    public static void logTestEnd(String testname){
        System.out.println("end of testmethod: " + testname);
        System.out.println("");
    }
    
}
