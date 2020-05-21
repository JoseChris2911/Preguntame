package net.azarquiel.firebase2020prueba.view

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import net.azarquiel.firebase2020prueba.R
import net.azarquiel.firebase2020prueba.adapter.CustomAdapterCategorias
import net.azarquiel.firebase2020prueba.model.Categorias
import net.azarquiel.firebase2020prueba.tools.PickerImage
import org.jetbrains.anko.*
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var ib: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CustomAdapterCategorias
    private var categorias: ArrayList<Categorias> = ArrayList()
    private lateinit var mAuth: FirebaseAuth
    private lateinit var optiondialog: DialogInterface
    private lateinit var picker: PickerImage
    private lateinit var mStorageRef: StorageReference



    companion object {
        const val REQUEST_PERMISSION = 200
        const val REQUEST_GALLERY = 1
        const val REQUEST_CAMERA = 2
        const val TAG = "ImgPicker"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        mStorageRef = FirebaseStorage.getInstance().getReference()
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        picker = PickerImage(this)
        initRV()
        setListenerCategorias()
        title="Categorias"

    }



    private fun initRV() {

        adapter = CustomAdapterCategorias(this, R.layout.rowcategorias)
        rvCategorias.adapter = adapter
        rvCategorias.layoutManager = LinearLayoutManager(this)
    }
    private fun setListenerCategorias() {
        val docRefCate = db.collection("categorias")
        docRefCate.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListCat(snapshot.documents)

                adapter.setCategorias(categorias)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }
    fun onClickCategoria(v: View){
        val categoria = v.tag as Categorias
        val intent = Intent(this, PreguntaActivity::class.java)
        intent.putExtra("categoria",categoria)
        startActivity(intent)
    }
    private fun documentToListCat(documents: List<DocumentSnapshot>) {
        categorias.clear()
        documents.forEach { d ->
            val nombre = d["Nombre"] as String
            val id = d.id
            categorias.add(Categorias(id = id,nombre = nombre))
        }

    }





    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.login -> optionsDialog()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        picker.onActivityResult(requestCode, resultCode, data)
        subirFoto(data)
    }


    private fun subirFoto(
        data: Intent?
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val progress = indeterminateProgressDialog("subiendo foto, espera")
        val storageRef = mStorageRef.storage.reference

        var photoRef = storageRef.child("images/${currentUser!!.uid}.jpg")
        if(photoRef == null){
            photoRef = storageRef.child("images/")
        }
        //Log.d("File path", data?.dataString)
        lateinit var uri: Uri
        if(data != null){
            uri = Uri.parse("content://media"+data?.data?.path)

        }else{
            uri = getImageUri(this, picker.bitmap!!)!!

        }
        val file = uri
        photoRef.putFile(file)
            .addOnSuccessListener { taskSnapshot -> // Get a URL to the uploaded content

                val profileUpdates =
                    UserProfileChangeRequest.Builder().setPhotoUri(uri).build()
                    //UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse("content://media"+data?.data?.path)).build()
                currentUser!!.updateProfile(profileUpdates)
                ib.setImageURI(currentUser?.photoUrl)
                progress.dismiss()
            }
            .addOnFailureListener {
                // Handle unsuccessful uploads
                // ...
                progress.dismiss()
                toast("No se pudo subir la foto")
            }
    }
    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            inContext.getContentResolver(),
            inImage, "Title", null
        )
        return Uri.parse(path)
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        picker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun optionsDialog(){
        val currentUser = FirebaseAuth.getInstance().currentUser
        optiondialog = alert {
            if(mAuth.currentUser == null){

                title = "¡Bienvenido a Preguntame!"
                customView{
                    verticalLayout{
                        lparams(width = wrapContent, height = wrapContent)
                        textView(""){
                            textSize=20f
                            gravity=Gravity.CENTER

                        }
                        button("Registrarse"){setOnClickListener{registerDialog()}}
                        button("Login"){setOnClickListener{
                            loginDialog()

                        }}
                        positiveButton("Cerrar"){}
                    }
                }
            }else{

                title="¡Bienvenido ${mAuth.currentUser?.displayName}!"
                //imagen potencial
                customView{
                    verticalLayout{
                        lparams(width = wrapContent, height = wrapContent)
                        button("Cambiar imagen"){
                            setOnClickListener{
                                picker.showPictureDialog()
                                optiondialog.dismiss()
                            }

                        }
                        ib = imageView{
                            maxHeight = 10
                            maxWidth = 10
                        }
                        if(currentUser?.photoUrl == null){
                            ib.setImageResource(R.drawable.noimage)
                        }else{
                            ib.setImageURI(currentUser?.photoUrl)
                        }

                        negativeButton("Logout"){logout()}
                        positiveButton("Cerrar"){}
                    }
                }
            }

        }.show()
    }



    private fun logout() {
        mAuth.signOut()
    }

    private fun loginDialog() {
        alert {
            title = "LOGIN"
            customView {
                verticalLayout {

                    lparams(width = wrapContent, height = wrapContent)
                    val etNombre = editText {
                        hint = "Email"
                        padding = dip(16)
                    }
                    etNombre.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                    val etPass = editText {
                        hint = "Contraseña"
                        padding = dip(16)
                        inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    etPass.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    positiveButton("Aceptar") {
                        if (etNombre.text.toString().isEmpty() || etPass.text.toString().isEmpty())
                            toast("Campos obligatorios...")

                        else {
                            login(etNombre.text.toString(),etPass.text.toString())
                            optiondialog.dismiss()
                        }

                    }
                }
            }
        }.show()

    }

    private fun registerDialog() {
        alert {
            title = "REGISTER"
            customView {
                verticalLayout {
                    lparams(width = wrapContent, height = wrapContent)
                    val etNombre = editText {
                        hint = "Nick"
                        padding = dip(16)
                    }
                    etNombre.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL)
                    val etEmail = editText {
                        hint = "Email"
                        padding = dip(16)
                    }
                    etNombre.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                    val etPass = editText {
                        hint = "Contraseña"
                        padding = dip(16)
                        inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    }
                    etPass.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    positiveButton("Aceptar") {
                        if (etEmail.text.toString().isEmpty() || etPass.text.toString().isEmpty() || etNombre.text.toString().isEmpty())
                            toast("Required fields...")
                        else {
                            register(etEmail.text.toString(),etPass.text.toString(), etNombre.text.toString())
                            optiondialog.dismiss()
                        }

                    }
                }
            }
        }.show()
    }

    private fun register(email: String, password: String, nombre: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("REGISTER", "createUserWithEmail:success")
                    val user = FirebaseAuth.getInstance().currentUser
                    val profileUpdates =
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(nombre).build()
                    user!!.updateProfile(profileUpdates)

                   // val user = mAuth.currentUser

                   // updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        "REGISTER",
                        "createUserWithEmail:failure",
                        task.exception
                    )
                    Toast.makeText(
                        this@MainActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()

                }


            }


    }




    private fun login(email: String, pass: String) {
        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LOGIN", "signInWithEmail:success")

                    val user = mAuth.currentUser
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        "LOGIN",
                        "signInWithEmail:failure",
                        task.exception
                    )
                    Toast.makeText(
                        this@MainActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()

                }


            }

    }

}
