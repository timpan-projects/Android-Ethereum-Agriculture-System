package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.nkzawa.socketio.client.IO;

import org.json.JSONObject;
import java.net.URISyntaxException;

import tw.edu.nthu.cs.hsnl.agrilogistics.model.SliderAddressTutorial;

public class SetWalletActivity extends AppCompatActivity {

    EditText editText_address;
    Button btn_bind;
    com.github.nkzawa.socketio.client.Socket socket;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wallet);

        try{
            SharedPreferences prefs = getSharedPreferences("ServerIP", MODE_PRIVATE);
            socket = IO.socket(prefs.getString("ip", getString(R.string.app_server_url)));//設定Socket連接的IP位址
        } catch (URISyntaxException e){
            Log.d("Socket connection error", e.getMessage());
        }
        socket.connect();

        editText_address = findViewById(R.id.editText_address);
        btn_bind = findViewById(R.id.btn_bind);
        btn_bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(SetWalletActivity.this)
                        .setTitle("請確認以下資訊")
                        .setMessage("您確定要將虛擬錢包: " + editText_address.getText().toString() + "綁定至您的帳戶嗎?")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                JSONObject jObj = new JSONObject();
                                try {
                                    jObj.put("email", getIntent().getStringExtra("email"));
                                    jObj.put("address", editText_address.getText().toString());
                                } catch (Exception e) {Log.d("JSON Exception", e.getMessage());}
                                socket.emit("bind wallet", jObj);
                                new AlertDialog.Builder(SetWalletActivity.this)
                                        .setTitle("綁定成功")
                                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)
                        .show();
            }
        });
        viewPager = findViewById(R.id.viewPager);
        SliderAddressTutorial viewPagerAdapter = new SliderAddressTutorial(this);
        viewPager.setAdapter(viewPagerAdapter);
    }
}
