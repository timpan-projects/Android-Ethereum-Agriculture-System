package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class SplitWriteActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    Dialog nfcCheckDialog = null;
    int amount;
    TextView textView_remaining;
    ArrayList<String> newIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_write);

        amount = getIntent().getIntExtra("amount", 1);
        textView_remaining = findViewById(R.id.textView_remaining);
        textView_remaining.setText(String.valueOf(amount) + "張");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        checkNFCfunction();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            //Write
            //Toast.makeText(this, "寫入中...", Toast.LENGTH_SHORT).show();
            UUID uuid = UUID.randomUUID();
            String randomUUIDString = uuid.toString();
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefMessage ndefMessage = createNdefMessage(randomUUIDString);
            /*
                        Check if NFC is empty?
                        */
            writeNdefMessage(tag, ndefMessage, randomUUIDString);
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
        Intent intent = new Intent (this, SplitWriteActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
    }

    private void checkNFCfunction() {
        if (nfcAdapter == null || !nfcAdapter.isEnabled()){
            if (nfcCheckDialog != null)
                nfcCheckDialog.dismiss();

            AlertDialog dialog = new AlertDialog.Builder(SplitWriteActivity.this)
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

    private void formatTag (Tag tag, NdefMessage ndefMessage) {
        try{
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);

            if (ndefFormatable == null) {
                Toast.makeText(this, "Tag is not ndef formatable", Toast.LENGTH_SHORT).show();
                return;
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
            Toast.makeText(this, "寫入完成", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("formatTag", e.getMessage());
        }
    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage, String randomUUIDString) {
        try {
            if (tag == null) {
                Toast.makeText(this, "Tag object cannot be null", Toast.LENGTH_SHORT).show();
                return;
            }
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                formatTag(tag, ndefMessage);
            }
            else {
                ndef.connect();

                if (!ndef.isWritable()){
                    Toast.makeText(this, "Tag is not writable", Toast.LENGTH_SHORT).show();

                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();

                //Toast.makeText(this, "寫入完成", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "產生ID: " + randomUUIDString + " 並成功寫入NFC晶片", Toast.LENGTH_LONG).show();
                newIds.add(randomUUIDString);
                amount --;
                if (amount <= 0) {
                    Intent i = new Intent(SplitWriteActivity.this, SplitFinishActivity.class);
                    i.putExtra("amount", getIntent().getIntExtra("amount", 1));
                    i.putExtra("parentPackageId", getIntent().getStringExtra("parentPackageId"));
                    i.putStringArrayListExtra("newIds", newIds);
                    i.putExtra("fieldId", getIntent().getStringExtra("fieldId"));
                    i.putExtra("hubId", getIntent().getStringExtra("hubId"));
                    i.putExtra("field", getIntent().getStringExtra("field"));
                    i.putExtra("hub", getIntent().getStringExtra("hub"));
                    i.putExtra("uuid", getIntent().getStringExtra("uuid"));
                    i.putExtra("token", getIntent().getStringExtra("token"));
                    startActivityForResult(i, 1);
                }
                else {
                    textView_remaining.setText(String.valueOf(amount) + "張");
                }
            }
        } catch (Exception e) {
            Log.e("writeNdefMessage", e.getMessage());
        }
    }

    private NdefRecord createTextRecord(String content) {
        try{
            byte[] language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");

            final byte[] text = content.getBytes("UTF-8");
            final int languageSize = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1+ languageSize + textLength);

            payload.write((byte)(languageSize & 0x1F));
            payload.write(language, 0, languageSize);
            payload.write(text, 0, textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
        } catch (UnsupportedEncodingException e) {
            Log.e("createTextRecord", e.getMessage());
        }
        return null;
    }

    private NdefMessage createNdefMessage(String content) {
        NdefRecord ndefRecord = createTextRecord(content);
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
        return ndefMessage;
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
