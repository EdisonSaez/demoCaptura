package cl.autentia.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by UsherBaby on 2015/12/3.
 */
public class QRUtils {

    /**
     * decode a image file.
     *
     * @param url    image file path
     * @param reader Z_X_ing MultiFormatReader
     */
    public static QRResult decode(String url, MultiFormatReader reader) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(url, options);
            if (options.outWidth >= 1920) {
                options.inSampleSize = 6;
            } else if (options.outWidth >= 1280) {
                options.inSampleSize = 5;
            } else if (options.outWidth >= 1024) {
                options.inSampleSize = 4;
            } else if (options.outWidth >= 960) {
                options.inSampleSize = 3;
            }
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(url);
            if (bitmap == null) return null;
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            Bitmap bitmapRotate180 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

            Result result = decode(new RGBLuminanceSource(width, height, pixels), reader);
            if (result != null) {
                return new QRResult(bitmap, result, bitmapRotate180);
            }
            bitmap.recycle();
            return null;
        } catch (Exception e) {
            Log.e("decode exception", e.toString());
            return null;
        }
    }

    /**
     * decode a LuminanceSource bitmap.
     *
     * @param source LuminanceSource bitmap
     * @param reader Z_X_ing MultiFormatReader
     */
    public static Result decode(LuminanceSource source, MultiFormatReader reader) {
        Result result = null;
        if (source != null) {
            BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                result = reader.decodeWithState(bBitmap);
            } catch (ReaderException e) {
                result = null;
            } finally {
                reader.reset();
            }
        }
        return result;
    }

    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
    }

    public static void beep(Context context) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        mediaPlayer.start();
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    private static void writeToFile(String data, String fileName) {
        try {
            FileWriter writer = new FileWriter(new File(Environment.getExternalStorageDirectory(), fileName));
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private static void saveImage(Bitmap bitmap, String fileName) {
        FileOutputStream out = null;
        try {
            bitmap = Bitmap.createScaledBitmap(bitmap, 394, 394, true);
            out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + fileName + "_" + System.currentTimeMillis() + ".png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param bmpOriginal
     * @param config
     * @return
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal, Bitmap.Config config) {

        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, config); //.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * Genera estiramiento de contraste de un Bitmap
     *
     * @param src
     * @param cutOff
     * @return
     */
    public static Bitmap stretchHistogram(Bitmap src, double cutOff) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();

        // create a mutable empty bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        // create a canvas so that we can draw the bmOut Bitmap from source bitmap
        Canvas c = new Canvas();
        c.setBitmap(bmOut);

        // draw bitmap to bmOut from src bitmap so we can modify it
        c.drawBitmap(src, 0, 0, new Paint(Color.BLACK));

        // color information
        float[] histogram = new float[256];
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                int gray = rgb2gray(src.getPixel(x, y));
                histogram[gray] += 1;
            }
        }
        float maxCount = 0;
        for (float aCount : histogram) {
            maxCount = Math.max(maxCount, aCount);
        }
        for (int n = 0; n < histogram.length; n++) {
            histogram[n] /= maxCount;
        }
        int lowestGray = 0;
        int highestGray = 255;
        for (int n = 0; n < histogram.length; n++) {
            if (histogram[n] < cutOff) {
                lowestGray = n;
            } else break;
        }
        for (int n = 255; n >= 0; n--) {
            if (histogram[n] < cutOff) {
                highestGray = n;
            } else break;
        }
        // scan through all pixels to fix them
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = remap(Color.red(pixel), lowestGray, highestGray, 0, 255);
                G = remap(Color.green(pixel), lowestGray, highestGray, 0, 255);
                B = remap(Color.blue(pixel), lowestGray, highestGray, 0, 255);
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return bmOut;
    }

    private static int remap(int value, int lowIn, int highIn, int lowOut, int highOut) {
        int inRange = highIn - lowIn;
        int outRange = highOut - lowOut;
        float inOutScale = (float) outRange / inRange;
        return truncate((int) ((value - lowIn) * inOutScale + lowOut));
    }

    private static int rgb2gray(int pixel) {
        return truncate((int) ((Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) * 0.33333));
    }

    private static int truncate(int value) {
        if (value < 0) {
            return 0;
        } else if (value > 255) {
            return 255;
        }

        return value;
    }

}
