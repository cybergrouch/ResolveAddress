package com.paypal.first.resolveaddress;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = "ResolveAddress-LOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doResolveAddress(View v) {
        EditText input = (EditText) findViewById(R.id.addressText);
        Log.i(TAG, input.getText().toString());
        HttpAsyncTask asyncTask = new HttpAsyncTask();
        asyncTask.execute(input.getText().toString());
    }

    class HttpAsyncTask extends AsyncTask<String, Void, String> {

        private static final String REQUEST_FORMAT = "http://maps.google.com.sg/maps/api/geocode/json?address=%s&sensor=false";

        @Override
        protected String doInBackground(String... params) {
            // write code to do network call
            StringBuffer response = new StringBuffer();

            for (String place: params) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = null;
                try {
                    httpGet = new HttpGet(String.format(REQUEST_FORMAT, URLEncoder.encode(place, "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Error encoding URL parameter");
                }

                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response.append(s);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error on executing remote call", e);
                }
            }


            StringBuffer addressBuffer = new StringBuffer();

            try {
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray resArray = (JSONArray) jsonObject.get("results");
                JSONArray addresses = (JSONArray) ((JSONObject) resArray.get(0)).get("address_components");
                for (int i = 0; i < addresses.length(); i++) {
                    addressBuffer.append(((JSONObject) addresses.get(i)).getString("long_name")).append("\n");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing result", e);
            }

            return addressBuffer.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            // will run on user interface thread
            super.onPostExecute(s);

            TextView view = (TextView) findViewById(R.id.resultView);
            view.setText(s);
        }

    }
}
