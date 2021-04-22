package com.example.jscloudapi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.js_dynamicdatabase.helpers.ObserveEvent;
import com.example.js_dynamicdatabase.models.Collection;
import com.example.js_dynamicdatabase.JSCloudDynamicDB;
import com.example.js_dynamicdatabase.interfaces.OnCompleteListener;
import com.example.js_dynamicdatabase.interfaces.OnDocChangedListener;
import com.example.js_dynamicdatabase.models.TaskResult;
import com.example.js_dynamicdatabase.helpers.FilterQuery;
import com.example.js_dynamicdatabase.helpers.FilterQueryBuilder;
import com.example.jscloudapi.models.Post;

import java.util.ArrayList;

public class DbActivity extends AppCompatActivity {

    Collection<Post>collections;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);
        collections=JSCloudDynamicDB.getInstance().collection("jado");
        register_on_change(null);
    }

    public void insert_post(View view) {
        collections.insert(new Post()).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if(result.isSuccess())
                {
                    Post post=result.getResult(Post.class);
                    Log.e("POST",post.getpID());
                }
                Toast.makeText(getApplicationContext(),result.getMessage(),Toast.LENGTH_SHORT).show();

            }
        }).execute();
    }

    public void insert_all(View view) {
        ArrayList<Post> arr= new ArrayList<>();
        for(int i=0;i<999;i++)
        {
            Post post= new Post();
            post.setAuthorName("user"+i);
            post.setPublishedOn(System.currentTimeMillis()+(i*1000*60));
            post.setTags(new String[]{"#nomore","#funislife"});
            post.setPostContent("This is some amazing post content");
            post.setLikesCount(i);
            arr.add(post);
        }
        long t=System.currentTimeMillis();

        collections.insert(arr).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onTaskComplete(TaskResult result) {

                if(result.isSuccess())
                {
                    Post[] post=result.getResult(Post[].class);
                    Log.e("POST",post[0].getpID());
                }
                Toast.makeText(getApplicationContext(),"Time taken:"+(System.currentTimeMillis()-t)+"ms",Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    public void read_all(View view) {
        try {
            long t=System.currentTimeMillis();
            FilterQuery filterQuery=new FilterQueryBuilder(FilterQuery.Binary.MATCH)
                    .selectOnly("_id")
                    .whereEquals("authorName","user23")
                    .build();
            collections.read(filterQuery).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onTaskComplete(TaskResult result) {
                    if(result.isSuccess())
                    {
                        Post[] post=result.getResult(Post[].class);
                        Log.e("REad",post[0].getpID());
                    }
                    Toast.makeText(getApplicationContext(),"Time taken:"+(System.currentTimeMillis()-t)+"ms",Toast.LENGTH_SHORT).show();
                }
            }).execute();
        }catch (Exception e)
        {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    public void register_on_change(View view) {
        collections.observe(new OnDocChangedListener() {
            @Override
            public void onDocChanged(ObserveEvent event) {
                Log.e("DBActivity",event.getCollectionName()+" | "+event.getObserveType().name());
            }

            @Override
            public void onDocAdded(ObserveEvent event) {
                Log.e("DBActivity",event.getCollectionName()+" | "+event.getObserveType().name());
            }

            @Override
            public void onDocRemoved(ObserveEvent event) {
                Log.e("DBActivity",event.getCollectionName()+" | "+event.getObserveType().name());
            }

        });
    }
}