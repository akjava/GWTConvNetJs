package com.akjava.gwt.comvnetjs.client.worker;

import java.util.List;

import com.akjava.lib.common.graphics.IntRect;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;

public  class DetectParam extends JavaScriptObject{
	
	protected DetectParam(){}

	
	public static DetectParam create(String json,ImageData imageData,List<IntRect> rects){
		DetectParam param=createObject().cast();
		param.setJson(json);
		
		
		param.setImageData(imageData);
		
		JsArray<HaarRect> rectArray=createArray().cast();
		for(IntRect rect:rects){
			rectArray.push(HaarRect.create(rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight()));
		}
		param.setRects(rectArray);
	
		return param;
	}
	
	public static DetectParam create(String json,ImageData imageDatas,JsArray<HaarRect> rectArray){
		DetectParam param=createObject().cast();
		param.setJson(json);
		
		
		param.setImageData(imageDatas);
		
		
		param.setRects(rectArray);
	
		return param;
	}
	
	public final  native boolean isUseHorizontalFlip()/*-{
	return this.useHorizontalFlip;
	}-*/;
	public final  native void setUseHorizontalFlip(boolean  param)/*-{
	this.useHorizontalFlip=param;
	}-*/;	
	
	public final  native JsArrayNumber getTurnAngles()/*-{
	return this.turnAngles;
	}-*/;
	public final  native void setTurnAngles(JsArrayNumber  param)/*-{
	this.turnAngles=param;
	}-*/;	
	
	public final  native JsArrayNumber getDetectOption()/*-{
	return this.detectOption;
	}-*/;
	public final  native void setDetectOption(JsArrayNumber  param)/*-{
	this.detectOption=param;
	}-*/;	
	
	
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
	


	public final  native ImageData getImageData()/*-{
	return this.imageData;
	}-*/;
	public final  native void setImageData(JavaScriptObject  param)/*-{
	this.imageData=param;
	}-*/;
	
	public final static native JavaScriptObject toTransfer(JsArray<ImageData> imageDatas)/*-{
	var result=[];
	for(i=0;i<imageDatas.length;i++){
		result.push(imageDatas[i].data.buffer);
	}
	return result;
	}-*/;
}