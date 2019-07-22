package com.republicera.fragments


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.RegisterLoginActivity
import com.republicera.models.User
import kotlinx.android.synthetic.main.fragment_register_login_screens.*

class RegisterFragment : androidx.fragment.app.Fragment() {

    private lateinit var userFirstName: EditText
    private lateinit var userLastName: EditText
    private lateinit var userEmail: EditText
    lateinit var userPassword: EditText
    private lateinit var button: TextView
    private lateinit var loadingAnimation: ConstraintLayout

    lateinit var textUserLastName: String
    lateinit var textUserFirstName: String
    lateinit var textUserEmail: String

    private lateinit var firebaseAuth: FirebaseAuth

    private val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_register_login_screens, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        val googleLogin = register_fragment_google_login

        register_fragment_forgot_password.visibility = View.GONE
        register_fragment_button.text = getString(R.string.sign_up)

        userFirstName = register_fragment_first_name
        userLastName = register_fragment_last_name
        userEmail = register_fragment_email
        userPassword = register_fragment_password
        button = register_fragment_button
        loadingAnimation = register_fragment_spinner

        googleLogin.setOnClickListener {
            configureGoogleSignIn()
        }

        button.setOnClickListener {
            performRegister()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val firstName = user.displayName!!.substringBefore(" ")
                val lastName = user.displayName!!.substringAfter(" ")
                addUserToFirebaseDatabase(firstName,lastName, 1, user.uid)
            }

        }.addOnFailureListener {
            Toast.makeText(this.context, "Google sign in failed :(", Toast.LENGTH_LONG).show()
            Log.d("checkook", it.toString())
        }
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
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this.context, "Google sign in failed:( $e", Toast.LENGTH_LONG).show()
                Log.d("checkook", e.toString())
            }
        }
    }


    private fun performRegister() {
        button.isClickable = false
        loadingAnimation.visibility = View.VISIBLE

        textUserFirstName = userFirstName.text.toString().trimEnd()
        textUserLastName = userLastName.text.toString().trimEnd()
        textUserEmail = userEmail.text.toString().replace("\\s".toRegex(), "")
        val textUserPassword = userPassword.text.toString().replace("\\s".toRegex(), "")

        if (textUserLastName.length > 1 && textUserFirstName.length > 1) {
            if (textUserEmail.contains("@") && textUserEmail.contains(".")) {
                if (textUserPassword.length > 7) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(textUserEmail, textUserPassword)
                        .addOnSuccessListener {
                            addUserToFirebaseDatabase(textUserFirstName, textUserLastName, 0, it.user!!.uid)
                        }
                        .addOnFailureListener {
                            registerFail()
                            Toast.makeText(
                                this.context,
                                it.toString(),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                } else {
                    registerFail()
                    Toast.makeText(
                        this.context,
                        "Your password needs to be at least 6 characters long",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            } else {
                registerFail()
                Toast.makeText(this.context, "Please enter a valid email address", Toast.LENGTH_LONG).show()
            }
        } else {
            registerFail()
            Toast.makeText(this.context, "Please enter a valid name", Toast.LENGTH_LONG).show()
        }
    }


    private fun registerFail() {
        button.isClickable = true
        loadingAnimation.visibility = View.GONE
    }

    private fun addUserToFirebaseDatabase(
        userFirstNameForDatabase: String,
        userLastNameForDatabase: String,
        case: Int,
        uid: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val timestamp = System.currentTimeMillis()

        val newUser =
            User(
                uid,
                userFirstNameForDatabase,
                userLastNameForDatabase,
                mutableListOf(),
                listOf("en"),
                0,
                timestamp,
                timestamp
            )

        db.collection("users").document(uid).set(newUser).addOnSuccessListener {

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


    companion object {
        fun newInstance(): RegisterFragment = RegisterFragment()
    }
}
