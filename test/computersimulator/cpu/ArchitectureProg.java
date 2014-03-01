package computersimulator.cpu;

/**
 * Class for exercising/testing new code
 *
 * @author pawel
 *
 */
public class ArchitectureProg {

    /**
     *  Add two binary numbers
         int carry = 0 ;
   		for ( int i = 0 ; i < N ; i++ )
        { 
        int sum = xi + yi + carry ;
        zi = sum % 2 ;
        if ( sum >= 2 )
           carry = 1 ;
     *
     * @param operand1
     * @param operand2
     * @return binary
     */
    public String addBinary(String operand1, String operand2) {
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

        // check if overflow occured
        if (carry == 1) {
            System.out.print("\noverflow occured. ");
        }
        return res;

    }

    public void retrieve16LowHighOrderBits(Integer index) {
        Integer low = index & 0xffff; 				// extract low 16 bits
        Integer hi = (index >> 15) & 0xffff; 		// extract high 16 bits.

        System.out.println("16 low bits: " + Integer.toBinaryString(low)
                + ", 16 high bits: " + Integer.toBinaryString(hi));
    }
    
    public void retrieve20LowHighOrderBits(Integer index) {
        Integer low = index & 0xfffff; 				// extract low 20 bits
        Integer hi = (index >> 12) & 0xfffff; 		// extract high 20 bits.

        System.out.println("20 low bits: " + Integer.toBinaryString(low)
                + ", 20 high bits: " + Integer.toBinaryString(hi));
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ArchitectureProg a = new ArchitectureProg();

        // add binary test cases
        System.out.println("add operands:");
        System.out.println(a.addBinary("0010", "0010"));
        System.out.println(a.addBinary("0010", "10"));
        System.out.println(a.addBinary("10", "0000010"));
        System.out.println(a.addBinary("10", "10"));
        System.out.println(a.addBinary("111111111", "111111111"));

        System.out.println("############################################");

        
        int num = 2147483646;
        System.out.println("number " + Integer.toBinaryString(num));
        
        // retrieve 16 low/high order bits
        a.retrieve16LowHighOrderBits(num);
        
        // retrieve 20 low/high order bits 
        a.retrieve20LowHighOrderBits(num);
 
    }
}
