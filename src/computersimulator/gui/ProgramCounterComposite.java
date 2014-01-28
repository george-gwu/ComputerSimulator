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
 * Program Counter Composite/component
 * 
 * @author pawel
 */
public class ProgramCounterComposite extends JFrame {
    private JPanel p;
    private JButton b;
    private JFrame f;
    private final Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
        
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
        final JLabel [] pcLabels = new JLabel[n];
        f = new JFrame("PC Component");
        f.setVisible(true);
        f.setSize(850, 200);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        p = new JPanel();
        p.setBackground(Color.WHITE);
        b = new JButton("SET STATUS");
        
        for (int i = 0; i < n; i++) {
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
                    setProgramCounterComponent(pcLabels, "000000111");
                }
        });
        return pcLabels;
    }
    
    /**
     * Set program counter status based on instruction passed 
     * @param labels instruction current instruction that is passed
     * @param instruction instruction to be passed i.e.: 000000111
     */
    public void setProgramCounterComponent(JLabel[] labels, String instruction) {
        for (int i = 0; i < instruction.length(); i++) {
            String curr = instruction.charAt(i) + "";
            
            if (curr.equals("1")) {
                labels[i].setBackground(Color.red);
            }    
            else if (curr.equals("0")) {
                labels[i].setOpaque(true);
            }
        }
    }

    /**
     * main method
     * @param args 
     */
    public static void main(String[] args) {
        int numberOfLabels = 9;
        new ProgramCounterComposite(numberOfLabels);
    }
}
