package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONObject;

import java.net.URISyntaxException;

public class SendWeiActivity extends AppCompatActivity {

    com.github.nkzawa.socketio.client.Socket socket;
    EditText editText_email;
    EditText editText_value;
    Button btn_send;
    Spinner spinner_unit;
    Spinner spinner_quick;
    ArrayAdapter adapter_unit;
    ArrayAdapter adapter_quick;
    final String[] unitOptions = new String[] {"wei","Eth"};
    final String[] quickOptions = new String[] {"快速輸入","1 wei", "10 wei", "100 wei", "1000 wei", "1 Eth", "2 Eth", "5 Eth", "10 Eth"};
    ImageView btn_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_wei);

        try{
            SharedPreferences prefs = getSharedPreferences("ServerIP", MODE_PRIVATE);
            socket = IO.socket(prefs.getString("ip", getString(R.string.app_server_url)));//設定Socket連接的IP位址
        } catch (URISyntaxException e){
            Log.d("Socket connection error", e.getMessage());
        }
        socket.connect();

        editText_email = findViewById(R.id.editText_email);
        editText_value = findViewById(R.id.editText_value);
        spinner_unit = findViewById(R.id.spinner_unit);
        spinner_quick = findViewById(R.id.spinner_quick);
        adapter_unit = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, unitOptions);
        adapter_quick = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, quickOptions);
        spinner_unit.setAdapter(adapter_unit);
        spinner_quick.setAdapter(adapter_quick);
        spinner_quick.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        editText_value.setText("1");
                        spinner_unit.setSelection(0, true);
                        break;
                    case 2:
                        editText_value.setText("10");
                        spinner_unit.setSelection(0, true);
                        break;
                    case 3:
                        editText_value.setText("100");
                        spinner_unit.setSelection(0, true);
                        break;
                    case 4:
                        editText_value.setText("1000");
                        spinner_unit.setSelection(0, true);
                        break;
                    case 5:
                        editText_value.setText("1");
                        spinner_unit.setSelection(1, true);
                        break;
                    case 6:
                        editText_value.setText("2");
                        spinner_unit.setSelection(1, true);
                        break;
                    case 7:
                        editText_value.setText("5");
                        spinner_unit.setSelection(1, true);
                        break;
                    case 8:
                        editText_value.setText("10");
                        spinner_unit.setSelection(1, true);
                        break;
                }
                spinner_quick.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinner_quick.setSelection(0);
            }
        });
        btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(SendWeiActivity.this)
                        .setTitle("請確認以下資訊")
                        .setMessage("您是否確定要將 " + editText_value.getText().toString() + " " + spinner_unit.getSelectedItem() +" 轉至帳戶: " + editText_email.getText().toString())
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                socket.emit("search wallet", editText_email.getText().toString());
                                editText_email.setEnabled(false);
                                editText_value.setEnabled(false);
                                spinner_unit.setEnabled(false);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)
                        .show();
            }
        });
        btn_list = findViewById(R.id.imageView_list);
        btn_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SendWeiActivity.this, WalletListActivity.class);
                startActivity(i);
            }
        });
        socket.on("return wallet address", echoReturn);
    }

    private Emitter.Listener echoReturn = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String url;
            if (spinner_unit.getSelectedItemPosition() == 0)
                url = "http://35.187.159.253:8080/home.html?value=" + editText_value.getText().toString() + "&address=" + args[0];
            else
                url = "http://35.187.159.253:8080/home.html?value=" + editText_value.getText().toString() + "000000000000000000&address=" + args[0];
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
                    editText_email.setEnabled(true);
                    editText_value.setEnabled(true);
                    spinner_unit.setEnabled(true);
                }
            });
            startActivity(intent);
        }
    };
}