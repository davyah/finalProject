package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.example.finalproject.customfonts.EditText_Roboto_Regular;
import com.example.finalproject.model.User;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreateProfileActivity extends AppCompatActivity {
    @BindView(R.id.pick_image)
    CircleImageView profileImage;

    @BindView(R.id.username)
    EditText_Roboto_Regular userNameEditText;

    @BindView(R.id.name)
    EditText_Roboto_Regular nameEditText;

    FirebaseStorage storage;
    FirebaseDatabase database;
    DatabaseReference userRef;
    StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private String imageUrl;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        storage = FirebaseStorage.getInstance();
        storageReference =storage.getReference();
        database = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef =database.getReference().child("users").child(firebaseUser.getUid());
        ButterKnife.bind(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            Uri fileUri = data.getData();
            profileImage.setImageURI(fileUri);

            //You can get File object from intent
           File file = ImagePicker.Companion.getFile(data);
           uploadImage(file.getPath(),file);

            //You can also get File Path from intent
             String imageUri = ImagePicker.Companion.getFilePath(data);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.Companion.getError(data), Toast.LENGTH_SHORT).show();
        }
    }
    @OnClick(R.id.save_btn) void save(){
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if(user!=null) {
                    user.setName(nameEditText.getText().toString().trim());
                    user.setUsername(userNameEditText.getText().toString().trim());
                    if (imageUrl != null) {
                        user.setImage_url(imageUrl);
                    }
                    userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(CreateProfileActivity.this,MainActivity.class));
                        }
                    });;
                }else {
                 user = new User();
                    user.setEmail(firebaseUser.getEmail());
                    user.setName(nameEditText.getText().toString().trim());
                    user.setUsername(userNameEditText.getText().toString().trim());
                    if (imageUrl != null) {
                        user.setImage_url(imageUrl);
                    }
                    userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(CreateProfileActivity.this,MainActivity.class));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @OnClick(R.id.pick_image) void pickImage(){
        ImagePicker.Companion.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }
    // UploadImage method
    private void uploadImage(String filePath,File file)
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(Uri.fromFile(file))
                    .addOnSuccessListener(
                            taskSnapshot -> {
                                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(uri -> imageUrl = uri.toString());
                                // Image uploaded successfully
                                // Dismiss dialog
                                progressDialog.dismiss();
                                Toast
                                        .makeText(CreateProfileActivity.this,
                                                "Image Uploaded!!",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            })

                    .addOnFailureListener(e -> {

                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Toast
                                .makeText(CreateProfileActivity.this,
                                        "Failed " + e.getMessage(),
                                        Toast.LENGTH_SHORT)
                                .show();
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int)progress + "%");
                                }
                            });
        }
    }
}

