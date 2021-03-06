package com.project.tutortime.Controller.customerprofile;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.tutortime.MainActivity;
import com.project.tutortime.Model.firebase.userObj;
import com.project.tutortime.R;
import com.project.tutortime.databinding.FragmentCustomerProfileBinding;
import com.project.tutortime.Model.firebase.FireBaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomerProfile extends Fragment {

    private CustomerProfileViewModel CustomerProfileViewModel;
    private FragmentCustomerProfileBinding binding;

    EditText fname, lname;
    Spinner citySpinner, genderSpinner;
    Button saveProfile;

    String teacherID;
    FireBaseUser fbUser = new FireBaseUser();

    public static CustomerProfile newInstance() {
        return new CustomerProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        CustomerProfileViewModel = new ViewModelProvider(this).get(CustomerProfileViewModel.class);
        binding = FragmentCustomerProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fname = binding.myFName;
        lname = binding.myLName;
        citySpinner = binding.spinnerCity;
        genderSpinner = binding.spinnerGender;
        saveProfile = binding.btnSaveProfile;

        /* Select City Spinner Code () */
        ArrayAdapter<String> a = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        String[] select_gender = getResources().getStringArray(R.array.Gender);
        a.add(getResources().getString(R.string.CustomerProfileChooseGender));
        a.addAll(select_gender);
        genderSpinner.setAdapter(a);
        genderSpinner.setSelection(0); //display hint

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        String[] cities = getResources().getStringArray(R.array.Cities);
        adapter.add(getResources().getString(R.string.CustomerProfileChooseCity));
        adapter.addAll(cities);
        citySpinner.setAdapter(adapter);
        citySpinner.setSelection(0); //display hint
        /* END Select City Spinner Code () */

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = fname.getText().toString().trim();
                String lastName = lname.getText().toString().trim();
                String city = citySpinner.getSelectedItem().toString();
                String gender = genderSpinner.getSelectedItem().toString();
                if (TextUtils.isEmpty(firstName)) {
                    fname.setError(getResources().getString(R.string.CustomerProfileFirstNameRequired));
                    return; }
                if (TextUtils.isEmpty(lastName)) {
                    lname.setError(getResources().getString(R.string.CustomerProfileLastNameRequired));
                    return; }
                if (citySpinner.getSelectedItemPosition() == 0) {
                    TextView errorText = (TextView) citySpinner.getSelectedView();
                    errorText.setError(getResources().getString(R.string.CustomerProfileCityRequired));
                    Toast.makeText(getActivity(), getResources().getString(R.string.CustomerProfileCityRequired),
                            Toast.LENGTH_SHORT).show();
                    return; }
                if (gender.equals("Choose Gender")) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.CustomerProfileGenderRequired), Toast.LENGTH_SHORT).show();
                    return; }
                fbUser.updateUserDetails(firstName,lastName, city, gender);
                goToTutorMain(getActivity());
            }
        });
        return root;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fbUser.getUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userObj user = dataSnapshot.getValue(userObj.class);
                fname.setText(user.getfName());
                lname.setText(user.getlName());
                teacherID = user.getTeacherID();
                String currCity = user.getCity();
                String[] cities = getResources().getStringArray(R.array.Cities);
                for (int i = 0; i < cities.length; i++) {
                    /* adding +1 to citySpinner position since the first item is the Hint */
                    if (citySpinner.getItemAtPosition(i + 1).equals(currCity)) {
                        citySpinner.setSelection(i + 1);
                        break;
                    }
                }
                String currGender = user.getGender();
                String[] arrGender = getResources().getStringArray(R.array.Gender);
                for (int i = 0; i < arrGender.length; i++) {
                    /* adding +1 to genderSpinner position since the first item is the Hint */
                    if (genderSpinner.getItemAtPosition(i + 1).equals(currGender)) {
                        genderSpinner.setSelection(i + 1);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "onCreate error. " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* get fragment activity (to do actions on the activity) */
    private void goToTutorMain(Activity currentActivity) {
        //Activity currentActivity = getContext();
        /* were logging in as tutor (tutor status value = 1).
         * therefore, pass 'Status' value (1) to MainActivity. */
        final ArrayList<Integer> arr = new ArrayList<Integer>();
        arr.add(0);
        //Intent intent = new Intent(SetTutorProfile.this, MainActivity.class);
        Intent intent = new Intent(currentActivity, MainActivity.class);
        /* disable returning to SetTutorProfile class after opening main
         * activity, since we don't want the user to re-choose Profile
         * -> because the tutor profile data still exists with no use!
         * (unless we implementing method to remove the previous data) */
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("status",arr);
        /* finish last activities to prevent last MainActivity to run with Customer view */
        currentActivity.finishAffinity();
        currentActivity.startActivity(intent);
        currentActivity.finish();
    }

}