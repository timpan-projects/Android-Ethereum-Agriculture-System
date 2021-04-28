package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

import static android.view.View.GONE;


public class RecordFinishActivity_style extends AppCompatActivity {

    TextView textView_loading;
    Button btn_return;
    Socket socket;
    LinearLayout container;
    Activity a = this;
    int last_stop = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_finish);

        try{
            socket = IO.socket(getString(R.string.app_server_url));//設定Socket連接的IP位址
        } catch (URISyntaxException e){
            Log.d("Socket connection error", e.getMessage());
        }

        socket.connect();

        textView_loading = findViewById(R.id.textView_loading);
        container = findViewById(R.id.container);

        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
        socket.emit("get record", getIntent().getStringExtra("packageId"));
        socket.on("return record", echoReturn);
    }

    private Emitter.Listener echoReturn = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            final LinearLayout linearLayout = new LinearLayout(getBaseContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final LinearLayout recordLayout = new LinearLayout(getBaseContext());
            recordLayout.setOrientation(LinearLayout.VERTICAL);
            try {
                final JSONObject jObj = new JSONObject(args[0].toString());
                JSONArray jArr = jObj.getJSONArray("packageRecords");

                LayoutInflater inflater = LayoutInflater.from(RecordFinishActivity_style.this);

                long first = 0;
                for (int i = 0; i < jArr.length(); i ++) {
                    Log.d("Check", String.valueOf(i));
                    final JSONObject record = (JSONObject) jObj.getJSONArray("packageRecords").get(i);

                    LinearLayout linearLayout_recordItem;


                    if (last_stop == 0) {
                        if (i == jArr.length() - 1) {
                            linearLayout_recordItem = (LinearLayout) inflater.inflate(R.layout.curve_end_right, null, false);
                            last_stop = -1;
                        }
                        else{
                            linearLayout_recordItem = (LinearLayout) inflater.inflate(R.layout.curve_right, null, false);
                            last_stop = 1;
                        }
                    }
                    else if (last_stop == 1){
                        if (i == jArr.length() - 1) {
                            linearLayout_recordItem = (LinearLayout) inflater.inflate(R.layout.curve_end_left, null, false);
                            last_stop = -1;
                        }
                        else{
                            linearLayout_recordItem = (LinearLayout) inflater.inflate(R.layout.curve_left, null, false);
                            last_stop = 2;
                        }
                    }
                    else if (last_stop == 2) {
                        if (i == jArr.length() - 1) {
                            linearLayout_recordItem = (LinearLayout) inflater.inflate(R.layout.curve_end_right, null, false);
                            last_stop = -1;
                        }
                        else{
                            linearLayout_recordItem = (LinearLayout) inflater.inflate(R.layout.curve_right, null, false);
                            last_stop = 1;
                        }
                    }
                    else{
                        if (jObj.getString("parentId").matches(""))
                            linearLayout_recordItem = (LinearLayout) inflater.inflate(R.layout.curve_start_from_farm, null, false);
                        else
                            linearLayout_recordItem = (LinearLayout) inflater.inflate(R.layout.curve_start_after_split, null, false);
                        last_stop = 0;
                    }
                    final LinearLayout expandBtn = linearLayout_recordItem.findViewById(R.id.path);
                    expandBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (expandBtn.getChildAt(0).getVisibility() == View.GONE)
                                expandBtn.getChildAt(0).setVisibility(View.VISIBLE);
                            else
                                expandBtn.getChildAt(0).setVisibility(View.GONE);
                        }
                    });

                    //recordList.addView(linearLayout_recordItem);
                    TextView recordTitle = linearLayout_recordItem.findViewById(R.id.textView_recordTitle);
                    recordTitle.setText("第" + (i+1) + "站: ");

                    TextView platformName = linearLayout_recordItem.findViewById(R.id.textView_checkpoint);

                    platformName.setText(/*record.getString("platformName")*/ "");

                    Button btn_details = linearLayout_recordItem.findViewById(R.id.btn_sensorId);
                    final int index = i;
                    if (i + 1 < jArr.length()) {
                        btn_details.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(RecordFinishActivity_style.this, RecordSensorListActivity.class);
                                i.putExtra("uuid", getIntent().getStringExtra("uuid"));
                                i.putExtra("token", getIntent().getStringExtra("token"));
                                try{
                                    i.putExtra("hubId", record.getString("sensorUnitId"));
                                    i.putExtra("start", record.getString("timestamp"));
                                    JSONObject nextRecord = (JSONObject) jObj.getJSONArray("packageRecords").get(index + 1);
                                    i.putExtra("end", nextRecord.getString("timestamp"));
                                }
                                catch(Exception e) {
                                    i.putExtra("hubId", e.getMessage());
                                }
                                startActivityForResult(i, 1);
                            }
                        });
                    }
                    else {
                        btn_details.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(RecordFinishActivity_style.this)
                                        .setTitle("無資料可供查詢")
                                        .setMessage("貨物運輸中或已抵達終點，無法查詢此階段數據")
                                        .setPositiveButton("確定", null)
                                        .setCancelable(true)
                                        .show();
                            }
                        });
                    }

                    TextView timestamp = linearLayout_recordItem.findViewById(R.id.textView_time);
                    long unixtime = Long.parseLong(record.getString("timestamp"));
                    //if (i == 0) {
                    //    first = unixtime;
                    //}
                    //if (i == 1) {
                    //    if (first > unixtime) {
                    //        linearLayout.addView(linearLayout_recordItem, 0);
                    //        TextView firstTitle = linearLayout.getChildAt(0).findViewById(R.id.textView_recordTitle);
                    //        TextView secondTitle = linearLayout.getChildAt(1).findViewById(R.id.textView_recordTitle);
                    //        firstTitle.setText("第1站: ");
                    //        secondTitle.setText("第2站: ");
                    //        LinearLayout firstBackground = linearLayout.getChildAt(0).findViewById(R.id.path);
                    //        LinearLayout secondBackground = linearLayout.getChildAt(1).findViewById(R.id.path);
                    //        firstBackground.setBackgroundResource(R.mipmap.curve_start_from_farm);
                    //        firstBackground.setGravity(Gravity.RIGHT);
                    //        secondBackground.setBackgroundResource(R.mipmap.curve_right);
                    //        secondBackground.setGravity(Gravity.LEFT | Gravity.CENTER);
                    //    }
                    //    else {
                    //        linearLayout.addView(linearLayout_recordItem);
                    //    }
                    //}
                    //else {
                        linearLayout.addView(linearLayout_recordItem);
                    //}
                    java.util.Date time = new java.util.Date(unixtime);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd,        a h:mm");
                    timestamp.setText(sdf.format(time));
                }
                last_stop = -1;
            }
            catch (Exception e) {
                Log.d("Exception", e.getMessage());
            }

            linearLayout.addView(recordLayout);

            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    textView_loading.setVisibility(GONE);
                    container.addView(linearLayout, 0);//加在最前面
                }
            });
        }
    };
}
