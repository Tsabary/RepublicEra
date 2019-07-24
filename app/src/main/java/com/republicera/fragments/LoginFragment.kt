package com.republicera.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.RegisterLoginActivity
import com.republicera.interfaces.GeneralMethods
import kotlinx.android.synthetic.main.fragment_register_login_screens.*

class LoginFragment : Fragment(), GeneralMethods {

    private lateinit var userEmail: EditText
    private lateinit var userPassword : EditText
    private lateinit var button : TextView
    private lateinit var loadingAnimation : ConstraintLayout

    private lateinit var firebaseAuth: FirebaseAuth

    private val RC_SIGN_IN: Int = 1
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_register_login_screens, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as RegisterLoginActivity

//        firebaseAuth = FirebaseAuth.getInstance()
//        val googleLogin = register_fragment_google_login
//
//        register_fragment_last_name.visibility = View.GONE
//        register_fragment_button.text = getString(R.string.login)
//
//        val forgotPassword = register_fragment_forgot_password
//
//        userEmail = register_fragment_email
//        userPassword = register_fragment_password
//        button = register_fragment_button
//
//        loadingAnimation = register_fragment_spinner

        button.setOnClickListener {
            performLogin()
        }
//
//        googleLogin.setOnClickListener {
//            configureGoogleSignIn()
//        }
//
//        forgotPassword.setOnClickListener {
//            val userEmailInput = userEmail.text.toString().trimEnd().replace("\\s".toRegex(), "")
//            if (userEmailInput.contains("@") && userEmailInput.contains(".")) {
//                FirebaseAuth.getInstance().sendPasswordResetEmail(userEmailInput)
//                    .addOnSuccessListener {
//                        Toast.makeText(this.context, "A reset link has been sent to your email", Toast.LENGTH_SHORT).show()
//                    }
//            } else {
//                Toast.makeText(this.context, "Please enter your email", Toast.LENGTH_SHORT).show()
//            }
//        }

    }


    private fun performLogin() {
        loadingAnimation.visibility = View.VISIBLE
        button.isClickable = false
        closeKeyboard(activity as RegisterLoginActivity)

        val logEmail = userEmail.text.toString().trimEnd()
        val logPass = userPassword.text.toString().replace("\\s".toRegex(), "")

//        Patterns.EMAIL_ADDRESS.matcher(logEmail).matches()  <--- this methos was used before for the if statement but I've replaced it as I kept getting the invalid email error

        if (logEmail.contains("@") && logEmail.contains(".")) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(logEmail, logPass).addOnSuccessListener {
                FirebaseInstanceId.getInstance().instanceId
                    .addOnSuccessListener {
                        // Log and toast
                        val uid = FirebaseAuth.getInstance().uid

                        FirebaseMessaging.getInstance().subscribeToTopic(uid).addOnSuccessListener {

                            if (hasNoPermissions()) {
                                requestPermission()
                            } else {
                                val intent = Intent(this.context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        }
                    }.addOnFailureListener {
                        registerFail()
                        Toast.makeText(this.context, "Failed to log you in. ${it.localizedMessage}", Toast.LENGTH_LONG)
                            .show()
                    }

            }.addOnFailureListener {
                registerFail()
                Toast.makeText(this.context, "Failed to log you in. ${it.localizedMessage}", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            registerFail()
            Toast.makeText(this.context, "Please enter a valid email address", Toast.LENGTH_LONG).show()
        }
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = firebaseAuth.currentUser
                if (user != null){
                    val intent = Intent(this.context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this.context, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(activity as RegisterLoginActivity, mGoogleSignInOptions)

        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account !=null){
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this.context, "Google sign in failed:( $e", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(activity as RegisterLoginActivity, permissions, 0)

        val intent = Intent(this.context, RegisterLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun registerFail() {
        button.isClickable = true
        loadingAnimation.visibility = View.GONE
    }

    companion object {
        fun newInstance(): LoginFragment = LoginFragment()
    }
}