package computersimulator.cpu;

import computersimulator.components.HaltSystemException;

/**
 * The CPU class primarily represents a placeholder for ALU and the ControlUnit.
 * It also passes the reference to memory to symbolize that communication buffer.
 */
public class CentralProcessingUnit implements IClockCycle {
    
    private ControlUnit controlUnit;
    private ArithmeticLogicUnit alu;
    private MemoryControlUnit memory;
    private InputOutputController ioController;
    private Computer computer;
    
    private Boolean running = false;
    

    public CentralProcessingUnit(MemoryControlUnit mem) {        
        this.memory = mem;
        alu = new ArithmeticLogicUnit();
        
        controlUnit = new ControlUnit(this.memory, this.alu);   
        alu.setControlUnit(controlUnit); // exchange reference
        
        ioController = new InputOutputController();

    }
    
    /**
     * Clock cycle. This is the main function which causes the CPU to do work.
     *  This serves as a publicly accessible method, but delegates
     * to the ALU/ControlUnit/IOController.
     * @throws java.lang.Exception
     */
    @Override
    public void clockCycle() throws Exception{       
        try {
            this.controlUnit.clockCycle();
            this.alu.clockCycle();
            this.ioController.clockCycle();
        } catch(HaltSystemException hse){
            this.running=false;
        }
    }           

    public ControlUnit getControlUnit() {
        return controlUnit;
    }

    public ArithmeticLogicUnit getALU() {
        return alu;
    }
    
    private InputOutputController getIOController() {
        return ioController;
    }
    
    public Boolean isRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }
    
    
}


