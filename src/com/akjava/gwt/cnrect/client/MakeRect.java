package com.akjava.gwt.cnrect.client;

import java.util.List;

import com.akjava.gwt.comvnetjs.client.RectGenerator;
import com.akjava.gwt.comvnetjs.client.worker.MakeRectParam;
import com.akjava.lib.common.graphics.Rect;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

import elemental.js.html.JsDedicatedWorkerGlobalScope;

public class MakeRect extends JsDedicatedWorkerGlobalScope implements EntryPoint {
	protected MakeRect(){
		
	}
	@Override
	public final  void onModuleLoad() {
		 exportBridge();
	}
	
	 // This method is the bridge between HTML worker callback and GWT/Java code
    public final static void bridge(JavaScriptObject msg) {
    	doReceiveMessage(msg);
    }
    
    // create a reference in the browser js to the Java method
    private final native void exportBridge() /*-{
      $workergwtbridge = function(str) {
      	 @com.akjava.gwt.cnrect.client.MakeRect::bridge(Lcom/google/gwt/core/client/JavaScriptObject;)(str);
      };
    }-*/;
    
    public final  static void doReceiveMessage(JavaScriptObject object){
    	MakeRectParam param=object.cast();
    	List<Rect> rects=RectGenerator.generateRect(param.getImageW(),param.getImageH(), param.getStepScale(), param.getScale_factor(),param.getMinW(),param.getMinH(),param.getMin_scale());
    	JsArrayString rectsString=JsArrayString.createArray().cast();
    	for(Rect rect:rects){
    		rectsString.push(rect.toKanmaString());
    	}
    	doPostMessage(param.getName(),rectsString);
    }
    
    // Workaround for posting from static method
    private final native static void doPostMessage(String name,JsArrayString rects) /*-{
      self.postMessage({name:name,rects:rects});
    }-*/;
    
}
