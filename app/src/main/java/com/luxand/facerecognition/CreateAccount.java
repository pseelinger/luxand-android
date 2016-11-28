package com.luxand.facerecognition;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CreateAccount extends AppCompatActivity implements View.OnClickListener{


    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignin;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth =FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            //user is already logged in, profile activity here
            finish();
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }

        progressDialog = new ProgressDialog(this);


        //initializing views
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textViewSignin = (TextView) findViewById(R.id.textViewSignin);


        //adding listening to button
        buttonRegister.setOnClickListener(this);
        textViewSignin.setOnClickListener(this);

    }


    private void registerUser(){
        //getting email and password from edit texts
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();


        //checking if email and passwords are empty
        if (TextUtils.isEmpty(email)){
            //email is empty
            Toast.makeText(this, "Please enter your email",Toast.LENGTH_SHORT).show();
            //will stop function from executing further
            return;
        }

        if (TextUtils.isEmpty(password)){
            //password is empty
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            //stops function from executing further
            return;
        }

        //if validations are ok
        //we will first show a progress bar
        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                                //user is already logged in, profile activity here
                                finish();
                                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

                            //user is successfully registered and logged in
                            //we will start the profile activity here
                            //right now lets display a toast only
                           // Toast.makeText(CreateAccount.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CreateAccount.this, "Could not register. Please try again.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }


    @Override
    public void onClick(View view) {
        if (view == buttonRegister){
            registerUser();
        }

        if (view==textViewSignin){
            //will open login activity here
            startActivity(new Intent(this, login_activity.class));
        }
    }

}
