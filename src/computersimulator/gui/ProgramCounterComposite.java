package computersimulator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Program Counter Composite for creating and setting states in labels based on
 * passed instructions
 * 
 * @author pawel
 */
public class ProgramCounterComposite extends JFrame {
    private final int numOfPcLabels = 9;
    private final JLabel [] pcLabels = new JLabel[numOfPcLabels];
    int [] a = new int[10];
    private JPanel p;
    private JButton b;
    private JFrame f;
    private final Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
        
    public ProgramCounterComposite() {
        createProgramCounterComponent();
    }

   /**
     * Create PC Component
     */
    public void createProgramCounterComponent() {
        f = new JFrame("PC Component");
        f.setVisible(true);
        f.setSize(850, 200);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        p = new JPanel();
        p.setBackground(Color.WHITE);
        b = new JButton("SET STATUS");
        
        for (int i = 0; i < numOfPcLabels; i++) {
            pcLabels[i] = new JLabel();
            pcLabels[i].setBorder(border);
            pcLabels[i].setOpaque(true);
            pcLabels[i].setPreferredSize(new Dimension(20, 20));
            p.add(pcLabels[i]);
        }
        
        p.add(b);
        f.add(p, BorderLayout.NORTH);
        f.setVisible(true);
        
        
        b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setPcStatus("000000111");
                }
            });
        }
    
    /**
     * Set program counter status based on instruction passed 
     * @param instruction current instruction that is passed
     */
    public void setPcStatus(String instruction) {
        for (int i = 0; i < instruction.length(); i++) {
            
            String curr = instruction.charAt(i) + "";
            
            if (curr.equals("1")) {
                pcLabels[i].setBackground(Color.red);
            }    
            else if (curr.equals("0")) {
                pcLabels[i].setOpaque(true);
            }
        }
    }

    public static void main(String[] args) {
        new ProgramCounterComposite();
    }
}
