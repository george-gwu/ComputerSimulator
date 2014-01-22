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
    
}
