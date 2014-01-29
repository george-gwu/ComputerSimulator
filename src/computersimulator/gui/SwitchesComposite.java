package computersimulator.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Switches composite - component to represent switches
 * 
 * @author pawel
 */
public class SwitchesComposite extends JPanel {
    private JPanel p;
    private JButton b;
    private JCheckBox[] switches; 
    
    /**
     * Constructor
     * @param n number of switches
     */
    public SwitchesComposite(int n) {
        createSwitchesComponent(n);
    }
    
     /**
     * Create Switches Component with n elements
     * @param n number of checkBoxes
     * @return returns check boxes
     */
    public JCheckBox[] createSwitchesComponent(int n) {
        switches = new JCheckBox[n];
        
        p = new JPanel();           
        p.setBackground(Color.GRAY);
        b = new JButton("START");
        
        // create switches
        for (int i = 0; i < n; i++) {
            switches[i] = new JCheckBox("");
            p.add(switches[i]);
        }
        
        // add listener to the button
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Selected state: " + getSwitchesComponent());
            }
        });
        // add button to panel
        p.add(b);
        return switches;
    }
    
    /**
     * Get status of switches represented by checkboxes
     * @param switches
     * @return the status of Switches
     */
    public String getSwitchesComponent() {
        if (switches == null) {
            throw new RuntimeException("please make sure to initialize "
                        + "labels in Switches");
        }
        
        StringBuilder code = new StringBuilder("");
        
        for (int i = 0; i < switches.length; i++) {
            if (switches[i].isSelected()) { 
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
}
