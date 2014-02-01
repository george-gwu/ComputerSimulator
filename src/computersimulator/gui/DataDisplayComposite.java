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
    private JLabel [] bits;
    private JCheckBox checkBox;  
    private final Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
    private int i;
    
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
            p.add(new JLabel(" ")); // add empty placeholder between checkbox and bits
        }
        
        // component name
        p.add(new JLabel(name));    
        
        // Labels used for Dispaly of Bits
        bits = new JLabel[this.source.getSize()];
        for (int i = 0; i < this.source.getSize(); i++) {
            bits[i] = new JLabel();
            bits[i].setBorder(border);
            bits[i].setBackground(Color.gray);
            bits[i].setOpaque(true);
            bits[i].setPreferredSize(new Dimension(15, 15));
            p.add(bits[i]);
        }                
    }
    
    public void updateDisplay(){
        Integer[] data = this.source.getBinaryArray();
        
        for(int i = 0; i<data.length; i++){
            bits[i].setBackground((data[i]==1) ? Color.red : Color.gray);
        }
    }   

    /**
     * @return panel instance
     */ 
    public JComponent getGUI() {
        return p;
    }    
}
