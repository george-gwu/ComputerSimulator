package computersimulator.gui;

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
    private JPanel p;
    private JLabel [] labels;
    private JCheckBox checkBox;   
    private JLabel componentName;
    private final Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
    private int size;
    
    public DataDisplayComposite() {
    }    
    
    /**
     * Constructor
     * @param size number of labels
     * @param name composite name
     */
    public DataDisplayComposite(int size, String name) {
        this.size = size;
        createDataDisplayComposite(size, name);
    }
    
     /**
     * Initialize data display
     * @param size 
     */
    public void init(int size) {
        if (labels == null) {
                throw new RuntimeException("please make sure to initialize " 
                        + "labels");
        }
        for (int i = 0; i < size; i++) {
            labels[i].setBackground(Color.gray);
        }
    }

    /**
     * Create Data Display Component with n labels
     * @param size number of labels
     * @return returns labels
     */
    public JLabel[] createDataDisplayComposite(int size, String name) {
        labels = new JLabel[size];
        
        p = new JPanel();
        p.setBackground(Color.WHITE);
        
        // checkbox
        checkBox = new JCheckBox();
        p.add(checkBox);
        p.add(new JLabel(" ")); // add empty placeholder between checkbox and labels
        
        // componenent name
        componentName = new JLabel(name);
    
        p.add(componentName);
        for (int i = 0; i < size; i++) {
            labels[i] = new JLabel();
            labels[i].setBorder(border);
            labels[i].setOpaque(true);
            labels[i].setPreferredSize(new Dimension(15, 15));
            p.add(labels[i]);
        }

        return labels;
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
