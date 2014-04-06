package computersimulator.cpu;

import computersimulator.components.Unit;
import computersimulator.components.Word;

/**
 * This is the ALU class. It receives 3 inputs, processes them, and sets an 
 * output and condition flags.  It operates on the clock cycle and requires one 
 * clock cycle to process any inputs and set outputs.  All inputs are reset upon
 * computation.
 */
public class ArithmeticLogicUnit implements IClockCycle {
    

    
    // OP1 - Unit - Up to 20 Bits
    private Unit operand1;
    
    // OP2 - Unit - Up to 20 Bits
    private Unit operand2;
    
    //CTRL - Unit - ? Bits      // @TODO Verify states are correct. I used placeholders 0,1,2.
    private int control;
    public final static int CONTROL_NONE=0;
    public final static int CONTROL_ADD=1;
    public final static int CONTROL_SUBTRACT=2;  
    public final static int CONTROL_MULTIPLY=3;
    public final static int CONTROL_DIVIDE=4;
    
    
    // RES - Unit - Up to 20 Bits
    private Unit result;
    
    private int state;

    private final static int STATE_NONE = 0;
    private final static int STATE_START_COMPUTATION = 1;
    private final static int STATE_COMPUTATION_FINISHED = 2;
    
    private ControlUnit controlUnit;
    
    

    public ArithmeticLogicUnit() {
    
    }
    
    public void setControlUnit(ControlUnit c){
        this.controlUnit=c;
    }
    
   
    /**
     * Clock cycle. This is the main function which causes the ALU to do work.
     *  This serves as a publicly accessible method, but delegates to other methods.
     * @throws Exception    
     */
    @Override
    public void clockCycle() throws Exception {
        switch(this.state){
            case ArithmeticLogicUnit.STATE_START_COMPUTATION:
                if(this.operand1==null || this.operand2==null){
                    throw new Exception("ALU Error Missing Operand");
                }
                this.compute();
                break;
            case ArithmeticLogicUnit.STATE_COMPUTATION_FINISHED:                
            case ArithmeticLogicUnit.STATE_NONE:
            default:
                break;
                
                
        }
        
    }

    /**
     * This is used by the ControlUnit to tell ALU all parameters are set.
     */
    public void signalReadyToStartComputation(){
        this.setState(ArithmeticLogicUnit.STATE_START_COMPUTATION);
    }
    
    /** 
     * Used internally on the clock cycle when start computation is set.
     */
    private void compute(){
        switch(this.control){
            case ArithmeticLogicUnit.CONTROL_ADD:
                this.setResult(this.add(operand1, operand2));
                break;
            case ArithmeticLogicUnit.CONTROL_SUBTRACT:
                this.setResult(this.subtract(operand1, operand2));                 
                break;
            case ArithmeticLogicUnit.CONTROL_MULTIPLY:
                this.setResult(this.multiply(operand1, operand2));                 
                break;
            case ArithmeticLogicUnit.CONTROL_DIVIDE:
                this.setResult(this.divide(operand1, operand2)); 
            break;
            case ArithmeticLogicUnit.CONTROL_NONE:
            default:
                System.out.println("Unhandled ALU operation");
                break;
        }                
        // Reset inputs & set state to finished
        this.setControl(ArithmeticLogicUnit.CONTROL_NONE);
        this.setOperand1(null);
        this.setOperand2(null);
        this.setState(ArithmeticLogicUnit.STATE_COMPUTATION_FINISHED);
    }
        

    
    
    public Unit getOperand1() {
        return operand1;
    }

    public void setOperand1(Unit operand1) {
        this.operand1 = operand1;
    }

    public Unit getOperand2() {
        return operand2;
    }

    public void setOperand2(Unit operand2) {
        this.operand2 = operand2;
    }

    public int getControl() {
        return control;
    }

    public void setControl(int controlState) {
        this.control = controlState;
    }

    public int getState() {
        return state;
    }

    private void setState(int state) {
        this.state = state;
    }    

    public Unit getResult() {
        if(this.getState() == ArithmeticLogicUnit.STATE_COMPUTATION_FINISHED){
            return result;
        } else {
            return null;
        }
    }

