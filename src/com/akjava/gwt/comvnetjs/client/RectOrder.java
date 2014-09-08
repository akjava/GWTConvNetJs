package com.akjava.gwt.comvnetjs.client;

import com.akjava.lib.common.graphics.Rect;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;

public class RectOrder {

	private static Ordering<Rect> ratioOrdering;
	public static Ordering<Rect> orderByRatio(){
		if(ratioOrdering==null){
		ratioOrdering = new Ordering<Rect>() {
			  public int compare(Rect left, Rect right) {
				double leftRatio=left.hasWidthAndHeight()?(double)left.getWidth()/left.getHeight():0;
				double rightRatio=right.hasWidthAndHeight()?(double)right.getWidth()/right.getHeight():0;
			    return Doubles.compare(leftRatio,rightRatio);
			  }
			};
		}
		return 	ratioOrdering;
	}
}
