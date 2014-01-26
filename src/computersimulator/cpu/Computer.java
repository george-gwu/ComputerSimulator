
package computersimulator.cpu;

import computersimulator.components.*;

/**
 *
 * @author george
 */
public class Computer {
    
    private CentralProcessingUnit cpu;
    private MemoryControlUnit memory;
    private InputOutputController io;

    public Computer() {
        cpu = new CentralProcessingUnit(); // contains ALU,  ControlUnit      
        memory = new MemoryControlUnit();  
        io = new InputOutputController();
        
        this.memoryReadWriteTest();
        
                
        // do {
            //cpu.clockCycle();
            //memory.clockCycle();
            //io.clockCycle()l
        
        // while (running);
        
    }   
    
    
    private void memoryReadWriteTest(){
        
        Unit addr = Unit.UnitFromBinaryString("111");
        Word val = new Word(55);
        // Fetch (should be 0)        
        System.out.println("Fetching memory address "+addr.getBinaryString()+". Should be 0 if first run");        
        this.memory.clockCycle(); // make sure we're not busy
        this.memory.setMAR(addr);
        this.memory.clearMBR();   // wipe MBR for get
        this.memory.clockCycle(); // fetch        
        System.out.println("Result is: "+this.memory.getMBR());  
        
        
        // Set to 55
        System.out.println("Setting M("+addr.getBinaryString()+") to "+val.getValue()+".");
        this.memory.clockCycle(); // make sure we're not busy
        this.memory.setMAR(addr);
        this.memory.setMBR(val);
        this.memory.clockCycle();
        
        
        // Fetch(should be 55)
        System.out.println("Fetching memory address "+addr.getBinaryString()+". Should be "+val.getValue()+" now");         
        this.memory.clockCycle(); // make sure we're not busy
        this.memory.setMAR(addr);
        this.memory.clearMBR();   // wipe MBR for get
        this.memory.clockCycle(); // fetch           
        System.out.println("Result is: "+this.memory.getMBR());  
              
        
        
    }
    
}
