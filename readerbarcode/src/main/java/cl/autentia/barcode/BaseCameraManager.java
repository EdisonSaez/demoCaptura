package cl.autentia.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseCameraManager {
    private Point qrBoxSize;

    protected boolean hook = false;
    protected int rotate;
    protected int count = 0;
    protected boolean isRelease = true;
    protected ExecutorService executor;
    protected int displayOrientation;
    protected MultiFormatReader reader;
    protected OnResultListener onResultListener;
    protected Context context;

    public BaseCameraManager(Context context) {
        this.context = context;
        executor = Executors.newSingleThreadExecutor();
        reader = new MultiFormatReader();
        qrBoxSize = new Point();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;//Integer.parseInt(context.getResources().getString(R.string.resolution_w));
        int height = displayMetrics.heightPixels;//Integer.parseInt(context.getResources().getString(R.string.resolution_h));

        qrBoxSize.x = width; //*/(int)context.getResources().getDimension(R.dimen.width_qr_box_view);
        qrBoxSize.y = height; //*/(int) context.getResources().getDimension(R.dimen.height_qr_box_view);
    }

    protected QRResult getCodeValue(byte[] data, Point previewSize) {

        Bitmap bitmap = null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream(data.length);
        YuvImage image = new YuvImage(data, ImageFormat.NV21, previewSize.x, previewSize.y, null);
        int left = previewSize.x - previewSize.x >> 1;
        int right = previewSize.x + previewSize.x >> 1;
        int top = previewSize.y - previewSize.y >> 1;
        int bottom = previewSize.y + previewSize.y >> 1;
        Rect rect = new Rect(left, top, right, bottom);
        if (image.compressToJpeg(rect, 100, stream)) {
            byte[] bytes = stream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        try {
            stream.close();
        } catch (IOException e) {
            Log.e("onPreviewFrame", e.toString());
        }

        if (displayOrientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(displayOrientation);
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            bitmap.recycle();
            bitmap = newBitmap;
        }

        Bitmap bitmapNormal = bitmap;

        //Cédula invertida 180°
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        Bitmap bitmapRotate180 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        //Cédula en la posición original
        Result result = getResult(bitmapNormal);
        QRResult qrResult = new QRResult(bitmapNormal, result, bitmapRotate180);

        if (qrResult.getResult() != null) {
            return qrResult;
        } else {
            bitmap.recycle();
            return null;
        }
    }

    private Result getResult(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        return QRUtils.decode(new RGBLuminanceSource(width, height, pixels), reader);
    }

    public void setHook(boolean hook) {
        this.hook = hook;
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public void shutdownExecutor() {
        executor.shutdown();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public abstract void connectCamera(SurfaceHolder surfaceHolder);

    public abstract void setCameraParameter();

    public abstract void startCapture();

    public abstract void releaseCamera();

    public interface OnResultListener {
        void onResult(QRResult qrResult);
    }
}
