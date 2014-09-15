package com.akjava.gwt.comvnetjs.client.worker;

import java.util.List;

import com.akjava.gwt.lib.client.JavaScriptUtils;
import com.akjava.lib.common.graphics.Rect;
import com.google.common.collect.Lists;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public  class DetectParam extends JavaScriptObject{
	
	protected DetectParam(){}

	
	public static DetectParam create(String json,JsArray<ImageData> imageDatas,List<Rect> rects){
		DetectParam param=createObject().cast();
		param.setJson(json);
		
		
		param.setImageDatas(imageDatas);
		
		JsArray<HaarRect> rectArray=createArray().cast();
		for(Rect rect:rects){
			rectArray.push(HaarRect.create(rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight()));
		}
		param.setRects(rectArray);
	
		return param;
	}
	
	public static DetectParam create(String json,JsArray<ImageData> imageDatas,JsArray<HaarRect> rectArray){
		DetectParam param=createObject().cast();
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
	


	public final  native JsArray<ImageData> getImageDatas()/*-{
	return this.imageDatas;
	}-*/;
	public final  native void setImageDatas(JavaScriptObject  param)/*-{
	this.imageDatas=param;
	}-*/;
	
	public final static native JavaScriptObject toTransfer(JsArray<ImageData> imageDatas)/*-{
	var result=[];
	for(i=0;i<imageDatas.length;i++){
		result.push(imageDatas[i].data.buffer);
	}
	return result;
	}-*/;
}