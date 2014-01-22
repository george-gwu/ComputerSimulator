package computersimulator.components;

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
    
    public Unit(int Size, int value){
        if(Size>32 || Size<1){
            throw new java.lang.ArithmeticException("Unit size valid range 1-32 ("+Size+")");
           
        }
        this.size = Size;
        this.MIN_VALUE = (int)-(Math.pow(2,(Size-1)));
        this.MAX_VALUE = (int)Math.pow(2,(Size-1))-1;        
        this.setValue(value);
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
            //@TODO: or overflow?
        }
    }

    public Integer getValue() {
        return data;
    }   
    
    
    public Integer[] getBinaryArray(){
        Integer[] digits = new Integer[this.size];
        int x = this.data;
        for (int i = 0; i < this.size; ++i) {
            digits[this.size-i-1] = x & 0x1;  // mask of the lowest bit and assign it to the next-to-last
            x >>= 1; // Shift off that bit moving the next bit into place
        }

        return digits;  
    }
    
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
        return this.size+"-Bit Unit{" + "base10=" + this.data +",binary=" + this.getBinaryString() + '}';
    }
    
}
