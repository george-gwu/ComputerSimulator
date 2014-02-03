package computersimulator.cpu;

import computersimulator.components.Unit;

/**
 *
 * @author george
 * @author pawel
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

    public ArithmeticLogicUnit() {
        this.conditionCode = new Unit(4);   // @TODO: GT, EQ, LT ?        
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
        
        StringBuilder result = new StringBuilder();
        for (Integer el : raw) {
            result.append(el);
        }
        this.conditionCode.setValueBinary(result.toString());        
    }
    
    /**
     * Unset a Condition Flag
     * Usage:  this.unsetCondition(ArithmeticLogicUnit.CONDITION_REGISTER_OVERFLOW);
     * @param ConditionRegister (see static variables)
     */
    private void unsetCondition(int ConditionRegister){
        Integer[] raw = this.conditionCode.getBinaryArray();
        raw[ConditionRegister] = 0;
        
        StringBuilder result = new StringBuilder();
        for (Integer el : raw) {
            result.append(el);
        }
        this.conditionCode.setValueBinary(result.toString());         
    }
    
    /**
     * Clear any previously set condition codes
     */
    private void clearConditions(){
        this.conditionCode.setValueBinary("0000");
    }
    
    
    /**
     * Clock cycle. This is the main function which causes the ALU to do work.
     *  This serves as a publicly accessible method, but delegates to other methods.
     */
    @Override
    public void clockCycle(){
        
    }  
    
    /**
     * Perform subtract operation implements twos complement math. 
     * Convert this and addee to binary, run twos complement addition, then add
     * @param operand1
     * @param operand2 
     * 
     * tested for negative numbers 
     */
    public int subtract(Unit operand1, Unit operand2) {
        int combined = 0;
        Integer[] op1Binary = operand1.getBinaryArray();
        Integer[] op2Binary = operand2.getBinaryArray();

        for (int i = 0; i < op2Binary.length; i++) {                            // invert bits
            op2Binary[i] = 1 - op2Binary[i];
        }

        String op1Str = intArrayToString(op1Binary);				// convert to string format
        String op2Str = intArrayToString(op2Binary);

        // add 1 bit
        String oneBitStr = createOneBitString(op2Binary.length - 1);            // create 1 bit string 
        String oper2withOneBit = addBinary(op2Str, oneBitStr);			// add one bit to operand2 (this will create negative number)

        // Perform addition (which is really subtraction since operand2 is negative)
        String finalResultStr = addBinary(oper2withOneBit, op1Str);             // add operands

        if (finalResultStr.length() >= operand2.getSize()) {                    // this is to return the right range of binary string 
            combined = bTD(finalResultStr.substring(finalResultStr.length() - operand2.getSize()));        
        } else {
            combined = bTD(finalResultStr); 
        }
        return combined;                                                        // return result
    }
    
    public int add(Unit operand1, Unit operand2){
        int combined = 0;
        Integer[] op1Binary = operand1.getBinaryArray();
        Integer[] op2Binary = operand2.getBinaryArray();
        
        String op1Str = intArrayToString(op1Binary);				// convert to string format
        String op2Str = intArrayToString(op2Binary);
        
        String finalResultStr = addBinary(op1Str, op2Str);                      // add operands
        
        if (finalResultStr.length() >= operand2.getSize()) {                    // this is to return the right range of binary string 
            combined = bTD(finalResultStr.substring(finalResultStr.length() - operand2.getSize()));        
        } else {
            combined = bTD(finalResultStr); 
        }
        return combined;
    }

    /**
     * Binary do decimal - used for subtractions 
     *
     * @param bin
     * @return
     */
    private int bTD(String bin) {
        int dec = 0;
        for (int i = bin.length() - 1, j = 0; i >= 0; i--, j++) {
            String curr = bin.charAt(i) + "";

            // check for negative bit (left most position)
            if (i == 0 && curr.equals("1")) {
                dec = (int) (dec - (Integer.parseInt(curr) * Math.pow(2, j)));
            } else {

                dec = (int) (dec + Integer.parseInt(curr) * Math.pow(2, j));
            }
        }
        return dec;
    }
    

    /**
     * Convert an array of ints to String
     *
     * @param a
     * @return
     */
    private String intArrayToString(Integer[] a) {
        String s = "";
        for (int i = 0; i < a.length; i++) {
            s = s + a[i];
        }
        return s;
    }

    /**
     * Create 1 bit format: ie. 000000001
     *
     * @param size
     * @return
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
