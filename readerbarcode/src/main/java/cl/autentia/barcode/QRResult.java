package cl.autentia.barcode;

import android.graphics.Bitmap;

import com.google.zxing.Result;

/**
 * Created by UsherBaby on 2015/12/4.
 */
public class QRResult {
    private Bitmap bitmap;
    private Result result;
    private Bitmap bitmapInvert180;

    public QRResult(Bitmap bitmap, Result result, Bitmap bitmapInvert180) {
        this.bitmap = bitmap;
        this.result = result;
        this.bitmapInvert180 = bitmapInvert180;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Bitmap getBitmapInvert180() {
        return bitmapInvert180;
    }

    public void setBitmapInvert180(Bitmap bitmapInvert180) {
        this.bitmapInvert180 = bitmapInvert180;
    }
}
