package com.project.tutortime.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.tutortime.MainActivity;
import com.project.tutortime.Model.firebase.FireBaseNotifications;
import com.project.tutortime.Model.firebase.FireBaseTutor;
import com.project.tutortime.Model.firebase.FirebaseManager;
import com.project.tutortime.Model.firebase.rankObj;
import com.project.tutortime.Model.firebase.subjectObj;
import com.project.tutortime.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;

public class SetTutorProfile extends AppCompatActivity {
    TextView citySpinner;
    EditText PhoneNumber, description;
    Button profile, addSub, addImage;
    ImageView img;
    ListView subjectList;
    ArrayList<subjectObj> list = new ArrayList<>();
    ArrayList<String> listSub = new ArrayList<>();
    Uri imageData;
    String imgURL;
    private static final int GALLERY_REQUEST_COD = 1;
    boolean del = false;
    ArrayList<String> listCities = new ArrayList<>();
    String teacherID;
    SubListAdapter sLstAd;
    private FirebaseManager fm = new FirebaseManager();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_tutor_profile);

        /* DISABLE landscape orientation  */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //delImage = findViewById(R.id.deleteImage);
        PhoneNumber = findViewById(R.id.editPhoneNumber);
        TextInputLayout descriptionInputLayout = findViewById(R.id.editDescription);
        description = descriptionInputLayout.getEditText();
        addSub = findViewById(R.id.addSubject);
        profile = findViewById(R.id.btnSaveProfile);
        addImage = findViewById(R.id.btnUpdateImage);
        img = findViewById(R.id.imageView);
        citySpinner = findViewById(R.id.txtCities);
        subjectList = (ListView) findViewById(R.id.subList);
        sLstAd = new SubListAdapter(this, R.layout.single_sub_row, list, teacherID);
        subjectList.setAdapter(sLstAd);
        sLstAd.notifyDataSetChanged();

        String[] cities = getResources().getStringArray(R.array.Cities);
        boolean[] selectCities = new boolean[cities.length];
        ArrayList<Integer> listCitiesNum = new ArrayList<>();
        setSpinnerCity(citySpinner, selectCities, listCitiesNum, cities);

        PhoneNumber.setHint("Enter Phone Number");
        PhoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) PhoneNumber.setHint("");
                else PhoneNumber.setHint("Enter Phone Number");
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imgURL != null && del) {
                    final Dialog d = new Dialog(SetTutorProfile.this);
                    Button editImage, deleteImage;
                    d.setContentView(R.layout.image_dialog);
                    //d.setTitle("Add Subject");
                    d.setCancelable(true);
                    editImage = d.findViewById(R.id.btnEditImage);
                    deleteImage = d.findViewById(R.id.DeleteImage);
                    editImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intent, GALLERY_REQUEST_COD);
                            del = true;
                            d.dismiss();
                        }
                    });

                    deleteImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            img.setImageDrawable(null);
                            img.setBackgroundResource(R.mipmap.ic_android_round);
                            del = false;
                            d.dismiss();
                        }
                    });
                    d.show();
                }
                else{
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, GALLERY_REQUEST_COD);
                    del = true;
                }
            }
        });

        subjectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                subjectObj s = (subjectObj) subjectList.getItemAtPosition(i);
                createEditDialog(s);
            }
        });

        addSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String userID = fm.getCurrentUserID();
                String pNum = PhoneNumber.getText().toString().trim();
                String descrip = description.getText().toString().trim();
                if (TextUtils.isEmpty(pNum)) {
                    PhoneNumber.setError("PhoneNumber is required.");
                    return;
                }
                if (pNum.charAt(0) != '0' || pNum.charAt(1) != '5' || pNum.length() != 10) {

                    PhoneNumber.setError("Invalid phoneNumber.");
                    return;
                }
                if (list.isEmpty()) {
                    Toast.makeText(SetTutorProfile.this, "You must choose at least one subject",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                int count = 0;
                boolean flage = false;
                for (subjectObj sub : list) {
                    count++;
                    if(sub.getType().equals("frontal")  ||  sub.getType().equals("both"))
                        flage = true;
                    if (listCities.isEmpty() && flage) {
                        Toast.makeText(SetTutorProfile.this, "You must choose at least one service city",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(listCities.size() != 0 && count == list.size() && flage == false){
                        Toast.makeText(SetTutorProfile.this, "You have chosen to transfer" +
                                        " private lessons only online, do not select service cities",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                /* add rank */
                rankObj rank = new rankObj(new HashMap<>(), 0);
                FireBaseTutor t = new FireBaseTutor();
                /* set isTeacher to teacher status (1=teacher,0=customer) */
                fm.setUserType(1);
                //mDatabase.child("users").child(userID).child("isTeacher").setValue(1);
                /* add the teacher to database */
                /* img=null because there is no need to store url before the image was successfully uploaded */
                String teacherID = t.addTeacherToDB(pNum, descrip, listCities, list, null, rank); // imgURL
                /* upload the image and ON SUCCESS store url on the teacher database */
                /* if no image to upload */
                if (imageData==null) {
                    goToTutorMain();
                } else {
                    uploadImageAndGoToMain(teacherID);
                }
            }
        });
    }

    /* Creating a dialogue for choosing cities where the teacher tutor */
    private void setSpinnerCity(TextView citySpinner, boolean[] selectCities,
                                ArrayList<Integer> listCitiesNum, String[] cities) {
        citySpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SetTutorProfile.this);
                builder.setTitle("Select service cities");
                builder.setCancelable(false);
                builder.setMultiChoiceItems(cities, selectCities, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            listCitiesNum.add(which);
                            listCities.add(cities[which]);
                            Collections.sort(listCitiesNum);
                        } else {
                            listCitiesNum.remove((Integer) which);
                            listCities.remove(cities[which]);
                        }
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(listCities.isEmpty()){
                            citySpinner.setTextColor(Color.GRAY);
                            citySpinner.setText("Select service cities");
                        } else {
                            citySpinner.setTextColor(Color.BLACK);
                            Collections.sort(listCities);
                            citySpinner.setText(printList(listCities));
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < selectCities.length; i++)
                            selectCities[i] = false;
//                        for(String rCity : listCities)
//                            removeTempCities.add(rCity);
                        listCitiesNum.clear();
                        listCities.clear();
                        citySpinner.setTextColor(Color.GRAY);
                        citySpinner.setText("Select service cities");
                        //removeServiceCities(removeTempCities);
                    }
                });
                builder.show();
            }
        });
    }

    private void goToTutorMain() {
        /* were logging in as tutor (tutor status value = 1).
         * therefore, pass 'Status' value (1) to MainActivity. */
        final ArrayList<Integer> arr = new ArrayList<Integer>();
        arr.add(1);
        //Intent intent = new Intent(SetTutorProfile.this, MainActivity.class);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        /* disable returning to SetTutorProfile class after opening main
         * activity, since we don't want the user to re-choose Profile
         * because the tutor profile data still exists with no use!
         * (unless we implementing method to remove the previous data) */
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("status",arr);
        /* finish last activities to prevent last MainActivity to run with Customer view */
        finishAffinity();
        startActivity(intent);
        finish();
    }

    public void createDialog() {
        final Dialog d = new Dialog(this);
        Spinner priceEdit;
        EditText expEdit;
        Button addBtn, closeBtn;
        Spinner nameSpinner, typeSpinner;
        d.setContentView(R.layout.subject_add_dialog);
        d.setTitle("Add Subject");
        d.setCancelable(true);
        priceEdit = d.findViewById(R.id.editPrice);
        expEdit = d.findViewById(R.id.editExp);
        addBtn = d.findViewById(R.id.btnAddS);
        closeBtn = d.findViewById(R.id.btnCloseS);
        nameSpinner = d.findViewById(R.id.spinnerSubName);
        priceEdit.setAdapter(new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, subjectObj.Prices.values()));
        nameSpinner.setAdapter(new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, subjectObj.SubName.values()));
        typeSpinner = d.findViewById(R.id.spinnerLType);
        typeSpinner.setAdapter(new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, subjectObj.Type.values()));
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String price = priceEdit.getText().toString().trim();
                String price = priceEdit.getSelectedItem().toString().trim();
                String exp = expEdit.getText().toString().trim();
                String nameSub = nameSpinner.getSelectedItem().toString().trim();
                String type = typeSpinner.getSelectedItem().toString().trim();
                if (price.equals("Select Price")) {
                    Toast.makeText(SetTutorProfile.this, "Please select a price.", Toast.LENGTH_SHORT).show();
                    return; }
                if (listSub.contains(nameSub)) {
                    Toast.makeText(SetTutorProfile.this, "You already have selected this subject.", Toast.LENGTH_SHORT).show();
                    return; }
                if (nameSub.equals("Select subject")) {
                    Toast.makeText(SetTutorProfile.this, "Subject is required.", Toast.LENGTH_SHORT).show();
                    return; }
                if (type.equals("Select learning type")) {
                    Toast.makeText(SetTutorProfile.this, "Learning type is required.", Toast.LENGTH_SHORT).show();
                    return; }
                subjectObj s = new subjectObj(nameSub, type, Integer.parseInt(price), exp);
                list.add(s);
                subjectList.setAdapter(sLstAd);
                sLstAd.notifyDataSetChanged();
                listSub.add(nameSub);
                d.dismiss();
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sLstAd = new SubListAdapter(SetTutorProfile.this, R.layout.single_sub_row, list, teacherID);
                subjectList.setAdapter(sLstAd);
                sLstAd.notifyDataSetChanged();
                d.dismiss();
            }
        });
        d.show();
    }

    public void createEditDialog(subjectObj currSub){
        final Dialog d = new Dialog(SetTutorProfile.this);
        Spinner priceEdit;
        EditText expEdit;
        Button saveBtn, deleteBtn, closeBtn;
        Spinner nameSpinner, typeSpinner;
        d.setContentView(R.layout.subject_edit_dialog);
        d.setTitle("Edit Subject");
        d.setCancelable(true);
        priceEdit = d.findViewById(R.id.PriceEdit);
        expEdit = d.findViewById(R.id.ExpEdit);
        saveBtn = d.findViewById(R.id.btnSave);
        closeBtn = d.findViewById(R.id.btnClose);
        deleteBtn = d.findViewById(R.id.btnDelete);
        nameSpinner = d.findViewById(R.id.spinSubName);
        priceEdit.setAdapter(new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, subjectObj.Prices.values()));
        ArrayAdapter nameAd = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                subjectObj.SubName.values());
        nameSpinner.setAdapter(nameAd);
        nameSpinner.setSelection(subjectObj.SubName.valueOf(currSub.getsName().
                replaceAll("\\s+","")).ordinal());
        ArrayAdapter typeAd = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                subjectObj.Type.values());
        typeSpinner = d.findViewById(R.id.spinType);
        typeSpinner.setAdapter(typeAd);
        typeSpinner.setSelection(subjectObj.Type.valueOf(currSub.getType().
                replaceAll("/","")).ordinal());
        //priceEdit.setText(Integer.toString((currSub.getPrice())));
        priceEdit.setSelection(currSub.getPricesEnumPosition(currSub.getPrice()));
        expEdit.setText((currSub.getExperience()));
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String price = priceEdit.getText().toString().trim();
                String price = priceEdit.getSelectedItem().toString().trim();
                String exp = expEdit.getText().toString().trim();
                String nameSub = nameSpinner.getSelectedItem().toString().trim();
                String type = typeSpinner.getSelectedItem().toString().trim();
                if (price.equals("Select Price")) {
                    //priceEdit.setError("Price is required.");
                    Toast.makeText(SetTutorProfile.this, "Please select a price.", Toast.LENGTH_SHORT).show();
                    return; }
                if (nameSub.isEmpty() || nameSub.equals(subjectObj.SubName.HINT)) {
                    Toast.makeText(SetTutorProfile.this, "Subject is required.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (type.isEmpty() || type.equals(subjectObj.Type.HINT)) {
                    Toast.makeText(SetTutorProfile.this, "Learning type is required.", Toast.LENGTH_SHORT).show();
                    return;
                }
                /* if the subject already exists BUT IN THE ENTRY THAT CURRENTLY EDITING - IT'S OK!  */
                if (listSub.contains(nameSub) && !nameSub.equals(currSub.getsName())) {
                    Toast.makeText(SetTutorProfile.this, "You already have selected this subject.", Toast.LENGTH_SHORT).show();
                    return; }
                subjectObj s = new subjectObj(nameSub, type, Integer.parseInt(price), exp);
                /* remove the last entry (before the edit) */
                list.remove(currSub);
                /*  */
                list.add(s);
                listSub.add(nameSub);
                sLstAd = new SubListAdapter(SetTutorProfile.this, R.layout.single_sub_row, list, teacherID);
                subjectList.setAdapter(sLstAd);
                sLstAd.notifyDataSetChanged();
                //sent notification to user
                //String userID= fAuth.getCurrentUser().getUid();
//                myRef.child("users").child(userID).child("fName").addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        String userName = snapshot.getValue(String.class);
//                        FireBaseNotifications.sendNotification(userID,"TutorProfile",userName);
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) { }
//                });
                d.dismiss();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.remove(currSub);
                sLstAd = new SubListAdapter(SetTutorProfile.this, R.layout.single_sub_row, list, teacherID);
                subjectList.setAdapter(sLstAd);
                sLstAd.notifyDataSetChanged();
                d.dismiss();
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sLstAd = new SubListAdapter(SetTutorProfile.this, R.layout.single_sub_row, list, teacherID);
                subjectList.setAdapter(sLstAd);
                sLstAd.notifyDataSetChanged();
                d.dismiss();
            }
        });
        d.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem toggleservice = menu.findItem(R.id.lang_switch);
        final ToggleSwitch langSwitch = toggleservice.getActionView().findViewById(R.id.lan);
        // TODO: https://stackoverflow.com/questions/32813934/save-language-chosen-by-user-android
        langSwitch.setOnToggleSwitchChangeListener(new ToggleSwitch.OnToggleSwitchChangeListener(){
            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                if(position==0) { // English
                    setLocale("en");
                    recreate();
                }
                if(position==1) { // Hebrew
                    setLocale("iw");
                    recreate();
                }
            }
        });

        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_COD && resultCode == Activity.RESULT_OK && data != null) {
            try {
                imageData = data.getData();
                /* crop the image bmp to square */
                InputStream imageStream = getContentResolver().openInputStream(imageData);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImage = getResizedBitmap(selectedImage, 200);// 400 is for example, replace with desired size
                /* show the new image on screen */
                img.setImageBitmap(selectedImage);

                /* convert the new bmp to Uri & assign the new Uri to 'imageData' */
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), selectedImage, "IMG_" + Calendar.getInstance().getTime(), null);
                if (path!=null) imageData = Uri.parse(path);
                else {
                    imageData = null;
                    Toast.makeText(getApplicationContext(), "Upload image Failed", Toast.LENGTH_LONG).show();
                }
                /* Note that a new image has been created in the gallery
                 * but the image will be deleted after uploading it to the server.
                 * (In the 'fileUploader' method below) */
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int maxSize) {
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
        Bitmap resizedImg = Bitmap.createScaledBitmap(bitmap, maxSize, maxSize, false);
        return resizedImg;
    }

    private String getExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadImageAndGoToMain(String teacherID) {
        imgURL = System.currentTimeMillis()+"."+getExtension(imageData);
        StorageReference Ref= FirebaseStorage.getInstance().getReference().child(imgURL);
        Ref.putFile(imageData)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override // onProgress show loading screen
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        /* loading screen section (showing loading screen until data received from FireBase) */
                        Intent intent = new Intent(SetTutorProfile.this, LoadingScreen.class);
                        /* prevent going back to this loading screen (from the next screen) */
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        finishAffinity();
                        startActivity(intent);
                        /* END loading screen section */
                        /* END loading screen section */
                        finish();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override // on success set image URL on tutor database & go to main
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        /* Set the image URL AFTER After the image has been successfully uploaded */
                        //mDatabase.child("teachers").child(teacherID).child("imgUrl").setValue(imgURL);
                        fm.setImageURL(teacherID,imgURL);
                        /* remove the cropped image from gallery */
                        if (imageData!=null) getApplicationContext().getContentResolver().delete(imageData, null, null);
                        goToTutorMain();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), "Upload imageFailed", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void sendNotification(String userID, String userName) {
        HashMap<String, Object> map = new HashMap<>();
        String notificationID = FirebaseDatabase.getInstance().getReference().child("notifications").child(userID).push().getKey();
        map.put("notificationID",notificationID);
        map.put("title","TutorProfile");
        map.put("sentFrom",userName);
        map.put("read",0);
        if (notificationID != null)
            FirebaseDatabase.getInstance().getReference().child("notifications").child(userID).child(notificationID).setValue(map);
    }

    // Print the List of cities that the teacher tutor
    private String printList (ArrayList < String > list) {
        String s =list.toString();
        s = s.replace("[","");
        s = s.replace("]","");
        return s;
    }
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        /* save data to shared  preferences */
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }
    public class SubListAdapter extends BaseAdapter {
        Context context;
        Fragment fragment;
        int layoutResourceId;
        ArrayList<subjectObj> subs;
        ArrayList<Pair<String, String>> filteredData;
        String teacherID;
//    private ItemFilter mFilter = new ItemFilter();


        public SubListAdapter(Context context, int layoutResourceId, ArrayList<subjectObj> subs,
                              String teacherID) {
            super();
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.subs = subs;
            this.teacherID = teacherID;

        }

        @Override
        public int getCount() {
            return subs.size();
        }

        @Override
        public subjectObj getItem(int position) {
            int c = 0;
            for (subjectObj sub : subs) {
                if (c == position) return sub;
                c++;
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            SetTutorProfile.AppInfoHolder holder = null;

            if (row == null) {

                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new SetTutorProfile.AppInfoHolder();

                holder.nameText = (TextView) row.findViewById(R.id.singleSubName);
                holder.typeText = (TextView) row.findViewById(R.id.singleSubType);
                holder.priceText = (TextView) row.findViewById(R.id.singleSubPrice);
                holder.expText = (TextView) row.findViewById(R.id.singleSubEx);
                row.setTag(holder);

            } else {
                holder = (SetTutorProfile.AppInfoHolder) row.getTag();
            }
            SetTutorProfile.AppInfoHolder finalHolder = holder;
            subjectObj s = getItem(position);
            finalHolder.nameText.setText(s.getsName());
            finalHolder.typeText.setText("Type: "+s.getType());
            finalHolder.priceText.setText("Price: " +String.valueOf(s.getPrice()));
            finalHolder.expText.setText("Experience: " +s.getExperience());
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createEditDialog(s);
                }
            });
            return row;
        }
    }
    static class AppInfoHolder {
        TextView nameText, typeText, priceText, expText;
    }
}