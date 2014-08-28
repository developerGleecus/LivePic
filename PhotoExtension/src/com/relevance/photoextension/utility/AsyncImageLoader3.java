package com.relevance.photoextension.utility;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

public class AsyncImageLoader3 {

//Listing Images
	
public Map<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
private ExecutorService executorService = Executors.newFixedThreadPool(5); // 
private final Handler handler = new Handler();

/**
* 
* @param imageUrl
*            url
* @param callback
*            
* @return ?null
*/
public Bitmap loadBitmap(final String imageUrl, final ImageView image,final ImageCallback callback) {
// 
if (imageCache.containsKey(imageUrl)) {
SoftReference<Bitmap> softReference = imageCache.get(imageUrl);
if (softReference.get() != null) {
return softReference.get();
}
}
// Reading from SDcard
executorService.submit(new Runnable() {
public void run() {
try {
final Bitmap bitmap = loadImageFromSD(imageUrl); 
imageCache.put(imageUrl, new SoftReference<Bitmap>(
bitmap));

handler.post(new Runnable() {
public void run() {
callback.imageLoaded(bitmap, image,imageUrl);
}
});
} catch (Exception e) {
throw new RuntimeException(e);
}
}
});
return null;
}

// Loading Images from SDcard
protected Bitmap loadImageFromSD(String path) {
try {
// ??
//	SystemClock.sleep(2000);
Bitmap bm = new BitmapTool().charge(path);
return bm;
} catch (Exception e) {
throw new RuntimeException(e);
}
}

// Image CallBack for Other Classes 
public interface ImageCallback {
//  
public void imageLoaded(Bitmap imageDrawable, ImageView image, String imageUrl);
}

}