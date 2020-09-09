package com.sample.cpcl_image_print;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Context context=this;
    final static String TAG="PCX2CPCL";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrintPCX_cpcl printPCX_cpcl=new PrintPCX_cpcl(context);
                try {
                    printPCX_cpcl.printBitmapBT();
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage());
                }

            }
        });
    }
}
