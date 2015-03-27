/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import java.util.LinkedList;

/**
 *
 * @author Nina Gonschorreck
 */
public class ThreadObjectList {

    private LinkedList<String> specs = new LinkedList<String>();
    private static int counter=0;

    public ThreadObject getSpec() {
        synchronized (specs) {
            ThreadObject t = new ThreadObject(specs.pollFirst(), counter);
            counter++;
            return t;
        }
    }

    public void addSpec(String s) {
        synchronized (specs) {
            specs.addLast(s);
        }
    }

    public boolean isEmpty() {
        synchronized (specs) {
            return specs.isEmpty();
        }
    }
}
