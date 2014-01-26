package computersimulator.cpu;

/**
 *
 * @author george
 */
public class CentralProcessingUnit {
    
    private ArithmeticLogicUnit alu;
    private ControlUnit control;
    

    public CentralProcessingUnit() {        
        alu = new ArithmeticLogicUnit();
        control = new ControlUnit();   
    }
    
    /**
     * Clock cycle. This is the main function which causes the CPU to do work.
     *  This serves as a publicly accessible method, but delegates
     * to the ALU/ControlUnit.
     */
    public void clockCycle(){
        this.control.clockCycle();
        this.alu.clockCycle();
    }    
    
}
