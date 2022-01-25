package com.example.profileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity{

    TextView followers, collections, scoreboard;

    ImageView imageProfile;
    ProgressDialog progressDialog;

    private static final int CAMERA_REQUEST = 100;
    private static final int IMAGE_PICK_CAMERA_REQUEST = 400;

    String cameraPermission[];
    Uri imageUri;
    String profileOrCoverImage;
    MaterialButton editImage;
    MaterialButton btnEditInfo;
    EditText editName;
    EditText editAge;
    EditText editLocation;
    EditText editUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        followers = findViewById(R.id.followers_text_view);
        followers.setTextAppearance(this, R.style.Widget_Support_CoordinatorLayout);

        collections = findViewById(R.id.collections_text_view);
        scoreboard = findViewById(R.id.scoreboard_text_view);

        String followers_name = getResources().getString(R.string.followers_editText);
        String quantity_followers = getResources().getString(R.string.followers_quantity);

        String collections_name = getResources().getString(R.string.collections_editText);
        String quantity_collections = getResources().getString(R.string.collections_quantity);

        String scoreboard_name = getResources().getString(R.string.scoreboard_editText);
        String quantity_scoreboard = getResources().getString(R.string.scoreboard_quantity);

        followers.setText(prefs.getString(quantity_followers, quantity_followers)
                + prefs.getString(followers_name, "\n\n"+followers_name));

        collections.setText(prefs.getString(quantity_collections, quantity_collections)
                + prefs.getString(collections_name, "\n\n"+collections_name));

        scoreboard.setText(prefs.getString(quantity_scoreboard, quantity_scoreboard)
                + prefs.getString(scoreboard_name, "\n\n"+scoreboard_name));

        editImage = findViewById(R.id.btn_edit_image);
        imageProfile = findViewById(R.id.profile_image);

        btnEditInfo = findViewById(R.id.btn_save);
        editName = findViewById(R.id.full_name_edit_text);
        editAge = findViewById(R.id.age_edit_text);
        editLocation = findViewById(R.id.location_edit_text);
        editUsername = findViewById(R.id.username_edit_text);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        editImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                progressDialog.setMessage("Updating Profile Picture");
                profileOrCoverImage = "Image";
                showImagePicDialog();
                imageProfile.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                imageProfile.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;

                imageProfile.setBackgroundColor(getResources().getColor(R.color.white));

                imageProfile.requestLayout();
            }
        });

        btnEditInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(btnEditInfo.getText() == getResources().getString(R.string.button_edit)){

                    btnEditInfo.setText(R.string.button_save);
                    editName.setEnabled(true);
                    editAge.setEnabled(true);
                    editLocation.setEnabled(true);
                    editUsername.setEnabled(true);

                }else{
                    btnEditInfo.setText(R.string.button_edit);
                    btnEditInfo.requestFocus();
                    editName.setEnabled(false);
                    editAge.setEnabled(false);
                    editLocation.setEnabled(false);
                    editUsername.setEnabled(false);

                    TextView user_tool = findViewById(R.id.username_menu);
                    TextView username_text_view = findViewById(R.id.username_text_view);

                    user_tool.setText("@"+editUsername.getText());
                    username_text_view.setText(editName.getText());

                    Toast.makeText(getApplicationContext(), "Info Saved", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onPause(){

        super.onPause();

        Glide.with(this).load(imageUri).into(imageProfile);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult){

        super.onRequestPermissionsResult(requestCode, permissions, grantResult);

        switch (requestCode){

            case CAMERA_REQUEST: {

                if(grantResult.length > 0){

                    boolean cameraAccepted = grantResult[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResult[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && writeStorageAccepted){

                        pickFromCamera();
                    }else {
                        Toast.makeText(this, "Please enable camera and storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(this, "Something went wrong! try again", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    private void showImagePicDialog(){

        String options[] = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0){

                    if(!checkCameraPermission()){

                        requestCameraPermission();

                    }else{
                        pickFromCamera();
                    }
                }
            }
        });
        builder.create().show();
    }

    private Boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission(){

        requestPermissions(cameraPermission, CAMERA_REQUEST);
    }

    private void pickFromCamera(){

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_description");

        imageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST);
    }

}