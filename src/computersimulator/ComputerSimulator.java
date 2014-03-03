package computersimulator;

import computersimulator.components.*;
import javax.swing.SwingUtilities;
import computersimulator.cpu.Computer;
import computersimulator.gui.OperatorConsole;
import computersimulator.io.ReadFilebyJava;

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
         */
            
        OperatorConsole opconsole = new OperatorConsole();        
        opconsole.setComputer(computer); // pass computer instance into GUI
    
        String filename = null;
        if(args.length>0){
            filename = args[0];
        } else {
            filename = "src/computersimulator/io/input.txt";
        }
 
        ReadFilebyJava fileReader=new ReadFilebyJava();
        fileReader.ReadFromFile(filename, computer, new Unit(13,63));
        
        /***** Testing Data *****/
        computer.getMemory().engineerSetMemoryLocation(new Unit(13, 128), new Word(256));
        computer.getMemory().engineerSetMemoryLocation(new Unit(13, 256), new Word(1023));
        computer.getMemory().engineerSetMemoryLocation(new Unit(13, 512), new Word(1024));
        computer.getMemory().engineerSetMemoryLocation(new Unit(13, 383 ), new Word(768));
        computer.getCpu().getControlUnit().setGeneralPurposeRegister(2, new Word(255));
        computer.getCpu().getControlUnit().setIndexRegister(1, new Unit(13,255));
        computer.getMemory().setMAR(new Unit(13,1));
        computer.getCpu().getControlUnit().setProgramCounter(new Unit(13, 1)); // Start at 1
        /**************************/
        
        // JMP,64  acts as our bootloader
        computer.getMemory().engineerSetMemoryLocation(new Unit(13, 1), Word.WordFromBinaryString("00110100000001000000"));
                
        
        
        
        
        SwingUtilities.invokeLater(opconsole);
        
        
    }
    
}
