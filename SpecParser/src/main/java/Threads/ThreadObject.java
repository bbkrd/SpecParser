/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Threads;

/**
 *
 * @author Nina Gonschorreck
 */
public class ThreadObject {
    
    private String spec_name;
    private int index;
    
    
    public ThreadObject(String s, int i){
        this.spec_name=s;
        this.index=i;
    }
    
    public int getIndex(){
        return index;
    }
    
    public String getSpecName(){
        return spec_name;
    }
}
