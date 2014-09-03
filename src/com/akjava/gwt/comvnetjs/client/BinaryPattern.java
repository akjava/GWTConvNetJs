package com.akjava.gwt.comvnetjs.client;

public class BinaryPattern {
	public static int[] dataToBinaryPattern(int[][] data,int split,int edgeX,int edgeY){

		int volOff=0;
		
		
		int w=(data.length-edgeX*2)/split;
		int h=(data.length-edgeY*2)/split;
		int[] retInt=new int[8*split*split];
		for(int i=0;i<split*split;i++){
			int histgram[]=new int[8];
			int ox=i%split;
			int oy=i/split;
			int offx=edgeX/2+ox;
			int offy=edgeY/2+oy*w;
			for(int x=0;x<w;x++){
				for(int y=0;y<h;y++){
					String binary=Integer.toBinaryString(data[x+offx][y+offy]);
					for(int j=0;j<8;j++){
						if(binary.length()>j && binary.charAt(j)=='1'){
							histgram[j]++;
						}
					}
				}
				
				
			}
			
			for(int j=0;j<8;j++){
				retInt[j+8*volOff]=histgram[j];
			//vol.set(1, 1, i+8*volOff, histgram[i]);
			}
			volOff++;
		}
		
		return retInt;
	}
}
