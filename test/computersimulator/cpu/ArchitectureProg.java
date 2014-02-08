package computersimulator.cpu;

/**
 * This is a holder class for exercising and testing different ideas/calculations
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
           
	 * @param operand1
	 * @param operand2
	 * @return binary 
	 */
	public String addBinary(String operand1, String operand2) {
		int carry = 0 ;
		String res = "";        // result
                
                // prepare operands: add leading zeros if needed
                int diff = Math.abs(operand1.length() - operand2.length());
                String zeros = "";
                for (int i = 0; i < diff; i ++) {
                        zeros +="0";
                }
                if (operand1.length() > operand2.length()) {
                    operand2 = zeros + operand2;
                } else {
                    operand1 = zeros + operand1;
                }
                
                
		// perform addition on operands
		for (int i = operand2.length() - 1; i >= 0; i--) {
			int sum = Integer.parseInt((operand1.charAt(i)+"")) + Integer.parseInt((operand2.charAt(i)+"")) + carry;
                        carry = 0;              // reset carry
			res = sum % 2 + res;
			
			if (sum >= 2) {
				carry = 1;   
			}
		}
		
                // check if overflow occured
                if (carry == 1)
                    System.out.print("\noverflow occured. ");
		return res;
	}
        
       
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArchitectureProg a = new ArchitectureProg();
                
                // test cases
		System.out.println("add operands: " + a.addBinary("0010",  "0010"));
                
                System.out.println("add operands: " + a.addBinary("0010",  "10"));
                
                System.out.println("add operands: " + a.addBinary("10",  "0000010"));
                          
                System.out.println("add operands: " + a.addBinary("10",  "10"));

	}
}
