
package computersimulator.cpu;

/**
 *
 * @author george
 */
public class Computer {
    
    private CentralProcessingUnit cpu;
    private MemoryUnit memory;
    private InputOutputController io;

    public Computer() {
        cpu = new CentralProcessingUnit(); // contains ALU,  ControlUnit      
        memory = new MemoryUnit();  
        io = new InputOutputController();
        
        // do {
            //cpu.clockCycle();
            memory.clockCycle();
            //io.clockCycle()l
        
        // while (running);
        
    }    
    
}
