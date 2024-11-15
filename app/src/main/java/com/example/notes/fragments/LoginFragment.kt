package com.example.notes.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.notes.R
import com.example.notes.fragments.NoteListFragment
import com.example.notes.utils.SharedPreferencesHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var logoImg: ImageView
    private lateinit var progressBar: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        // Set up login button
        view.findViewById<Button>(R.id.btn_google_login).setOnClickListener {
            signInWithGoogle()
        }

        logoImg = view.findViewById(R.id.logo)
        logoImg.setImageResource(R.drawable.logo)

        progressBar = view.findViewById(R.id.progressBar)

        return view
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken)
            progressBar.visibility = View.GONE
        } catch (e: ApiException) {
            progressBar.visibility = View.GONE
            Log.w("LoginFragment", "Google sign-in failed", e)
        }
    }

    private fun signInWithGoogle() {
        progressBar.visibility = View.VISIBLE
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {

                    progressBar.visibility = View.GONE
                    // Sign in success
                    SharedPreferencesHelper.setLoggedIn(requireContext(), true)
                    navigateToNoteList()
                } else {
                    progressBar.visibility = View.GONE
                    // If sign-in fails, display a message to the user.
                    Log.w("LoginFragment", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun navigateToNoteList() {
        progressBar.visibility = View.GONE
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NoteListFragment())
            .commit()
    }
}
