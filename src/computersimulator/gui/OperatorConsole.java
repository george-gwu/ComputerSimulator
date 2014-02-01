package computersimulator.gui;

import computersimulator.components.Unit;
import computersimulator.cpu.Computer;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * OperatorConsole should include:
 Display mainWindowor all registers
 Display mainWindowor machine status and condition registers
      Displays:
          * Current Memory Address
          * Various Registers (as mentioned above)
          * Sense Switches (?) to inmainWindoworm the program  (relates to I/O). One DEVID accesses one sense switch.
 * An IPL button (to start the simulation)
 * Switches (simulated as buttons) to load data into registers, select displays, and initiate certain conditions in the machine.
 * 
 * 
 * @author george
 * @author pawel
 */

public class OperatorConsole implements Runnable {
    
    private Computer computer;

    // HashMap of Visual Components
    private HashMap<String,DataDisplayComposite> displayComponents;
    
    // Data Entry Widget
    private DataEntryComposite input;
    
    private JFrame mainWindow;
    
    public void setComputer(Computer computer){
        this.computer = computer;
    }
    
    
    public void createComponent(Unit src, String name, boolean edit){    
        DataDisplayComposite widget = new DataDisplayComposite(src, name, edit);        
        this.displayComponents.put(name,widget);
        mainWindow.add(widget.getGUI());
    }
    
    @Override
    public void run() {       
        displayComponents = new HashMap<>();
        
        // Create the window
        mainWindow =  new JFrame("Group 3 Computer Simulator: Operator Console");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // create panel to hold all components
        JPanel labelHolder = new JPanel();
                
        // create title
        JLabel title = new JLabel("Operator Console");
        Font font = new Font("Verdana", Font.BOLD, 14);
        title.setFont(font);
        title.setForeground(Color.BLACK);
        labelHolder.add(title);
        mainWindow.add(labelHolder);
        
        // create grid layout - each component/register will be placed as a separate line
        GridLayout layout = new GridLayout(15, 1, 15, 5);
        //layout.setVgap(1);
        mainWindow.setLayout(layout);
               
        // Create simulator components and initialize the initial state         
        createComponent(computer.getCpu().getALU().getConditionCode(),               "CC", false);
        createComponent(computer.getCpu().getControlUnit().getProgramCounter(),      "PC", true);
        
        createComponent(computer.getCpu().getControlUnit().getGpRegisters()[0],      "R0", true);
        createComponent(computer.getCpu().getControlUnit().getGpRegisters()[1],      "R1", true);
        createComponent(computer.getCpu().getControlUnit().getGpRegisters()[2],      "R2", true);
        createComponent(computer.getCpu().getControlUnit().getGpRegisters()[3],      "R3", true);
        
        createComponent(computer.getCpu().getControlUnit().getIndexRegisters()[0],   "X1", true);
        createComponent(computer.getCpu().getControlUnit().getIndexRegisters()[1],   "X2", true);
        createComponent(computer.getCpu().getControlUnit().getIndexRegisters()[2],   "X3", true);
        
        createComponent(computer.getMemory().getMAR(), "MAR", true);
        createComponent(computer.getMemory().getMBR(), "MBR", true);
                        
        createComponent(computer.getCpu().getControlUnit().getInstructionRegister(), "IR", true);
        
        
                       
        input = new DataEntryComposite(20, "Input");
        mainWindow.add(input.getGUI());
         
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
        mainWindow.add(buttonPanel);
        
        // add listener
       
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
//               pc.setDataDisplayComposite("101010111");
            }
        });
        
        // reset
        reset.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
//                    pc.setDataDisplayComposite("000000000");
                }
        });
        
        mainWindow.pack();
        mainWindow.setVisible(true);
    }
     
}
