package computersimulator;

import computersimulator.components.Word;
import computersimulator.components.Unit;
import javax.swing.SwingUtilities;
import computersimulator.cpu.Computer;
import computersimulator.gui.OperatorConsole;

/**
 * 
 * @author george
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
        
             
        /* 
        Consider the LDR instruction which we did in class:
        LDR r, x, address [,I]
        A particular example is:	LDR, 3, 0, 52, I
        which says “load R3 from address 54 indirect with no indexing”  
        Let location 52 contain 100, and location 100 contain 1023
        The format in binary looks like this:
        000001 11 00 1 0 00110100
        */        
        Word example = Word.WordFromBinaryString("000001 11 00 1 0 00110100");
        System.out.println("Raw Word Before Composition: "+example+"\n");              
        
        Unit opcode = example.decomposeByOffset(0,5);
        System.out.println("OPCODE: "+opcode);                
        
        Unit rfi1 = example.decomposeByOffset(6,7);
        System.out.println("RFI1: "+rfi1);        
        
        Unit xfi1 = example.decomposeByOffset(8,9);
        System.out.println("XFI1: "+xfi1);        
      
        Unit index = example.decomposeByOffset(10);
        System.out.println("INDEX: "+index);        

        Unit trace = example.decomposeByOffset(11);
        System.out.println("TRACE: "+trace);        
        
        Unit address = example.decomposeByOffset(12,19);
        System.out.println("ADDR: "+address);        
        
        
        
        
        
        OperatorConsole opconsole = new OperatorConsole();        
        opconsole.setComputer(computer); // pass computer instance into GUI
        
        
        
        SwingUtilities.invokeLater(opconsole);
        
        
    }
    
}
