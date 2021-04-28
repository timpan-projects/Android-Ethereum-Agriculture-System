package tw.edu.nthu.cs.hsnl.agrilogistics;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class WalletListActivity extends AppCompatActivity {

    com.github.nkzawa.socketio.client.Socket socket;
    LinearLayout section_list;
    Button btn_finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);
        section_list = findViewById(R.id.section_list);
        btn_finish = findViewById(R.id.btn_finish);
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        try{
            SharedPreferences prefs = getSharedPreferences("ServerIP", MODE_PRIVATE);
            socket = IO.socket(prefs.getString("ip", getString(R.string.app_server_url)));//設定Socket連接的IP位址
        } catch (URISyntaxException e){
            Log.d("Socket connection error", e.getMessage());
        }
        socket.connect();
        socket.emit("get wallet list");
        socket.on("return wallet list", echoReturn);
    }

    private Emitter.Listener echoReturn = new Emitter.Listener() {
        String temp = "";
        @Override
        public void call(Object... args) {
            final JSONArray jArr = (JSONArray) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < jArr.length(); i++) {
                            JSONObject jObj = new JSONObject(jArr.get(i).toString());
                            temp = temp + jObj.getString("email");
                            temp = temp + jObj.getString("address");
                            LayoutInflater inflater = LayoutInflater.from(WalletListActivity.this);
                            LinearLayout user_item = (LinearLayout) inflater.inflate(R.layout.item_wallet, null, false);
                            section_list.addView(user_item);
                            final TextView textView_email = user_item.findViewById(R.id.textView_email);
                            final TextView textView_address = user_item.findViewById(R.id.textView_address);
                            textView_email.setText(jObj.getString("email"));
                            textView_address.setText(jObj.getString("address"));
                            Button btn_copy = user_item.findViewById(R.id.btn_copy);
                            btn_copy.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("address", textView_email.getText());
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(WalletListActivity.this, "已複製用戶信箱", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {}
                }
            });
        }
    };
}