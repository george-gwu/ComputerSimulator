package computersimulator.cpu;

/**
 *
 * @author george
 */
public class CentralProcessingUnit {
    
    private ControlUnit controlUnit;
    private ArithmeticLogicUnit alu;
    private MemoryControlUnit memory;
    

    public CentralProcessingUnit(MemoryControlUnit mem) {        
        this.memory = mem;
        
        controlUnit = new ControlUnit(this.memory);   
        alu = new ArithmeticLogicUnit();
        
    }
    
    /**
     * Clock cycle. This is the main function which causes the CPU to do work.
     *  This serves as a publicly accessible method, but delegates
     * to the ALU/ControlUnit.
     */
    public void clockCycle(){
        this.controlUnit.clockCycle();
        this.alu.clockCycle();
    }           

    public ControlUnit getControlUnit() {
        return controlUnit;
    }

    public ArithmeticLogicUnit getALU() {
        return alu;
    }
    
   
    
    
}


