package cl.autentia;

import android.graphics.Bitmap;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

/**
 * Created by iroman on 04-01-2017.
 */

public class ImageUtils {

      public static byte[] bitmapToByteArray(Bitmap bitmap) {
          byte[] result = new byte[bitmap.getHeight() * bitmap.getWidth()];
          for (int y = 0; y < bitmap.getHeight(); y++) {
              for (int x = 0; x < bitmap.getWidth(); x++) {
                  int p = bitmap.getPixel(x, y);
                  result[y * bitmap.getWidth() + x] = RGB2L(p);
              }
          }
          return result;
    }

    private static byte RGB2L(int p) {
        int r = red(p);
        int g = green(p);
        int b = blue(p);
        return (byte) ((r + g + b) / 3);
    }
}
