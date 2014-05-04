package computersimulator.cpu;

import computersimulator.components.MachineFaultException;
import computersimulator.components.Unit;
import computersimulator.components.Word;
import java.util.HashMap;

/**
 * Unit for speculative execution
 *
 */
public class SpeculativeExecutionUnit implements IClockCycle {
    
    private MemoryControlUnit memory;
    private ControlUnit controlUnit;
    
    public SpeculativeExecutionUnit(MemoryControlUnit memory, ControlUnit controlUnit) {
        this.memory = memory;
        this.controlUnit = controlUnit;
    }
    
    @Override
    public void clockCycle(){
     
    }
    
    /**
     * 
     * @return true if jump taken
     */
    public boolean jumpTaken() {
       /*
        1. scan each memory address
        2. read the first 6 chars
        3. and look for: JZ, JNE, JCC, SOB, JGE
        
        */
        return true;
        
    }

    /**
     * 
     * @return true if jump not taken
     */
    public boolean jumpNotTaken() {
        /*
        1. scan each memory address
        2. read the first 6 chars
        3. and look for: JZ, JNE, JCC, SOB, JGE
        
        */
        return true;
        
    }
    
    /**
     * Scans memory
     */
    public void scanMemory() throws MachineFaultException {
        
        for(int m=0;m<MemoryControlUnit.getMemoryMaxSize();m++){
            Word cell = memory.engineerFetchByMemoryLocation(new Word(m));
            
            HashMap<String,Unit> hashmap = controlUnit.decodeInstructionRegister(cell);
        }                
        
        // TODO: iterate hashmap

    }
}
