package computersimulator.gui;

import computersimulator.components.Unit;
import computersimulator.cpu.Computer;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author pawel
 */
public class DataDisplayComposite {
    private Computer computer;
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
    public DataDisplayComposite(Computer computer, String name, boolean edit) throws Exception {
        this.name = name;
        this.computer = computer;
                
        int size = computer.getComponentValueByName(name).getSize();
        
        p = new JPanel();
        p.setBackground(Color.WHITE);
        
        this.checkBox = new JCheckBox();
        if(edit){ // checkbox for use with Deposit, only visible if edited                        
            p.add(this.checkBox);
            p.add(new JLabel(" ")); // add empty placeholder between checkbox and bits
        }
        
        // component name
        p.add(new JLabel(name));    
        
        // Labels used for Display of Bits
        bits = new JLabel[size];
        for (int i = 0; i < size; i++) {
            bits[i] = new JLabel();
            bits[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            bits[i].setBackground(Color.gray);
            bits[i].setOpaque(true);
            bits[i].setPreferredSize(new Dimension(15, 15));
            p.add(bits[i]);
        }                
    }
    
    public void updateDisplay() throws Exception{
        Integer[] data = this.getSource().getBinaryArray();
        
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
    

    public Unit getSource() throws Exception {
        return this.computer.getComponentValueByName(this.name);
    }
    
    
    
    

    /**
     * @return panel instance
     */ 
    public JComponent getGUI() {
        return p;
    }    
}
