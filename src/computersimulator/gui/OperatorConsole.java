package computersimulator.gui;

import computersimulator.cpu.Computer;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * OperatorConsole should include:
 * Display for all registers
 * Display for machine status and condition registers
 *      Displays:
 *          * Current Memory Address
 *          * Various Registers (as mentioned above)
 *          * Sense Switches (?) to inform the program  (relates to I/O). One DEVID accesses one sense switch.
 * An IPL button (to start the simulation)
 * Switches (simulated as buttons) to load data into registers, select displays, and initiate certain conditions in the machine.
 * 
 * 
 * @author george
 */

public class OperatorConsole implements Runnable {
    
    
    private Computer computer;
    
    
    public void setComputer(Computer computer){
        this.computer = computer;
    }
    
     @Override
    public void run() {
        // Create the window
        JFrame f = new JFrame("Group 3 Computer Simulator: Operator Console");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set Layout
        f.setLayout(new FlowLayout());
        
        
        // Add GUI Elements
        f.add(new JLabel("Operator Console"));
        f.add(new JButton("IPL"));
        
        // Organize and Display
        f.setSize(600,400); // w,h
        //f.pack();        
        f.setVisible(true);
    }
    
    
    
    
}
