
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
        
        
         /* 
        Consider the LDR instruction which we did in class:
        LDR r, x, address [,I]
        A particular example is:	LDR, 3, 0, 52, I
        which says “load R3 from address 54 indirect with no indexing”  
        Let location 52 contain 100, and location 100 contain 1023
        The format in binary looks like this:
        000001 11 00 1 0 00110100
        */    
        // Set Memory location 15 to the instruction LDR
        this.memory.engineerSetMemoryLocation(new Unit(13, 15), Word.WordFromBinaryString("000001 11 00 1 0 00110100"));
        
        // TEAM:  You can add more test instructions here. add a 16, then 17, then 18, etc.
        //this.memory.engineerSetMemoryLocation(new Unit(13, 16), Word.WordFromBinaryString("000001 11 00 1 0 00110100"));

        // Set memory location 16 to the instruction STR
        this.memory.engineerSetMemoryLocation(new Unit(13, 16), Word.WordFromBinaryString("000010 11 00 1 0 00110100"));
        
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
        
    
    
    private void memoryReadWriteTest(){
        
        Unit addr = Unit.UnitFromBinaryString("1111");
        Word val = new Word(55);
        
        // Fetch (should be 0)        
        System.out.println("Fetching memory address "+addr.getBinaryString()+". Should be 0 if first run");        
        this.clockCycle(); // make sure we're not busy
        this.memory.setMAR(addr);
        this.memory.clearMBR();   // wipe MBR for get
        this.clockCycle(); // fetch        
        System.out.println("Result is: "+this.memory.getMBR());  
        
        
        // Set to 55
        System.out.println("Setting M("+addr.getBinaryString()+") to "+val.getValue()+".");
        this.clockCycle(); // make sure we're not busy
        this.memory.setMAR(addr);
        this.memory.setMBR(val);
        this.clockCycle();
        
        
        // Fetch(should be 55)
        System.out.println("Fetching memory address "+addr.getBinaryString()+". Should be "+val.getValue()+" now");         
        this.clockCycle(); // make sure we're not busy
        this.memory.setMAR(addr);
        this.memory.clearMBR();   // wipe MBR for get
        this.clockCycle(); // fetch           
        System.out.println("Result is: "+this.memory.getMBR());  
              
        
        
    }
    
}
