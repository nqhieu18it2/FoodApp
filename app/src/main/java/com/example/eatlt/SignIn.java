package com.example.eatlt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.example.eatlt.Commom.Commom;
import com.example.eatlt.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    EditText edtPhone, edtPassword;
    Button btnSignIn;
    AppCompatCheckBox ckbRemember;
    TextView txtForgotPwd;
     FirebaseDatabase database;
     DatabaseReference table_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        edtPhone = (MaterialEditText) findViewById(R.id.edtPhone);
        edtPassword = (MaterialEditText) findViewById(R.id.edtPassword);
        btnSignIn = (Button) findViewById(R.id.buttonSignIn);
        ckbRemember =(AppCompatCheckBox) findViewById(R.id.ckbRemember);
        txtForgotPwd = (TextView) findViewById(R.id.txtForgotPwd);

        //Init Paper
        Paper.init(this);

        //Init Firebase
         database = FirebaseDatabase.getInstance() ;
         table_user = database.getReference("User");

        txtForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPwdDialog();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Commom.isConnectedToInternet(getBaseContext())) {
                    // save user and password
                    if (ckbRemember.isChecked()) {
                        Paper.book().write(Commom.USER_KEY, edtPhone.getText().toString());
                        Paper.book().write(Commom.PWD_KEY, edtPassword.getText().toString());
                    }


                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                    mDialog.setMessage("Please Wait....");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Check if user not exist in database
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                //get user information
                                mDialog.dismiss();

                                User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                user.setPhone(edtPhone.getText().toString()); //set the phone of user
                                if (user.getPassword().equals(edtPassword.getText().toString())) {

                                    Intent homeIntent = new Intent(SignIn.this, HomeActivity.class);
                                    Commom.currentUser = user;
                                    startActivity(homeIntent);
                                    finish();

                                } else {
                                    Toast.makeText(SignIn.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "User not exists", Toast.LENGTH_SHORT).show();

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(SignIn.this, "Please Check Internet Connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }

    private void showForgotPwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");
        builder.setMessage("Enter you secure code");

        LayoutInflater inflater = this.getLayoutInflater();
        View forgot_view = inflater.inflate(R.layout.forgot_password_layout,null);

        builder.setView(forgot_view);
        builder.setIcon(R.drawable.ic_baseline_security_24);

        MaterialEditText edtPhone = (MaterialEditText) forgot_view.findViewById(R.id.edtPhone);
        MaterialEditText edtSecureCode = (MaterialEditText) forgot_view.findViewById(R.id.edtSecureCode);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //check if user available
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);

                         if(user.getSecureCode().equals(edtSecureCode.getText().toString()))
                            Toast.makeText(SignIn.this, "Your password : "+user.getPassword(), Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(SignIn.this, "Wrong secure code !", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }
}