package computersimulator.cpu;

import computersimulator.components.MachineFaultException;
import computersimulator.components.Unit;
import computersimulator.components.Word;
import java.util.HashMap;

/**
 * Unit for speculative execution
 *
 */
public class BranchPredictor {
    
    private final MemoryControlUnit memory;
    private final ControlUnit controlUnit;    
    
    private static final Boolean BRANCH_NOT_TAKEN = false;
    private static final Boolean BRANCH_TAKEN = true;    

    private HashMap<Integer, Boolean> branchHistoryTable;
    
    public BranchPredictor(MemoryControlUnit memory, ControlUnit controlUnit) {
        this.memory = memory;
        this.controlUnit = controlUnit;     
        
        this.branchHistoryTable = new HashMap();
    }
    
    /**
     * Update table for PC to annotate jump taken
     * @param pcRaw
     */
    public void branchTaken(int pcRaw) {       
        branchHistoryTable.put(pcRaw, BranchPredictor.BRANCH_TAKEN);        
    }
    
    /**
     * Update table for PC to annotate jump NOT taken
     * @param pcRaw
     */
    public void branchNotTaken(int pcRaw) {       
        branchHistoryTable.put(pcRaw, BranchPredictor.BRANCH_NOT_TAKEN);        
    }    
    
    /**
     * Get the branch prediction table
     * @return
     */
    public HashMap<Integer, Boolean> getPredictionTable(){
        return this.branchHistoryTable;
    }

   
    /**
     * Scans memory looking for all conditional branch instructions.
     * @throws computersimulator.components.MachineFaultException
     */
    public void scanMemory() throws MachineFaultException {
        
        for(int m=0;m<MemoryControlUnit.getMemoryMaxSize();m++){
            Word cell = memory.engineerFetchByMemoryLocation(new Word(m));
            
            HashMap<String,Unit> decodedInstruction = controlUnit.decodeInstructionRegister(cell);
            
            Unit opcodeUnit = decodedInstruction.get("opcode");
            int opcode = opcodeUnit.getUnsignedValue();
            
            switch(opcode){
                case ControlUnit.OPCODE_JCC:
                case ControlUnit.OPCODE_JNE:
                case ControlUnit.OPCODE_JGE:                
                case ControlUnit.OPCODE_SOB:
                    
                    this.branchTaken(m);                                        
                
            }
            
        }         
    }
}
