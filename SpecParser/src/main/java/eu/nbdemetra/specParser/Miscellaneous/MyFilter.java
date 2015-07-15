/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.nbdemetra.specParser.Miscellaneous;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Nina Gonschorreck
 */
public class MyFilter extends FileFilter {

        private String extension;

        public MyFilter(String endung) {
            this.extension = endung;
        }

        @Override
        public boolean accept(File f) {
            if (f == null) {
                return false;
            }

            // Ordner anzeigen
            if (f.isDirectory()) {
                return true;
            }

            // true, wenn File gewuenschte Endung besitzt
            return f.getName().toLowerCase().endsWith(extension);
        }

        @Override
        public String getDescription() {
            return "*" + extension;
        }
    }
