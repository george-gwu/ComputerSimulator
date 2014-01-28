package computersimulator.components;

import java.util.Arrays;

/**
 * I'm not sure if this is the best way to store a word, but I figure we should
 * build a primitive class and add methods to it. I'm trying to figure out how 
 * we can implement a 20-bit word size and handle overflow, etc.  My thought is
 * to use a 32-bit integer as the internal storage, but only expose 20 bits?
 * Any better ideas?
 * 
 * @author george
 */
public class Unit {
    
    private Integer data;
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
        
        // Calculate 0xn for MIN_VALUE
        char[] zeros = new char[Size];
        Arrays.fill(zeros, '0');
        this.MIN_VALUE = Integer.parseInt(new String(zeros),2);
        
        // Calculate 1xn for MAX_VALUE
        char[] ones = new char[Size];
        Arrays.fill(ones, '1');
        this.MAX_VALUE = Integer.parseInt(new String(ones),2);        
        
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
        
        int size = binary.length();
        int intValue = Integer.parseInt(binary, 2);
        
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
            this.data = value;
        } else {
            throw new java.lang.ArithmeticException("{"+value+"} Out Of Range: ["+this.MIN_VALUE+" through "+this.MAX_VALUE+"]"); 
            //@TODO: this is a great location to throw a special overflow exception which can be caught later
        }
    }
    
    /**
     *
     * @param addee The Unit to add to this Unit. (ie. EAX = EAX + addee)
     */
    public void add(Unit addee){
        // @TODO: This needs to implement twos complement math. 
        // convert this and addee to binary, run twos complement addition, then add
        // this should be modifying the return, which creates this:
        // EAX = EAX + EBX(addee)
        
        //@TODO This also needs to handle type conversion ie. adding a 13 bit value to a 20 bit
        //@TODO This also needs to be tested for negative numbers
        
                   
        //@TODO This is the hack fix.. Convert this.       
        int combined = (this.getValue() + addee.getValue());
        this.setValue(combined);                
    }
    
    

    /**
     * This method is probably unused outside this class due to the Integer storage type (why it is private).
     * @return raw value as Integer
     */
    public Integer getValue() {
        return data;
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
     *
     * @return Binary representation as a String
     */
    public String getBinaryString(){
        StringBuilder result = new StringBuilder();
        Integer[] arr = getBinaryArray();
        for (Integer el : arr) {
            result.append(el);
        }
        return result.toString();
    }          
    
    @Override
    public String toString() {
        return this.size+"-Bit Unit{" + "base10=" + this.getValue() +" (v:["+this.MIN_VALUE+"to"+this.MAX_VALUE+"]),binary=" + this.getBinaryString() + '}';
    }
    
}
