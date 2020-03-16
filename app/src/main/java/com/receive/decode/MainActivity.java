package com.receive.decode;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.net.DatagramSocket;
import nc.*;
public class MainActivity extends Activity {

//    Button but;
    public static Handler handler;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        but=(Button)findViewById(R.id.count);
        handler =new MyHandler2();
        textView=(TextView)findViewById(R.id.textView);
//        but.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int a=Counts.getInstance().getCount();
//                int b=Counts.getInstance().getCountz();
//                Toast.makeText(MainActivity.this,"收包数："+a+"|解帧数："+b,Toast.LENGTH_LONG).show();
//            }
//        });
//        byte[] a={1,2,3,4};
//        byte[] b={2,4,6,8};
//        byte[] b=new byte[4];
//        byte[] c=new  byte[4];
//        c=NCUtils.mul(a,b);
//        int i=0;
//        for(i=0;i<4;i++)
//        {
//            Log.e("李玉龙",(int)c[i]+"//");
//        }
//        int c=NCUtils.getRank1(b,2,2);
//        Log.e("李玉龙",c+"//");
    }
    public static void handle1(int a,int b)
    {
        Message message = Message.obtain();
        message.what=0;
        message.arg1 = a;
        message.arg2 = b;
        handler.sendMessage(message);
    }
    class MyHandler2 extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            int a = msg.arg1;
            int b = msg.arg2;
            float c=a*100/239;
//            float d=100-b*100/1787;
            String name = "解码帧数：";
//            String name1 = "接收包数：";
            String name2 = "解码率：";
//            String name3 = "丢包率：";
            switch (what) {
                case 0:
                    textView.setText(name + a +name2+c+"%");
//                    textView.setText(name + a + name1 + b+name2+c+"%"+name3+d+"%");
                    break;
                default:
                    break;
            }
        }
    }

}
