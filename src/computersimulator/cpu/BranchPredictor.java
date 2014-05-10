package computersimulator.cpu;

import computersimulator.components.MachineFaultException;
import computersimulator.components.Unit;
import computersimulator.components.Word;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Unit for speculative execution.
 * Scans memory for conditional jump instructions and returns their predicted behavior.
 */
public class BranchPredictor {
    
    private final MemoryControlUnit memory;
    private final ControlUnit controlUnit;    
    
    private static final Boolean BRANCH_NOT_TAKEN = false;
    private static final Boolean BRANCH_TAKEN = true;    

    private HashMap<Integer, Boolean> branchHistoryTable;
    private HashMap<Integer, String> branchDescriptorTable;
    private HashMap<Integer, Integer> branchCountTable;
    private HashMap<Integer, Integer> branchAccuracyTable;
    
    public BranchPredictor(MemoryControlUnit memory, ControlUnit controlUnit) {
        this.memory = memory;
        this.controlUnit = controlUnit;     
        
        this.branchHistoryTable = new HashMap();
        this.branchDescriptorTable = new HashMap();
        this.branchCountTable = new HashMap();
        this.branchAccuracyTable = new HashMap();
        
    }
    
    /**
     * Update table for PC to annotate jump taken
     * @param pcRaw
     */
    public void branchTaken(int pcRaw) {  
        if(Boolean.compare(branchHistoryTable.get(pcRaw), BranchPredictor.BRANCH_TAKEN) == 0){                  
            int accurateCount = branchAccuracyTable.get(pcRaw);
            branchAccuracyTable.put(pcRaw,++accurateCount);
        }                        
        
        int branches = branchCountTable.get(pcRaw);
        branchCountTable.put(pcRaw,++branches);
        
        branchHistoryTable.put(pcRaw, BranchPredictor.BRANCH_TAKEN);  
    }
    
    /**
     * Update table for PC to annotate jump NOT taken
     * @param pcRaw
     */
    public void branchNotTaken(int pcRaw) {
        if(Boolean.compare(branchHistoryTable.get(pcRaw), BranchPredictor.BRANCH_NOT_TAKEN) == 0){                  
            int accurateCount = branchAccuracyTable.get(pcRaw);
            branchAccuracyTable.put(pcRaw,++accurateCount);
        }
                        
        int branches = branchCountTable.get(pcRaw);
        branchCountTable.put(pcRaw,++branches);
        
        branchHistoryTable.put(pcRaw, BranchPredictor.BRANCH_NOT_TAKEN);
    }
    
    public void setBranchDescriptor(int pcRaw, String descriptor) {
        branchDescriptorTable.put(pcRaw, descriptor);
        
        if(!branchCountTable.containsKey(pcRaw)){
            branchCountTable.put(pcRaw, 0);
        }
        
        if(!branchAccuracyTable.containsKey(pcRaw)){
            branchAccuracyTable.put(pcRaw, 0);
        }               
    }
    
    /**
     * Initialize the branch history table for conditional jump instructions as 'Will be taken'.
     * @param pcRaw 
     */
    public void setInitialStatus(int pcRaw){
        if(!branchHistoryTable.containsKey(pcRaw)){
            branchHistoryTable.put(pcRaw, BranchPredictor.BRANCH_TAKEN); 
        }
    }
    
    /**
     * Get the branch prediction table
     * @return
     */
    public String getPredictionTableForTextArea(){
        String results="";
        
        List<Integer> keys = new ArrayList(branchDescriptorTable.keySet());
        Collections.sort(keys);
        
        for (Integer m : keys) {            
            String descriptor = branchDescriptorTable.get(m);
            Boolean prediction = branchHistoryTable.get(m);            
            String mZeroPadded = String.format("%04d", m);
            
            int count = branchCountTable.get(m);
            int accurate = branchAccuracyTable.get(m);
            int accuracy = 0;
            if(count>0){
                accuracy = (int)Math.round((double)accurate/(double)count*100.0);
            }
            
            results += mZeroPadded+" - "+descriptor+": " + ((prediction) ? "Branch Predicted" : "No Branch           ")+" "+accuracy+"%\n";
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
                    this.setInitialStatus(m);
                    break;
                case ControlUnit.OPCODE_JNE:
                    this.setBranchDescriptor(m, "JNE");
                    this.setInitialStatus(m);
                    break;
                case ControlUnit.OPCODE_JGE:                
                    this.setBranchDescriptor(m, "JGE");                  
                    this.setInitialStatus(m);
                    break;
                case ControlUnit.OPCODE_SOB:
                    this.setBranchDescriptor(m, "SOB");             
                    this.setInitialStatus(m);                                        
                    break;                
            }
            
        }         
    }
}
