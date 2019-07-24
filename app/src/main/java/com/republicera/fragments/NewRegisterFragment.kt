package com.republicera.fragments


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.RegisterLoginActivity
import com.republicera.interfaces.GeneralMethods
import com.republicera.models.User
import kotlinx.android.synthetic.main.fragment_register.*
import java.util.regex.Matcher


class NewRegisterFragment : Fragment(), GeneralMethods {

    private lateinit var loadingAnimation: ConstraintLayout
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var submitButton: TextView

    private lateinit var firebaseAuth: FirebaseAuth

    private val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_register, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        loadingAnimation = register_login_spinner
        emailField = register_login_email
        passwordField = register_login_password
        submitButton = register_login_button

        val eyeIcon = register_login_password_eye
        eyeIcon.tag = "hidden"

        eyeIcon.setOnClickListener {
            changePasswordVisibility(passwordField, eyeIcon)
        }

        submitButton.setOnClickListener {
            performLogin()
        }

        val googleLogin = register_login_google_login
        googleLogin.setOnClickListener {
            configureGoogleSignIn()
        }

    }




    private fun performLogin() {
        loadingAnimation.visibility = View.VISIBLE
        submitButton.isClickable = false
        closeKeyboard(activity as RegisterLoginActivity)

        val logEmail = emailField.text.toString().trim()
        val logPass = passwordField.text.toString().replace("\\s".toRegex(), "")

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
                    }
            }.addOnFailureListener {
                if (it is FirebaseAuthInvalidUserException) {
                    if (it.errorCode == "ERROR_USER_NOT_FOUND") {
                        performRegister()
                    }
                }
            }
        } else {
            registerFail()
            Toast.makeText(this.context, "Please enter a valid email address", Toast.LENGTH_LONG).show()
        }
    }



    private fun performRegister() {

        val email = emailField.text.toString()

        val textUserPassword = passwordField.text.toString().replace("\\s".toRegex(), "")

        if (email.contains("@") && email.contains(".")) {
            if (textUserPassword.length >= 8) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, textUserPassword)
                    .addOnSuccessListener {
                        addUserToFirebaseDatabase("","",0, it.user!!.uid)
                    }
                    .addOnFailureListener {
                        registerFail()
                        Toast.makeText(
                            this.context,
                            it.localizedMessage,
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
            } else {
                registerFail()
                Toast.makeText(
                    this.context,
                    "Your password needs to be at least 8 characters long",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        } else {
            registerFail()
            Toast.makeText(this.context, "Please enter a valid email address", Toast.LENGTH_LONG).show()
        }
    }





    private fun addUserToFirebaseDatabase(
        firstName: String,
        lastName: String,
        case: Int,
        uid: String
    ) {

        if (case == 0) {
            val user = FirebaseAuth.getInstance().currentUser
            user?.sendEmailVerification()?.addOnSuccessListener {
                Toast.makeText(
                    this.context,
                    "Please check your email and click the link in our message",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            val intent = Intent(this.context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

//        val db = FirebaseFirestore.getInstance()
//        val timestamp = System.currentTimeMillis()
//
//        val newUser = User(
//                uid,
//                firstName,
//                lastName,
//                mutableListOf(),
//                listOf("en"),
//                0,
//                timestamp,
//                timestamp
//            )
//
//        db.collection("users").document(uid).set(newUser).addOnSuccessListener {
//
//        }
    }





    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(activity as RegisterLoginActivity, mGoogleSignInOptions)

        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.givenName ?: "", account.familyName ?: "", account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this.context, "Google sign in failed:( ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun firebaseAuthWithGoogle(firstName: String, lastName : String, acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener {
            val user = firebaseAuth.currentUser
            if (user != null) {
                addUserToFirebaseDatabase(firstName, lastName, 1, user.uid)
            }
        }.addOnFailureListener {
            Toast.makeText(this.context, "Google sign in failed :(", Toast.LENGTH_LONG).show()
        }
    }



    override fun onStart() {
        super.onStart()

        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            FirebaseAuth.getInstance().currentUser!!.reload().addOnSuccessListener {
                val updatedUser = FirebaseAuth.getInstance().currentUser
                if (updatedUser!!.isEmailVerified) {
                    val intent = Intent(this.context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
    }

    private fun changePasswordVisibility(editText: EditText, icon: ImageButton) {
        if (icon.tag == "hidden") {
            icon.tag = "visible"
            icon.setImageResource(R.drawable.eye_with_line)
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()

        } else {
            icon.tag = "hidden"
            icon.setImageResource(R.drawable.eye)
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
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
        submitButton.isClickable = true
        loadingAnimation.visibility = View.GONE
    }


}
