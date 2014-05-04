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

    HashMap<String, Integer> branchHistory = new HashMap();
    HashMap<String, String> prediction = new HashMap();
    
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
    public HashMap getPredictionTable(){
        
        /**Initialize the HashMap to 1.
         * 1 - Strongly not taken, 2 - Weakly not taken, 3 - Weakly taken, 4 - Strongly taken.
         */
        branchHistory.put("JZ", 1);       
        branchHistory.put("JNE", 1);
        branchHistory.put("JCC", 1);
        branchHistory.put("SOB", 1);
        branchHistory.put("JGE", 1);
        
        /**
         * Based on branchHistory, we can populate the 'prediction' hashmap with predictions for the instructions.
         * This can then be used for printing on the GUI.
         * Sample of prediction hashmap:
         * prediction.put<"JZ", "Not speculatively executed/Not taken">;
         * prediction.put<"JNE, "Speculatively executed/Taken">
         */
        
        return prediction;
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
