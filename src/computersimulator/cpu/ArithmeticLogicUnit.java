package computersimulator.cpu;

import computersimulator.components.Unit;

/**
 *
 * @author george
 */
public class ArithmeticLogicUnit {
    
    //CC	4 bits	Condition Code: set when arithmetic/logical operations are executed; it has four 1-bit elements: overflow, underflow, division by zero, equal-or-not. They may be referenced as cc(1), cc(2), cc(3), cc(4). Or by the names OVERFLOW, UNDERFLOW, DIVZERO, EQUALORNOT
    private Unit conditionCode;

    public ArithmeticLogicUnit() {
        this.conditionCode = new Unit(4);   // @TODO: GT, EQ, LT ?
        
    }
    
    
}
