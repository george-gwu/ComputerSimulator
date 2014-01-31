
package computersimulator.cpu;

import computersimulator.components.*;

/**
 *
 * @author george
 */
public class Computer implements IClockCycle {
    
    private CentralProcessingUnit cpu;
    private MemoryControlUnit memory;
    private InputOutputController io;

    public Computer() {        
        memory = new MemoryControlUnit();  
        cpu = new CentralProcessingUnit(memory); // contains ALU,  ControlUnit      
        io = new InputOutputController();
        
        // M(15) <- STR, 3, 0, 52, I
        this.memory.engineerSetMemoryLocation(new Unit(13, 15), Word.WordFromBinaryString("000010 11 00 1 0 00110100"));
        
         /* 
        LDR r, x, address [,I]	
        which says “load R3 from address 52 indirect with no indexing”  
        Let location 52 contain 100, and location 100 contain 1023
        The format in binary looks like this:
        000001 11 00 1 0 00110100
        */    
        // M(16) <- LDR, 3, 0, 52, I
        this.memory.engineerSetMemoryLocation(new Unit(13, 16), Word.WordFromBinaryString("000001 11 00 1 0 00110100"));
        
        /*
        LDA r, x, address[,I]
        which says "Load Register with Address"
        */
        // M(17) <- LDA, 3, 0, 52, I
        this.memory.engineerSetMemoryLocation(new Unit(13, 17), Word.WordFromBinaryString("000011 11 00 1 0 00110100"));        
        
        /*
        LDX x, address[,I]
        which says "Load Index Register from Memory"
        Let the memory location be 52.
        */
        // M(18) <- LDX, 3, 52, I
        this.memory.engineerSetMemoryLocation(new Unit(13, 18), Word.WordFromBinaryString("101001 00 11 1 0 00110100"));
               
        /*
        STX x, address[,I]
        which says "Store Index Register to Memory"
        Let the memory location be 52.
        */
        // M(19) <- STX, 3, 52, I
        this.memory.engineerSetMemoryLocation(new Unit(13, 19), Word.WordFromBinaryString("101010 00 11 1 0 00110100"));
        
        // set PC to 15 for testing, this will increment until no more instructions exist, then crash
        cpu.getControlUnit().setPC(new Unit(13,15));
        
        boolean running = true; // @TODO hook this to IPL button
        
        do {
            this.clockCycle();
        } while(running==true);
        
    }   
    
    
    @Override
    public final void clockCycle(){
            System.out.println("-------- CLOCK CYCLE --------");
            this.cpu.clockCycle();
            this.memory.clockCycle();
            this.io.clockCycle();                
    }                    
    
}
