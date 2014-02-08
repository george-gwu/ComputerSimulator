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
		String res = "";								// result
		
		for (int i = operand2.length() - 1; i >= 0; i--) {
			int sum = Integer.parseInt((operand1.charAt(i)+"")) + Integer.parseInt((operand2.charAt(i)+"")) + carry;
			res = sum % 2 + res;
			
			if (sum >= 2) {
				carry = 1;
			}
		}
		res = carry + res;
		
		return res;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArchitectureProg a = new ArchitectureProg();
		System.out.println("add: " + a.addBinary("10",  "10"));

	}
}
