package com.example.telephonystate;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	
private	 TelephonyManager telephonymanager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        telephonymanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TextView textview = (TextView)findViewById(R.id.telephony_text);
        String telephString = getTelephonyInformation();
        
        textview.setText(telephString);
        
    }

    private String getTelephonyInformation() {
		// TODO Auto-generated method stub
    	int callstate = telephonymanager.getCallState();
    	String callStateString=null;
    	switch(callstate){
    	
    	
    	case TelephonyManager.CALL_STATE_IDLE:
    		callStateString="IDEL";
    		
    		break;
    		
    	case TelephonyManager.CALL_STATE_OFFHOOK:
    		
    		callStateString="OffHOOK state";
    		break;
    		
case TelephonyManager.CALL_STATE_RINGING:
    		
    		callStateString="Ringing  state";
    		break;
    		
      default:
    			
    			break;
    	
    	
    	}
    	
    	GsmCellLocation cellLocation =(GsmCellLocation)telephonymanager.getCellLocation();
    	String cellLocationString =cellLocation.getLac()+""+cellLocation.getCid();
    	/*String deviceid=telephonymanager.getDeviceId();
    	String devicesoftwareOnfo=telephonymanager.getDeviceSoftwareVersion();
    	String lineNumber = telephonymanager.getLine1Number();
    	String networkOperator = telephonymanager.getNetworkOperator();
    	String networkcountry = telephonymanager.getNetworkCountryIso();
    	String networkOperatorName = telephonymanager.getNetworkOperatorName();
    	String SimOperatorName = telephonymanager.getSimOperator();
    	String simserialNumber = telephonymanager.getSimSerialNumber();
    	int simState = telephonymanager.getSimState();  
    	String simstateString="NA";
    	switch(simState)
    	
    	{
    	case TelephonyManager.SIM_STATE_ABSENT:
    	simstateString="SIM Absent";
    	
    	break;
    	
    	default:
    		break;
    		   	
    	}*/
    	StringBuilder sb =new StringBuilder();
    	sb.append("call state"+callStateString );
    	sb.append("\n Cell Location -" +cellLocationString);
    	/*sb.append("\n Device Id -" +deviceid);
    	sb.append("\n Device Software Information -" +devicesoftwareOnfo);
    	sb.append("\n Line Number -" +lineNumber);
    	sb.append("\n Network Country -" +networkcountry);
    	sb.append("\n Network operator name -" +networkOperatorName);  
    	sb.append("\n Net Work Operator -" +networkOperator);
    	
    	sb.append("\n Sim Operator Name -" +SimOperatorName);
    	sb.append("\n Sim Serial no" + simserialNumber);
    	sb.append("\n Sim State -" +simstateString);
    	*/
    	
    	
    	
    	
    	
    	
		return sb.toString();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}


































































































































