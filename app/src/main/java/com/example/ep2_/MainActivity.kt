package com.example.ep2_

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var etCorreo: EditText
    private lateinit var etpass: EditText
    private lateinit var btnInicio: Button
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    private lateinit var btnGoogle: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, Registrar::class.java)
            startActivity(intent)
        }

        etCorreo = findViewById(R.id.etCorreo)
        etpass = findViewById(R.id.etPassword)
        btnInicio = findViewById(R.id.btnIngresar)
        dbHelper = DatabaseHelper(this)

        btnGoogle = findViewById(R.id.btnGoogle)
        auth = FirebaseAuth.getInstance()

        // Cierra cualquier sesión previa de Google para que siempre pida iniciar sesión al abrir la app
        auth.signOut()

        credentialManager = CredentialManager.create(this)

        // Evento para Iniciar Sesion normal (SQLite)
        btnInicio.setOnClickListener {
            iniciarSesion()
        }

        // Evento para Iniciar Sesion con Google
        btnGoogle.setOnClickListener {
            iniciarSesionGoogle()
        }
    }

    // Metodo para iniciar Sesion con base de datos local
    private fun iniciarSesion() {
        val correo = etCorreo.text.toString().trim()
        val pass = etpass.text.toString().trim()

        if (correo.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val resultado = dbHelper.iniciarSesion(correo, pass)

        if (resultado) {
            Toast.makeText(this, "Bienvenido :D", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, RutaActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Datos Incorrectos :(", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. METODO PARA AUTENTICAR EN FIREBASE
    private fun autenticarFirebase(idToken: String) {
        val credencialFirebase = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credencialFirebase).addOnCompleteListener(this) { inicio ->
            if (inicio.isSuccessful) {
                val usuario = auth.currentUser
                Toast.makeText(this, "Bienvenido ${usuario?.displayName}", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, RutaActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al validar con Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. METODO PARA PROCESAR LA CREDENCIAL
    private fun procesarCredencial(credential: Credential) {
        if (credential is CustomCredential && credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val credencialGoogle = GoogleIdTokenCredential.createFrom(credential.data)
                autenticarFirebase(credencialGoogle.idToken)
            } catch (e: GoogleIdTokenParsingException) {
                Toast.makeText(this, "Credencial invalida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 5. METODO PARA INICIAR SESIÓN CON GOOGLE
    private fun iniciarSesionGoogle() {
        val inicioGoogle = GetSignInWithGoogleOption.Builder(
            getString(R.string.default_web_client_id)
        ).build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(inicioGoogle)
            .build()

        lifecycleScope.launch {
            try {
                val resultado = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = solicitud
                )
                procesarCredencial(resultado.credential)
            } catch (e: GetCredentialException) {
                Toast.makeText(this@MainActivity, "Error al iniciar sesion", Toast.LENGTH_SHORT).show()
            }
        }
    }
}