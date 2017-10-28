package com.example.wot.parentapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.fanrunqi.waveprogress.WaveProgressView;

import static java.net.HttpURLConnection.HTTP_OK;

public class BatteryActivity extends AppCompatActivity {

    WaveProgressView waveProgressView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String cid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);

        waveProgressView = (WaveProgressView)findViewById(R.id.waveProgressbar);
        sharedPreferences = getSharedPreferences("PARENT",MODE_PRIVATE);
        cid= sharedPreferences.getString("CHILD","no child");

        new BatteryTask().execute(cid);


    }

    private class BatteryTask extends AsyncTask<String,String,String>
    {
        ProgressDialog pd = new ProgressDialog(BatteryActivity.this);
        HttpURLConnection connection;
        URL url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Loading...");
            pd.setCancelable(false);
            pd.show();

        }

        @Override
        protected String doInBackground(String... strings) {

            try {

                url=new URL("https://app-1503993646.000webhostapp.com/parentchild/getbattery.php");

            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
            try {
                connection=(HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder =new Uri.Builder();
                builder.appendQueryParameter("cid",strings[0]);



                String query = builder.build().getEncodedQuery();

                OutputStream os=connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                connection.connect();

                int rc = connection.getResponseCode();
                if(rc == HTTP_OK)
                {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder sb=new StringBuilder();
                    String line;
                    while ((line=reader.readLine())!=null)
                    {
                        sb.append(line);
                    }
                    return sb.toString();
                }
                else{
                    Log.i("Error","Unsuccessfulcode"+rc);
                    return "unsuccessfull";
                }


            } catch (IOException e1) {
                e1.printStackTrace();
                return "Exception";
            }


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            Log.i("LoginResult",s);
            if(s.equals("0 results"))
            {
             //   Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
            else if (s.equalsIgnoreCase("exception") || s.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(BatteryActivity.this, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
                finish();
            }

            else {
                int percentage = Integer.parseInt(s);
                waveProgressView.setCurrent(percentage,s+" %"); // 77, "788M/1024M"
                waveProgressView.setMaxProgress(100);
                waveProgressView.setText("#3949ab",100);//"#FFFF00", 41
                waveProgressView.setWaveColor("#5b9ef4"); //"#5b9ef4"

                waveProgressView.setWave(Float.parseFloat("30"),Float.parseFloat("200"));
                waveProgressView.setmWaveSpeed(50);//The larger the value, the slower the vibration
               // startActivity(new Intent(LoginActivity.this,SelectChildActivity.class));
            }

        }
    }
}
