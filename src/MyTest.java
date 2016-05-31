import junit.framework.TestCase;

import com.akjava.gwt.lib.client.experimental.lbp.BinaryPattern;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;


public class MyTest extends TestCase {

	public void testLength1(){
		
		int[][] data={{0,0},{0,0}};
		
		int[] result=BinaryPattern.dataToBinaryPattern(data,2,0,0);
		
		assertEquals(8*2*2, result.length);
	}
	
public void testLength2(){
		
		int[][] data={{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
		
		int[] result=BinaryPattern.dataToBinaryPattern(data,4,0,0);
		
		assertEquals(8*4*4, result.length);
	}


public void testValue1(){
	
	int[][] data={{1,1},{1,1}};
	
	int[] result=BinaryPattern.dataToBinaryPattern(data,2,0,0);
	
	
	String hexText=Joiner.on("").join(Ints.asList(result));
	assertEquals("10000000100000001000000010000000", hexText);
}

public void testBinary1(){
	String binary=Integer.toBinaryString(3);
	assertEquals("11",binary);
}
public void testValue2(){
	
	int[][] data={{1,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
	
	int[] result=BinaryPattern.dataToBinaryPattern(data,4,0,0);
	
	
	String hexText=Joiner.on("").join(Ints.asList(result));
	assertEquals("10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", hexText);
}

}
