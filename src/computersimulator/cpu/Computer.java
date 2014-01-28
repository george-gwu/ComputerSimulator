
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
        
        this.instructionCycleTest();
        
        // do {
        //    this.clockCycle();
        // while (running);
        
    }   
    
    
    public void clockCycle(){
            this.cpu.clockCycle();
            this.memory.clockCycle();
            this.io.clockCycle();                
    }
    
    private void instructionCycleTest(){
        
        // fill memory at 1111 with instruction
        this.memory.engineerSetMemoryLocation(Unit.UnitFromBinaryString("1111"), Word.WordFromBinaryString("000001 11 00 1 0 00110100"));
        // set PC to 1111
        cpu.getControlUnit().setPC(Unit.UnitFromBinaryString("1111"));
        
        for(int i=0;i<7;i++){
            this.clockCycle();
        }
        
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
