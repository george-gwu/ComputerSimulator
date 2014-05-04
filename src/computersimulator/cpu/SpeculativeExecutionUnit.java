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
    
    private static final int OPCODE_JZ=10;
    private static final int OPCODE_JNE=11;
    private static final int OPCODE_JCC=12;
    private static final int OPCODE_SOB=16;
    private static final int OPCODE_JGE=17;

    
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
            
            int opcode = hashmap.get("opcode").getUnsignedValue();
            if ((opcode == OPCODE_JZ) 
                || (opcode == OPCODE_JNE)
                || (opcode == OPCODE_JCC)
                || (opcode == OPCODE_SOB)
                || (opcode == OPCODE_JGE)) {
                
                jumpTaken(cell);
                
            } else {
               jumpNotTaken(cell); 
            }

        }                
    }
}
