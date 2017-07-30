package com.example.bxlt.renames;

import android.app.ProgressDialog;
import android.os.Environment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        edt_want = (EditText) findViewById(R.id.edt_want);
        edt_need = (EditText) findViewById(R.id.edt_need);
    }

    public void btn_start(View view) {
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
