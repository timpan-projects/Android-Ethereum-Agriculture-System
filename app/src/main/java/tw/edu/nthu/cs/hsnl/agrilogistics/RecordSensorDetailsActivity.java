package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordSensorDetailsActivity extends AppCompatActivity {

    LinearLayout section_sensorList;

    //Variables
    LineChart chart;
    TextView now;
    TextView max;
    TextView min;
    TextView avg;
    List<String> dateStrings =  new ArrayList<String>();
    String start = "0";
    String end = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sensor_details);
        section_sensorList = findViewById(R.id.section_sensorList);
        chart = findViewById(R.id.chart);
        now = findViewById(R.id.textView_now);
        max = findViewById(R.id.textView_max);
        min = findViewById(R.id.textView_min);
        avg = findViewById(R.id.textView_avg);

        //new getData().execute(getIntent().getStringExtra("sensorType"), getIntent().getStringExtra("sensorId"), "1557673772000", "1557683772000");
        Log.d("Check", "type:" + getIntent().getStringExtra("sensorType"));
        Log.d("Check", "id:" + getIntent().getStringExtra("sensorId"));
        Log.d("Check", "start:" + getIntent().getStringExtra("start"));
        Log.d("Check", "end:" + getIntent().getStringExtra("end"));

        new getData().execute(getIntent().getStringExtra("sensorType"), getIntent().getStringExtra("sensorId"), getIntent().getStringExtra("start"), getIntent().getStringExtra("end"));
    }

    public class getData extends AsyncTask<Object, Void, Void> {

        String url = getString(R.string.iot_platform_url) + getString(R.string.iot_platform_getData);
        String response_str;
        protected Void doInBackground(Object... args) {
            callHttpGet(url + args[0] + "/" + args[1] + "/" + args[2] + "/" + args[3]);
            return null;
        }

        public Void callHttpGet(String url) {
            HttpClient client = new DefaultHttpClient();
            try {
                HttpGet get = new HttpGet(url);
                get.setHeader("Content-type", "application/json");
                get.setHeader("token",getIntent().getStringExtra("token"));
                HttpResponse response = client.execute(get);
                response_str = EntityUtils.toString(response.getEntity(), org.apache.http.protocol.HTTP.UTF_8);
                Log.d("Data response", response_str);
                response_str = URLDecoder.decode(response_str, "utf-8");
            } catch (Exception e) {
                Log.e(e.getClass().getName(), e.getMessage(), e);
                response_str = "Exception";
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            List<float[]> data = abstractDataPoints(response_str, "Items", "timestamp", "value", 1, 1);
            drawChart(data , chart, "");
        }
    }

    //Functions
    List<float[]> abstractDataPoints(String jsonSource, String jsonArrayName, String variable_X, String variable_Y, int scale_X, int scale_Y) {

        JSONObject jsonObj = null;
        List<float[]> sensorData = new ArrayList<float[]>();
        Double fix = 0d;
        Float now_value = 0f;
        Float max_value = 0f;
        Float min_value = 0f;
        double total = 0d;
        try {
            jsonObj = new JSONObject(jsonSource);
            JSONArray jsonArr = jsonObj.getJSONArray(jsonArrayName);
            int data_count = jsonArr.length();

            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject dataList = jsonArr.getJSONObject(i);

                Double dataX = Double.valueOf(dataList.getString(variable_X));
                Float dataY = 0f;
                if (dataList.getString(variable_Y).equals("null")) {
                    dataY = Float.NaN;
                    data_count--;
                }
                else{
                    dataY = Float.valueOf(dataList.getString(variable_Y));
                    now_value = dataY;
                    total = total + dataY;

                    if(i == 0) {
                        fix = dataX;
                        now_value = dataY;
                        max_value = dataY;
                        min_value = dataY;
                        Log.d("fix", String.valueOf(fix));
                    }
                    if(dataY > max_value)
                        max_value = dataY;
                    if(dataY < min_value)
                        min_value = dataY;
                    if(i == jsonArr.length() - 1)
                        avg.setText(String.format("%.3f", total/data_count));
                }

                float[] data = {((float)(dataX/*-fix*/ / scale_X))/*i*/, dataY / scale_Y};
                sensorData.add(data);
                Log.d("data point x", String.valueOf(dataX - fix));
                Log.d("data point y", dataY.toString());
                Date dateString = new Date((long)(dataX+0));
                String[] splitted = dateString.toString().split(":|\\s+");
                dateStrings.add(splitted[1] + "-" + splitted[2] + "\n" + splitted[3] + ":" + splitted[4]);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        now.setText(String.format("%.1f", now_value));
        max.setText(String.format("%.1f", max_value));
        min.setText(String.format("%.1f", min_value));
        return sensorData;
    }

    void drawChart(List<float[]> chartData, LineChart chart, String label_y) {
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < chartData.size(); i++){
            Log.d("add into entry", String.valueOf(chartData.get(i)[0]));
            entries.add(new Entry(chartData.get(i)[0], chartData.get(i)[1]));
        }
        LineDataSet set1 = new LineDataSet(entries, "");

        set1.setDrawIcons(false);
        // set the line to be drawn like this "- - - - - -"
        //set1.enableDashedLine(10f, 5f, 0f);
        //set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.parseColor("#19B4CC"));
        set1.setCircleColor(Color.parseColor("#19B4CC"));
        set1.setLineWidth(2f);
        set1.setDrawValues(false);
        if (entries.size() < 50){
            set1.setDrawCircleHole(true);
            if(entries.size() <= 10)
                set1.setCircleRadius(4f);
            else
                set1.setCircleRadius(3f);
        }
        else {
            set1.setLineWidth(1f);
            set1.setCircleRadius(2f);
            set1.setDrawCircleHole(false);
        }

        set1.setDrawFilled(true);
        set1.setFormLineWidth(1f);
        set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set1.setFormSize(15.f);
        if (Utils.getSDKInt() >= 18)// fill drawable only supported on api level 18 and above
            set1.setFillDrawable(this.getResources().getDrawable(R.drawable.fade_blue));
        else
            set1.setFillColor(Color.parseColor("#BEE5EB"));
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
        //chart.getXAxis().setDrawLabels(false);

        LineData lineData = new LineData(set1);
        //chart.setXAxisRenderer(new CustomXAxisRenderer(chart.getViewPortHandler(), chart.getXAxis(), chart.getTransformer(YAxis.AxisDependency.LEFT)));
        chart.setData(lineData);
        chart.setDrawGridBackground(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.animateX(1000);
        IMarker marker = new YourMarkerView(this, R.layout.custom_marker_view);
        chart.setMarker(marker);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        XAxis topAxis = chart.getXAxis();
        topAxis.setEnabled(false);

        chart.invalidate();
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
