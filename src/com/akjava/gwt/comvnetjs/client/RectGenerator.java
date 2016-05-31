package com.akjava.gwt.comvnetjs.client;

import java.util.List;

import com.akjava.gwt.comvnetjs.client.worker.HaarRect;
import com.akjava.lib.common.graphics.SimpleRect;
import com.akjava.lib.common.graphics.IntRect;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class RectGenerator {
	//TODO need test for sure both result is same
	private RectGenerator(){}
	public static JsArray<HaarRect> generateHaarRect(int imageW,int imageH,int stepScale,double scale_factor,int minW,int minH,double min_scale) {
		//Stopwatch watch=Stopwatch.createStarted();
		JsArray<HaarRect> rects=JavaScriptObject.createArray().cast();
		
		
		
		
		
		while(minW*min_scale<imageW && minH*min_scale<imageH){
			generateHaarRect(minW,minH,stepScale,min_scale,rects,imageW,imageH);
			min_scale*=scale_factor;
		}
		
		
		return rects;
	}
	public static void generateHaarRect(int minW,int minH,int stepScale,double scale,JsArray<HaarRect> rects,int imageW,int imageH) {
		int clipWidth=(int) (minW*scale);
		int clipHeight=(int)(minH*scale);
        double step_x = (0.5 * scale + 1.5)*stepScale;//usually stepScale use for reduce detect rect size.js is really slow than C-lang
        double step_y = step_x;
        
        IntRect sharedRect=new IntRect(0,0,clipWidth,clipHeight);
        int endX=imageW-clipWidth;
        int endY=imageH-clipHeight;
        
        
        for(double x=0;x<endX;x+=step_x){
        	for(double y=0;y<endY;y+=step_y){
        		int dx=(int) x;
        		int dy=(int)y;
        		sharedRect.setX(dx);
        		sharedRect.setY(dy);
        		rects.push(HaarRect.create(sharedRect.getX(), sharedRect.getY(), sharedRect.getWidth(), sharedRect.getHeight()));
        	}
        }
        
       
	}
	
	public static List<IntRect> generateRect(int imageW,int imageH,int stepScale,double scale_factor,int minW,int minH,double min_scale) {
		//Stopwatch watch=Stopwatch.createStarted();
		List<IntRect> rects=Lists.newArrayList();
		
		
		
		
		
		while(minW*min_scale<imageW && minH*min_scale<imageH){
			generateRect(minW,minH,stepScale,min_scale,rects,imageW,imageH);
			min_scale*=scale_factor;
		}
		
		//LogUtils.log("genrate-rect-time:"+rects.size()+",time="+watch.elapsed(TimeUnit.SECONDS)+"s");
		
		return rects;
	}
	public static void generateRect(int minW,int minH,int stepScale,double scale,List<IntRect> rects,int imageW,int imageH) {
		int clipWidth=(int) (minW*scale);
		int clipHeight=(int)(minH*scale);
        double step_x = (0.5 * scale + 1.5)*stepScale;//usually stepScale use for reduce detect rect size.js is really slow than C-lang
        double step_y = step_x;
        
        IntRect sharedRect=new IntRect(0,0,clipWidth,clipHeight);
        int endX=imageW-clipWidth;
        int endY=imageH-clipHeight;
        
        
        for(double x=0;x<endX;x+=step_x){
        	for(double y=0;y<endY;y+=step_y){
        		int dx=(int) x;
        		int dy=(int)y;
        		sharedRect.setX(dx);
        		sharedRect.setY(dy);
        		rects.add(sharedRect.copy());
        	}
        }
	}
	
	public static List<SimpleRect> generateIntegerRect(int imageW,int imageH,int stepScale,double scale_factor,int minW,int minH,double min_scale) {
		//Stopwatch watch=Stopwatch.createStarted();
		List<SimpleRect> rects=Lists.newArrayList();
		
		
		
		
		
		while(minW*min_scale<imageW && minH*min_scale<imageH){
			generateIntegerRect(minW,minH,stepScale,min_scale,rects,imageW,imageH);
			min_scale*=scale_factor;
		}
		
		//LogUtils.log("genrate-rect-time:"+rects.size()+",time="+watch.elapsed(TimeUnit.SECONDS)+"s");
		
		return rects;
	}
	public static void generateIntegerRect(int minW,int minH,int stepScale,double scale,List<SimpleRect> rects,int imageW,int imageH) {
		int clipWidth=(int) (minW*scale);
		int clipHeight=(int)(minH*scale);
        double step_x = (0.5 * scale + 1.5)*stepScale;//usually stepScale use for reduce detect rect size.js is really slow than C-lang
        double step_y = step_x;
        
        int endX=imageW-clipWidth;
        int endY=imageH-clipHeight;
        
        
        for(double x=0;x<endX;x+=step_x){
        	for(double y=0;y<endY;y+=step_y){
        		int dx=(int) x;
        		int dy=(int)y;
        		
        		rects.add(new SimpleRect(dx, dy, clipWidth, clipHeight));
        	}
        }
	}
}
