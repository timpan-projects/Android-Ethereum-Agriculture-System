package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MidReceiveActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    Button btn_next;
    ArrayList<String> idList;
    Dialog nfcCheckDialog = null;
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mid_recieve);

        try{
            SharedPreferences prefs = getSharedPreferences("ServerIP", MODE_PRIVATE);
            socket = IO.socket(prefs.getString("ip", getString(R.string.app_server_url)));//設定Socket連接的IP位址
        } catch (URISyntaxException e){
            Log.d("Socket connection error", e.getMessage());
        }
        socket.connect();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        idList = new ArrayList<>();
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MidReceiveActivity.this, MidFinishActivity.class);
                i.putStringArrayListExtra("idList", idList);
                i.putExtra("uuid", getIntent().getStringExtra("uuid"));
                i.putExtra("token", getIntent().getStringExtra("token"));
                startActivityForResult(i, 1);
            }
        });
        checkNFCfunction();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            //Read mode
            //Toast.makeText(this, "讀取成功!", Toast.LENGTH_SHORT).show();
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (parcelables != null && parcelables.length > 0) {
                readTextFromMessage((NdefMessage) parcelables[0]);
            } else {
                Toast.makeText(this, "此NFC標籤無附帶訊息", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onResume() {
        checkNFCfunction();
        enableForegroundDispatch();
        super.onResume();
    }

    protected void onPause() {
        disableForegroundDispatch();
        super.onPause();
    }

    private void disableForegroundDispatch() {

        nfcAdapter.disableForegroundDispatch(this);
    }

    private void enableForegroundDispatch() {
        Intent intent = new Intent (this, MidReceiveActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
    }

    private void checkNFCfunction() {
        if (nfcAdapter == null || !nfcAdapter.isEnabled()){
            if (nfcCheckDialog != null)
                nfcCheckDialog.dismiss();

            AlertDialog dialog = new AlertDialog.Builder(MidReceiveActivity.this)
                    .setTitle("請開啟NFC功能")
                    .setMessage("請至設定>>NFC，開啟手機近距離無線通訊功能")
                    .setPositiveButton("前往設定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                        }
                    })
                    .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();

            nfcCheckDialog = dialog;
        }
    }

    private void readTextFromMessage(NdefMessage ndefMessage) {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        if (ndefRecords != null && ndefRecords.length > 0) {
            NdefRecord ndefRecord = ndefRecords[0];

            String tagContent = getTextFromNdefRecord(ndefRecord);
            idList.add(tagContent);
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("uuid", tagContent);
                jObj.put("hubId", getIntent().getStringExtra("hubId"));
                jObj.put("field", getIntent().getStringExtra("field"));
                //jObj.put("hub", getIntent().getStringExtra("hub"));
                jObj.put("time", System.currentTimeMillis());
            } catch (Exception e) {Log.d("JSON Exception", e.getMessage());}
            socket.emit("receive package", jObj);
            Toast.makeText(this, "確認接收貨物，ID: " + tagContent + "  已接收" + idList.size() + "箱", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "No NDEF records found", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTextFromNdefRecord(NdefRecord ndefRecord) {
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String (payload, languageSize + 1, payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.d("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
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
