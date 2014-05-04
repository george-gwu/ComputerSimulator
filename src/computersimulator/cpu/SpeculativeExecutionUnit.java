package computersimulator.cpu;

/**
 * Unit for speculative execution
 *
 */
public class SpeculativeExecutionUnit {
    
    private MemoryControlUnit memory;
    
    public SpeculativeExecutionUnit(MemoryControlUnit memory) {
        this.memory = memory;
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
    public void scanMemory() {
     
        //TODO:
     
    }
}
