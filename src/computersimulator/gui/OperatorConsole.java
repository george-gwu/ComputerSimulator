package computersimulator.gui;

import computersimulator.components.*;
import computersimulator.cpu.Computer;
import computersimulator.cpu.ControlUnit;
import computersimulator.cpu.InputOutputController;
import computersimulator.io.ConsoleKeyboard;
import computersimulator.io.ConsolePrinter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * OperatorConsole should include:
 Display all registers
 Display machine status and condition registers
      Displays:
          * Current Memory Address
          * Various Registers (as mentioned above)
          * Sense Switches (?) to inmainWindoworm the program  (relates to I/O). One DEVID accesses one sense switch.
 * An IPL button (to start the simulation) 
 * Switches (simulated as checkboxes) to load data into registers, select displays, and initiate certain conditions in the machine.
 * Numeric pad 
 */

public class OperatorConsole implements Runnable {    

    private Computer computer;

    // HashMap of Visual Components
    private HashMap<String, DataDisplayComposite> displayComponents;

    // Data Entry Widget
    private DataEntryComposite input;

    private JFrame mainWindow;

    private JPanel leftPanel, centerPanel, rightPanel;    
    
    private JTextArea branchPredictionOutput;
    
    private JPanel bottomPanel;    

    public void setComputer(Computer computer) {
        this.computer = computer;
    }

    public void createComponent(String name, boolean edit) throws Exception {
        DataDisplayComposite widget = new DataDisplayComposite(this.computer, name, edit);
        this.displayComponents.put(name, widget);
        leftPanel.add((widget.getGUI()));

    }

    @Override
    public void run() {
        displayComponents = new HashMap<>();

        // Create the Main window
        mainWindow = new JFrame("Group 3 Computer Simulator: Operator Console");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the left pane
        leftPanel = new JPanel();

        // Create the right pane
        centerPanel = new JPanel(); 
        
        // Create the right pane
        rightPanel = new JPanel(); 
        
        // Create the bottom pane
        bottomPanel = new JPanel();       
        
        // Create Grids
        
        // Create grid bag layout to achieve the ability to organize elements
        GridBagLayout layout = new GridBagLayout();                     
        //layout.setVgap(1);
        mainWindow.setLayout(layout);
        
        // create grid layout for left panel
        GridLayout leftLayout = new GridLayout(14, 1, 15, 4);
        leftPanel.setLayout(leftLayout);

        // create grid layout for center panel
        GridLayout centerLayout = new GridLayout(2, 1);
        centerPanel.setLayout(centerLayout);
        
        // create grid layout for right panel
        GridLayout rightLayout = new GridLayout(2, 1);
        rightPanel.setLayout(rightLayout);
        
        // create top label panel for simulator 
        JPanel labelHolder = new JPanel();
        JLabel title = new JLabel("Operator Console");
        Font font = new Font("Verdana", Font.BOLD, 14);
        title.setFont(font);
        title.setForeground(Color.BLACK);
        labelHolder.add(title);    
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        mainWindow.add(labelHolder, c);  
        
        // create top label panel for prediction 
        JPanel labelPreditionHolder = new JPanel();
        JLabel labelPreditionTitle = new JLabel("Branch Prediction:");
        labelPreditionTitle.setFont(font);
        labelPreditionTitle.setForeground(Color.BLACK);
        labelPreditionHolder.add(labelPreditionTitle);  
        c.gridx = 3;
        c.gridy = 0;
        mainWindow.add(labelPreditionHolder, c);  
        
        // Create simulator components and initialize the initial state         
        try {
            createComponent("R0", true);
            createComponent("R1", true);
            createComponent("R2", true);
            createComponent("R3", true);

            createComponent("X1", true);
            createComponent("X2", true);
            createComponent("X3", true);

            createComponent("MAR", true);
            createComponent("MBR", true);

            createComponent("PC", true);
            createComponent("CC", false);

            createComponent("IR", true);

        } catch (Exception err) {
            System.out.println("Error: " + err);
        }
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0,0,0,0);  
        // add left panel to main window
        mainWindow.add(leftPanel, c);
        
        // add text area for console printer 
        
        JTextArea guiTextPrinter = new JTextArea(15, 20);
        JScrollPane scrollPane = new JScrollPane(guiTextPrinter);
        centerPanel.add(scrollPane);
        ConsolePrinter consolePrinter = (ConsolePrinter)computer.getIO().getDevice(InputOutputController.DEVICE_CONSOLEPRINTER);
        guiTextPrinter.setEditable(false);
        consolePrinter.setDisplay(guiTextPrinter);
        
        // add numeric pad component to right pane
        
        PadComposite pad = new PadComposite(computer);
        pad.createComposite();
        centerPanel.add(pad.getGUI());
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(10,10,10,10);  
        // add center panel to main window
        mainWindow.add(centerPanel, c);  
        
        // add prediction panel to right panel 
        
        branchPredictionOutput = new JTextArea(15, 25);
        branchPredictionOutput.setEditable(false);  
         c.gridx = 3;
        c.gridy = 1;
        c.insets = new Insets(5,0,10,10); 
        // add right panel to main window
        rightPanel.add(branchPredictionOutput);
        mainWindow.add(rightPanel, c);
        
        // bottom/input panel

        input = new DataEntryComposite(20, "Input");
        bottomPanel.add(input.getGUI());        
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(0,0,0,0);  
        c.gridwidth = 4;
        mainWindow.add(bottomPanel, c);
        
        // button panel with all buttons
        
