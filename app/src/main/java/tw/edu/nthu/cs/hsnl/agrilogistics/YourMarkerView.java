package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.Date;

public class YourMarkerView extends MarkerView {

    private TextView text_date;
    private TextView text_time;
    private TextView text_value;

    public YourMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        text_date = (TextView) findViewById(R.id.date);
        text_time = (TextView) findViewById(R.id.time);
        text_value = (TextView) findViewById(R.id.value);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        //tvContent.setText("" + Utils.formatNumber(e.getY(), 1, true));
        Date dateString = new Date((long)(e.getX()));
        String[] dateDetails = dateString.toString().split(" ");
        String year = dateDetails[5];
        String month = monthTranslate(dateDetails[1]);
        String day = dateDetails[2];
        String time = dateDetails[3].split(":")[0] + ":" + dateDetails[3].split(":")[1];
        text_date.setText("日期: " + year + "/" + month + "/" + day);
        text_time.setText("時間: " + time);
        text_value.setText("數值: " + String.valueOf(e.getY()));

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }

    private String monthTranslate(String eng){
        int number = 0;
        if(eng.matches("Jan"))
            number = 1;
        if(eng.matches("Feb"))
            number = 2;
        if(eng.matches("Mar"))
            number = 3;
        if(eng.matches("Apr"))
            number = 4;
        if(eng.matches("May"))
            number = 5;
        if(eng.matches("Jun"))
            number = 6;
        if(eng.matches("Jul"))
            number = 7;
        if(eng.matches("Aug"))
            number = 8;
        if(eng.matches("Sep"))
            number = 9;
        if(eng.matches("Nov"))
            number = 11;
        if(eng.matches("Dec"))
            number = 12;
        //return
        if (number == 0)
            return eng;
        else
            return String.valueOf(number);
    }
}
