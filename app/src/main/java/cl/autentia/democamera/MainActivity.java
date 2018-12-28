package cl.autentia.democamera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cl.autentia.barcode.QRActivity;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CAMERA = 0x00002;
    private static final int REQUEST_PERMISSION = 0x015;

    private String[] permission = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.bt_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readIdCard();
            }

        });

        requestPermission(permission, REQUEST_PERMISSION);
    }

    private void requestPermission(String[] permissions, int requestCode) {

        List<String> permissionToGranted = new ArrayList();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionToGranted.add(permission);
            }
        }

        if (permissionToGranted.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionToGranted.toArray(new String[permissionToGranted.size()]), requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case REQUEST_PERMISSION:

                List<String> permissionToGranted = new ArrayList();
                for (int i = 0; i < permissions.length; i++) {

                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        permissionToGranted.add(permission);
                    }
                }

                if (permissionToGranted.size() > 0) {

                    ActivityCompat.requestPermissions(this, permissionToGranted.toArray(new String[permissionToGranted.size()]), requestCode);
                } else {
                    readIdCard();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void readIdCard(){
        Intent intent = new Intent(this, QRActivity.class);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA) {
            showMessage("Huella capturada\nDirectorio: /AutentiaMovil/evidencia");
        }
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setTitle("Resultado:")
                .setCancelable(false)
                .setPositiveButton("Aceptar", null)
                .create().show();
    }
}
