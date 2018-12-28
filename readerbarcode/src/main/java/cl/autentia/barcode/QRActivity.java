package cl.autentia.barcode;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.zxing.MultiFormatReader;
import com.pacific.mvc.Activity;
import com.trello.rxlifecycle.ActivityEvent;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class QRActivity extends Activity<QRModel> {

    public static final int CODE_PICK_IMAGE = 0x00000100;
    private BaseCameraManager cameraManager;
    private PreferencesUtils oPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        oPreferences = new PreferencesUtils(this);
        oPreferences.setString(PreferencesUtils.KEY_TYPE, null);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Bundle args = getIntent().getExtras();

        if (args != null) {

            if (args.getBoolean(Extras.In.REVERSE, false)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
            if (args.containsKey(Extras.In.PARAMETER_TYPE))
                oPreferences.setString(PreferencesUtils.KEY_TYPE, args.getString(Extras.In.PARAMETER_TYPE));

            if (args.containsKey(Extras.In.PORTRAIT))
                oPreferences.setBoolean(PreferencesUtils.KEY_PORTRAIT, args.getBoolean(Extras.In.PORTRAIT));
        }

        setContentView(R.layout.activity_qr);

        cameraManager = new CameraManager(getApplication());
        model = new QRModel(new QRView(this));
        model.onCreate();

        cameraManager.setOnResultListener(new BaseCameraManager.OnResultListener() {
            @Override
            public void onResult(QRResult qrResult) {
                model.resultDialog(qrResult);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraManager.releaseCamera();
        cameraManager.shutdownExecutor();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CODE_PICK_IMAGE) {
            String[] columns = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(data.getData(), columns, null, null, null);
//            if (cursor.moveToFirst()) {
//                Observable
//                        .just(cursor.getString(cursor.getColumnIndex(columns[0])))
//                        .observeOn(Schedulers.from(cameraManager.getExecutor()))
//                        .compose(this.<String>bindUntilEvent(ActivityEvent.PAUSE))
//                        .map(new Func1<String, QRResult>() {
//                            @Override
//                            public QRResult call(String str) {
//                                return QRUtils.decode(str, new MultiFormatReader());
//                            }
//                        })
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Action1< QRResult>() {
//
//                            @Override
//                            public void call(QRResult qrResult) {
//                                model.resultDialog(qrResult);
//                            }
//                        });
//            }
            cursor.close();
        }
    }

    public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
        if (cameraManager.getExecutor().isShutdown()) return;
        Observable
                .just(surfaceHolder)
                .compose(this.<SurfaceHolder>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(Schedulers.from(cameraManager.getExecutor()))
                .map(new Func1<SurfaceHolder, Object>() {
                    @Override
                    public Object call(SurfaceHolder holder) {
                        cameraManager.setRotate(getWindowManager().getDefaultDisplay().getRotation());
                        cameraManager.connectCamera(holder);
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        model.setEmptyViewVisible(false);
                        cameraManager.startCapture();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (ActivityCompat.checkSelfPermission(QRActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            new AlertDialog.Builder(QRActivity.this)
                                    .setTitle("Sin permito de escritura")
                                    .setMessage("Asigne el permiso de escritura, para almacenar el código capturado")
                                    .setPositiveButton("Ok", null)
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {

                                            ActivityCompat.requestPermissions(QRActivity.this, new String[]{Manifest.permission.CAMERA}, 1);

                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:

                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            QRActivity.this.restartCapture();
                        } else {
                            ActivityCompat.requestPermissions(QRActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                        }
                    }
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void onSurfaceChanged(int rotation) {
        cameraManager.setRotate(rotation);
    }

    public void onSurfaceDestroyed() {
        cameraManager.releaseCamera();
    }

    public void restartCapture() {
        cameraManager.startCapture();
    }

    public void setHook(boolean hook) {
        cameraManager.setHook(hook);
    }

    public void requestPermission(String[] permissions, int requestCode) {

        List<String> permissionToGranted = new ArrayList();

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

                permissionToGranted.add(permission);
            }
        }

        if (permissionToGranted.size() > 0)
            ActivityCompat.requestPermissions(this, permissionToGranted.toArray(new String[permissionToGranted.size()]), requestCode);
    }

    interface Extras {
        interface In {
            String PARAMETER_TYPE = "TYPE";
            String REVERSE = "REVERSE";
            String PORTRAIT = "PORTRAIT";
        }

        interface Out {

        }
    }
}
