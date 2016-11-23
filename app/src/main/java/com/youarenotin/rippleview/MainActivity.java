package com.youarenotin.rippleview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getWindow().setEnterTransition(new Explode());
    }
    public void click(View view){
        Toast.makeText(this,"按钮响应",Toast.LENGTH_SHORT).show();
    }
}
