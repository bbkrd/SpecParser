/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.tss.documents.TsDocument;
import ec.ui.view.tsprocessing.DefaultProcessingViewer;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import org.openide.util.Exceptions;

/**
 *  This class shows the transformation of a SingleSpec.
 *  The WinX13Spec is in the front, the JD+Spec on the right and on 
 *  the bottom the error messages because of the translation between 
 *  the both specs.
 * 
 */
public class SpecViewer extends DefaultProcessingViewer<TsDocument> {

    private JTextArea winX13Text;
    private JTextArea errormessage;
    private JScrollPane scrollText;
    private JScrollPane scrollError;
    private JSplitPane split;
   

    // FACTORY METHODS >
    public static SpecViewer create(SpecCollector spec) {
//        this.spec=spec;
        SpecViewer viewer = new SpecViewer(Type.APPLY, spec);
        if (spec.getJDSpec() != null) {
            viewer.setDocument(spec.getJDSpec());
        }
        return viewer;
    }

    public SpecViewer(Type type, final SpecCollector spec) {
        super(type);
        remove(splitter);
       
        setSpecificationsVisible(true);

        //Button for load Winx13 Spec from file
        JButton load =  new JButton(new AbstractAction("Load WinX13Spec") {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new MyFilter(".spc"));
                fc.setAcceptAllFileFilterUsed(false);

                int state = fc.showOpenDialog(null);

                if (state == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();

                        try {
                            FileReader f = new FileReader(file);
                            BufferedReader br = new BufferedReader(f);
                            StringBuilder s = new StringBuilder();
                            String zeile;
                            while ((zeile = br.readLine()) != null) {
                                s.append(zeile);
                                s.append("\n");
                            }
                            br.close();
                            spec.setWinX13Spec(s.toString());
                            winX13Text.setText(spec.getWinX13Spec());
                            
                        } catch (FileNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                    
                } else {
                    errormessage.setText("Auswahl abgebrochen");
                }
            }
        });
        //Button for save WinX13 Spec to file ends with .spc
        JButton save = new JButton(new AbstractAction("Save WinX13Spec") {
            @Override
            public void actionPerformed(ActionEvent e) {

                String path = System.getProperty("user.home");
                JFileChooser chooser = new JFileChooser(path);
                chooser.setFileFilter(new MyFilter(".spc"));
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.showSaveDialog(null);
                try{
                    File file =chooser.getSelectedFile();
                    FileWriter fw = new FileWriter(file);
                    fw.write(winX13Text.getText());
                    fw.close();
                    if(!file.toString().endsWith(".spc")){
                        file.renameTo(new File(file.toString()+".spc"));
                    }
                }catch(IOException ex){
                    winX13Text.setText("Nothing happend");
                }
            }
        });
        //Button for refresh winX13Spec from JD+ Spec
        JButton refreshX13 = new JButton(new AbstractAction("Refresh WinX13Spec") {
            @Override
            public void actionPerformed(ActionEvent e) {

                JOptionPane.showInputDialog(new ImageIcon("C:/Daten/Bastelgarage (290).gif"));
            }
        });
        //Button for refresh JD+Spec from WinX13Spec
        JButton refreshJD = new JButton(new AbstractAction("Refresh JD+ Spec") {
            @Override
            public void actionPerformed(ActionEvent e) {
//               do nothing
                JOptionPane.showInputDialog(new ImageIcon("C:/Daten/Bastelgarage (290).gif"));
            }
        });
        
        toolBar.add(load, 0);
        toolBar.add(save, 1);
        toolBar.add(refreshX13, 2);
        toolBar.add(refreshJD, 3);

        winX13Text = new JTextArea();
        errormessage = new JTextArea();

        winX13Text.setText("empty");
        errormessage.setText("Fehlermeldungen");

        scrollText = new JScrollPane(winX13Text);
        scrollError = new JScrollPane(errormessage);

        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollText, scrollError);
        split.setDividerLocation(0.5);
        split.setResizeWeight(0.5);

        add(split);
    }
    
    public String getWinX13Text(){
        
        return winX13Text.getText();
    }
    
 class MyFilter extends FileFilter {
        private String endung;
       
        public MyFilter(String endung) {
            this.endung = endung;
        }
       
        @Override
        public boolean accept(File f) {
            if(f == null) return false;
           
            // Ordner anzeigen
            if(f.isDirectory()) return true;
           
            // true, wenn File gewuenschte Endung besitzt
            return f.getName().toLowerCase().endsWith(endung);
        }
         @Override
        public String getDescription() {
            return "*"+endung;
        }
    }
}
