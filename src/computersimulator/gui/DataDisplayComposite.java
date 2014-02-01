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
    private String name;
    
    /**
     * Constructor
     * @param src Source element
     * @param name composite name
     * @param edit
     */
    public DataDisplayComposite(Unit src, String name, boolean edit){
        this.source = src;
        this.name = name;
        
        p = new JPanel();
        p.setBackground(Color.WHITE);
        
        this.checkBox = new JCheckBox();
        if(edit){ // checkbox for use with Deposit, only visible if edited                        
            p.add(this.checkBox);
            p.add(new JLabel(" ")); // add empty placeholder between checkbox and bits
        }
        
        // component name
        p.add(new JLabel(name));    
        
        // Labels used for Dispaly of Bits
        bits = new JLabel[this.source.getSize()];
        for (int i = 0; i < this.source.getSize(); i++) {
            bits[i] = new JLabel();
            bits[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
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
    
    public boolean isChecked(){
        return this.checkBox.isSelected();
    }
    
    
    public void uncheck(){
        this.checkBox.setSelected(false);
    }
    
    public void check(){
        this.checkBox.setSelected(true);
    }
    
    

    public String getName() {
        return name;
    }

    public Unit getSource() {
        return source;
    }
    
    
    
    

    /**
     * @return panel instance
     */ 
    public JComponent getGUI() {
        return p;
    }    
}
