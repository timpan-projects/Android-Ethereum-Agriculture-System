package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class FarmStartedActivity extends AppCompatActivity {

    TextView textView_time;
    TextView textView_fieldId;
    TextView textView_hubId;
    TextView textView_field;
    TextView textView_hub;
    Button btn_return;
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_started);

        try{
            SharedPreferences prefs = getSharedPreferences("ServerIP", MODE_PRIVATE);
            socket = IO.socket(prefs.getString("ip", getString(R.string.app_server_url)));//設定Socket連接的IP位址
        } catch (URISyntaxException e){
            Log.d("Socket connection error", e.getMessage());
        }
        socket.connect();
        textView_time = findViewById(R.id.textView_time);
        textView_fieldId = findViewById(R.id.textView_fieldId);
        textView_hubId = findViewById(R.id.textView_hubId);
        textView_field = findViewById(R.id.textView_field);
        textView_hub = findViewById(R.id.textView_hub);
        textView_time.setText(getIntent().getStringExtra("time"));
        textView_fieldId.setText(getIntent().getStringExtra("fieldId"));
        textView_hubId.setText(getIntent().getStringExtra("hubId"));
        textView_field.setText(getIntent().getStringExtra("field"));
        textView_hub.setText(getIntent().getStringExtra("hub"));
        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        final String time = getIntent().getStringExtra("time");
        final String hubId = getIntent().getStringExtra("hubId");
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("time", time);
            jObj.put("hubId", hubId);
        } catch (Exception e) {
            Log.d("Exception", e.getMessage());
        }
        socket.emit("start planting", jObj);
    }
}
