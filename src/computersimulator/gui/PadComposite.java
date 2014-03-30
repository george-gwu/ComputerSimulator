package computersimulator.gui;

import computersimulator.cpu.Computer;
import computersimulator.cpu.InputOutputController;
import computersimulator.io.ConsoleKeyboard;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Numeric pad composite 
 * @author perzanp
 */
public class PadComposite {
    private JPanel p;
    private Computer computer;
    
    public PadComposite(Computer computer) {
        this.computer = computer;
    }    
    

    
    /**
     * 
     */
    public void createComposite() {
        JPanel buttonPanel = new JPanel();
        JButton[] buttons = new JButton[10];    // create 10 buttons from 0 to 10
        p = new JPanel();
         
        buttonPanel.setLayout(new GridLayout(4,3));
        
        final InputOutputController ioController = computer.getIO();
        
        // add 10 buttons
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JButton(""+i+"");           

            buttons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // This reference can change, so we must re-fetch it.
                    ConsoleKeyboard consoleKeyboard = (ConsoleKeyboard)ioController.getDevice(InputOutputController.DEVICE_CONSOLEKEYBOARD);
                    JButton src = (JButton)e.getSource();
                    consoleKeyboard.buttonPress((Integer.parseInt(src.getText())+ 48));

                }
            });                  
             
            if(i>0)buttonPanel.add(buttons[i]);
        }
        
               
        
        buttonPanel.add(buttons[0]); // add 0 on the bottom row
        
        // add Enter/Clear buttons
        
        JButton btnEnter = new JButton("Enter");
        buttonPanel.add(btnEnter);
        btnEnter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConsoleKeyboard consoleKeyboard = (ConsoleKeyboard)ioController.getDevice(InputOutputController.DEVICE_CONSOLEKEYBOARD);
                consoleKeyboard.buttonPress(13);
            }
        });
        
        JButton btnBackspace = new JButton("<--");
        buttonPanel.add(btnBackspace);        
        btnBackspace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConsoleKeyboard consoleKeyboard = (ConsoleKeyboard)ioController.getDevice(InputOutputController.DEVICE_CONSOLEKEYBOARD);
                consoleKeyboard.buttonPress(8);
            }
        });
        
        buttonPanel.setPreferredSize(new Dimension(200, 200));
        p.add(buttonPanel);
    }
    
    /**
     * @return panel instance
     */ 
    public JComponent getGUI() {
        return p;
    }
}
