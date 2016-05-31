onmessage=function(e){
var data=e.data;
postMessage(encodeBase64(data));
}

//based http://stackoverflow.com/questions/12710001/how-to-convert-uint8-array-to-base64-encoded-string
function encodeBase64(u8Arr){
var CHUNK_SIZE = 0x8000; //arbitrary number
var index = 0;
var length = u8Arr.length;
var result = '';
var slice;
while (index < length) {
slice = u8Arr.subarray(index, Math.min(index + CHUNK_SIZE, length)); 
result += String.fromCharCode.apply(null, slice);
index += CHUNK_SIZE;
}
return btoa(result);

}