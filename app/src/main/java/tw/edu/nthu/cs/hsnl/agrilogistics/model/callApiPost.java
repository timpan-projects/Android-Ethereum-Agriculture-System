package tw.edu.nthu.cs.hsnl.agrilogistics.model;

import android.os.AsyncTask;
import android.util.Log;

import tw.edu.nthu.cs.hsnl.agrilogistics.config.Config;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.net.URLDecoder;


/**
 * Created by tim on 2018/2/4.
 */

public class callApiPost extends AsyncTask<String, Void, String> {

    protected String doInBackground(String... args) {
        Log.d("url post", Config.url + args[0]);
        return callHttpPost(Config.url + args[0], args[1], args[2]);
    }

    private String callHttpPost(String url, String email, String password) {
        HttpClient client = new DefaultHttpClient();
        String response_str;
        try {
            HttpPost post = new HttpPost(url);
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);
            post.setHeader("Content-type", "application/json");
            post.setEntity(new ByteArrayEntity(body.toString().getBytes("UTF8")));
            HttpResponse response = client.execute(post);
            response_str = EntityUtils.toString(response.getEntity(), org.apache.http.protocol.HTTP.UTF_8);
            Log.d("Post response", response_str);
            return URLDecoder.decode(response_str, "utf-8");
        } catch (Exception e) {
            Log.e(e.getClass().getName(), e.getMessage(), e);
            return ("Exception");
        }
    }
}


