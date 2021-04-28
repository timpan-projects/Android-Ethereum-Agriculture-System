package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class AccountActivity extends AppCompatActivity {

    Socket socket;
    TextView textView_wAddress;
    TextView textView_wBalance;
    //LinearLayout btn1;
    LinearLayout btn2;
    LinearLayout btn3;
    //LinearLayout btn4;
    LinearLayout btn5;
    LinearLayout btn6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        try{
            SharedPreferences prefs = getSharedPreferences("ServerIP", MODE_PRIVATE);
            socket = IO.socket(prefs.getString("ip", getString(R.string.app_server_url)));//設定Socket連接的IP位址
        } catch (URISyntaxException e){
            Log.d("Socket connection error", e.getMessage());
        }
        socket.connect();
        socket.emit("search wallet and balance", getIntent().getStringExtra("email"));
        socket.on("return wallet address for display", echoReturnAddress);
        socket.on("return wallet balance", echoReturnBalance);


        textView_wAddress = findViewById(R.id.textView_wAddress);
        textView_wBalance = findViewById(R.id.textView_wBalance);
        //btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        //btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AccountActivity.this, SendWeiActivity.class);
                i.putExtra("email", getIntent().getStringExtra("email"));
                startActivityForResult(i, 1);
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AccountActivity.this, SetWalletActivity.class);
                i.putExtra("email", getIntent().getStringExtra("email"));
                startActivityForResult(i, 1);
            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new AlertDialog.Builder(AccountActivity.this)
                //        .setTitle("即將登出您的帳號")
                //        .setMessage("您確定要登出嗎?")
                //        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                //            @Override
                //            public void onClick(DialogInterface dialog, int which) {
                //                Intent i = new Intent(AccountActivity.this, LoginActivity.class);
                //                startActivity(i);
                //                finish();
                //            }
                //        })
                //        .setNegativeButton("取消", null)
                //        .setCancelable(true)
                //        .show();
            }
        });
    }

    private Emitter.Listener echoReturnAddress = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final String address = args[0].toString();

            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    textView_wAddress.setText("錢包地址: " + address);
                }
            });
        }
    };

    private Emitter.Listener echoReturnBalance = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final String balance = args[0].toString();

            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    textView_wBalance.setText("錢包餘額: " + balance + " wei");
                }
            });
        }
    };
}
