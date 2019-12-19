package com.kangjj.hotfix;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kangjj.hotfix.helper.HotFixHelper;

public class MainActivity extends AppCompatActivity {
    private TextView mTvTip;
    private String mPatchName = "fix-bug.dex";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HotFixHelper.tryInjectDex(getApplicationContext(),mPatchName);
        mTvTip = findViewById(R.id.tv_tip);
    }

    public void fixBug(View view) {
        if (HotFixHelper.loadPath(getApplicationContext(),mPatchName)) {
            Toast.makeText(getApplicationContext(), "补丁加载成功，重启后生效", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "补丁加载失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void unloadPatch(View view) {
        if (HotFixHelper.deletePathFile(getApplicationContext(),mPatchName)) {
            Toast.makeText(getApplicationContext(), "补丁卸载成功，重启后生效", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "补丁卸载失败,请先确保补丁已加载", Toast.LENGTH_SHORT).show();
        }
    }

    public void showBug(View view) {
        mTvTip.setText(new Sample().getString());
    }
}
