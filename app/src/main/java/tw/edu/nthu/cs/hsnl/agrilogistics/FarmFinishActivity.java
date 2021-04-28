package tw.edu.nthu.cs.hsnl.agrilogistics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FarmFinishActivity extends AppCompatActivity {

    TextView textView_finish;
    Button btn_return;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_finish);

        textView_finish = findViewById(R.id.textView_time);
        textView_finish.setText("共完成"+ getIntent().getIntExtra("amount", 1) +"張NFC晶片");
        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
