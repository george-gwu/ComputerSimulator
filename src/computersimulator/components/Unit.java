package computersimulator.components;

/**
 * A Unit is the primary component for data inside our Computer Simulator. It
 * allows our computer to maintain N-bit data types. It also allows quick debugging
 * and access to both base-10 and base-2 values.
 */
public class Unit {
    
    private String data;   
    private final int size;


    private final int MIN_VALUE;
    private final int MAX_VALUE;
    
    public Unit(int Size) {
        this(Size,0);
    }

    
    public Unit(int Size, int Value){
        if(Size>32 || Size<1){
            throw new java.lang.ArithmeticException("Unit size valid range 1-32 ("+Size+")");
           
        }
        this.size = Size;
        
        // MAX VALUE = ((2xy(n-1))-1)      
        this.MAX_VALUE = Math.max((int)(Math.pow(2, (this.size-1))-1),1);
        // MIN VALUE = -((2xy(n-1))-1)
        this.MIN_VALUE = Math.min(0, -(this.MAX_VALUE));
      
        
        this.setValue(Value);
    }
    
    
    /**
     * Copy constructor
     * @param c
     */
    public Unit(Unit c){
        this.data = c.data;
        this.size = c.size;
        this.MIN_VALUE = c.MIN_VALUE;
        this.MAX_VALUE = c.MAX_VALUE;
    }    

    /** 
     * Creates a Unit from a Binary String. This method allows for spacing which is trimmed for readability.
     * @param binaryReadable Binary String
     * @return Unit 
     */
    public static Unit UnitFromBinaryString(String binaryReadable){              
        String binary = binaryReadable.replace(" ", "");
        
        // original size
        int size = binary.length();
        
        // sign extend to use Long class for conversion to signed int (Java Integer class is unsigned)
        while(binary.length() < 32){
            binary = binary.substring(0,1) + binary;
        }        
        
        Long longValue = Long.parseLong(binary,2);        
        int intValue = longValue.intValue();
        
        return new Unit(size, intValue);       
    }
    
    /**
     *
     * @return Number of Bits of Unit 
    */
    public int getSize() {
        return size;
    }

    /**
     *
     * @param value
     * @throws ArithmeticException
     */
    public final void setValue(int value) throws java.lang.ArithmeticException {        
        if(value <= this.MAX_VALUE && value >= this.MIN_VALUE){           
            this.data = Integer.toBinaryString(value);
        } else {
            throw new java.lang.ArithmeticException("{"+value+"} Out Of Range: ["+this.MIN_VALUE+" through "+this.MAX_VALUE+"]"); 
            //@TODO: this is a great location to throw a special overflow exception which can be caught later
        }
    }        

    /**
     * This method is probably unused outside this class due to the Integer storage type (why it is private).
     * @return raw value as Integer
     */
    public Integer getValue() {        
        return this.getLongValue().intValue();
    }
    
    public Long getLongValue(){
        return Long.parseLong(this.data, 2);
    }
    
    /**
     * Decomposes a larger Unit and returns a smaller Unit by offset
     * @param start Start Index
     * @param stop  Stop Index
     * @return Unit(Start to Stop)
     */
    public Unit decomposeByOffset(int start, int stop){
        Integer[] digits = this.getBinaryArray();
        StringBuilder tempBinaryString = new StringBuilder();
        
        for(int i=start; i<=stop;i++){
            tempBinaryString.append(digits[i]);            
        }
        
        String binary = tempBinaryString.toString();
        
        int intValue = Integer.parseInt(binary, 2);
        
        return new Unit(binary.length(), intValue);        
    }

    /**
     * Extract out a single bit from a Unit and return a smaller Unit
     * @param index
     * @return Unit(index)
     */
    public Unit decomposeByIndex(int index){
        Integer[] digits = this.getBinaryArray();
        
        int intValue = Integer.parseInt(String.valueOf(digits[index]), 2);
        
        return new Unit(1, intValue);        
    }