        JPanel buttonPanel = new JPanel();
        // create buttons
        JButton start = new JButton("IPL");
        JButton load = new JButton("Load");
        JButton deposit = new JButton("Deposit");
        JButton go = new JButton("Go");
        JButton halt = new JButton("Halt");
        buttonPanel.add(start);
        buttonPanel.add(load);
        buttonPanel.add(deposit);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        
        SpinnerListModel model = new SpinnerListModel(new String[] {"Step", "Run"});
        final JSpinner spinner = new JSpinner(model);        
        JComponent field = ((JSpinner.DefaultEditor) spinner.getEditor());
        Dimension prefSize = field.getPreferredSize();
        prefSize = new Dimension(80, prefSize.height);
        spinner.setValue("Run");
        computer.setRunmode(Computer.RUNMODE_RUN);
        field.setPreferredSize(prefSize);        
        buttonPanel.add(spinner);
        buttonPanel.add(go);
        buttonPanel.add(halt);       
        // add button panel to Main window
        c.gridx = 0;
        c.gridy = 4;
        mainWindow.add(buttonPanel, c);
        
        // add listeners
        final OperatorConsole opconsole = this;

        // Start Button
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                computer.IPL();
                opconsole.updateDisplay();
            }
        });
        

        // deposit
        deposit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String valueToDeposit = input.getValueAsBinaryString();
                Unit unitToDeposit = Unit.UnitFromBinaryString(valueToDeposit);
                input.resetToZero();

                for (Map.Entry<String, DataDisplayComposite> el : displayComponents.entrySet()) {
                    DataDisplayComposite widget = el.getValue();

                    // If Widget is checked, it is receiving the deposit
                    if (widget.isChecked()) {
                        computer.setComponentValueByName(widget.getName(), unitToDeposit);
                        System.out.println("Deposit s" + unitToDeposit.getSignedValue() + "/u" + unitToDeposit.getUnsignedValue() + " to " + widget.getName());
                        widget.uncheck();
                        widget.updateDisplay();
                    }
                }
            }
        });

        // load
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                input.resetToZero();

                String loadStr = "";
                for (Map.Entry<String, DataDisplayComposite> el : displayComponents.entrySet()) {
                    DataDisplayComposite widget = el.getValue();

                    // If Widget is checked, it is receiving the deposit
                    if (widget.isChecked()) {
                        loadStr = widget.getSource().getBinaryString();
                        widget.uncheck();
                    }
                }
                input.setFromBinaryString(loadStr);

                System.out.println("Load Requested: " + loadStr);
            }
        });

        // go
        go.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                        @Override
                        protected Void doInBackground() throws Exception {
                            
                            
                            switch(computer.getRunmode()){
//                                case Computer.RUNMODE_MICROSTEP: // runs one micro instruction
//                                    computer.getCpu().setRunning(true);
//                                    computer.clockCycle();
//                                    publish();
//                                    break;
                                case Computer.RUNMODE_STEP: // runs until instruction complete
                                    computer.getCpu().setRunning(true);
                                    do {
                                        computer.clockCycle();
                                        publish();
                                    } while(computer.getCpu().getControlUnit().getState() > ControlUnit.STATE_NONE && computer.getCpu().isRunning());
                                    break;
                                case Computer.RUNMODE_RUN: // runs until halt
                                    computer.getCpu().setRunning(true);
                                    do {
                                        computer.clockCycle();
                                        publish();
                                    } while(computer.getCpu().isRunning());                
                                    break;
                            }                                    
                             
                            return null;
                        }
                        
                        @Override
                        protected void done() {
                            opconsole.updateDisplay(); 
                        }
                        
                        @Override
                        protected void process(List<Void> chunks) {
                            opconsole.updateDisplay(); 
                            
                        }
                        
                    };
                    worker.execute();
                                   
                } catch (Exception err) {
                    System.out.println("Error: " + err);
                }
            }
        });
        
        
        // Halt Button
        halt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                computer.getCpu().setRunning(false);
            }
        });
        
        spinner.addChangeListener(new ChangeListener(){
           @Override
           public void stateChanged(ChangeEvent e){
               switch((String)spinner.getModel().getValue()){
                   case "Step":
                       computer.setRunmode(Computer.RUNMODE_STEP);
                       break;
                   case "Microstep":
                       computer.setRunmode(Computer.RUNMODE_MICROSTEP);
                       break;
                   case "Run":
                       computer.setRunmode(Computer.RUNMODE_RUN);
                       break;
               }
           }                      
        });
        
        final InputOutputController ioController = computer.getIO();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher( new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
              if (e.getID() == KeyEvent.KEY_PRESSED) {
                int keyCode = (int)e.getKeyCode();
                if(keyCode==10){ keyCode=13; } // convert \n to \r 
                // Filter input to lowercase alphanumeric only
                if( (keyCode >= 48 && keyCode <= 57) || // numbers
                    (keyCode >= 65 && keyCode <= 90) || // lowercase
                    (keyCode == 13) ){              
                        // Delegate the physical keypress to the virtual keyboard
                        ConsoleKeyboard consoleKeyboard = (ConsoleKeyboard)ioController.getDevice(InputOutputController.DEVICE_CONSOLEKEYBOARD);                
                        consoleKeyboard.buttonPress(keyCode);                    
                }                    
              }
              return false;
            }
        });        

        this.updateDisplay();

        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    public void updateDisplay() {
        for (Map.Entry<String, DataDisplayComposite> el : displayComponents.entrySet()) {
            DataDisplayComposite widget = el.getValue();
            widget.updateDisplay();
            
            branchPredictionOutput.setText(computer.getCpu().getBranchPredictor().getPredictionTableForTextArea());
        }        
    }

}
