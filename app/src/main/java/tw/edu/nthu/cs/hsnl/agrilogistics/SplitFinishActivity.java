package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;

public class SplitFinishActivity extends AppCompatActivity {

    TextView textView_finish;
    Button btn_return;
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_finish);

        try{
            SharedPreferences prefs = getSharedPreferences("ServerIP", MODE_PRIVATE);
            socket = IO.socket(prefs.getString("ip", getString(R.string.app_server_url)));//設定Socket連接的IP位址
        } catch (URISyntaxException e){
            Log.d("Socket connection error", e.getMessage());
        }
        socket.connect();

        textView_finish = findViewById(R.id.textView_time);
        textView_finish.setText("已將貨物分裝成"+ getIntent().getIntExtra("amount", 1) +"箱新貨物");
        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("originalId", getIntent().getStringExtra("parentPackageId"));
            jObj.put("newIds", getIntent().getStringArrayListExtra("newIds"));
            jObj.put("sensorUnitId", getIntent().getStringExtra("hubId"));
            jObj.put("time", System.currentTimeMillis());
        } catch (Exception e) {Log.d("JSON Exception", e.getMessage());}
        socket.emit("split package", jObj);
    }
}
