package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;

public class RecordSensorListActivity extends AppCompatActivity {

    LinearLayout section_sensorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sensor_list);

        section_sensorList = findViewById(R.id.section_sensorList);


        Log.d("Check", "start:" + getIntent().getStringExtra("start"));
        Log.d("Check", "end:" + getIntent().getStringExtra("end"));
        Log.d("Check", getIntent().getStringExtra("hubId"));
        new getSensorList().execute(getIntent().getStringExtra("hubId"));
    }

    public class getSensorList extends AsyncTask<Object, Void, Void> {

        String url = getString(R.string.iot_platform_url) + getString(R.string.iot_platform_getSensors);
        String response_str;
        String[] list;
        String[] id;
        String[] type;
        protected Void doInBackground(Object... args) {
            callHttpGet(url + args[0]);
            try {
                JSONObject jObj = new JSONObject(response_str);
                JSONArray jArr = jObj.getJSONArray("Items");
                list = new String[jArr.length()];
                id = new String[jArr.length()];
                type = new String[jArr.length()];
                for (int i=0; i<jArr.length(); i++) {
                    list[i] = jArr.getJSONObject(i).get("name").toString();
                    id[i] = jArr.getJSONObject(i).get("sensorId").toString();
                    type[i] = jArr.getJSONObject(i).get("sensorType").toString();
                }
            }catch(Exception e){
                Log.d("getSensorList", e.getMessage());
            }
            return null;
        }

        public Void callHttpGet(String url) {
            HttpClient client = new DefaultHttpClient();
            try {
                HttpGet get = new HttpGet(url);
                get.setHeader("Content-type", "application/json");
                get.setHeader("token",getIntent().getStringExtra("token"));
                HttpResponse response = client.execute(get);
                response_str = EntityUtils.toString(response.getEntity(), org.apache.http.protocol.HTTP.UTF_8);
                Log.d("Sensor response", response_str);
                response_str = URLDecoder.decode(response_str, "utf-8");
            } catch (Exception e) {
                Log.e(e.getClass().getName(), e.getMessage(), e);
                response_str = "Exception";
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            for (int i = 0; i < list.length; i++) {
                LayoutInflater inflater = LayoutInflater.from(RecordSensorListActivity.this);
                LinearLayout item_sensor = (LinearLayout) inflater.inflate(R.layout.item_sensor, null, false);
                section_sensorList.addView(item_sensor);
                TextView name = item_sensor.findViewById(R.id.sensor_name);
                TextView sensorId = item_sensor.findViewById(R.id.sensor_id);
                name.setText(list[i]);
                sensorId.setText(id[i]);
                final int index = i;
                item_sensor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(RecordSensorListActivity.this, RecordSensorDetailsActivity.class);
                        i.putExtra("uuid", getIntent().getStringExtra("uuid"));
                        i.putExtra("token", getIntent().getStringExtra("token"));
                        i.putExtra("sensorId", id[index]);
                        i.putExtra("sensorType", type[index]);
                        i.putExtra("start", getIntent().getStringExtra("start"));
                        i.putExtra("end", getIntent().getStringExtra("end"));
                        startActivityForResult(i, 1);
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("ResultCode", "code: " + resultCode);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
