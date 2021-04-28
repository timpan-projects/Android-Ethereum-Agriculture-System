package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tw.edu.nthu.cs.hsnl.agrilogistics.model.callApiPost;

public class LoginActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.editText_username);
        password = findViewById(R.id.editText_password);
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().matches("")) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("請輸入帳號")
                            .setMessage("帳號欄位不可留空")
                            .setPositiveButton("重試", null)
                            .show();
                }
                else if (password.getText().toString().matches("")) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("請輸入密碼")
                            .setMessage("密碼欄位不可留空")
                            .setPositiveButton("重試", null)
                            .show();
                }
                else
                    new Login().execute(username.getText().toString(), password.getText().toString());
            }
        });
    }

    public class Login extends AsyncTask<String, Void, Void> {

        private String url = getString(R.string.iot_platform_url);
        private String response_str;
        private ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("登入中，請稍後...");
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }

        protected Void doInBackground(String... args) {
            callHttpPost(url + getString(R.string.iot_platform_login), args[0], args[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            try {
                JSONObject jObj = new JSONObject(response_str);
                String uuid = jObj.get("uuid").toString();
                String token = jObj.get("token").toString();
                if (token.matches("false")) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("登入失敗")
                            .setMessage("請確認您的帳號密碼已輸入正確，並確保網路連線狀況穩定")
                            .setPositiveButton("重試",null)
                            .show();
                }
                else {
                    Intent i = new Intent(LoginActivity.this, SelectRoleActivity.class);
                    i.putExtra("email", username.getText().toString());
                    i.putExtra("uuid", uuid);
                    i.putExtra("token", token);
                    startActivity(i);
                }
            } catch (Exception e) {
                Log.d("onPostExecute", e.getMessage());
            }
        }



        private Void callHttpPost(String url, String username, String password) {
            HttpClient client = new DefaultHttpClient();
            try {
                HttpPost post = new HttpPost(url);
                JSONObject body = new JSONObject();

                body.put("email", username);
                body.put("password", password);
                Log.d("body", body.toString());

                post.setHeader("Content-type", "application/json");
                post.setEntity(new ByteArrayEntity(body.toString().getBytes("UTF8")));
                HttpResponse response = client.execute(post);
                response_str = EntityUtils.toString(response.getEntity(), org.apache.http.protocol.HTTP.UTF_8);
                Log.d("Login response", response_str);

            } catch (Exception e) {
                Log.d("Login exception", e.getMessage());
            }
            return null;
        }
    }

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("即將離開AgriLogistic")
                .setMessage("您確定要關閉應用程式嗎?")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(true)
                .show();
    }
}
