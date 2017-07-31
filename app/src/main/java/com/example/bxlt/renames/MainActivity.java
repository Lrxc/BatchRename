package com.example.bxlt.renames;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.CharConversionException;
import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog show;
    private EditText edt_want;
    private EditText edt_need;
    private String want;
    private String need;
    private final int PremissWhat1 = 001;
    private boolean isPremiss;//是否有权限

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initPermissions();
    }

    private void initView() {
        edt_want = (EditText) findViewById(R.id.edt_want);
        edt_need = (EditText) findViewById(R.id.edt_need);
    }

    //申请权限
    private void initPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            int perm1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int perm2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            if (perm1 != PackageManager.PERMISSION_GRANTED || perm2 != PackageManager.PERMISSION_GRANTED) {//未获取权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //当用户第一次申请拒绝时，再次申请该权限调用
                    Toast.makeText(this, "必须权限，否则程序将异常", Toast.LENGTH_SHORT).show();
                }
                //申请权限
                String[] strings = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
                ActivityCompat.requestPermissions(this, strings, PremissWhat1);
            } else {//已经获得了权限
                isPremiss = true;//同步账号
            }
        } else {
            isPremiss = true;//同步账号
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull String[] permissions, @android.support.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PremissWhat1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//授权成功
            isPremiss = true;//同步账号
        } else {//授权失败
            Toast.makeText(this, "获取权限失败，请重新授权", Toast.LENGTH_SHORT).show();
        }
    }

    public void btn_start(View view) {
        if (!isPremiss) {
            Toast.makeText(this, "授权失败，请重启软件", Toast.LENGTH_SHORT).show();
            return;
        }
        want = edt_want.getText().toString();
        need = edt_need.getText().toString();

        if (TextUtils.isEmpty(want)) {
            Toast.makeText(this, "要替换的字段不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(need)) {
            Toast.makeText(this, "替换成的字段不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        initRxjava();
    }

    private void initRxjava() {
        show = ProgressDialog.show(this, "", "改名中...");
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                File file = new File(Environment.getExternalStorageDirectory() + "/001");
                if (file.exists()) {
                    getAllFiles(file);
                    e.onComplete();
                } else {
                    e.onError(new CharConversionException("请先复制到001目录"));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<String>() {
                    @Override
                    public void onNext(@NonNull String s) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        show.dismiss();
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        show.dismiss();
                        Toast.makeText(MainActivity.this, "改名成功", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    getAllFiles(f);
                    rename(f);
                } else {
                    rename(f);
                }
            }
        }
    }

    private void rename(File file) {
        if (file.getName().contains(want)) {
            String oldPath = file.getAbsolutePath();
            file.renameTo(new File(oldPath.replace(want, need)));
        }
    }
}
