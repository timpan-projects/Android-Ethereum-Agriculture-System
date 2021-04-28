package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.UUID;

public class FarmPaymentActivity extends AppCompatActivity {

    Socket socket;
    Button test_btn;
    ProgressDialog dialog;
    UUID uuid = UUID.randomUUID();
    String orderid = uuid.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_payment);

        try{
            SharedPreferences prefs = getSharedPreferences("ServerIP", MODE_PRIVATE);
            socket = IO.socket(prefs.getString("ip", getString(R.string.app_server_url)));//設定Socket連接的IP位址
        } catch (URISyntaxException e){
            Log.d("Socket connection error", e.getMessage());
        }
        socket.connect();
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("amount", getIntent().getIntExtra("amount", 1));
            jObj.put("brand", getIntent().getStringExtra("brand"));
            jObj.put("orderid", orderid);
            jObj.put("email", getIntent().getStringExtra("email"));
        } catch (Exception e) {Log.d("JSON Exception", e.getMessage());}
        socket.emit("payment created", jObj);

        socket.on("payment request", echoRequest);
        socket.on("payment success", echoSuccess);
        socket.on("payment fail", echoFail);



        dialog = new ProgressDialog(FarmPaymentActivity.this);
        test_btn = findViewById(R.id.testBtn);
        test_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FarmPaymentActivity.this, FarmWriteActivity.class);
                i.putExtra("amount", getIntent().getIntExtra("amount", 1));
                i.putExtra("uuid", getIntent().getStringExtra("uuid"));
                i.putExtra("token", getIntent().getStringExtra("token"));
                i.putExtra("fieldId", getIntent().getStringExtra("fieldId"));
                i.putExtra("hubId", getIntent().getStringExtra("hubId"));
                i.putExtra("field", getIntent().getStringExtra("field"));
                i.putExtra("hub", getIntent().getStringExtra("hub"));
                i.putExtra("orderid", orderid);
                startActivityForResult(i, 1);
            }
        });
    }

    private Emitter.Listener echoSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Intent i = new Intent(FarmPaymentActivity.this, FarmWriteActivity.class);
            i.putExtra("amount", getIntent().getIntExtra("amount", 1));
            i.putExtra("uuid", getIntent().getStringExtra("uuid"));
            i.putExtra("token", getIntent().getStringExtra("token"));
            i.putExtra("fieldId", getIntent().getStringExtra("fieldId"));
            i.putExtra("hubId", getIntent().getStringExtra("hubId"));
            i.putExtra("field", getIntent().getStringExtra("field"));
            i.putExtra("hub", getIntent().getStringExtra("hub"));
            i.putExtra("orderid", orderid);
            startActivityForResult(i, 1);
            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
        }
    };

    private Emitter.Listener echoFail = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    new AlertDialog.Builder(FarmPaymentActivity.this)
                            .setTitle("付款失敗")
                            .setMessage("")
                            .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            });
        }
    };

    private Emitter.Listener echoRequest = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("value", args[0]);
                jObj.put("email", getIntent().getStringExtra("email"));
            } catch (Exception e) {Log.d("JSON Exception", e.getMessage());}
            socket.emit("payment check", jObj);

            String url;
            double amount = Double.parseDouble(args[0].toString());
            amount = amount * 1000000000000000000.0;
            url = "http://35.187.159.253:8080/payment.html?value=" + BigDecimal.valueOf(amount).toPlainString();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName("org.mozilla.firefox", "org.mozilla.firefox.App"));
            intent.setAction("org.mozilla.gecko.BOOKMARK");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("args", "--url=" + url);
            intent.setData(Uri.parse(url));
            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    dialog.setMessage("款項驗證中，請稍後...");
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
            });
            startActivity(intent);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("ResultCode", "code: " + resultCode);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