    /** 
     * Alias for decompose by index for cleaner code.
     * @param index
     * @return Unit(index)
     */
    public Unit decomposeByOffset(int index){       
        return this.decomposeByIndex(index);       
    }
    
    
    /**
     *
     * @return Array of Bits (Only possible values are 1/0 despite integer storage)
     */
    public Integer[] getBinaryArray(){
        Integer[] digits = new Integer[this.size];
        int x = this.getValue();
        for (int i = 0; i < this.size; ++i) {
            digits[this.size-i-1] = x & 0x1;  // mask of the lowest bit and assign it to the next-to-last
            x >>= 1; // Shift off that bit moving the next bit into place
        }

        return digits;  
    }
    
    /**
     * Sets value using an array of integers
     * @param raw Integer Binary Array
     */
    public void setValueBinaryArray(Integer[] raw){
        this.setValueBinary(Unit.IntArrayToBinaryString(raw));
    }
    
    /**
     * Perform Shift Operation 
     * @param leftRight shifted left (L/R =1) or right (L/R = 0)
     * @param count number of times to shift
     * @param algorithmicLogical logically (A/L = 1) or arithmetically (A/L = 0)
     */
    public void shiftByCount(int leftRight, int count, int algorithmicLogical){
        if(leftRight==1){ // left
            // logical / algorithmic are identical
            while(count>0){
                Integer[] shiftTemp= this.getBinaryArray();
                for(int i=0; i<this.size-1; i++){
                    shiftTemp[i] = shiftTemp[i+1];
                }
                shiftTemp[this.size-1] = 0; // shiftOff in Rotate
                this.setValueBinaryArray(shiftTemp);
                count--;
            }
        } else { // right            
            while(count>0){
                Integer[] shiftTemp= this.getBinaryArray();
                int shiftOn=(algorithmicLogical==0) ? shiftTemp[0] : 0; 

                for(int i=this.size-1; i>0; i--){
                    shiftTemp[i] = shiftTemp[i-1];
                }
                shiftTemp[0] = shiftOn;
                this.setValueBinaryArray(shiftTemp);
                count--;
            }
            
        }
    }
    
    /**
     * Perform Rotate Operation 
     * @param leftRight rotated left (L/R =1) or right (L/R = 0)
     * @param count number of times to rotate
     */
    public void rotateByCount(int leftRight, int count){
        if(leftRight==1){ // left
            // logical / algorithmic are identical
            while(count>0){
                Integer[] shiftTemp= this.getBinaryArray();
                int shiftOff = shiftTemp[0]; // shifted on to the right side below
                for(int i=0; i<this.size-1; i++){
                    shiftTemp[i] = shiftTemp[i+1];
                }
                shiftTemp[this.size-1] = shiftOff; 
                this.setValueBinaryArray(shiftTemp);
                count--;
            }
        } else { // right            
            while(count>0){
                Integer[] shiftTemp= this.getBinaryArray();
                int shiftOff = shiftTemp[this.size-1]; // used in rotate
                for(int i=this.size-1; i>0; i--){
                    shiftTemp[i] = shiftTemp[i-1];
                }
                shiftTemp[0] = shiftOff;
                this.setValueBinaryArray(shiftTemp);
                count--;
            }            
        }
    }    
    
    /**
     *
     * @return Binary representation as a String
     */
    public String getBinaryString(){        
        Integer[] arr = getBinaryArray();
        return Unit.IntArrayToBinaryString(arr);                
    }
    
    public static String IntArrayToBinaryString(Integer [] arr){
        StringBuilder result = new StringBuilder() ;
        for (Integer el : arr) {
            result.append(el);
        }
        return result.toString();
    }

    /**
     * Accepts a binary string and loads it into the Unit
     * @param binary
     */
    public void setValueBinary(String binary){
        if(binary.length()==this.size){
            this.data = binary;
        } else if(binary.length() < this.size){
            do { // extend sign until bitsize matches
                binary = binary.substring(0,1) + binary;
            } while(binary.length() < this.size);
        } else {
            this.data = binary.substring(binary.length()-this.size); // cut from end (overflow left)
            System.out.println("!!!!!overflow in set value -- this should never happen!!!!!");
        }
            
    }
    
    @Override
    public String toString() {
        return "Unit("+this.size+"){" + "b10=" + this.getValue() +" (v:["+this.MIN_VALUE+"to"+this.MAX_VALUE+"]), b2=" + this.getBinaryString() + '}';
    }
    
}
