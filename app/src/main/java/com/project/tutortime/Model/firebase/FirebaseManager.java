package com.project.tutortime.Model.firebase;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.tutortime.MainActivity;
import com.project.tutortime.View.ChooseStatus;
import com.project.tutortime.View.LoadingScreen;
import com.project.tutortime.View.Login;

import java.util.ArrayList;

public class FirebaseManager {
    protected DatabaseReference myRef;
    static FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();;

    public FirebaseManager(){
        myRef= FirebaseDatabase.getInstance().getReference();
    }

    public boolean isLoggedIn() {
        return fAuth.getCurrentUser()!= null;
    }

    /* after confirmed as connected this method redirects to the appropriate page depending on the user status */
    final ArrayList<Integer> arr = new ArrayList<Integer>();
    public void getInside(String userID, Activity c) {
        mDatabase.child("users").child(userID).child("isTeacher").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getValue()!= null) {
                    int status = dataSnapshot.getValue(Integer.class);
                    if (status == -1) { /* if the status is not selected */
                        c.startActivity(new Intent(c, ChooseStatus.class));
                    } else { /* status entered - pass the status to Main Activity */
                        arr.add(dataSnapshot.getValue(Integer.class));
                        Intent intent = new Intent(c, MainActivity.class);
                        intent.putExtra("status", arr);
                        c.startActivity(intent);
                    }
                    ((Activity) c).finish();
                } else {
                    Toast.makeText(c, "Could not retrieve value from database.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        /* loading screen section (showing loading screen until data received from FireBase) */
        Intent intent = new Intent(c, LoadingScreen.class);
        /* prevent going back to this loading screen (from the next screen) */
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        c.startActivity(intent);
        /* END loading screen section */
        ((Activity) c).finish();
    }

}
