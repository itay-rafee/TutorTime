package com.project.tutortime.Controller.chats;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.tutortime.Model.firebase.FireBaseChats;
import com.project.tutortime.View.MessageActivity;
import com.project.tutortime.R;


import java.util.List;

/**
 * This class adapter the chat with  the view
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {
    private final Context mContext;
    private final List<Chat> mChats;//list of all the active chats

    public ChatsAdapter(Context mContext, List<Chat> mChats) {
        this.mContext = mContext;
        this.mChats = mChats;
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //set the view to the chats item
        View view = LayoutInflater.from(mContext).inflate(R.layout.chats_item,parent,false);
        return new ChatsAdapter.ChatsViewHolder(view);
    }

    /**
     * This method bind between the chat data and the view
     * here we set the chat data and the buttons
     */
    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, @SuppressLint("RecyclerView") int position) {
        /* get the current chat */
        Chat chat = mChats.get(position);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* set the chat to the other user data */
        if(userID.equals(chat.getStudentID())){
            /* if this user is the student then the name on the chat should be the teacher name */
            holder.UserName.setText(chat.getTeacherName());
            /* if the teacher already set his profile image then load it */
            if(chat.getImageUrl()!=null) {
                loadUserImage(chat.getImageUrl(),holder.profilePicture); //load the image
            }
            else{ /* the teacher didn't set a profile picture so set the profile image to default */
                holder.profilePicture.setImageResource(R.drawable.profile);
            }
        }
        else{ /* this is the teacher */
            holder.UserName.setText(chat.getStudentName());
            holder.profilePicture.setImageResource(R.drawable.profile);
        }
        /* set the last message to the last message that sent in this chat */
        holder.lastMessage.setText(chat.getLastMessage());
        /* set the last seen time in the chat text view */
        FireBaseChats.setLastSeen(chat.getStudentID(), chat.getTeacherID(), mContext, holder.lastSeen);
        /* allow the user to remove the chat */
        holder.deleteChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {/* delete the chat from the view and database */
                deleteChatDialog(chat,holder.getAdapterPosition(),FirebaseAuth.getInstance().getCurrentUser().getUid());
            }
        });
        /* when click on profile picture show the user the teacher card if he is teacher */
        holder.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //itay add here the teacherCard fragment - only if the user is teacher !



            }
        }); /* open the chat when click on the chat linear layout */
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* mark the chat as read */
                FireBaseChats.markChatAsRead(chat.getChatID(),FirebaseAuth.getInstance().getCurrentUser().getUid());
                //start the activity and pass the chat data to the message activity
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("studentName",chat.getStudentName());
                intent.putExtra("student",chat.getStudentID().toString());
                intent.putExtra("teacher",chat.getTeacherID().toString());
                intent.putExtra("chat",chat.getChatID().toString());
                mContext.startActivity(intent);
            }
        });
    }


    /**
     * return the chats amount
     * @return chat amounts
     */
    @Override
    public int getItemCount() {
        return mChats.size();
    }

    /**
     * This class hold the chats view objects
     */
    public class ChatsViewHolder extends RecyclerView.ViewHolder {

        public TextView UserName;
        public TextView lastMessage;
        public TextView lastSeen;
        public ImageView profilePicture;
        public ImageView deleteChat;
        public LinearLayout chat;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            UserName = (TextView)itemView.findViewById(R.id.UserNameChat);
            lastMessage = (TextView)itemView.findViewById(R.id.last_message);
            lastSeen = (TextView)itemView.findViewById(R.id.last_seen);
            chat = (LinearLayout) itemView.findViewById(R.id.chat_linear_layout);
            profilePicture = (ImageView)itemView.findViewById(R.id.profile_image_chat);
            deleteChat = (ImageView) itemView.findViewById(R.id.delete_chat);

        }
    }

    /**
     * This method allows to remove the chat from the list view and from the database
     * @param chat the chat that the user want to remove
     * @param position the current position of the chat in the view
     * @param userID the id of the user
     */

    private void removeChat(Chat chat, int position,String userID) {
        //remove from list view
        mChats.remove(chat);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,mChats.size());
        //remove from data base
        FireBaseChats.removeChat(userID,chat.getChatID());
    }

    /**
     * This method creating a dialog with the user and when click yes the chat will removed
     * otherwise nothing will happened
     */
    private void deleteChatDialog(Chat chat, int position,String userID){
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(mContext.getResources().getString(R.string.deleteChatDialog))
                .setIcon(R.drawable.bell)
                .setMessage(mContext.getResources().getString(R.string.areYouSure))
                .setPositiveButton(mContext.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).setNegativeButton(mContext.getResources().getString(R.string.yes),new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                removeChat(chat,position,userID);
            }
        }).show();
    }


    /**
     * This method load the user image from given url and set the imageview
     * @param url image url
     * @param image the imageview to load
     */
    private void loadUserImage(String url,ImageView image){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(url);
        Glide.with(mContext)
                .asBitmap()
                .load(storageReference)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        image.setImageBitmap(resource);

                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { }
                });
        }

}
