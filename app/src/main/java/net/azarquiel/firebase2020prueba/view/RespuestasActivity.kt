package net.azarquiel.firebase2020prueba.view

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import net.azarquiel.firebase2020prueba.R

import kotlinx.android.synthetic.main.activity_respuestas.*
import kotlinx.android.synthetic.main.content_respuestas.*
import net.azarquiel.firebase2020prueba.adapter.CustomAdapterComentarios
import net.azarquiel.firebase2020prueba.model.Preguntas
import net.azarquiel.firebase2020prueba.model.Respuestas
import org.jetbrains.anko.*

class RespuestasActivity : AppCompatActivity() {
    private lateinit var thispregunta: Preguntas
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapter: CustomAdapterComentarios
    private lateinit var mStorageRef: StorageReference
    private var respuestas: ArrayList<Respuestas> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respuestas)
        setSupportActionBar(toolbar)
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference()
        db = FirebaseFirestore.getInstance()
        thispregunta = intent.getSerializableExtra("pregunta") as Preguntas
        initRV()
        setListener()
        title=""
        fab.setOnClickListener { view ->
            if(mAuth.currentUser?.displayName == null){
                toast("Logeate para usar esta funcion")
            }else{
                addRespuestaDialog()
            }

        }
    }

    private fun addRespuestaDialog() {
        alert {
            title="Escribe tu respuesta"
            customView {
                verticalLayout{
                    lparams(width = wrapContent, height = wrapContent)
                    val comment = editText{
                        padding = dip(16)
                        inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        maxLines = 6
                        maxHeight = 10

                        singleLine = false
                        setHorizontallyScrolling(false)
                        hint = "Añade una descripcion..."
                    }
                    positiveButton("Añadir"){
                        if(comment.text.toString().isEmpty()){
                            toast("No se ha añadido nada")
                        }else{
                            databaseanswer(comment.text.toString())
                        }
                    }
                }
            }
        }.show()
    }

    private fun databaseanswer(comment: String) {
        val thiscomment: MutableMap<String, Any> = HashMap() // diccionario key value
        thiscomment["IDPregunta"] = thispregunta.id
        thiscomment["Owner"] = mAuth.currentUser!!.displayName.toString()
        thiscomment["IDOwner"] = mAuth.currentUser!!.uid
        thiscomment["Content"] = comment
        db.collection("respuestas")
            .add(thiscomment)
            .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->
                Log.d("Respuesta","DocumentSnapshot added with ID: " + documentReference.id)
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w("Respuesta","Error adding document", e)
            })
    }

    private fun setListener() {
        val docRef = db.collection("respuestas")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(MainActivity.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToList(snapshot.documents)

                adapter.setRespuestas(respuestas)
            } else {
                Log.d(MainActivity.TAG, "Current data: null")
            }
        }
    }
    private fun documentToList(documents: List<DocumentSnapshot>) {
        respuestas.clear()
        documents.forEach { d ->
            val content = d["Content"] as String
            val idpregunta = d["IDPregunta"] as String
            val owner = d["Owner"] as String
            val idowner = d["IDOwner"] as String
            if(idpregunta == thispregunta.id){
                respuestas.add(Respuestas( idPregunta = idpregunta,owner = owner,content = content, idowner = idowner))
            }
        }

    }

    private fun initRV() {
        tvTituloRes.text = thispregunta.titulo
        tvDesc.text = thispregunta.desc
        adapter = CustomAdapterComentarios(this, R.layout.rowcomentarios, mStorageRef)
        rvComentarios.adapter = adapter
        rvComentarios.layoutManager = LinearLayoutManager(this)
    }

}
