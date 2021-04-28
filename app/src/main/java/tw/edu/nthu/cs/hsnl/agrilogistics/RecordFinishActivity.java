package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

public class RecordFinishActivity extends AppCompatActivity {

    TextView textView_loading;
    Button btn_return;
    Socket socket;
    LinearLayout container;
    Activity a = this;
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

                LayoutInflater inflater = LayoutInflater.from(RecordFinishActivity.this);
                LinearLayout linearLayout_package = (LinearLayout) inflater.inflate(R.layout.item_package_layout, null, false);
                recordLayout.addView(linearLayout_package);

                final LinearLayout packageDetails = linearLayout_package.findViewById(R.id.section_packageDetails);
                final ImageView expandButtonPackage =  linearLayout_package.findViewById(R.id.imageView_expandPackage);
                TextView packageTitle = linearLayout_package.findViewById(R.id.textView_packageTitle);
                if (jObj.getString("parentId").matches(""))
                    packageTitle.setText("貨物編號: " + jObj.getString("packageId"));
                else
                    packageTitle.setText("貨物編號: " + jObj.getString("packageId") + "\n" + "分裝自編號: " + jObj.getString("parentId"));
                packageTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (packageDetails.getVisibility() == View.GONE) {
                            packageDetails.setVisibility(View.VISIBLE);
                            expandButtonPackage.setImageResource(R.mipmap.minus_icon);
                        }
                        else {
                            packageDetails.setVisibility(View.GONE);
                            expandButtonPackage.setImageResource(R.mipmap.plus_icon);
                        }
                    }
                });

                LinearLayout recordList = linearLayout_package.findViewById(R.id.section_recordList);
                long first = 0;
                for (int i = 0; i < jArr.length(); i ++) {
                    final JSONObject record = (JSONObject) jObj.getJSONArray("packageRecords").get(i);

                    LinearLayout linearLayout_recordItem = (LinearLayout) inflater.inflate(R.layout.item_record_layout, null, false);
                    //recordList.addView(linearLayout_recordItem);

                    final LinearLayout recordDetails = linearLayout_recordItem.findViewById(R.id.section_details);
                    final ImageView expandButtonRecord = linearLayout_recordItem.findViewById(R.id.imageView_expand);
                    TextView recordTitle = linearLayout_recordItem.findViewById(R.id.textView_recordTitle);
                    recordTitle.setText("第" + (i+1) + "站: ");
                    recordTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (recordDetails.getVisibility() == View.GONE) {
                                recordDetails.setVisibility(View.VISIBLE);
                                expandButtonRecord.setImageResource(R.mipmap.minus_icon);
                            }
                            else {
                                recordDetails.setVisibility(View.GONE);
                                expandButtonRecord.setImageResource(R.mipmap.plus_icon);
                            }
                        }
                    });

                    TextView platformName = linearLayout_recordItem.findViewById(R.id.textView_checkpoint);
                    String temp = record.getString("platformName");
                    //if (temp.substring(temp.length() - 6).matches("(開始種植)"))
                    //    platformName.setText("666");
                    //else
                        platformName.setText(record.getString("platformName"));
                    //TextView platformName_title = new TextView(getBaseContext());
                    //recordLayout.addView(platformName_title);
                    //platformName_title.setText("Checkpoint " + i + ": ");
