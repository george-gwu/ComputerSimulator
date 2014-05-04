package computersimulator.cpu;

import computersimulator.components.MachineFaultException;
import computersimulator.components.Unit;
import computersimulator.components.Word;
import java.util.HashMap;

/**
 * Unit for speculative execution
 *
 */
public class SpeculativeExecutionUnit {
    
    private MemoryControlUnit memory;
    private ControlUnit controlUnit;
    
    public SpeculativeExecutionUnit(MemoryControlUnit memory, ControlUnit controlUnit) {
        this.memory = memory;
        this.controlUnit = controlUnit;
    }
    
    /**
     * 
     * @return true if jump taken
     */
    public boolean jumpTaken(Unit pc) {
       /*        
        1. Update table for PC to annotate jump taken
        
        */
        return true;
        
    }

    /**
     * 
     * @return true if jump not taken
     */
    public boolean jumpNotTaken(Unit pc) {
        /*
        1. Update table for PC to annotate NOT jump taken
        
        */
        return true;
        
    }
    
    /**
     * TBD: Determine return structure... hashmap?
     */
    public void getPredictionTable(){
        
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
