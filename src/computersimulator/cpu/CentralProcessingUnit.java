package computersimulator.cpu;

/**
 *
 * @author george
 */
public class CentralProcessingUnit implements IClockCycle {
    
    private ControlUnit controlUnit;
    private ArithmeticLogicUnit alu;
    private MemoryControlUnit memory;
    

    public CentralProcessingUnit(MemoryControlUnit mem) {        
        this.memory = mem;
        alu = new ArithmeticLogicUnit();
        
        controlUnit = new ControlUnit(this.memory, this.alu);   
        
        
    }
    
    /**
     * Clock cycle. This is the main function which causes the CPU to do work.
     *  This serves as a publicly accessible method, but delegates
     * to the ALU/ControlUnit.
     * @throws java.lang.Exception
     */
    @Override
    public void clockCycle() throws Exception{
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


