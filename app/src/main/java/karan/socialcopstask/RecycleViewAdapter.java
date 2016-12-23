package karan.socialcopstask;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by stpl on 12/23/2016.
 */

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.Holder> {
    private ArrayList<Model> model;
    VideoListener videoListener;
    private final static int FADE_DURATION = 1000; // in milliseconds
    int lastPosition = -1;


    public RecycleViewAdapter(ArrayList<Model> model,VideoListener listener) {
        this.model= model;
        videoListener=listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);

        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(RecycleViewAdapter.Holder holder, final int position) {
        Bitmap bitmap=model.get(position).getBitmap();
        holder.imageView.setImageBitmap(bitmap);
        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoListener.playVideo(model.get(position).getUrl());
            }
        });
        setScaleAnimation((View) holder.imageView.getParent().getParent(), position);


    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton playButton;
        Holder(View itemView) {
            super(itemView);
            imageView= (ImageView) itemView.findViewById(R.id.imageView);
            playButton = (ImageButton) itemView.findViewById(R.id.button);
        }
    }

    private void setScaleAnimation(View view, int position) {
        if (position > lastPosition) {
            ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(FADE_DURATION);
            view.startAnimation(anim);
            lastPosition = position;
        }

    }
}
