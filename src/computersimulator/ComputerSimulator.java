package computersimulator;

import javax.swing.SwingUtilities;
import computersimulator.cpu.Computer;
import computersimulator.gui.OperatorConsole;

/**
 * Computer Simulator Program - This controls the GUI and instantiates a 
 * Computer, which represents the main simulator.
 */
public class ComputerSimulator {
    
    private static Computer computer;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        computer = new Computer();  // (contains memory, cpu, and IO)
               
        /** @TODO: Load ROM on boot via simple ROM loader 
         *      ROM Loader should read a boot program from a virtual card 
         *      reader to memory, then transfer execution to program.
         *      The card reader is implemented as a file. (via IO Controller?)
         *  Question: Should this be instantiated by the I/O Controller, or does vice-versa?
         */
                          
        
        OperatorConsole opconsole = new OperatorConsole();        
        opconsole.setComputer(computer); // pass computer instance into GUI
        
        
        
        SwingUtilities.invokeLater(opconsole);
        
        
    }
    
}
