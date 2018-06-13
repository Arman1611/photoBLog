package com.example.armangupta.photoblog;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Arman Gupta on 12-06-2018.
 */

public class RecyclerPostAdapter extends RecyclerView.Adapter<RecyclerPostAdapter.ViewHolder> {
    List<UserData> list;
    Context context;
    FirebaseFirestore firebaseFirestore;
    public RecyclerPostAdapter(List<UserData>list) {
        this.list=list;
        firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View view=(View) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_post,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
      holder.txtDescWhilePosting.setText(list.get(position).getDescription());

      final String userId=list.get(position).getUserId();

      final String imageUrl=list.get(position).getImageUrl();
      final String imagethumb=list.get(position).getImagethumb();
        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String name=task.getResult().getString("name");
                    String photoUri=task.getResult().getString("PhotoURI");
                    holder.setUserNameAndUserImage(name,photoUri);
                }
                else {
                    Log.d("firebase error", "onComplete: "+task.getException().getMessage());
                }
            }
        });

        holder.setPostImage(imageUrl,imagethumb);

        try {
            long millisecond = list.get(position).getTimestamp().getTime();
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            String date = sfd.format(new Date(millisecond)).toString();
            holder.setPostDate(date);
        }
        catch (Exception e){}
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView txtDescWhilePosting;
        TextView txtUsernameWhilePosting;
        CircleImageView imgUserWhilePosting;
        ImageView imgPostWhilePosting;
        TextView txtDateWhilePosting;
        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            txtDescWhilePosting = view.findViewById(R.id.txtDescWhilePost);
            txtUsernameWhilePosting=view.findViewById(R.id.txtUsernameWhilePost);
            imgUserWhilePosting=view.findViewById(R.id.imgUserWhilePost);
            imgPostWhilePosting=view.findViewById(R.id.imgPostWhilePost);
            txtDateWhilePosting=view.findViewById(R.id.txtDateWhilePost);

        }
        public void setUserNameAndUserImage(String name,String photoUri){

            txtUsernameWhilePosting.setText(name);
            Glide.with(context).load(photoUri).into(imgUserWhilePosting);

        }
        public void setPostImage(String imageUrl,String imageThumb)
        {
            Glide.with(context).applyDefaultRequestOptions(RequestOptions.placeholderOf(R.drawable.default_posting_image)).load(imageUrl)
                    .thumbnail(Glide.with(context).load(imageThumb)).into(imgPostWhilePosting);
        }
        public void setPostDate(String date)
        {
            txtDateWhilePosting.setText(date);
        }

    }
}
