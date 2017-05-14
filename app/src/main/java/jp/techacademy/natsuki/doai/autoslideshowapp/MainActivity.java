package jp.techacademy.natsuki.doai.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.os.Handler;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    Cursor cursor;
    ContentResolver resolver;
    ImageView imageVIew;

    Timer timer;

    Handler handler = new Handler();

    Button button1;
    Button button2;
    Button button3;

    boolean playing = false;  //スライドショーの現在の状態を判定 trueなら再生中

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);

        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);

        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            showImage(cursor);
        }
    }

    public void showImage(Cursor cursor) {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }

    public void onClick(View v) {
        if (cursor == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("画像が読み込めません");
            builder.show();

        }else {

            if (v.getId() == R.id.button1) {            //進むボタン
                if (cursor.moveToNext()) {
                    showImage(cursor);
                } else if (cursor.moveToFirst()) {
                    showImage(cursor);
                }

            } else if (v.getId() == R.id.button2) {          //戻るボタン
                if (cursor.moveToPrevious()) {
                    showImage(cursor);
                } else if (cursor.moveToLast()) {
                    showImage(cursor);
                }

            } else if (v.getId() == R.id.button3) {   //再生/停止ボタン

                if (!playing && timer == null) {                 //再生ボタン
                    button1.setEnabled(false);  //進むボタンと戻るボタンを無効化
                    button2.setEnabled(false);

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (cursor.moveToNext()) {
                                        showImage(cursor);
                                    } else if (cursor.moveToFirst()) {
                                        showImage(cursor);
                                    }
                                }
                            });
                        }
                    }, 2000, 2000);

                    playing = true;
                    button3.setText(R.string.button_pause);


                } else if (playing && timer != null) {                            //停止ボタン
                    timer.cancel();
                    playing = false;
                    timer = null;
                    button3.setText(R.string.button_play);

                    button1.setEnabled(true);  //進むボタンと戻るボタンの復活
                    button2.setEnabled(true);

                }
            }
        }
    }

}