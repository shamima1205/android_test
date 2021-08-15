package com.example.mabia.smartpoultrymanagement;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity {

    MqttHelper mqttHelper;
    String mqttTopic;
    float temp,time,hum;

    TextView tvFortemp,tvForHum,tvForCo2,tvForNH4,tvNotifyForTemp,tvNotifyForHum,tvNotifyForLight,tvNotifyForTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean status =  activeNetworkInfo != null && activeNetworkInfo.isConnected();

        tvFortemp = (TextView)findViewById(R.id.textViewTemp);
        tvForHum = (TextView) findViewById(R.id.textViewHum);
        tvForCo2 = (TextView) findViewById(R.id.textView_CO2);
        tvForNH4 = (TextView) findViewById(R.id.textView_NH4);

        tvNotifyForTemp = (TextView) findViewById(R.id.textViewTempNotification);
        tvNotifyForHum = (TextView) findViewById(R.id.textViewHumNotification);
        tvNotifyForLight = (TextView) findViewById(R.id.textViewLightNotification);
        tvNotifyForTime = (TextView) findViewById(R.id.textViewTimeNotification);

        if(status){
            startMqtt();
        }else {
            Toast.makeText(DashboardActivity.this, "Internet connection failed, Connect to Internet", Toast.LENGTH_LONG).show();
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_control:
                //Intent intent = new Intent();
                Intent launchNewIntent = new Intent(DashboardActivity.this,ControlActivity.class);
                startActivityForResult(launchNewIntent, 0);
                return true;
            case R.id.logout:
                Intent signout = new Intent(DashboardActivity.this,LoginActivity.class);
                startActivity(signout);
                return true;
            case R.id.mode:
                Intent modeSetting = new Intent(DashboardActivity.this,ModeSettingActivity.class);
                startActivity(modeSetting);
                return true;
        }
        return false;
    }

    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(DashboardActivity.this, "Connection established ".toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(DashboardActivity.this, "Connection lost ".toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Toast.makeText(DashboardActivity.this, "messageArrived", Toast.LENGTH_LONG).show();
                Log.w("Debug", mqttMessage.toString());
                Log.d("Tag","**********Data: "+mqttMessage.toString());
                mqttTopic=new String(topic);

                Log.d("Topic","---------top-----------"+mqttTopic);
                if(mqttTopic.equals("turkey/data")){
                    JSONObject jsobj =  new JSONObject(mqttMessage.toString());
                    Log.d("Topic Value","---------temp-----------"+jsobj.getString("Temp")+" ---hum-------- "+
                            jsobj.getString("Hum")+"---CO2-------- "+jsobj.getString("Co2")+"---NH4-------- "+jsobj.getString("NH4"));
                    tvFortemp.setText(jsobj.getString("Temp")+" °C");
                    tvForHum.setText(jsobj.getString("Hum")+" %");
                    tvForCo2.setText(jsobj.getString("Co2")+" ppm");
                    tvForNH4.setText(jsobj.getString("NH4")+" ppm");

                    // Humidity Checking
                    hum = Float.parseFloat(jsobj.getString("Hum"));
                    if(hum >= 40.00 || hum <= 60.00){
                        tvNotifyForHum.setText("Humidity is in correct range");
                    }else{
                        tvNotifyForHum.setText("Humidity is not in correct range");
                    }

                    temp = Float.parseFloat(jsobj.getString("Temp"));
                }
                if(mqttTopic.equals("turkey/LightStatus")) {
                    JSONObject jsobjForLight =  new JSONObject(mqttMessage.toString());
                    Log.d("Topic Value","---------LightStatus-----------"+jsobjForLight.getString("LightStatus"));
                    DecimalToBinary(jsobjForLight.getString("LightStatus"));

                }
                if(mqttTopic.equals("turkey/ElapsedTime")){
                    JSONObject jsonForDate =  new JSONObject(mqttMessage.toString());
                    Log.d("Topic Value","---------ElapsedTime-----------"+jsonForDate.getString("ElapsedTime"));
                    time = Float.parseFloat(jsonForDate.getString("ElapsedTime"));
                }

                displayNotification(temp,time);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Toast.makeText(DashboardActivity.this, "Delivery Completed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayNotification(Float temperature,Float elapsed_time){
        //Toast.makeText(DashboardActivity.this, "Delivery Completed "+temperature , Toast.LENGTH_SHORT).show();
        int week,day;
        week = (int) (elapsed_time/7);
        day = (int) (elapsed_time%7);

        Log.d("Check",(week == 2 && (temperature >= 32.00 || temperature <= 34.00))+" ");
        Log.e("Check","Time passes "+week+" week "+" "+day+" day");
        tvNotifyForTime.setText("Time passes "+week+" week "+" "+day+" day");
        tvNotifyForTemp.setTextColor(Color.GREEN);
        if(week == 1 && (temperature >= 35.00 || temperature <= 37.00)){
            tvNotifyForTemp.setText("Temperature is in correct range");
        }else if(week == 2 && (temperature >= 32.00 || temperature <= 34.00)){
            tvNotifyForTemp.setText("Temperature is in correct range");
        }else if(week == 3 && (temperature >= 24.00 || temperature <= 31.00)){
            tvNotifyForTemp.setText("Temperature is in correct range");
        }else if(week == 4 && (temperature >= 27.00 || temperature <= 28.00)){
            tvNotifyForTemp.setText("Temperature is in correct range");
        }else if(week == 5 && (temperature >= 24.00 || temperature <= 26.00)){
            tvNotifyForTemp.setText("Temperature is in correct range");
        }else {
            tvNotifyForTemp.setText("Temperature is out of range");
            tvNotifyForTemp.setTextColor(Color.RED);
        }

    }

    public void DecimalToBinary(String lightStatuses) {
        //tvNotifyForLight
        Log.d("Decimal","to Binary "+lightStatuses);
        int no = Integer.parseInt(lightStatuses);

        int i = 0, temp[] = new int[7];
        int binary[];
        while (no > 0) {
            temp[i++] = no % 2;
            no /= 2;
        }
        binary = new int[i];
        int k = 0;
        for (int j = i - 1; j >= 0; j--) {
            binary[k++] = temp[j];
        }
        DisplayLightStatus(binary);
    }

    public void DisplayLightStatus(int[] binary ){
        String txt ="",l1="",l2="",l3="",l4="";

            for(int m=binary.length-1;m>=0; m--){
                //Log.d("Array 2", String.valueOf(binary.length));
//                Log.d("Array Show bin next :", (String.valueOf(binary[m])));
                l1=(m==0 && binary[m]==1)?"ON":"OFF";
                l2=(m==1 && binary[m]==1)?"ON":"OFF";
                l3=(m==2 && binary[m]==1)?"ON":"OFF";
                l4=(m==3 && binary[m]==1)?"ON":"OFF";
                //name = ((city == null) || (city.getName() == null) ? "N/A" : city.getName());
                //l1 = (m == 0 )
                Log.d("^^^L1",l1);Log.d("^^^L2",l2);Log.d("^^^L3",l3);Log.d("^^^L4",l4);
            }
        Spannable wordtoSpan = new SpannableString("L1 is "+l1+" , L2 is "+l2+" , L3 is "+l3+" , L4 is "+l4);

//        wordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), 15, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if(l1.matches("ON")){
            wordtoSpan.setSpan(new ForegroundColorSpan(Color.RED), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else{
            wordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if(l2.matches("ON")){
            wordtoSpan.setSpan(new ForegroundColorSpan(Color.RED), 8, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else{
            wordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 8, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if(l3.matches("ON")){
            wordtoSpan.setSpan(new ForegroundColorSpan(Color.RED), 15, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else{
            wordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 15, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if(l4.matches("ON")){
            wordtoSpan.setSpan(new ForegroundColorSpan(Color.RED), 22, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else{
            wordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 22, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        tvNotifyForLight.setText(wordtoSpan);

//
//        SpannableString text3 = new SpannableString("android-coding.blogspot.com");
//        text3.setSpan(new BackgroundColorSpan(Color.LTGRAY), 0, text3.length(), 0);
//        text3.setSpan(new ForegroundColorSpan(Color.RED), 0, 14, 0);
//        text3.setSpan(new ForegroundColorSpan(Color.GREEN), 6, 11, 0);
//        text3.setSpan(new ForegroundColorSpan(0xFF0000FF), 14, 23, 0);
//        text3.setSpan(new ForegroundColorSpan(0x500000FF), 23, text3.length(), 0);
//        colorText3.setText(text3, BufferType.SPANNABLE);
    }
}
