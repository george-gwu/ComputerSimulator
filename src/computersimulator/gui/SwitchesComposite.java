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
 * Class to create Switches component and read its status/state 
 * @author pawel
 */
public class SwitchesComposite extends JFrame {
    // checkboxes to hold status of Switches, optionally make this an array
    private JCheckBox c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, 
            c11, c12, c13, c14, c15, c16, c17, c18, c19,  c20;
    private JPanel p;
    private JButton b;
    private JFrame f;
    
    public SwitchesComposite() {
        createSwitchesComponent();
    }
    
    /**
     * Create Switches Component
     */
    public void createSwitchesComponent() {
        f = new JFrame("Switches Component");
        f.setVisible(true);
        f.setSize(850, 200);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        p = new JPanel();
        p.setBackground(Color.GRAY);
        b = new JButton("START");
        
        // create switches
        c1 = new JCheckBox("");c2 = new JCheckBox("");
        c3 = new JCheckBox("");c4 = new JCheckBox("");
        c5 = new JCheckBox("");c6 = new JCheckBox("");
        c7 = new JCheckBox("");c8 = new JCheckBox("");
        c9 = new JCheckBox("");c10 = new JCheckBox("");
        c11 = new JCheckBox("");c12 = new JCheckBox("");
        c13 = new JCheckBox("");c14 = new JCheckBox("");
        c15 = new JCheckBox("");c16 = new JCheckBox("");
        c17 = new JCheckBox("");c18 = new JCheckBox("");
        c19 = new JCheckBox("");c20 = new JCheckBox("");

	p.add(c1);p.add(c2);
        p.add(c3);p.add(c4);
        p.add(c5);p.add(c6);
        p.add(c7);p.add(c8);
        p.add(c9);p.add(c10);
        p.add(c11); p.add(c12);
        p.add(c13); p.add(c14);
        p.add(c15); p.add(c16);
        p.add(c17);p.add(c18);
        p.add(c19);p.add(c20);

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Selected state: " + getSwitchesStatus());
            }
        });
        // add button
        p.add(b);
        f.add(p, BorderLayout.NORTH);
        f.setVisible(true);
       
    }
    
    /**
     * Returns the status of Switches 
     * @return the status of Switches
     */
    public String getSwitchesStatus() {
        StringBuilder code = new StringBuilder("");
        
        // get the state of the swiches
        if (c1.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c2.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c3.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c4.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c5.isSelected()) { code.append("1"); } else { code.append("0"); }       
        if (c6.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c7.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c8.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c9.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c10.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c11.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c12.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c13.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c14.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c15.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c16.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c17.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c18.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c19.isSelected()) { code.append("1"); } else { code.append("0"); }
        if (c20.isSelected()) { code.append("1"); } else { code.append("0"); }
        return new String(code);
    }

    public static void main(String[] args) {
        new SwitchesComposite();
    }
}
