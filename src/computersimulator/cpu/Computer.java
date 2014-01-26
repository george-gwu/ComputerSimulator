
package computersimulator.cpu;

import computersimulator.components.*;

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
        
        this.memoryReadWriteTest();
        
                
        // do {
            //cpu.clockCycle();
            //memory.clockCycle();
            //io.clockCycle()l
        
        // while (running);
        
    }   
    
    
    private void memoryReadWriteTest(){
        
        // Fetch (should be 0)
        System.out.println("Fetching memory address 111. Should be 0 if first run");        
        this.memory.clockCycle(); // make sure we're not busy
        this.memory.setMAR(Unit.UnitFromBinaryString("111"));
        this.memory.clearMBR();   // wipe MBR for get
        this.memory.clockCycle(); // fetch        
        System.out.println("Result is: "+this.memory.getMBR());  
        
        
        // Set to 55
        System.out.println("Setting M(111) to 55.");
        this.memory.clockCycle(); // make sure we're not busy
        this.memory.setMAR(Unit.UnitFromBinaryString("111"));
        this.memory.setMBR(new Word(55));
        this.memory.clockCycle();
        
        
        // Fetch(should be 55)
        System.out.println("Fetching memory address 111. Should be 55 now");         
        this.memory.clockCycle(); // make sure we're not busy
        this.memory.setMAR(Unit.UnitFromBinaryString("111"));
        this.memory.clearMBR();   // wipe MBR for get
        this.memory.clockCycle(); // fetch           
        System.out.println("Result is: "+this.memory.getMBR());  
              
        
        
    }
    
}
