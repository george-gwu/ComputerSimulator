package computersimulator.gui;

import computersimulator.cpu.Computer;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
        JFrame f =  new JFrame("Group 3 Computer Simulator: Operator Console");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new GridLayout(4, 1));
         
        JPanel labelHolder = new JPanel();
        labelHolder.add(new JLabel("Operator Console"));
        f.add(labelHolder);
         
        // Invoke ProgramCounter composite and set to the initial state (zeros represent grey labels)
        ProgramCounterComposite pc = new ProgramCounterComposite(9);
        pc.setProgramCounterComponent("000000000");
        
        // Instantiate Switches composite
        SwitchesComposite sc = new SwitchesComposite(20);
        f.add(pc.getGUI());
        f.add(sc.getGUI());
         
        // IPL 
        JPanel iplHolder = new JPanel();   
        iplHolder.add(new JButton("IPL"));
        f.add(iplHolder);
         
        f.pack();
        f.setVisible(true);
    }
}