    private void setResult(Unit result) {
        this.result = result;
    }
    
 
    /**
     * Perform subtract operation implements twos complement math. 
     * @param operand1
     * @param operand2
     * @return rets
     */
    private Unit subtract(Unit operand1, Unit operand2) {        
        String op1Str = operand1.getBinaryString();
        String op2Str = operand2.getBinaryString();
       
        // prepare both operands
        int diff = Math.abs(op1Str.length() - op2Str.length());
        String zeros = "";
        for (int i = 0; i < diff; i++) {
            zeros += "0";
        }
        if (op1Str.length() > op2Str.length()) {
            op2Str = zeros + op2Str;
        } else {
            op1Str = zeros + op1Str;
        }
         
        // prepare to invert bits
        Integer[] op2Binary = new Integer[op2Str.length()];
        for (int i = 0; i < op2Binary.length; i ++) {
            op2Binary[i] = Integer.parseInt(op2Str.charAt(i) + "");
        }
        for (int i = 0; i < op2Binary.length; i++) {                            
            op2Binary[i] = 1 - op2Binary[i];
        }
        op2Str = Unit.IntArrayToBinaryString(op2Binary);    // get inverted operand2
        
        // add 1 bit to operand 2 to make it negative
        String oneBitStr = createOneBitString(op1Str.length() -1);              // create 1 bit string 
        String negativeOperand2 = addBinaryOperands(op2Str, oneBitStr);		// add one bit to operand2 
        
        // Perform addition 
        String finalResultStr = addBinaryOperands(negativeOperand2, op1Str);    // add operands
        
        int size = (operand1.getSize() > operand2.getSize() ? operand1.getSize() : operand2.getSize());
        
        Unit resultTemporary = Unit.UnitFromBinaryString(finalResultStr);
        Unit resultResized = new Unit(size);        
        resultResized.setValueBinary(resultTemporary.getBinaryString());
        
         // check underflow
        long resultLong = (long)operand1.getSignedValue() - (long)operand2.getSignedValue();
        if (checkUnderflow(resultLong)) {
            this.controlUnit.setCondition(ControlUnit.CONDITION_REGISTER_UNDERFLOW);
        } else {
            this.controlUnit.unsetCondition(ControlUnit.CONDITION_REGISTER_UNDERFLOW);
        } 

        
        return resultResized;        
    }
    
    /**
     * Perform addition operation implements twos complement math. 
     * @param operand1
     * @param operand2 
     * @return  rets
     * 
     */    
    private Unit add(Unit operand1, Unit operand2){
        String op1Str = operand1.getBinaryString();
        String op2Str = operand2.getBinaryString();
        
        String finalResultStr = addBinaryOperands(op1Str, op2Str);       // add operands
              
        int size = (operand1.getSize() > operand2.getSize() ? operand1.getSize() : operand2.getSize());
        
        Unit resultTemporary = Unit.UnitFromBinaryString(finalResultStr);
        Unit resultResized = new Unit(size);        
        resultResized.setValueBinary(resultTemporary.getBinaryString());
        
         // check underflow
        long resultLong = (long)operand1.getSignedValue() + (long)operand2.getSignedValue();
        if (checkUnderflow(resultLong)) {
            this.controlUnit.setCondition(ControlUnit.CONDITION_REGISTER_UNDERFLOW);
        } else {
            this.controlUnit.unsetCondition(ControlUnit.CONDITION_REGISTER_UNDERFLOW);
        }   
          
        return resultResized;
    }      
    
    /**
     * Create 1 bit
     *
     * @param size
     * @return 1 bit binary string
     */
    private String createOneBitString(int size) {
        String str = "";
        for (int i = 0; i < size; i++) {
            str += "0";

            if (i == size - 1) {
                str += "1";
            }
        }
        return str;
    }
    
