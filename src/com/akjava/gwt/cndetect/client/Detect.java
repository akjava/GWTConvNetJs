package com.akjava.gwt.cndetect.client;

import com.akjava.gwt.comvnetjs.client.CascadeNet;
import com.akjava.gwt.comvnetjs.client.GWTConvNetJs;
import com.akjava.gwt.comvnetjs.client.Net;
import com.akjava.gwt.comvnetjs.client.Vol;
import com.akjava.gwt.comvnetjs.client.worker.DetectParam;
import com.akjava.gwt.comvnetjs.client.worker.DetectParam2;
import com.akjava.gwt.comvnetjs.client.worker.HaarRect;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;

import elemental.js.html.JsDedicatedWorkerGlobalScope;

public class Detect extends JsDedicatedWorkerGlobalScope implements EntryPoint {

	protected Detect(){
		
	}
	
	// Export the brige method when the application is loaded
  final public void onModuleLoad() {
        exportBridge();
        
    }

    // This method is the bridge between HTML worker callback and GWT/Java code
    public static void bridge(JavaScriptObject msg) {
    	detect((DetectParam2)msg.cast());
    }
    
    // create a reference in the browser js to the Java method
    
    // you can't create canvas
   final  private native void exportBridge() /*-{
   	 //console.log("v12"); //for version check but after set "worker.js?"+System.currentTimeMillis(); never load cached
   	 window=self;//not this.
   	 $wnd=self;//not this.
   	
   	 self.importScripts("/convnet.js");//need dummy window
   	 
      $workergwtbridge = function(str) {
      	 @com.akjava.gwt.cndetect.client.Detect::bridge(Lcom/google/gwt/core/client/JavaScriptObject;)(str);
      };
    }-*/;
    
   final static private native JsArray<Net> jsonToNet(String json) /*-{
 	var jsonObject=JSON.parse(json);
 	var netsJson=jsonObject.nets;
 	var result=[];
 	for(i=0;i<netsJson.length;i++){
 		var net2 = new convnetjs.Net(); // create an empty network
		net2.fromJSON(netsJson[i])
 		
 		result.push(net2);
 	}
 	return result;
  }-*/;
   

   
   public static double passAll(JsArray<Net> nets,Vol vol){
	   double r=0;
	   
	   for(int i=0;i<nets.length();i++){
		   Vol result=nets.get(i).forward(vol);
		   if(!CascadeNet.isZeroIndexMostMatched(result)){
			   return -1;
		   }
		   r=result.getW(0);
	   }
	   
	   return r;
   }
   
    public static void detect(DetectParam2 param){
    	
    	
    	
    	JsArray<HaarRect> resultRect=JsArray.createArray().cast();
    	
    	JsArray<Net> nets=jsonToNet(param.getJson());
    	
    	//log("detect");
    	//log(""+param.getImageDatas().length());
		for(int i=0;i<param.getRects().length();i++){
			//log("start:"+i);
			HaarRect rect=param.getRects().get(i);
			JsArrayNumber retInt=param.getImageDatas().get(i);
			//this must extreamly slow
			Vol vol=GWTConvNetJs.createNewVol();

					
					//set vol
					for(int j=0;j<retInt.length();j++){
						double v=(double)retInt.get(j)/128-1; //max valu is 16x16(256) to range(-1 - 1)
						//double v=(double)retInt[i]/72-1;//maybe range(-1 - 1) //split must be 2 for 12x12(144) block <-- for 24x24
						//double v=(double)retInt[i]/18-1;//maybe -1 - 1 //split must be 4 for 6x6(36) block  <-- for 24x24
						
						
						vol.set(0, 0,j,v);
					}
			//Vol vol=ConvnetJs.createGrayVolFromGrayScaleImage(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
			
			double result=passAll(nets,vol);
			if(result!=-1){
				rect.setConfidence(result);
    			resultRect.push(rect);
			}
			
			
			//dispose(imageData);
			
		}
		//log("done");
		/*
		param.setImageDatas(null);
		param.setRects(null);
		param.setJson(null);
		*/
    	
    	//recover from jsons
    	//do somethings.
    	//create image from image data
    	doPostMessage(resultRect);
    	
    }
    
    /*
     * deprecased because of memory consume
     */
    public static void detect1(DetectParam param){
JsArray<HaarRect> resultRect=JsArray.createArray().cast();
    	
    	JsArray<Net> nets=jsonToNet(param.getJson());
    	
    	
		for(int i=0;i<param.getRects().length();i++){
			HaarRect rect=param.getRects().get(i);
			ImageData imageData=param.getImageDatas().get(i);
			
			
			Vol vol=GWTConvNetJs.createVolFromGrayscaleImageData(imageData);
			
			double result=passAll(nets,vol);
			if(result!=-1){
				rect.setConfidence(result);
    			resultRect.push(rect);
			}
			
			
		}
    	doPostMessage(resultRect);
    	
    }
    
    private final native static void logVol(Vol vol) /*-{
    console.log(vol.w);
  }-*/;
    
    private final native static void log(String log) /*-{
    console.log(log);
  }-*/;
    
    private final native static void dispose(ImageData data) /*-{
    data.data.buffer=new ArrayBuffer(0);
  }-*/;
    
    // Workaround for posting from static method
    private final native static void doPostMessage(JsArray<HaarRect> rects) /*-{
      self.postMessage(rects);
    }-*/;
    


}