//
                    //TextView platformName = new TextView(getBaseContext());
                    //recordLayout.addView(platformName);
                    //platformName.setText(record.getString("platformName"));


                    Button sensorUnitId = linearLayout_recordItem.findViewById(R.id.btn_sensorId);

                    final int index = i;
                    if (i - 1 >= 0) {
                        sensorUnitId.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(RecordFinishActivity.this, RecordSensorListActivity.class);
                                i.putExtra("uuid", getIntent().getStringExtra("uuid"));
                                i.putExtra("token", getIntent().getStringExtra("token"));
                                try{
                                    i.putExtra("hubId", record.getString("sensorUnitId"));
                                    i.putExtra("start", record.getString("timestamp"));
                                    JSONObject nextRecord = (JSONObject) jObj.getJSONArray("packageRecords").get(index - 1);
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
                        sensorUnitId.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(RecordFinishActivity.this)
                                        .setTitle("無資料可供查詢")
                                        .setMessage("貨物尚未離開此運輸階段，無法查詢數據")
                                        .setPositiveButton("確定", null)
                                        .setCancelable(true)
                                        .show();
                            }
                        });
                    }
                    //TextView sensorUnitId_title = new TextView(getBaseContext());
                    //recordLayout.addView(sensorUnitId_title);
                    //sensorUnitId_title.setText("Related sensor ID: ");
//
                    //TextView sensorUnitId = new TextView(getBaseContext());
                    //recordLayout.addView(sensorUnitId);
                    //sensorUnitId.setText(record.getString("sensorUnitId"));

                    TextView timestamp = linearLayout_recordItem.findViewById(R.id.textView_time);
                    long unixtime = Long.parseLong(record.getString("timestamp"));
                    if (i == 0) {
                        first = unixtime;
                    }
                    if (i == 1) {
                        if (first > unixtime) {
                            recordList.addView(linearLayout_recordItem, 0);
                            TextView firstTitle = recordList.getChildAt(0).findViewById(R.id.textView_recordTitle);
                            TextView secondTitle = recordList.getChildAt(1).findViewById(R.id.textView_recordTitle);
                            firstTitle.setText("第1站: ");
                            secondTitle.setText("第2站: ");
                        }
                        else {
                            recordList.addView(linearLayout_recordItem);
                        }
                    }
                    else {
                        recordList.addView(linearLayout_recordItem);
                    }
                    java.util.Date time = new java.util.Date(unixtime);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd, a h:mm");
                    timestamp.setText(sdf.format(time));

                    //TextView timestamp_title = new TextView(getBaseContext());
                    //recordLayout.addView(timestamp_title);
                    //timestamp_title.setText("Received time: ");
//
                    //TextView timestamp = new TextView(getBaseContext());
                    //recordLayout.addView(timestamp);
                    //long unixtime = Long.parseLong(record.getString("timestamp"));
                    //java.util.Date time = new java.util.Date(unixtime);
                    //SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
                    //timestamp.setText(sdf.format(time));
//
                    //TextView end_title = new TextView(getBaseContext());
                    //recordLayout.addView(end_title);
                    //end_title.setText("Reached logistic end: ");
//
                    //TextView end = new TextView(getBaseContext());
                    //recordLayout.addView(end);
                    //end.setText(record.getString("end"));
//
                    //TextView space = new TextView(getBaseContext());
                    //recordLayout.addView(space);
                    //space.setText(" ");
                }
            }
            catch (Exception e) {}

            //TextView divider1 = new TextView(getBaseContext());
            //linearLayout.addView(divider1);
            //divider1.setText("==========================================");
//
            //TextView packageId_title = new TextView(getBaseContext());
            //linearLayout.addView(packageId_title);
            //packageId_title.setText("Package ID: ");
//
            //linearLayout.addView(packageId);
//
            //TextView divider2 = new TextView(getBaseContext());
            //linearLayout.addView(divider2);
            //divider2.setText("==========================================");
//
            //TextView parentId_title = new TextView(getBaseContext());
            //linearLayout.addView(parentId_title);
            //parentId_title.setText("Parent package ID: ");
//
            //linearLayout.addView(parentId);
//
            //TextView divider3 = new TextView(getBaseContext());
            //linearLayout.addView(divider3);
            //divider3.setText("==========================================");
//
            //TextView space = new TextView(getBaseContext());
            //linearLayout.addView(space);
            //space.setText(" ");

            linearLayout.addView(recordLayout);

            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    textView_loading.setVisibility(View.GONE);
                    container.addView(linearLayout, 0);
                }
            });
        }
    };
}
