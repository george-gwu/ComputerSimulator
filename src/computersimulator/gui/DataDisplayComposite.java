package computersimulator.gui;

import computersimulator.components.Unit;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 *
 * @author pawel
 */
public class DataDisplayComposite {
    private Unit source;
    private JPanel p;
    private JLabel [] labels;
    private JCheckBox checkBox;  
    private final Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
    
    /**
     * Constructor
     * @param src Source element
     * @param name composite name
     * @param edit
     */
    public DataDisplayComposite(Unit src, String name, boolean edit){
        this.source = src;
        
        p = new JPanel();
        p.setBackground(Color.WHITE);
        
        if(edit){ // checkbox for use with Deposit            
            checkBox = new JCheckBox();
            p.add(checkBox);
            p.add(new JLabel(" ")); // add empty placeholder between checkbox and labels
        }
        
        // component name
        p.add(new JLabel(name));    
        
        int size = this.source.getSize();
        
        // Labels Used For Binary Display
        labels = new JLabel[size];
        for (int i = 0; i < size; i++) {
            labels[i] = new JLabel();
            labels[i].setBorder(border);
            labels[i].setBackground(Color.gray);
            labels[i].setOpaque(true);
            labels[i].setPreferredSize(new Dimension(15, 15));
            p.add(labels[i]);
        }        
        
    }
 

    
    /**
     * Set status based on instruction passed 
     * @param instruction instruction to be passed i.e.: 000000111
     */
     public void setDataDisplayComposite(String instruction) {
        for (int i = 0; i < instruction.length(); i++) {
            if (labels == null) {
                throw new RuntimeException("please make sure to initialize "
                        + "labels");
            }
            String curr = instruction.charAt(i) + "";
            
            if (curr.equals("1")) {
                labels[i].setBackground(Color.red);
            }    
            else if (curr.equals("0")) {
                labels[i].setBackground(Color.gray);
            }
        }        
    }

    /**
     * @return panel instance
     */ 
    public JComponent getGUI() {
        return p;
    }    
}
