package cl.autentia.barcode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.List;

/*import org.opencv.android.Utils;
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.imgproc.Imgproc;
 */

public class Util {

    public static String savebytefile(byte[] data, String filename) {
        File file = new File(filename);

        if (file.getAbsoluteFile().exists()) {
            file.delete();
        }

        file.getParentFile().mkdirs();

        // deleteAllFilesInDirectory(file.getParentFile());

        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(data);
            out.close();
        } catch (Exception e) {
            return null;
        }
        return file.getAbsolutePath();
    }

    public static void deleteAllFilesInDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                if (getFileExtension(child).equals("data")) {
                    child.delete();
                } else {
                    deleteAllFilesInDirectory(child);
                }
            }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static int[] decodeRect(float[] params, int x, int y, double measure) {

        int[] values = new int[8];
        values[0] = (int) (x + measure * params[0]); // x1
        values[1] = (int) (y + measure * params[1]); // y1

        values[2] = (int) (x + measure * params[2]); // x2
        values[3] = (int) (y + measure * params[1]); // y2

        values[4] = (int) (x + measure * params[2]); // x3
        values[5] = (int) (y + measure * params[3]); // y3

        values[6] = (int) (x + measure * params[0]); // x4
        values[7] = (int) (y + measure * params[3]); // y4

        return values;
    }

    public static float[] rawToFingerRect(byte[] rawData) {

        String data = new String(rawData);
        String[] strValues = data.split(",");
        float[] values = new float[strValues.length];
        for (int i = 0; i < strValues.length; i++) {
            values[i] = Float.parseFloat(strValues[i]);
        }
        return values;
    }

    public static Size getMaxtPreviewSize(Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPreviewSizes();
        return Util.getMaxSize(sizes);
    }

    public static Size getMaxSize(List<Size> sizes) {
        int biggestSize = 0;
        Size biggest = null;
        for (Size size : sizes) {
            int currentSize = size.width * size.height;
            if (currentSize > biggestSize) {
                biggestSize = currentSize;
                biggest = size;
            }
        }

        return biggest;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
                Bitmap.Config.ALPHA_8);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Size getBestSize(List<Size> sizes, int idealW, int idealH) {
        double idealProportion = idealW / (idealH + 0.0);
        double bestProportion = Double.MAX_VALUE;

        // encuentro la proporsion mas sercana a la entregada
        for (Size supportedSize : sizes) {
            double proportion = supportedSize.width
                    / (supportedSize.height + 0.0);
            if (Math.abs(idealProportion - proportion) < Math
                    .abs(idealProportion - bestProportion)) {
                bestProportion = proportion;
            }
        }

        // Obtengo el tamaño mas grande de las proporciones mas cercanas.
        Size bestSize = sizes.get(0);
        for (Size supportedSize : sizes) {
            double proportion = supportedSize.width
                    / (supportedSize.height + 0.0);
            if (proportion == bestProportion
                    && (supportedSize.width > bestSize.width)) {
                bestSize = supportedSize;
            }
        }
        return bestSize;
    }

    public static Size getClosestUpSize(List<Size> sizes, int idealW) {
        // encuentro la proporsion mas sercana a la entregada
        Size bestSize = getMaxSize(sizes);
        for (Size supportedSize : sizes) {
            if (supportedSize.width >= idealW
                    && supportedSize.width < bestSize.width) {
                bestSize = supportedSize;
            }
        }
        return bestSize;

    }

    /*
     * (x1,y1) ---------- (x2,y2) | | | | | | | | | | (x4,y4) ---------- (x3,y3)
     */
    public static Rect getRectFromArray(float[] array) {
        int[] valores = new int[array.length];
        for (int i = 0; i < valores.length; i++) {
            valores[i] = Math.round(array[i]);
        }
        return getRectFromArray(valores);
    }

    public static Rect getRectFromArray(int[] array) {
        Rect retorno = new Rect();
        int x1 = array[0];
        int y1 = array[1];
        int x2 = array[2];
        int y2 = array[3];
        int x3 = array[4];
        int y3 = array[5];
        int x4 = array[6];
        int y4 = array[7];

        retorno.left = x1 < x4 ? x1 : x4;
        retorno.right = x2 > x3 ? x2 : x3;
        retorno.top = y1 < y2 ? y1 : y2;
        retorno.bottom = y3 > y4 ? y3 : y4;

        return retorno;
    }

    public static void saveGreySaleImage(byte[] data, int width, int height) {
        File photo = new File(Environment.getExternalStorageDirectory(),
                "imagen_" + System.currentTimeMillis() + ".png");
        if (photo.exists()) {
            photo.delete();
        }
        try {
            Bitmap bm = renderCroppedGreyscaleBitmap(data, width, height);
            // Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            FileOutputStream out = new FileOutputStream(photo);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveRaw(byte[] data, int width, int height) {
        File photo = new File(Environment.getExternalStorageDirectory(),
                "byte_imagen_" + width + "_" + height + "_"
                        + System.currentTimeMillis() + ".NV21");
        if (photo.exists()) {
            photo.delete();
        }
        try {
            // Bitmap bm = renderCroppedGreyscaleBitmap(data, width, height);
            FileOutputStream out = new FileOutputStream(photo);
            out.write(data);
            out.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveBitmap(Bitmap data, String filename) {
        File photo = new File(filename);
        if (photo.exists()) {
            photo.delete();
        }
        try {
            // Bitmap bm = renderCroppedGreyscaleBitmap(data, width, height);
            FileOutputStream out = new FileOutputStream(photo);
            // Bitmap binary = binarize(data);
            data.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param src
     * @return
     */
    public static Bitmap createRotateBitmap(Bitmap src) {

        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static Bitmap renderCroppedGreyscaleBitmap(byte[] data, int width,
                                                      int height) {
        int[] pixels = new int[width * height];
        byte[] yuv = data;
        int row = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int grey = yuv[row + x] & 0xff;
                pixels[row + x] = 0xFF000000 | (grey * 0x00010101);
                // pixels[row + x] = yuv[row + x];
            }
            row += width;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static void saveRGBImage(int[] data, int width, int height) {
        File photo = new File(Environment.getExternalStorageDirectory(),
                "rgb_imagen_" + System.currentTimeMillis() + ".png");
        if (photo.exists()) {
            photo.delete();
        }
        try {
            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(data, 0, width, 0, 0, width, height);
            FileOutputStream out = new FileOutputStream(photo);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveColorImage(byte[] data, int width, int height) {
        File photo = new File(Environment.getExternalStorageDirectory(),
                "color_imagen_" + System.currentTimeMillis() + ".png");
        if (photo.exists()) {
            photo.delete();
        }
        try {
            int[] rgb = convertYUV420_NV21toRGB8888(data, width, height);
            // Bitmap bm = renderCroppedGreyscaleBitmap(data, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(rgb, 0, width, 0, 0, width, height);
            FileOutputStream out = new FileOutputStream(photo);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static int[] convertYUV420_NV21toRGB8888(byte[] data, int width,
                                                    int height) {
        int size = width * height;
        int offset = size;
        int[] pixels = new int[size];
        int u, v, y1, y2, y3, y4;

        // i percorre os Y and the final pixels
        // k percorre os pixles U e V
        for (int i = 0, k = 0; i < size; i += 2, k += 2) {
            y1 = data[i] & 0xff;
            y2 = data[i + 1] & 0xff;
            y3 = data[width + i] & 0xff;
            y4 = data[width + i + 1] & 0xff;

            u = data[offset + k] & 0xff;
            v = data[offset + k + 1] & 0xff;
            u = u - 128;
            v = v - 128;

            pixels[i] = convertYUVtoRGB(y1, u, v);
            pixels[i + 1] = convertYUVtoRGB(y2, u, v);
            pixels[width + i] = convertYUVtoRGB(y3, u, v);
            pixels[width + i + 1] = convertYUVtoRGB(y4, u, v);

            if (i != 0 && (i + 2) % width == 0)
                i += width;
        }

        return pixels;
    }

    private static int convertYUVtoRGB(int y, int u, int v) {
        int r, g, b;

        r = y + (int) 1.402f * v;
        g = y - (int) (0.344f * u + 0.714f * v);
        b = y + (int) 1.772f * u;
        r = r > 255 ? 255 : r < 0 ? 0 : r;
        g = g > 255 ? 255 : g < 0 ? 0 : g;
        b = b > 255 ? 255 : b < 0 ? 0 : b;
        return 0xff000000 | (b << 16) | (g << 8) | r;
    }

    public static byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    public static byte[] getNV21(int inputWidth, int inputHeight, int[] argb) {

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        return yuv;
    }

    public static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width,
                                      int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each
                // sampled by a factor of 2
                // meaning for every 4 Y pixels there are 1 V and 1 U. Note the
                // sampling is every other
                // pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0
                        : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0
                            : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0
                            : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

    public static int[] binarize(byte[] data, int width, int height) {
        int[] pixels = new int[width * height];
        byte[] yuv = data;
        int RADIO = 4;
        int WHITE = Color.WHITE;
        int BLACK = Color.BLACK;

        int row = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int grey = yuv[row + x] & 0xff;
                pixels[row + x] = grey;
            }
            row += width;
        }

        double globalAvg = getAvg(pixels, width, height);

        row = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double colorPoint = getColor(pixels, width, height, x, y);
                double colorLocal = getAvg(pixels, width, height, x, y, RADIO);
                if (colorPoint > globalAvg) {
                    if (colorPoint < colorLocal * 0.7)
                        pixels[row + x] = BLACK;
                    else
                        pixels[row + x] = WHITE;
                } else if (colorPoint > colorLocal) {
                    pixels[row + x] = WHITE;
                } else {
                    pixels[row + x] = BLACK;
                }
            }
            row += width;
        }

        return pixels;
    }

    public static double getAvg(int[] pixels, int width, int height) {

        double sum = 0;
        double total = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Integer color = getColor(pixels, width, height, i, j);
                if (color != null) {
                    sum += color;
                    total++;
                }
            }
        }
        return sum / total;
    }

    public static double getAvg(int[] pixels, int width, int height, int x,
                                int y, int radio) {
        double sum = 0;
        double total = 0;
        for (int i = x - radio; i <= x + radio; i++) {
            for (int j = y - radio; j <= y + radio; j++) {
                Integer color = getColor(pixels, width, height, i, j);
                if (color != null) {
                    sum += color;
                    total++;
                }
            }
        }
        return sum / total;
    }

    public static Integer getColor(int[] pixels, int width, int height, int x,
                                   int y) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            return null;
        else
            return pixels[x + (y * width)];
    }

    public static Bitmap binarize(Bitmap img) {
        int radio = 4;

        Bitmap binarized = toGrayscale(img);
        int width, height;
        height = binarized.getHeight();
        width = binarized.getWidth();
        double globalAvg = getAvg(binarized);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double colorPoint = getColor(img, i, j);
                double colorLocal = getAvg(img, i, j, radio);
                if (colorPoint > globalAvg) {
                    if (colorPoint < colorLocal * 0.7)
                        binarized.setPixel(i, j, Color.BLACK);
                    else
                        binarized.setPixel(i, j, Color.WHITE);
                } else if (colorPoint > colorLocal) {
                    binarized.setPixel(i, j, Color.WHITE);
                } else {
                    binarized.setPixel(i, j, Color.BLACK);
                }

            }
        }
        return binarized;
    }

    public static double getAvg(Bitmap img) {

        double sum = 0;
        double total = 0;

        int width, height;
        height = img.getHeight();
        width = img.getWidth();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                sum += Color.red(img.getPixel(i, j));
                total++;
            }
        }
        return sum / total;
    }

    public static double getAvg(Bitmap img, int x, int y, int radio) {
        double sum = 0;
        double cant = 0;
        for (int i = x - radio; i <= x + radio; i++) {
            for (int j = y - radio; j <= y + radio; j++) {
                Integer color = getColor(img, i, j);
                if (color != null) {
                    sum += color;
                    cant++;
                }
            }
        }
        return sum / cant;
    }

    public static Integer getColor(Bitmap img, int x, int y) {
        int width, height;
        height = img.getHeight();
        width = img.getWidth();
        if (x < 0 || y < 0 || x >= width || y >= height)
            return null;
        else
            return Color.red(img.getPixel(x, y));
    }

    public static Bitmap toGrayscale2(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static byte[] toGrayscale3(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        int lenght = width * height;
        IntBuffer buffer = IntBuffer.allocate(lenght);
        bmpOriginal.copyPixelsToBuffer(buffer);

        return colorIntToGrayscaleByte(buffer.array());
    }

    public static byte[] colorIntToGrayscaleByte(int[] input) {
        byte[] output = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            // byte a = (byte) ((input[i] >> 24) & 0xFF);
            byte r = (byte) ((input[i] >> 16) & 0xFF);
            // byte g = (byte) ((input[i] >> 8) & 0xFF);
            // byte b = (byte) (input[i] & 0xFF);
            output[i] = r;
        }
        return output;
    }

}
