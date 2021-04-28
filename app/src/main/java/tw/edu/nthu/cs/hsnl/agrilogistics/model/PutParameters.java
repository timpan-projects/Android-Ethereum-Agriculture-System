package tw.edu.nthu.cs.hsnl.agrilogistics.model;

import android.content.Context;

public class PutParameters {
    public Context context;
    public String url;
    public String field;
    public String field_list;

    public PutParameters(Context context, String url, String field, String field_list){
        this.context = context;
        this.url = url;
        this.field = field;
        this.field_list = field_list;
    }
}