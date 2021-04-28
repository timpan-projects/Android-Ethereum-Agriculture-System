package tw.edu.nthu.cs.hsnl.agrilogistics.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import tw.edu.nthu.cs.hsnl.agrilogistics.config.Config;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.net.URLDecoder;

/**
 * Created by tim on 2018/2/4.
 */

public class callApiGet extends AsyncTask<GetParameters, Void, String> {

    public static final String TOKEN_PREFS = "token_prefs";
    public static final String SAVED_TOKEN = "saved_token";

    protected String doInBackground(GetParameters... args ) {
        return callHttpGet(args[0].context, Config.url+args[0].url, args[0].id);
    }

    public String callHttpGet(Context context, String url, String token) {
        HttpClient client = new DefaultHttpClient();
        String response_str;
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_PREFS, Context.MODE_PRIVATE);
            HttpGet get = new HttpGet(url);
            get.setHeader("Content-type", "application/json");
            get.setHeader("token", sharedPreferences.getString(SAVED_TOKEN, ""));
            HttpResponse response = client.execute(get);
            response_str = EntityUtils.toString(response.getEntity(), org.apache.http.protocol.HTTP.UTF_8);
            Log.d("HttpGet", response_str);

            return URLDecoder.decode(response_str, "utf-8");
        } catch (Exception e) {
            Log.e(e.getClass().getName(), e.getMessage(), e);
            return ("Exception");
        }
    }
}


