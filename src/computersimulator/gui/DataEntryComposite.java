package computersimulator.gui;

import java.awt.Color;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author pawel
 */
public class DataEntryComposite {
    private JPanel p;
    private JCheckBox[] entries; 
    private JLabel componentName;
    private int size;
    
    /**
     * Constructor
     * @param size number of entries
     * @param name Name of Field
     */
    public DataEntryComposite(int size, String name) {
        this.size = size;
        createDataEntryComposite(size, name);
    }
    
     /**
     * Create Data Entry Component with n elements
     * @param size  number of entries
     * @return returns entries
     */
    private JCheckBox[] createDataEntryComposite(int size, String name) {
        entries = new JCheckBox[size];
        p = new JPanel();           
        p.setBackground(Color.LIGHT_GRAY);
        
        componentName = new JLabel(name);
        p.add(componentName);
        
        // create entries
        for (int i = 0; i < size; i++) {
            entries[i] = new JCheckBox("");
            p.add(entries[i]);
        }
        
        return entries;
    }
    
    /**
     * Get status of entries
     * @return the status of entries
     */
    public String getDataEntryComposite() {
        if (entries == null) {
            throw new RuntimeException("please make sure to initialize "
                        + "labels");
        }
        
        StringBuilder code = new StringBuilder("");
        
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].isSelected()) { 
                code.append("1");
            } else { 
                code.append("0");
            }
        }
        return new String(code);
    }

    /**
     * @return panel instance
     */
    public JComponent getGUI() {
        return p;
    }
    
   /**
     * Get the size of the component
     * @return size of the component
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Set the size of the component
     * @param size 
     */
    public void setSize(int size) {
        this.size = size;
    }
}
