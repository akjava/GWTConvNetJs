package com.akjava.gwt.comvnetjs.client;

import com.akjava.lib.common.graphics.IntRect;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;

public class RectOrder {

	private static Ordering<IntRect> ratioOrdering;
	public static Ordering<IntRect> orderByRatio(){
		if(ratioOrdering==null){
		ratioOrdering = new Ordering<IntRect>() {
			  public int compare(IntRect left, IntRect right) {
				double leftRatio=left.hasWidthAndHeight()?(double)left.getWidth()/left.getHeight():0;
				double rightRatio=right.hasWidthAndHeight()?(double)right.getWidth()/right.getHeight():0;
			    return Doubles.compare(leftRatio,rightRatio);
			  }
			};
		}
		return 	ratioOrdering;
	}
}
