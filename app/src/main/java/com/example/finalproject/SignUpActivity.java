package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.finalproject.customfonts.EditText_Roboto_Regular;
import com.example.finalproject.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener listener;
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private DatabaseReference userRef;
    private String email,password,retype;
    @BindView(R.id.pass)
    EditText_Roboto_Regular passwordEditText;
    @BindView(R.id.retype_pass)
    EditText_Roboto_Regular retypePassword;
    @BindView(R.id.username)
    EditText_Roboto_Regular userEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();



    }
    @OnClick(R.id.btn_sign_up)void signUp(){
        if(isValid()){
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(task -> {
                        User user = new User();
                        user.setEmail(mAuth.getCurrentUser().getEmail());
                        myRef.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(task2 -> startActivity(new Intent(SignUpActivity.this,CreateProfileActivity.class)));
            }).addOnFailureListener(e -> {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private boolean isValid() {
        password = passwordEditText.getText().toString();
        email = userEditText.getText().toString();
        retype = retypePassword.getText().toString();
        Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
        Matcher m = p.matcher(email);
        if(!m.matches())
        {
            userEditText.setError("this is not an email adress!");
            return false;
        }
        else if(password.length()<=6)
        {
            passwordEditText.setError(getString(R.string.too_short));
            return false;
        }
        else if(!password.equals(retype))
        {
            passwordEditText.setError(getString(R.string.no_match));
            return false;
        }
        else
        {
            return true;
        }
    }
}
