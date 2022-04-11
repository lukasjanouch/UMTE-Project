package com.example.poha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.poha.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterUserActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewRegistration;
    private EditText editTextUserName, editTextEmail, editTextPassword;
    private Button btnRegister;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        btnRegister = findViewById(R.id.registerButton);
        btnRegister.setOnClickListener(this);

        editTextUserName = findViewById(R.id.editTextTextUserName);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress2);
        editTextPassword = findViewById(R.id.editTextTextPassword2);

        progressBar = findViewById(R.id.progressBar2);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.registerButton){
            registerUser();
        }
        /* switch (view.getId()) {
            case R.id.registerButton:
                registerUser();
                break;
        }*/
    }

    private void registerUser() {
        String userName = editTextUserName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (userName.isEmpty()) {
            editTextUserName.requestFocus();
            editTextUserName.setError("Uživatelské jméno musí být vyplněno.");
            return;
        }
        if (email.isEmpty()) {
            editTextEmail.setError("Email musí být vyplněn.");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Uveďte prosím email ve správném tvaru.");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Heslo musí být vyplněno.");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6){
            editTextPassword.setError("Heslo musí mít nejméně 6 znaků.");
            editTextPassword.requestFocus();
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    User user = new User(userName, email);

                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(RegisterUserActivity.this, "Jste úspěšně zaregistrován/a", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                //přesměrovat na přihlášení
                                startActivity(new Intent(RegisterUserActivity.this, MainActivity.class));
                            }else{
                                Toast.makeText(RegisterUserActivity.this, "Registrace selhala, zkuste to znovu", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });

                }else{
                    Toast.makeText(RegisterUserActivity.this, "Registrace selhala, zkuste to znovu", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
