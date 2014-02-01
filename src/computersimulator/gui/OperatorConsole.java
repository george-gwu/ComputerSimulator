package computersimulator.gui;

import computersimulator.cpu.Computer;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
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
 * @author pawel
 */

public class OperatorConsole implements Runnable {
    
    private Computer computer;
    private List<DataDisplayComposite> displayComponents;         // list for storing components
    private List<DataEntryComposite> entryComponents;
    
    // declare components
    private DataDisplayComposite ir;
    private DataDisplayComposite cc;
    private DataDisplayComposite pc;
    private DataDisplayComposite r0;
    private DataDisplayComposite r1;
    private DataDisplayComposite r2;
    private DataDisplayComposite r3;
    private DataDisplayComposite mdr;
    private DataDisplayComposite mar;
    private DataDisplayComposite x0;
    private DataDisplayComposite x1;
    private DataDisplayComposite x2;
    private DataEntryComposite input;
    
    public void setComputer(Computer computer){
        this.computer = computer;
    }
    
    @Override
    public void run() {
        
        // Create the window
        JFrame f =  new JFrame("Group 3 Computer Simulator: Operator Console");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // create panel to hold all components
        JPanel labelHolder = new JPanel();
        
        // maintain the lists of all components
        displayComponents = new ArrayList<DataDisplayComposite>();
        entryComponents = new ArrayList<DataEntryComposite>();
        
        // create title
        JLabel title = new JLabel("Operator Console");
        Font font = new Font("Verdana", Font.BOLD, 14);
        title.setFont(font);
        title.setForeground(Color.BLACK);
        labelHolder.add(title);
        f.add(labelHolder);
        
        // create grid layout - each component/register will be placed as a seperate line
        GridLayout layout = new GridLayout(15, 1, 15, 5);
        //layout.setVgap(1);
        f.setLayout(layout);
         
      
        // Create simulator components and initialize the initial state 
        // create IR
        ir = new DataDisplayComposite(19, "IR");
        ir.init(19);
        
        // create CC
        cc = new DataDisplayComposite(4, "CC");
        cc.init(4);
        
        // create program counter component
        pc = new DataDisplayComposite(9, "PC");
        pc.init(9);
        
        // create R0 component
        r0 = new DataDisplayComposite(19, "R0");
        r0.init(19);
        
        // create R1 component
        r1 = new DataDisplayComposite(19, "R1");
        r1.init(19);
        
        // create R2 component
        r2 = new DataDisplayComposite(19, "R2");
        r2.init(19);
        
        // create R3 component
        r3 = new DataDisplayComposite(19, "R3");
        r3.init(19);
        
        // create MDR component
        mdr = new DataDisplayComposite(20, "MDR");
        mdr.init(20);
        
        // create MAR component
        mar = new DataDisplayComposite(20, "MAR");
        mar.init(20);
        
        //create X0 component
        x0 = new DataDisplayComposite(20, "X0");
        x0.init(20);
        
        //create X1 component
        x1 = new DataDisplayComposite(20, "X1");
        x1.init(20);
        
        //create X2 component
        x2 = new DataDisplayComposite(20, "X2");
        x2.init(20);
        
        //create Input component
        input = new DataEntryComposite(20, "Input");
        
        // add all components to the frame
        f.add(ir.getGUI());
        f.add(pc.getGUI());
        f.add(cc.getGUI());
        f.add(r0.getGUI());
        f.add(r1.getGUI());
        f.add(r2.getGUI());
        f.add(r3.getGUI());
        f.add(mdr.getGUI());
        f.add(mar.getGUI());
        f.add(x0.getGUI());
        f.add(x1.getGUI());
        f.add(x2.getGUI());
        f.add(input.getGUI());
         
        // create button panel with all buttons
        JPanel buttonPanel = new JPanel();   
        // create buttons
        JButton start = new JButton("Start");
        JButton load = new JButton("Load");
        JButton deposit = new JButton("Deposit");
        JButton step = new JButton("Step");
        JButton stop = new JButton("Stop");
        JButton reset = new JButton("Reset");

        buttonPanel.add(start);
        buttonPanel.add(load);
        buttonPanel.add(deposit);
        buttonPanel.add(step);
        buttonPanel.add(stop);
        buttonPanel.add(reset);
        
        // add button panel to frame
        f.add(buttonPanel);
        
        // add listeners
       
        // start
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Selected state: " + input.getDataEntryComposite());
            }
        });
       
        // deposit
        deposit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               pc.setDataDisplayComposite("101010111");
            }
        });
        
        // reset
        reset.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pc.setDataDisplayComposite("000000000");
                }
        });
        
        f.pack();
        f.setVisible(true);
    }
}
