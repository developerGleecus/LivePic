package com.relevance.photoextension.utility;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BitmapTool {
public BitmapTool() {
}
/** Setting Bitmap Configuration and properties */
  public Bitmap change(String path) {

    Bitmap bm = null;   
    
    BitmapFactory.Options optsa = new BitmapFactory.Options();
    optsa.inSampleSize = 10;
    bm = BitmapFactory.decodeFile(path, optsa);
    
    if(bm != null) {
    } else {
      return null;
    }

    /***/
   if((bm.getHeight() ) <=150 || (bm.getWidth()) <=100 ) {
    optsa.inSampleSize = 1;
 	     bm = BitmapFactory.decodeFile(path, optsa);
    }
   else  if( (bm.getHeight() )<=300  || (bm.getWidth()) <= 200){
    optsa.inSampleSize =10;
    bm = BitmapFactory.decodeFile(path, optsa);
}
   
   else if(  ( (bm.getHeight() )<=450 ) ||(  (bm.getWidth()) <= 300)){
    optsa.inSampleSize =20;
    bm = BitmapFactory.decodeFile(path, optsa);
 } 
   else if(  ((bm.getHeight() )<=600 ) ||(  (bm.getWidth()) <= 400)){
    optsa.inSampleSize =30;
    bm = BitmapFactory.decodeFile(path, optsa);
  } 
   else if(  ( (bm.getHeight() )<=750 ) ||(   (bm.getWidth()) <= 500)) {
    optsa.inSampleSize = 40;
    bm = BitmapFactory.decodeFile(path, optsa);
  } 
   else  if(  ((bm.getHeight() )<=900 ) ||(   (bm.getWidth()) <= 600)) {
    optsa.inSampleSize = 50;
    bm = BitmapFactory.decodeFile(path, optsa);
  } 
   else  {
 	     optsa.inSampleSize = 60;
 	     bm = BitmapFactory.decodeFile(path, optsa);
   } 
   
    return bm;
  }
  
  /**  ,*/
  public Bitmap charge(String path) {
    Bitmap bm = this.change(path);
    int sWidth = bm.getWidth();
    int sHeigth = bm.getHeight();
    
    if(bm != null) {
      bm = this.zoomImage(bm,  sWidth,sHeigth);  
    } 
    
    return bm;
  }

  /***
     * 
     *
     * @param bgimage
     *            ?
     * @param newWidth
     *            ?
     * @param newHeight
     *            ?
     * @return
     */
    public Bitmap zoomImage(Bitmap bgimage, int newWidth, int newHeight) {
            // 
            int width = bgimage.getWidth();
            int height = bgimage.getHeight();
            // matrix
            Matrix matrix = new Matrix();
            // ?
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // 
            matrix.postScale(scaleWidth, scaleHeight);            
            Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, width, height,
                            matrix, true);
            return bitmap;
    }
}