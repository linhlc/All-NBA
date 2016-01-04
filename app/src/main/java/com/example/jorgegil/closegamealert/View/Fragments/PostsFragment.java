package com.example.jorgegil.closegamealert.View.Fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.jorgegil.closegamealert.General.Post;
import com.example.jorgegil.closegamealert.R;
import com.example.jorgegil.closegamealert.Utils.PostsAdapter;
import com.example.jorgegil.closegamealert.Utils.PostsLoader;

import java.util.ArrayList;

public class PostsFragment extends Fragment {
    Context context;
    View rootView;
    String type;
    String url;
    ListView postsListView;
    LinearLayout linlaHeaderProgress, videoProgressLayout;

    VideoView videoView;
    View background;

    ArrayList<Post> postsList;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getActivity();
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString("TYPE");
            url = getArguments().getString("URL");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_posts, container, false);
        postsListView = (ListView) rootView.findViewById(R.id.postsListView);
        linlaHeaderProgress = (LinearLayout) rootView.findViewById(R.id.linlaHeaderProgress);
        videoProgressLayout = (LinearLayout) rootView.findViewById(R.id.videoProgressLayout);
        videoView = (VideoView) rootView.findViewById(R.id.videoView);
        background = rootView.findViewById(R.id.background);
        background.setVisibility(View.GONE);

        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels;
        switch (type) {
            case "small":
                dpAsPixels = (int) (5 * scale + 0.5f);
                postsListView.setDividerHeight(dpAsPixels);
                break;
            case "large":
                dpAsPixels = (int) (10 * scale + 0.5f);
                postsListView.setDividerHeight(dpAsPixels);
                break;
        }

        getPosts();

        return rootView;
    }

    private void getPosts() {
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        postsListView.setVisibility(View.GONE);

        // Request /r/nba threads
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadPosts(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("POSTS", "Volley error: " + error.toString());
                Toast toast = Toast.makeText(context, "Error loading threads", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    private void loadPosts(String response) {
        if (context != null) {
            // Loads posts into list view adapter
            PostsLoader postsLoader = new PostsLoader(response);
            postsList = postsLoader.fetchPosts();
            postsListView.setAdapter(new PostsAdapter(context, postsList, type));

            postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String videoURL = postsList.get(position).url
                            .replace("https://streamable.com/", "http://cdn.streamable.com/video/mp4/");
                    videoURL = videoURL + ".mp4";
                    playVideo(videoURL);

                }
            });

            linlaHeaderProgress.setVisibility(View.GONE);
            postsListView.setVisibility(View.VISIBLE);
        }
    }

    private void playVideo(String url) {
        videoView.setVisibility(View.VISIBLE);
        videoView.setZOrderOnTop(true); //HIDE
        postsListView.setEnabled(false);
        background.setVisibility(View.VISIBLE);
        videoProgressLayout.setVisibility(View.VISIBLE);

        try {
            Uri uri = Uri.parse(url);
            videoView.setMediaController(new android.widget.MediaController(context));
            videoView.setVideoURI(uri);
            videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoProgressLayout.setVisibility(View.GONE);
                    videoView.setZOrderOnTop(false); //SHOW
                }
            });
            videoView.start();
        } catch (Exception e) {
            // TODO: Handle exception
            stopVideo();
            Toast.makeText(context, "Error loading video", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopVideo() {
        videoView.stopPlayback();
        videoView.setZOrderOnTop(true); //HIDE
        videoView.setVisibility(View.GONE);
        postsListView.setEnabled(true);
        background.setVisibility(View.GONE);
        videoProgressLayout.setVisibility(View.GONE);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                stopVideo();
                getPosts();
                Log.d("POSTS", "fragment reloaded");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
