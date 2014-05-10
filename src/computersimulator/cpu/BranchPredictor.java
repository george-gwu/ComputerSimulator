package computersimulator.cpu;

import computersimulator.components.MachineFaultException;
import computersimulator.components.Unit;
import computersimulator.components.Word;
import java.util.HashMap;
import java.util.Map;

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
    private HashMap<Integer, String> branchDescriptorTable;
    
    public BranchPredictor(MemoryControlUnit memory, ControlUnit controlUnit) {
        this.memory = memory;
        this.controlUnit = controlUnit;     
        
        this.branchHistoryTable = new HashMap();
        this.branchDescriptorTable = new HashMap();
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
    
    public void setBranchDescriptor(int pcRaw, String descriptor) {
        branchDescriptorTable.put(pcRaw, descriptor);
    }
    
    /**
     * Get the branch prediction table
     * @return
     */
    public String getPredictionTableForTextArea(){
        String results="";
        
        for (Map.Entry<Integer,String> entry : branchDescriptorTable.entrySet()) {            
            Integer m = entry.getKey();
            String descriptor = entry.getValue();
            Boolean prediction = branchHistoryTable.get(m);            
            String mZeroPadded = String.format("%04d", m);
            
            results += mZeroPadded+" - "+descriptor+": " + ((prediction) ? "Branch Predicted" : "Branch Not Predicted")+"\n";
        }
        
        return results;
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
                    this.setBranchDescriptor(m, "JCC");
                    this.branchTaken(m);
                    break;
                case ControlUnit.OPCODE_JNE:
                    this.setBranchDescriptor(m, "JNE");
                    this.branchTaken(m);
                    break;
                case ControlUnit.OPCODE_JGE:                
                    this.setBranchDescriptor(m, "JGE");                  
                    this.branchTaken(m);
                    break;
                case ControlUnit.OPCODE_SOB:
                    this.setBranchDescriptor(m, "SOB");             
                    this.branchTaken(m);                                        
                    break;                
            }
            
        }         
    }
}
