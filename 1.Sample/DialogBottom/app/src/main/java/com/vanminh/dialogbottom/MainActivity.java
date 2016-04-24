package com.vanminh.dialogbottom;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cocosw.bottomsheet.BottomSheet;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void btnTest(View v) {
        new BottomSheet.Builder(this).title("title")
                .icon(R.drawable.perm_group_microphone)
                .sheet(R.menu.list)
                .listener(new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.help:

                                break;
                        }
                    }
                }).show();
    }
}
