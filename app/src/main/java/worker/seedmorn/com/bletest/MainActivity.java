package worker.seedmorn.com.bletest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    BleService.MyBinder binder;
    String TAG="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ServiceConnection connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder= (BleService.MyBinder) service;
                if(binder!=null){
                    Log.d(TAG, "onServiceConnected: 绑定service成功");
                }else {
                    Log.d(TAG, "onServiceConnected: 绑定service失败");

                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: 断开service");
            }
        };
        Log.d(TAG, "onCreate: 开始绑定服务");
        bindService(new Intent(MainActivity.this,BleService.class),connection,BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:
                binder.startScan();
                break;
            case R.id.button_notify:
                binder.setNotify(true);
                break;
            case R.id.button_write:
                binder.send();
                break;
            case R.id.button_read:
                binder.readValues();
                break;
            case R.id.button_stop:
                binder.stop();
                break;


        }
    }
}
