package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FarmHarvestActivity extends AppCompatActivity {

    Spinner spinner_field;
    Spinner spinner_hub;
    Spinner spinner_dest;
    Spinner spinner_logi;
    ArrayAdapter adapter_field;
    ArrayAdapter adapter_hub;
    ArrayAdapter adapter_dest;
    ArrayAdapter adapter_logi;
    final String[] emptyOptions = new String[] {"-----------------", "-----------------", "-----------------"};
    final String[] destOptions = new String[] {"目的地A", "目的地B", "目的地C"};
    final String[] logiOptions = new String[] {"A", "B", "C"};
    final String[] logiEmails = new String[] {"fewfweff", "123", "frferfer"};
    //Passwords: {"ffffew", "123", "frefer"}
    String selectedFieldId;
    String selectedHubId;
    String selectedField;
    String selectedHub;
    NumberPicker numberPicker;
    Button btn_go;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_harvest);

        spinner_field = findViewById(R.id.spinner_field);
        spinner_hub = findViewById(R.id.spinner_hub);
        spinner_dest = findViewById(R.id.spinner_dest);
        spinner_logi = findViewById(R.id.spinner_logi);
        adapter_field = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, emptyOptions);
        adapter_hub = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, emptyOptions);
        adapter_dest = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, destOptions);
        adapter_logi = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, logiOptions);
        spinner_field.setAdapter(adapter_field);
        spinner_hub.setAdapter(adapter_hub);
        spinner_dest.setAdapter(adapter_dest);
        spinner_logi.setAdapter(adapter_logi);
        numberPicker = findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(1);
        btn_go = findViewById(R.id.btn_go);
        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                //Date date = new Date(System.currentTimeMillis());
                Intent i = new Intent(FarmHarvestActivity.this, FarmPaymentActivity.class);
                //i.putExtra("time", formatter.format(date));
                i.putExtra("fieldId", selectedFieldId);
                i.putExtra("hubId", selectedHubId);
                i.putExtra("field", selectedField);
                i.putExtra("hub", selectedHub);
                i.putExtra("amount", numberPicker.getValue());
                i.putExtra("email", getIntent().getStringExtra("email"));
                i.putExtra("uuid", getIntent().getStringExtra("uuid"));
                i.putExtra("token", getIntent().getStringExtra("token"));
                i.putExtra("brand", logiEmails[spinner_logi.getSelectedItemPosition()]);
                startActivityForResult(i, 1);
            }
        });


        new getFieldList().execute(getIntent().getStringExtra("uuid"), spinner_field);
    }

    public class getFieldList extends AsyncTask<Object, Void, Void> {

        String url = getString(R.string.iot_platform_url) + getString(R.string.iot_platform_getFields);
        String response_str;
        String[] list;
        String[] id;
        Spinner spinner;
        protected Void doInBackground(Object... args) {
            spinner = (Spinner) args[1];
            callHttpGet(url + args[0]);
            try {
                JSONObject jObj = new JSONObject(response_str);
                JSONArray jArr = jObj.getJSONArray("Items");
                list = new String[jArr.length()];
                id = new String[jArr.length()];
                for (int i=0; i<jArr.length(); i++) {
                    list[i] = jArr.getJSONObject(i).get("name").toString();
                    id[i] = jArr.getJSONObject(i).get("areaId").toString();
                }
            }catch(Exception e){
                Log.d("getFieldList", e.getMessage());
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
                Log.d("Field response", response_str);
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
            ArrayAdapter adapter = new ArrayAdapter(FarmHarvestActivity.this, android.R.layout.simple_spinner_dropdown_item, list);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedField = list[i];
                    selectedFieldId = id[i];
                    new getHubList().execute(id[i], spinner_hub);
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }
    }

    public class getHubList extends AsyncTask<Object, Void, Void> {

        String url = getString(R.string.iot_platform_url) + getString(R.string.iot_platform_getHubs);
        String response_str;
        String[] list;
        String[] id;
        Spinner spinner;
        protected Void doInBackground(Object... args) {
            spinner = (Spinner) args[1];
            callHttpGet(url + args[0]);
            try {
                JSONObject jObj = new JSONObject(response_str);
                JSONArray jArr = jObj.getJSONArray("Items");
                list = new String[jArr.length()];
                id = new String[jArr.length()];
                for (int i=0; i<jArr.length(); i++) {
                    list[i] = jArr.getJSONObject(i).get("name").toString();
                    id[i] = jArr.getJSONObject(i).get("groupId").toString();
                }
            }catch(Exception e){
                Log.d("getHubList", e.getMessage());
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
                Log.d("Hub response", response_str);
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
            ArrayAdapter adapter = new ArrayAdapter(FarmHarvestActivity.this, android.R.layout.simple_spinner_dropdown_item, list);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedHub = list[i];
                    selectedHubId = id[i];
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
            btn_go.setEnabled(true);
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
