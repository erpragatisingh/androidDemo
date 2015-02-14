package com.BluetoothDemo;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.TextView;

public class BluetoothDemoActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
      TextView  out=(TextView) findViewById(R.id.text1);
      BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
      out.append("\n Adapter"+adapter);
      if(adapter==null){
    	  out.append("bluetooth not support");
    	  return ;
    	  
      }
      out.append("starting discovery........");
      Set<BluetoothDevice> devices=adapter.getBondedDevices();
      for(BluetoothDevice device :devices)
      {
    	  out.append("found device"+ device);
      }
      
    }
}