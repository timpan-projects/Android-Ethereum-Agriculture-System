package tw.edu.nthu.cs.hsnl.agrilogistics.model;

import android.content.Context;

public class GetParameters {
    public Context context;
    public String url;
    public String id;

    public GetParameters(Context context, String url, String id){
        this.context = context;
        this.url = url;
        this.id = id;
    }
}
