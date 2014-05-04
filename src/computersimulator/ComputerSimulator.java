package computersimulator;

import computersimulator.cpu.Computer;
import computersimulator.cpu.ControlUnit;
import computersimulator.gui.OperatorConsole;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        
        Logger.getLogger("").setLevel(Level.ALL);
        
        Handler[] handlers = Logger.getLogger( "" ).getHandlers();
        for ( int index = 0; index < handlers.length; index++ ) {
            handlers[index].setLevel( Level.INFO); // This is what level is output
        }
                
        
        SwingUtilities.invokeLater(opconsole);
    }
    
}
