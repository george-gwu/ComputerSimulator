
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
        memory.clockCycle(); // make sure we're not busy
        memory.setMAR(Unit.UnitFromBinaryString("111"));
        memory.clearMBR();   // wipe MBR for get
        memory.clockCycle(); // fetch        
        System.out.println("Result is: "+memory.getMBR());  
        
        
        // Set to 55
        System.out.println("Setting M(111) to 55.");
        memory.clockCycle(); // make sure we're not busy
        memory.setMAR(Unit.UnitFromBinaryString("111"));
        memory.setMBR(new Word(55));
        memory.clockCycle();
        
        
        // Fetch(should be 55)
        System.out.println("Fetching memory address 111. Should be 55 now");         
        memory.clockCycle(); // make sure we're not busy
        memory.setMAR(Unit.UnitFromBinaryString("111"));
        memory.clearMBR();   // wipe MBR for get
        memory.clockCycle(); // fetch           
        System.out.println("Result is: "+memory.getMBR());  
              
        
        
    }
    
}
