package com.akjava.gwt.comvnetjs.client;

import java.util.List;

import com.akjava.lib.common.graphics.Rect;
import com.google.common.collect.Lists;

public class RectGenerator {
	private RectGenerator(){}
	public static List<Rect> generateRect(int imageW,int imageH,int stepScale,double scale_factor,int minW,int minH,double min_scale) {
		//Stopwatch watch=Stopwatch.createStarted();
		List<Rect> rects=Lists.newArrayList();
		
		
		
		
		
		while(minW*min_scale<imageW && minH*min_scale<imageH){
			generateRect(minW,minH,stepScale,min_scale,rects,imageW,imageH);
			min_scale*=scale_factor;
		}
		
		//LogUtils.log("genrate-rect-time:"+rects.size()+",time="+watch.elapsed(TimeUnit.SECONDS)+"s");
		
		return rects;
	}
	public static void generateRect(int minW,int minH,int stepScale,double scale,List<Rect> rects,int imageW,int imageH) {
		int clipWidth=(int) (minW*scale);
		int clipHeight=(int)(minH*scale);
        double step_x = (0.5 * scale + 1.5)*stepScale;//usually stepScale use for reduce detect rect size.js is really slow than C-lang
        double step_y = step_x;
        
        Rect sharedRect=new Rect(0,0,clipWidth,clipHeight);
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
}
