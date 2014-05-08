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
    private static final int INITIAL_VALUE = 1;
    
    public int opcode = 0;

    HashMap<Integer, Integer> branchHistory = new HashMap();
    HashMap<Integer, String> predictionTable = new HashMap();
    
    public SpeculativeExecutionUnit(MemoryControlUnit memory, ControlUnit controlUnit) {
        this.memory = memory;
        this.controlUnit = controlUnit;
        
        /**Initialize the HashMap to 1.
         * 1 - Strongly not taken, 2 - Weakly not taken, 3 - Weakly taken, 4 - Strongly taken.
         */
        branchHistory.put(OPCODE_JZ,  INITIAL_VALUE);       
        branchHistory.put(OPCODE_JNE, INITIAL_VALUE);
        branchHistory.put(OPCODE_JCC, INITIAL_VALUE);
        branchHistory.put(OPCODE_SOB, INITIAL_VALUE);
        branchHistory.put(OPCODE_JGE, INITIAL_VALUE);
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
     * Updates the branch history of each branch instruction.
     * Argument type takes values 0 or 1.
     * Use 0 to signal a decrement in branch history state and 1 to signal an increment.
     * @param x
     * @param type
     */
    public void updateBranchHistory(int x, int type) {
        if (type == 0)
        {
            branchHistory.put(x, (INITIAL_VALUE - 1));
        }
        
        if (type == 1)
        {
            branchHistory.put(x, (INITIAL_VALUE + 1));
        }
    }    

    /**
     * Set the predictions for each branch instruction.
     * @param x
     * @param prediction
     */
    public void setPredictionTable(int x, int prediction) {
        /**
         * Based on branchHistory, we can populate the 'prediction' hashmap with predictions for the instructions.
         * This can then be used for printing on the GUI.
         * Sample of prediction hashmap:
         * prediction.put<"JZ", "Not speculatively executed/Not taken">;
         * prediction.put<"JNE, "Speculatively executed/Taken">
         */
        opcode = x;
        
        if (prediction > 2)
        {
            predictionTable.put(opcode, "Speculatively Executed");
        }
        
        if (prediction <=2)
        {
            predictionTable.put(opcode, "Not Speculatively Executed");
        }
    }
    /**
     * Returns the prediction table.
     */
    public HashMap getPredictionTable() {
        
        return predictionTable;
    }
    
    /**
     * Scans memory
     */
    public void scanMemory() throws MachineFaultException {
        
        for(int m=0;m<MemoryControlUnit.getMemoryMaxSize();m++){
            Word cell = memory.engineerFetchByMemoryLocation(new Word(m));
            
            HashMap<String,Unit> hashmap = controlUnit.decodeInstructionRegister(cell);
            
            opcode = hashmap.get("opcode").getUnsignedValue();
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
