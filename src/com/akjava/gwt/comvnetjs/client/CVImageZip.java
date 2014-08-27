package com.akjava.gwt.comvnetjs.client;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.akjava.gwt.jszip.client.JSFile;
import com.akjava.gwt.jszip.client.JSZip;
import com.akjava.gwt.lib.client.Base64Utils;
import com.akjava.gwt.lib.client.ImageElementUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.opencv.CVImageDataConverter;
import com.akjava.gwt.lib.client.experimental.opencv.CVImageeData;
import com.akjava.lib.common.io.FileType;
import com.akjava.lib.common.utils.CSVUtils;
import com.akjava.lib.common.utils.FileNames;
import com.akjava.lib.common.utils.ListUtils;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

public class CVImageZip {
public static final int POSITIVES=0;
public static final int NEGATIVES=1;
public static final int IMAGES=2;
public int type;
private Map<String,ImageElement> cachedImageElement=new HashMap<String, ImageElement>();
private boolean useCache=true;//default true
private JSZip zip;
private String name="";
public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public JSZip getZip() {
	return zip;
}

public void setZip(JSZip zip) {
	this.zip = zip;
}

public int getType() {
	return type;
}

public List<CVImageeData> getDatas() {
	return datas;
}

private List<CVImageeData> datas=Lists.newArrayList();
	public boolean isUseCache() {
	return useCache;
}

public Optional<ImageElement> getImageElement(CVImageeData data){
	ImageElement image=null;
	
	if(useCache){//use cache really consume browser memory.take care
	image=cachedImageElement.get(data.getFileName());
	}
	if(image==null){//use cache for reduce extract time,here is bottleneck but burst memory
		JSFile imgFile=zip.getFile(data.getFileName());
		if(imgFile==null){
			LogUtils.log("warning image not found in zip:"+data.getFileName());
			return Optional.absent();
		}
		
		String extension=FileNames.getExtension(data.getFileName());
		FileType type=FileType.getFileTypeByExtension(extension);//need to know jpeg or png
		String dataUrl=Base64Utils.toDataUrl(type.getMimeType(),imgFile.asUint8Array().toByteArray());//should use cache 300MB etc
		image=ImageElementUtils.create(dataUrl);
		if(useCache){
			cachedImageElement.put(data.getFileName(), image);
		}
	}
	return Optional.of(image);
}
	
public void setUseCache(boolean useCache) {
	this.useCache = useCache;
}

public CVImageZip(ArrayBuffer buffer){
	this(JSZip.loadFromArrayBuffer(buffer));
	
}
public int size(){
	return datas.size();
}
public CVImageeData get(int index){
	return datas.get(index);
}

	public CVImageZip(JSZip zip){
		this.zip=zip;
		boolean negative=false;
		JSFile indexFile=zip.getFile("info.txt");
		if(indexFile==null){
			indexFile=zip.getFile("info.dat");//i'm windows-os user and .dat extensin used other case
			if(indexFile==null){
				indexFile=zip.getFile("bg.txt");
				negative=true;
			}
		}
		
		List<String> lines=null;
		
		if(indexFile==null){
			lines=Lists.newArrayList();
			JsArrayString files=zip.getFiles();
			for(int i=0;i<files.length();i++){
				String file=files.get(i);
				if(isImageFile(file)){
					lines.add(file);
				}
				if(file.toLowerCase().endsWith(".gif")){
					LogUtils.log("gif not support yet.hard to handel this on Html5Canvas");
				}
			}
			type=IMAGES;
		}else{
			//normal info.txt is just convert
			String text=indexFile.asText();
			if(!text.isEmpty()){
				lines=CSVUtils.splitLinesWithGuava(text);
			}else{
				lines=Lists.newArrayList();
			}
			if(negative){
				type=NEGATIVES;
			}else{
				type=POSITIVES;
			}
		}
		
		datas = Lists.newArrayList(FluentIterable.from(lines).transform(new CVImageDataConverter()).toList());
	}
	
	@SuppressWarnings("unchecked")
	public void shuffle(){
		datas=ListUtils.shuffle(datas);
	}
	
	public void clearCache(){
		cachedImageElement.clear();
	}
	
	private boolean isImageFile(String name){
		name=name.toLowerCase();
		return name.endsWith(".png") ||name.endsWith(".jpg")||name.endsWith(".jpeg") ||name.endsWith(".webp");
	}
}
