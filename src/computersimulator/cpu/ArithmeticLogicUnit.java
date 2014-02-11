package computersimulator.cpu;

import computersimulator.components.Unit;

/**
 * This is the ALU class. It receives 3 inputs, processes them, and sets an 
 * output and condition flags.  It operates on the clock cycle and requires one 
 * clock cycle to process any inputs and set outputs.  All inputs are reset upon
 * computation.
 */
public class ArithmeticLogicUnit implements IClockCycle {
    
    //CC	4 bits	Condition Code: set when arithmetic/logical operations are executed; 
    //          it has four 1-bit elements: overflow, underflow, division by zero, equal-or-not. 
    //          OVERFLOW[0], UNDERFLOW[1], DIVZERO[2], EQUALORNOT[3]
    private Unit conditionCode;
    
    private final static int CONDITION_REGISTER_OVERFLOW = 0;
    private final static int CONDITION_REGISTER_UNDERFLOW = 1;
    private final static int CONDITION_REGISTER_DIVZERO = 2;
    private final static int CONDITION_REGISTER_EQUALORNOT = 3;
    
    // OP1 - Unit - Up to 20 Bits
    private Unit operand1;
    
    // OP2 - Unit - Up to 20 Bits
    private Unit operand2;
    
    //CTRL - Unit - ? Bits      // @TODO Verify states are correct. I used placeholders 0,1,2.
    private int control;
    public final static int CONTROL_NONE=0;
    public final static int CONTROL_ADD=1;
    public final static int CONTROL_SUBTRACT=2;
    
    // RES - Unit - Up to 20 Bits
    private Unit result;
    
    private int state;

    private final static int STATE_NONE = 0;
    private final static int STATE_START_COMPUTATION = 1;
    private final static int STATE_COMPUTATION_FINISHED = 2;
    
    

    public ArithmeticLogicUnit() {
        this.conditionCode = new Unit(4);   // @TODO: GT, EQ, LT ?        
    }
    
   
    /**
     * Clock cycle. This is the main function which causes the ALU to do work.
     *  This serves as a publicly accessible method, but delegates to other methods.
     */
    @Override
    public void clockCycle(){
        switch(this.state){
            case ArithmeticLogicUnit.STATE_START_COMPUTATION:
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
        this.clearConditions();
        switch(this.control){
            case ArithmeticLogicUnit.CONTROL_ADD:
                this.setResult(this.add(operand1, operand2));
                break;
            case ArithmeticLogicUnit.CONTROL_SUBTRACT:
                // attempt to fix subtract, ready to test
                this.setResult(this.subtract(operand1, operand2));                 
                //@TODO: Hack fix. Subtract is broken
                //this.setResult(new Unit(operand1.getSize(), (operand1.getValue() - operand2.getValue())));
                break;
            case ArithmeticLogicUnit.CONTROL_NONE:
            default:
                //@TODO Handle error.
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
    
        
   
    public Unit getConditionCode() {
        return conditionCode;
    }    
    
    /**
     * Set a Condition Flag
     * Usage:  this.setCondition(ArithmeticLogicUnit.CONDITION_REGISTER_OVERFLOW);
     * @param ConditionRegister (see static variables)
     */
    private void setCondition(int ConditionRegister){
        Integer[] raw = this.conditionCode.getBinaryArray();
        raw[ConditionRegister] = 1;
        
        StringBuilder ret = new StringBuilder();
        for (Integer el : raw) {
            ret.append(el);
        }
        this.conditionCode.setValueBinary(ret.toString());        
    }
    
    /**
     * Unset a Condition Flag
     * Usage:  this.unsetCondition(ArithmeticLogicUnit.CONDITION_REGISTER_OVERFLOW);
     * @param ConditionRegister (see static variables)
     */
    private void unsetCondition(int ConditionRegister){
        Integer[] raw = this.conditionCode.getBinaryArray();
        raw[ConditionRegister] = 0;
        
        StringBuilder ret = new StringBuilder();
        for (Integer el : raw) {
            ret.append(el);
        }
        this.conditionCode.setValueBinary(ret.toString());         
    }
    
    /**
     * Clear any previously set condition codes
     */
    private void clearConditions(){
        this.conditionCode.setValueBinary("0000");
    }
    
 
    /**
     * Perform subtract operation implements twos complement math. 
     * @param operand1
     * @param operand2
     * @return rets
     */
    public Unit subtract(Unit operand1, Unit operand2) {        
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
        Unit resultResized = new Unit(size, resultTemporary.getValue());        
        
        return resultResized;        
    }
    
    /**
     * Perform addition operation implements twos complement math. 
     * @param operand1
     * @param operand2 
     * @return  rets
     * 
     */    
    public Unit add(Unit operand1, Unit operand2){
        String op1Str = operand1.getBinaryString();
        String op2Str = operand2.getBinaryString();
        
        String finalResultStr = addBinaryOperands(op1Str, op2Str);       // add operands
              
        int size = (operand1.getSize() > operand2.getSize() ? operand1.getSize() : operand2.getSize());
        
        Unit resultTemporary = Unit.UnitFromBinaryString(finalResultStr);
        Unit resultResized = new Unit(size, resultTemporary.getValue());
          
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
            this.setCondition(ArithmeticLogicUnit.CONDITION_REGISTER_OVERFLOW);
        }
        return res;
    }


    /**
     * TODO: to be removed
     * Add two binary numbers (in binary formats) Credit:
     * http://tianrunhe.wordpress.com/2012/07/08/sum-of-two-binary-strings-add-binary/
     *
     * @param a
     * @param b
     * @return
     */
    private String addBinary(String a, String b) {
        if (b.indexOf('1') == -1) {
            return a.indexOf('1') == -1 ? a : a.substring(a.indexOf('1'));
        }
        int diff = Math.abs(a.length() - b.length());
        if (a.length() > b.length()) {
            for (int i = 0; i < diff; ++i) {
                b = '0' + b;
            }
        } else {
            for (int i = 0; i < diff; ++i) {
                a = '0' + a;
            }
        }

        String sum = new String();
        String carry = "0";
        for (int i = a.length() - 1; i >= 0; --i) {
            if ((a.charAt(i) == '1' && b.charAt(i) == '1')
                    || (a.charAt(i) == '0' && b.charAt(i) == '0')) {
                sum = '0' + sum;
            } else {
                sum = '1' + sum;
            }
            if (a.charAt(i) == '1' && b.charAt(i) == '1') {
                carry = '1' + carry;
            } else {
                carry = '0' + carry;
            }
        }
        return addBinary(sum, carry);
    }
}
