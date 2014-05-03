package computersimulator;

import computersimulator.cpu.Computer;
import computersimulator.gui.OperatorConsole;
import javax.swing.SwingUtilities;

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
                           
        OperatorConsole opconsole = new OperatorConsole();        
        opconsole.setComputer(computer); // pass computer instance into GUI
                
        String filename = "program2.txt";
        if(args.length>0){
            filename = args[0];
        }       
        
        // Pass file from command line to IO Controller        
        computer.getIO().setFilename(filename);
                
        
        SwingUtilities.invokeLater(opconsole);
    }
    
}
