package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class EndFinishActivity extends AppCompatActivity {

    TextView textView_finish;
    Button btn_return;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_finish);

        ArrayList<String> idList = getIntent().getStringArrayListExtra("idList");
        textView_finish = findViewById(R.id.textView_time);
        textView_finish.setText("共有"+ idList.size() +"箱貨物抵達終點");
        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);//RESULT_OK (=-1) BTW, pressing back button triggers RESULT_CANCELED (= 0)
                finish();
            }
        });
        for (int i = 0; i < idList.size(); i++)
            Log.d("idList " + i, idList.get(i));
    }
}