    /**
     * Add two binary numbers 
     * 
     * @param operand1
     * @param operand2
     * @return binary
     */
    private String addBinaryOperands(String operand1, String operand2) {
        int carry = 0;
        String res = "";        // result

        // prepare operands: add leading zeros if needed
        int diff = Math.abs(operand1.length() - operand2.length());
        String zeros = "";
        for (int i = 0; i < diff; i++) {
            zeros += "0";
        }
        if (operand1.length() > operand2.length()) {
            operand2 = zeros + operand2;
        } else {
            operand1 = zeros + operand1;
        }

        // perform addition on operands
        for (int i = operand2.length() - 1; i >= 0; i--) {
            int sum = Integer.parseInt((operand1.charAt(i) + "")) + Integer.parseInt((operand2.charAt(i) + "")) + carry;
            carry = 0;              // reset carry
            res = sum % 2 + res;

            if (sum >= 2) {
                carry = 1;
            }
        }

        // check if overflow occurred
        if (carry == 1) {
            System.out.println("****overflow occured**** ");
            this.controlUnit.setCondition(ControlUnit.CONDITION_REGISTER_OVERFLOW);
        } else {
            this.controlUnit.unsetCondition(ControlUnit.CONDITION_REGISTER_OVERFLOW);
        }
        return res;
    }
    
     /**
     * Multiply operation
     * @param operand1
     * @param operand2
     * @return result of multiplication
     */
    private Unit multiply(Unit operand1, Unit operand2){
        long resultLong = (long)operand1.getSignedValue() * (long)operand2.getSignedValue();
        String binaryResult =Long.toBinaryString(resultLong);
        
        do{ // sign extend result
            binaryResult = "0" + binaryResult;
        }while(binaryResult.length()<40);
        
        String truncatedResult = binaryResult.substring(binaryResult.length()-40);     
        
        if(binaryResult.length()>40){
            this.controlUnit.setCondition(ControlUnit.CONDITION_REGISTER_OVERFLOW);
        } else {
            this.controlUnit.unsetCondition(ControlUnit.CONDITION_REGISTER_OVERFLOW);
        }        
        
        // check underflow
        if (checkUnderflow(resultLong)) {
            this.controlUnit.setCondition(ControlUnit.CONDITION_REGISTER_UNDERFLOW);
        } else {
            this.controlUnit.unsetCondition(ControlUnit.CONDITION_REGISTER_UNDERFLOW);
        }   

        return Unit.UnitFromBinaryString(truncatedResult); // 40 bit result
    }
    
     /**
     * Divide operation - calculates quotient
     * @param operand1
     * @param operand2
     * @return result of division
     */
    private Unit divide(Unit operand1, Unit operand2){
        int resultQuotient;
        if(operand2.getSignedValue()==0){
            resultQuotient = 0;
            this.controlUnit.setCondition(ControlUnit.CONDITION_REGISTER_DIVZERO);
        } else{
            this.controlUnit.unsetCondition(ControlUnit.CONDITION_REGISTER_DIVZERO);
            resultQuotient = (int)Math.floor((operand1.getSignedValue() / operand2.getSignedValue()));
        }
        int resultRemainder = operand1.getSignedValue() % operand2.getSignedValue();
        
        Word quotient = new Word(resultQuotient);
        Word remainder = new Word(resultRemainder);
        
        
        // Overload quotient & remainder into one 40 bit value
        Unit results = new Unit(40);
        results.setValueBinary(quotient.getBinaryString() + remainder.getBinaryString());
        
        // check underflow
        long resultLong = (long)operand1.getSignedValue() / (long)operand2.getSignedValue();
        if (checkUnderflow(resultLong)) {
            this.controlUnit.setCondition(ControlUnit.CONDITION_REGISTER_UNDERFLOW);
        } else {
            this.controlUnit.unsetCondition(ControlUnit.CONDITION_REGISTER_UNDERFLOW);
        }   

                
        return results; // 40 bit result
    }      
    
   /**
     * Check for overflow conditions
     * @param result
     * @return return true if overflow occurs
     */
    public boolean checkOverflow(long result) {
	boolean overflow = false;
		
	if (result > Integer.MAX_VALUE) {
            overflow = true;
	}		
	return overflow;
    }
    
    /**
     * Check for underflow conditions
     * @param result
     * @return return true if underflow occurs
     */
    public boolean checkUnderflow(long result) {
	boolean underflow = false;
		
	if (result < Integer.MIN_VALUE) {
            underflow = true;
	}		
	return underflow;
    }
}
