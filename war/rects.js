	function generateRect(minW,minH,stepScale,scale,rects,imageW,imageH) {
	
		var clipWidth=Math.floor(minW*scale);
		var clipHeight=Math.floor(minH*scale);
        var step_x = (0.5 * scale + 1.5)*stepScale;//usually stepScale use for reduce detect rect size.js is really slow than C-lang
        var step_y = step_x;
        
        var sharedRect={x:0,y:0,width:clipWidth,height:clipHeight};
        var endX=imageW-clipWidth;
        var endY=imageH-clipHeight;
        
        
        for(var x=0;x<endX;x+=step_x){
        	for(var y=0;y<endY;y+=step_y){
        		var dx=Math.floor(x);
        		var dy=Math.floor(y);
        		sharedRect.x=(dx);
        		sharedRect.y=(dy);
        		rects.push({x:sharedRect.x,y:sharedRect.y,width:sharedRect.width,height:sharedRect.height});
        	}
        }
	}
        
    	function generate(imageW,imageH,stepScale,scale_factor,minW,minH,min_scale) {
    		var rects=[];
    		while(minW*min_scale<imageW && minH*min_scale<imageH){
    			generateRect(minW,minH,stepScale,min_scale,rects,imageW,imageH);
    			min_scale*=scale_factor;
    		}
    		
    		return rects;
    	}