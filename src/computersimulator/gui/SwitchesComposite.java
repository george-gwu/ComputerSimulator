package computersimulator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Switches composite - component to represent switches
 * 
 * @author pawel
 */
public class SwitchesComposite extends JFrame {
    private JPanel p;
    private JButton b;
    private JFrame f;
    
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
        final JCheckBox[] switches = new JCheckBox[n];
        f = new JFrame("Switches Component");
        f.setVisible(true);
        f.setSize(850, 200);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        p = new JPanel();
        p.setBackground(Color.GRAY);
        b = new JButton("START");
        
        // create switches
        for (int i = 0; i < n; i++) {
            switches[i] = new JCheckBox("");
            p.add(switches[i]);
        }
        
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Selected state: " + getSwitchesComponent(switches));
            }
        });
        // add button
        p.add(b);
        f.add(p, BorderLayout.NORTH);
        f.setVisible(true);
        
        return switches;
    }
    
    /**
     * Get status of switches represented by checkboxes
     * @param switches
     * @return the status of Switches
     */
    public String getSwitchesComponent(JCheckBox[] switches) {
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
     * main method
     * @param args 
     */
    public static void main(String[] args) {
        new SwitchesComposite(20);      // create 20 switches (checkboxes)
    }
}
