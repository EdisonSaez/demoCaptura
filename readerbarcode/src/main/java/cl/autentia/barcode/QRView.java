package cl.autentia.barcode;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.Result;
import com.pacific.mvc.ActivityView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import cl.autentia.ImageUtils;

/**
 * Created by root on 16-5-8.
 */
public class QRView extends ActivityView<QRActivity> implements SurfaceHolder.Callback {

    public static final String PDF_417 = "PDF_417";
    public static final String QR = "QR";
    private QRCodeView qrCodeView;
    private SurfaceView surfaceView;
    private PreferencesUtils oPreferences;

    public QRView(QRActivity activity) {
        super(activity);
        oPreferences = new PreferencesUtils(this.activity);
    }

    @Override
    protected void findView() {
        surfaceView = retrieveView(R.id.sv_preview);
        qrCodeView = retrieveView(R.id.qr_view);
        qrCodeView.pressOnClickBackButton(this.activity);
    }

    @Override
    protected void setListener() {
        qrCodeView.setPickImageListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setHook(true);
                Intent galleryIntent = new Intent();
                if (Build.VERSION_CODES.KITKAT >= Build.VERSION.SDK_INT) {
                    galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                } else {
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                }
                galleryIntent.setType("image/*");
                Intent wrapperIntent = Intent.createChooser(galleryIntent, "");
                activity.startIntentForResult(wrapperIntent, QRActivity.CODE_PICK_IMAGE, null);
            }
        });
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    protected void setAdapter() {

    }

    @Override
    protected void initialize() {

    }

    @Override
    public void onClick(View v) {

    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void resultDialog(QRResult qrResult) {
        if (qrResult == null) {
            new AlertDialog.Builder(activity)
                    .setTitle("Código de barras vacío")
                    .setMessage("No fue posible realizar captura, reintente.")
                    .setPositiveButton("Ok", null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            activity.setHook(false);
                            activity.restartCapture();
                        }
                    })
                    .create()
                    .show();
            return;
        }

        if (qrResult.getBitmap() != null) {

            Bitmap bitmapNormal = qrResult.getBitmap();
            Result resultNormal = qrResult.getResult();
            Bitmap bitmapInverted180 = qrResult.getBitmapInvert180();

            try {

                String typeBarcodeResponse = resultNormal.getBarcodeFormat().name();

                if (typeBarcodeResponse.equalsIgnoreCase(PDF_417)) {

                    try {

                        //if (validateNotEqualsTypeBarcode(typeBarcodeResponse)) return;

                        Log.d(QRView.class.getName(), Arrays.toString(resultNormal.getResultPoints()));

                        int x1 = Math.round(resultNormal.getResultPoints()[2].getX());
                        int y1 = Math.round(resultNormal.getResultPoints()[2].getY());
                        int x2 = Math.round(resultNormal.getResultPoints()[3].getX());
                        int y2 = Math.round(resultNormal.getResultPoints()[3].getY());

                        float pdfDimension = (float) Math.pow(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2), 0.5);

                        byte[] dataBarcode = qrResult.getResult().getText().getBytes(StandardCharsets.ISO_8859_1);

                        if (pdfDimension > 200) {

                            Bitmap imagenHuellaNormal = Bitmap.createBitmap(bitmapNormal, x1, y1, (int) pdfDimension, (int) pdfDimension);
                            Bitmap fingerGrayScaleARGBNormal = QRUtils.toGrayscale(imagenHuellaNormal, Bitmap.Config.ARGB_8888);
                            Bitmap fingerStrechedNormal = QRUtils.stretchHistogram(fingerGrayScaleARGBNormal, 0.05f);

                            Bitmap imagenHuellaInverted180 = Bitmap.createBitmap(bitmapInverted180, x1, y1, (int) pdfDimension, (int) pdfDimension);
                            Bitmap fingerGrayScaleARGBInverted180 = QRUtils.toGrayscale(imagenHuellaInverted180, Bitmap.Config.ARGB_8888);
                            Bitmap fingerStrechedInverted180 = QRUtils.stretchHistogram(fingerGrayScaleARGBInverted180, 0.05f);

                            Intent intentReturn = new Intent();
                            intentReturn.putExtra("type", "pdf_417");
                            intentReturn.putExtra("rawdata", dataBarcode);
                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                new AlertDialog.Builder(activity)
                                        .setTitle("Sin permito de escritura")
                                        .setMessage("Asigne el permiso de escritura, para almacenar el código capturado")
                                        .setPositiveButton("Ok", null)
                                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {

                                                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                                activity.setHook(false);
                                                activity.restartCapture();
                                            }
                                        })
                                        .create()
                                        .show();
                                return;
                            }

                            String filenameNormal = String.format("%s/%s%s", Environment.getExternalStorageDirectory() + "/AutentiaMovil/evidencia", UUID.randomUUID().toString(), ".bmp");
                            String filenameInverted180 = String.format("%s/%s%s", Environment.getExternalStorageDirectory() + "/AutentiaMovil/evidencia", UUID.randomUUID().toString(), ".bmp");

                            //Elimina el contenido del directorio
                            File file = new File(filenameNormal);
                            if (file.getAbsoluteFile().exists()) {
                                file.delete();
                            }
                            file = new File(filenameInverted180);
                            if (file.getAbsoluteFile().exists()) {
                                file.delete();
                            }

                            file.getParentFile().mkdirs();
                            Util.deleteAllFilesInDirectory(file.getParentFile());

                            byte[] arrBmp1 = bmpToByte(fingerStrechedNormal);
                            byte[] arrBmp2 = bmpToByte(fingerStrechedInverted180);

                            Util.savebytefile(arrBmp1, filenameNormal);
                            Util.savebytefile(arrBmp2, filenameInverted180);

                            intentReturn.putExtra("barcode", String.format("%s|%s|%s|%s",
                                    Base64.encodeToString(dataBarcode, Base64.DEFAULT),
                                    filenameNormal, filenameInverted180, pdfDimension));

                            activity.setResult(Activity.RESULT_OK, intentReturn);
                            activity.finish();

                        } else {
                            Toast.makeText(activity, "Captura de baja resolución, utilice la guía visual para realizar una captura optima", Toast.LENGTH_LONG).show();
                            activity.setHook(false);
                            activity.restartCapture();
                            return;
                        }

                    } catch (Exception e) {
                        Log.d("BARCODE", e.getMessage());
                        Toast.makeText(activity, "Captura de la zona, utilice la guía visual para realizar una captura optima", Toast.LENGTH_LONG).show();
                        activity.setHook(false);
                        activity.restartCapture();
                        return;
                    }

                } else if (typeBarcodeResponse.equalsIgnoreCase("QR_CODE")) {

                    //if (validateNotEqualsTypeBarcode(typeBarcodeResponse)) return;

                    String dataBarcode = qrResult.getResult().getText();

                    Intent intentReturn = new Intent();
                    intentReturn.putExtra("type", "qr");
                    intentReturn.putExtra("barcode", dataBarcode);
                    activity.setResult(Activity.RESULT_OK, intentReturn);
                    activity.finish();

                }
            } catch (Exception e) {
                Intent intentReturn = new Intent();
                intentReturn.putExtra("type", "qr");
                intentReturn.putExtra("error", e.getMessage());
                activity.setResult(Activity.RESULT_CANCELED, intentReturn);
                activity.finish();
            }
        }
    }

    private byte[] bmpToByte(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bmp.recycle();

        return byteArray;
    }

    public void setEmptyViewVisible(boolean visible) {
        if (visible) {
            retrieveView(R.id.v_empty).setVisibility(View.VISIBLE);
        } else {
            retrieveView(R.id.v_empty).setVisibility(View.GONE);
        }
    }

    public void setSurfaceViewVisible(boolean visible) {
        if (visible) {
            surfaceView.setVisibility(View.VISIBLE);
        } else {
            surfaceView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        activity.onSurfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //activity.onSurfaceChanged((int)surfaceView.getRotation());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        setEmptyViewVisible(true);
        activity.onSurfaceDestroyed();
    }
}