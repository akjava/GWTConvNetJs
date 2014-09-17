package com.akjava.gwt.cndetect.client;

import com.akjava.gwt.comvnetjs.client.CascadeNet;
import com.akjava.gwt.comvnetjs.client.GWTConvNetJs;
import com.akjava.gwt.comvnetjs.client.Net;
import com.akjava.gwt.comvnetjs.client.Vol;
import com.akjava.gwt.comvnetjs.client.worker.DetectParam;
import com.akjava.gwt.comvnetjs.client.worker.DetectParam1;
import com.akjava.gwt.comvnetjs.client.worker.HaarRect;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.ImageDataUtils;
import com.akjava.gwt.lib.client.experimental.ResizeUtils;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;

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
    	detect((DetectParam)msg.cast());
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
   
    public static void detect(DetectParam param){
    	JsArray<HaarRect> resultRect=JsArray.createArray().cast();
    	JsArray<Net> nets=jsonToNet(param.getJson());
    	
    	ImageData imageData=param.getImageData();
    	if(param.getRects()==null){
    		//TODO get rect params
    		//detectAll();
    	}else{
    		for(int i=0;i<param.getRects().length();i++){
    			//log("start:"+i);
    			HaarRect rect=param.getRects().get(i);
    			
    			Uint8ArrayNative cropped=ImageDataUtils.cropRedOnlyPacked(imageData, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    			
    			/*
    			String debug="";
    			for(int k=0;k<cropped.length();k++){
    				debug+=cropped.get(k)+",";
    			}
    			LogUtils.log(debug);
    			*/
    			
    			Uint8ArrayNative resized=ResizeUtils.resizeBilinearRedOnlyPacked(cropped, rect.getWidth(), rect.getHeight(), 36,36);
    			
    			
    			
    			Vol vol=GWTConvNetJs.createVolFromIndexes(GWTConvNetJs.createLBPDepthFromUint8ArrayPacked(resized, false));
    			
    			double result=passAll(nets,vol);
    			if(result!=-1){
    				rect.setConfidence(result);
        			resultRect.push(rect);
    			}
    			
    			
    			//dispose(imageData);
    			
    		}
    		
    	}
    	
    	//log("detect");
    	//log(""+param.getImageDatas().length());
		
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
    public static void detect1(DetectParam1 param){
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
