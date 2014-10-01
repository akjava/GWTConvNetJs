package com.akjava.gwt.cnnegative.client;

import com.akjava.gwt.comvnetjs.client.CascadeNet;
import com.akjava.gwt.comvnetjs.client.GWTConvNetJs;
import com.akjava.gwt.comvnetjs.client.Net;
import com.akjava.gwt.comvnetjs.client.RectGenerator;
import com.akjava.gwt.comvnetjs.client.Vol;
import com.akjava.gwt.comvnetjs.client.worker.DetectParam;
import com.akjava.gwt.comvnetjs.client.worker.HaarRect;
import com.akjava.gwt.comvnetjs.client.worker.NegativeResult;
import com.akjava.gwt.lib.client.experimental.ResizeUtils;
import com.akjava.gwt.lib.client.experimental.lbp.SimpleLBP;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;

import elemental.js.html.JsDedicatedWorkerGlobalScope;

public class FindNegative extends JsDedicatedWorkerGlobalScope implements EntryPoint {

	protected FindNegative(){
		
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
   	
   	 self.importScripts("/js/convnet.js");//need dummy window
   	 
      $workergwtbridge = function(str) {
      	 @com.akjava.gwt.cnnegative.client.FindNegative::bridge(Lcom/google/gwt/core/client/JavaScriptObject;)(str);
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
    	JsArray<NegativeResult> resultRect=JsArray.createArray().cast();
    	JsArray<Net> nets=jsonToNet(param.getJson());
    	
    	ImageData imageData=param.getImageData();
    	
    	boolean useHorizontalFlip=param.isUseHorizontalFlip();
    	JsArrayNumber turnAngles=param.getTurnAngles();
    	if(turnAngles==null){//empty
    		turnAngles=JavaScriptObject.createArray().cast();
    	}
    	
    	//LogUtils.log("horizontal:"+useHorizontalFlip+",angles="+turnAngles.length());
    	
    	if(param.getRects()==null){
    		//TODO get rect params
    		
    		JsArrayNumber numbers=param.getDetectOption();
    		
    		int stepScale=4;
    		double scale_factor=1.6;
    		
    		int minW=24;//at least get this from detector
    		int minH=24;
    		
    		double min_scale=1.2;
    		
    		//duplidate
    		if(numbers!=null){
    			if(numbers.length()>0){
    				stepScale=(int)numbers.get(0);
    			}
    			if(numbers.length()>1){
    				scale_factor=numbers.get(1);
    			}
    			if(numbers.length()>2){
    				minW=(int)numbers.get(2);
    			}
    			if(numbers.length()>3){
    				minH=(int)numbers.get(3);
    			}
    			if(numbers.length()>4){
    				min_scale=numbers.get(4);
    			}
    		}
    		
    		//detectAll();
    		param.setRects(RectGenerator.generateHaarRect(imageData.getWidth(),imageData.getHeight(), stepScale, scale_factor,minW,minH,min_scale));
    		
    	}
    	
    	for(int i=0;i<param.getRects().length();i++){
			//log("start:"+i);
			HaarRect rect=param.getRects().get(i);
			
			//Uint8ArrayNative cropped=ImageDataUtils.cropRedOnlyPacked(imageData, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
			
			/*
			String debug="";
			for(int k=0;k<cropped.length();k++){
				debug+=cropped.get(k)+",";
			}
			LogUtils.log(debug);
			*/
			
			//Uint8ArrayNative resized=ResizeUtils.resizeBilinearRedOnlyPacked(cropped, rect.getWidth(), rect.getHeight(), 36,36);
			
			//little bit improved when
			Uint8ArrayNative resized=ResizeUtils.resizeBilinearRedOnly(imageData,rect.getX(),rect.getY(), rect.getWidth(), rect.getHeight(), GWTConvNetJs.netWidth+GWTConvNetJs.edgeSize,GWTConvNetJs.netHeight+GWTConvNetJs.edgeSize);
			
			
			int[] binaryPattern=GWTConvNetJs.createLBPDepthFromUint8ArrayPacked(resized, false);
			
			Vol vol=GWTConvNetJs.createVolFromIndexes(binaryPattern,GWTConvNetJs.parseMaxLBPValue());
			
			double result=passAll(nets,vol);
			if(result!=-1){
				//rect.setConfidence(result);
				NegativeResult negative=JavaScriptObject.createObject().cast();
				negative.setVol(vol);
    			resultRect.push(negative);
			}
			//angles TODO support future
			/*
			if(result==-1){
				for(int j=0;j<turnAngles.length();j++){
					int[] turned=SimpleLBP.turn3x3(binaryPattern, (int)turnAngles.get(j));
					Vol turnedVol=GWTConvNetJs.createVolFromIndexes(turned,GWTConvNetJs.parseMaxLBPValue());
					result=passAll(nets,turnedVol);
					if(result!=-1){
						rect.setConfidence(result);
		    			resultRect.push(rect);
		    			break;
					}
				}
			}
			*/
			
			if(useHorizontalFlip){
				int[] flippedPattern=SimpleLBP.flipHorizontal(binaryPattern,GWTConvNetJs.lbpDataSplit,GWTConvNetJs.lbpDataSplit);
				Vol flipped=GWTConvNetJs.createVolFromIndexes(flippedPattern,GWTConvNetJs.parseMaxLBPValue());
				result=passAll(nets,flipped);
				if(result!=-1){
					NegativeResult negative=JavaScriptObject.createObject().cast();
					negative.setVol(flipped);
					negative.setHorizontalFlipped(true);
	    			resultRect.push(negative);
				}else{
					//angles
					/*
					for(int j=0;j<turnAngles.length();j++){
						int[] turned=SimpleLBP.turn3x3(flippedPattern, (int)turnAngles.get(j));
						Vol turnedVol=GWTConvNetJs.createVolFromIndexes(turned,GWTConvNetJs.parseMaxLBPValue());
						result=passAll(nets,turnedVol);
						if(result!=-1){
							rect.setConfidence(result);
			    			resultRect.push(rect);
			    			break;
						}
					}
					*/
				}
			}
			
			
			/*
			}else{
				
				//flip horizontal check
				
				
			}
			*/
			
			
			//dispose(imageData);
			
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
    private final native static void doPostMessage(JsArray<NegativeResult> rects) /*-{
      self.postMessage(rects);
    }-*/;
    


}
