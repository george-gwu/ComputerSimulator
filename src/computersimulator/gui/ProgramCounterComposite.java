package computersimulator.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Program Counter Composite/component
 * 
 * @author pawel
 */
public class ProgramCounterComposite extends JPanel {
    private JPanel p;
    private JButton b;
    private JButton r;
    private JLabel [] pcLabels;
    private final Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
    
    public ProgramCounterComposite() {
    }    
    
    /**
     * Constructor
     * @param n number of labels
     */
    public ProgramCounterComposite(int n) {
        createProgramCounterComponent(n);
    }

    /**
     * Create Program Counter Component with n labels
     * @param n number of labels
     * @return returns labels
     */
    public JLabel[] createProgramCounterComponent(int n) {
        pcLabels = new JLabel[n];
        p = new JPanel();
        p.setBackground(Color.WHITE);
        b = new JButton("SET STATUS");
        r = new JButton("RESET STATUS");
        
        for (int i = 0; i < n; i++) {
            pcLabels[i] = new JLabel();
            pcLabels[i].setBorder(border);
            pcLabels[i].setOpaque(true);
            pcLabels[i].setPreferredSize(new Dimension(20, 20));
            p.add(pcLabels[i]);
        }
        
        p.add(b);
        p.add(r);

        b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setProgramCounterComponent("101010111");
                }
        });
         r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setProgramCounterComponent("000000000");
                }
        });

        return pcLabels;
    }
    
    /**
     * Set program counter status based on instruction passed 
     * @param instruction instruction to be passed i.e.: 000000111
     */
     public void setProgramCounterComponent(String instruction) {
        for (int i = 0; i < instruction.length(); i++) {
            if (pcLabels == null) {
                throw new RuntimeException("please make sure to initialize "
                        + "labels in Program Counter ");
            }
            String curr = instruction.charAt(i) + "";
            
            if (curr.equals("1")) {
                pcLabels[i].setBackground(Color.red);
            }    
            else if (curr.equals("0")) {
                pcLabels[i].setBackground(Color.gray);
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
