onmessage=function(e){
	var rects=e.data.rects;
	var imageData=e.data.imageData;
	var imageWidth=imageData.width;
	var result=[];
	var transfer=[];
	for(i=0;i<rects.length;i++){
		var r=rects[i];
		var array=new Uint8Array(new ArrayBuffer(r.width*r.height));
		var ind=0;
		for(y=r.y;y<r.y+r.height;y++){
			for(x=r.x;x<r.x+r.width;x++){
				var index=(x+y*imageWidth)*4;	//get offset-0(red)
				
				//var red=(x+y*imageWidth)*4;	//get offset-0(red)
				//var green=(x+y*imageWidth)*4+1;	
				//var blue=(x+y*imageWidth)*4+2;
				//array[ind]=Math.floor(0.299*imageData.data[red] + 0.587*imageData.data[green] + 0.114*imageData.data[blue]);
				array[ind]=imageData.data[index];
				ind++;
				
			}
		}
		result.push(array);
		transfer.push(array.buffer);
	}
	postMessage(result,transfer);
	}