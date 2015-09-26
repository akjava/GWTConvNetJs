package com.akjava.gwt.comvnetjs.client.worker;

import java.util.List;

import com.akjava.gwt.comvnetjs.client.Vol;
import com.akjava.lib.common.graphics.Rect;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;

public  class DetectParam2 extends JavaScriptObject{
	
	protected DetectParam2(){}

	
	public static DetectParam2 create(String json,JsArray<Vol> imageDatas,List<Rect> rects){
		DetectParam2 param=createObject().cast();
		param.setJson(json);
		
		
		param.setImageDatas(imageDatas);
		
		JsArray<HaarRect> rectArray=createArray().cast();
		for(Rect rect:rects){
			rectArray.push(HaarRect.create(rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight()));
		}
		param.setRects(rectArray);
	
		return param;
	}
	
	public static DetectParam2 create(String json,JsArray<JsArrayNumber> imageDatas,JsArray<HaarRect> rectArray){
		DetectParam2 param=createObject().cast();
		param.setJson(json);
		
		
		param.setImageDatas(imageDatas);
		
		
		param.setRects(rectArray);
	
		return param;
	}
	
	
	public final  native String getJson()/*-{
	return this.json;
	}-*/;
	public final  native void setJson(String  param)/*-{
	this.json=param;
	}-*/;	
	


	public final  native JsArray<HaarRect> getRects()/*-{
	return this.rects;
	}-*/;
	public final  native void setRects(JavaScriptObject  param)/*-{
	this.rects=param;
	}-*/;	
	


	public final  native JsArray<JsArrayNumber> getImageDatas()/*-{
	return this.imageDatas;
	}-*/;
	public final  native void setImageDatas(JavaScriptObject  param)/*-{
	this.imageDatas=param;
	}-*/;
	
	public final static native JavaScriptObject toTransfer(JsArray<Vol> imageDatas)/*-{
	var result=[];
	for(i=0;i<imageDatas.length;i++){
		result.push(imageDatas[i].w.buffer);
	}
	return result;
	}-*/;
}