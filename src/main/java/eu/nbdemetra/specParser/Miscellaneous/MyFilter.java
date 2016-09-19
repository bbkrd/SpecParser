/* 
 * Copyright 2016 Deutsche Bundesbank
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
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
