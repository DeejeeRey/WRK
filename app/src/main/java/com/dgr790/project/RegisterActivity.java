package com.dgr790.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dgr790.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    private CircleImageView userPhotoIV;
    private Uri pickedImgUri;

    static int PReqCode = 1;
    static int REQUESTCODE = 1;

    private EditText firstnameET, secondnameET, usernameET, emailET, passET, passConfET;
    private Button registerBtn;

    private String username, firstname, secondname, score, times, email, pass , passConf;

    private String[] userInformation;


    FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstnameET = findViewById(R.id.firstnameET);
        secondnameET = findViewById(R.id.secondnameET);
        usernameET = findViewById(R.id.usernameET);
        emailET = findViewById(R.id.emailET);
        passET = findViewById(R.id.passET);
        passConfET = findViewById(R.id.passConfET);
        registerBtn = findViewById(R.id.registerBtn);

        userInformation = new String[7];

        mAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firstname = firstnameET.getText().toString();
                secondname = secondnameET.getText().toString();
                username = usernameET.getText().toString();
                email = emailET.getText().toString();
                pass = passET.getText().toString();
                passConf = passConfET.getText().toString();

                if (firstname.isEmpty() || secondname.isEmpty() || username.isEmpty() || email.isEmpty() || pass.isEmpty() || passConf.isEmpty() || !pass.equals(passConf)) {
                    // Not all fields are filled or passwords do not match

                    showMessage("Please verify all fields");
                } else {
                    // All details are correct
                    addUserInformation();
                    createUserAccount(email, pass);
                }

            }
        });

        userPhotoIV = findViewById(R.id.userPhotoIV);

        userPhotoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestPermission();
                } else {
                    openGallery();
                }
            }
        });

    }

    private void addUserInformation() {
        score = Integer.toString(0);
        times = Integer.toString(0);

        userInformation[0] = firstname;
        userInformation[1] = secondname;
        userInformation[2] = username;
        userInformation[3] = email;
        userInformation[4] = score;
        userInformation[5] = times;
        userInformation[6] = "true";
    }

    private void createUserAccount(String email, String pass) {

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User account created successfully
                            showMessage("Account created successfully");
                            updateUserInfo();
                        } else {
                            // Account creation failed
                            showMessage("Account creation failed" + task.getException().getMessage());
                        }
                    }
                });

    }

    // Update user photo and name
    private void updateUserInfo() {

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");

        mStorage.child(pickedImgUri.getLastPathSegment()).putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Image uploaded successfully
                FirebaseStorage.getInstance().getReference().child("users_photos").child(pickedImgUri.getLastPathSegment()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Uri contains user image URL
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .setPhotoUri(uri)
                                .build();

                        mAuth.getCurrentUser().updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // User information updated successfully
                                    showMessage("Register complete");
//                                    addInfo();
//                                    setPhoto();
                                    updateUI();
                                }
                            }
                        });

                    }
                });
            }
        });

    }


    private void updateUI() {

        Intent homeActivity = new Intent(getApplicationContext(), MainActivity.class);
        homeActivity.putExtra("userInformation", userInformation);
        startActivity(homeActivity);
        finish();

    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESTCODE);

    }

    private void showMessage(String m) {
        Toast.makeText(RegisterActivity.this, m, Toast.LENGTH_SHORT).show();
    }

    private void checkAndRequestPermission() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessage("Please accept required permission");
            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        } else {
            openGallery();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null) {
            // User has successfully picked an image

            pickedImgUri = data.getData();
            userPhotoIV.setImageURI(pickedImgUri);
        }
    }
}
