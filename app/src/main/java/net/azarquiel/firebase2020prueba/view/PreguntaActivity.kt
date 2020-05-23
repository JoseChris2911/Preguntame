package net.azarquiel.firebase2020prueba.view

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.firebase2020prueba.R

import kotlinx.android.synthetic.main.activity_pregunta.*
import kotlinx.android.synthetic.main.content_pregunta.*
import net.azarquiel.firebase2020prueba.adapter.CustomAdapterPreguntas
import net.azarquiel.firebase2020prueba.model.Categorias
import net.azarquiel.firebase2020prueba.model.Preguntas
import org.jetbrains.anko.*

class PreguntaActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var searchView: SearchView
    private lateinit var db: FirebaseFirestore
    private lateinit var thiscategoria: Categorias
    private lateinit var adapter: CustomAdapterPreguntas
    private lateinit var mAuth: FirebaseAuth
    private var preguntas: ArrayList<Preguntas> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregunta)
        setSupportActionBar(toolbar)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance();
        thiscategoria = intent.getSerializableExtra("categoria") as Categorias
        title="Preguntas"
        initRV()
        setListener()

        fab.setOnClickListener { view ->
            if(mAuth.currentUser?.displayName == null){toast("Logeate para usar esta funcion")}else{añadirPregunta()}
        }
    }

    fun onClickPregunta(v: View){
        val pregunta = v.tag as Preguntas
        val intent = Intent(this, RespuestasActivity::class.java)
        intent.putExtra("pregunta",pregunta)
        startActivity(intent)
    }

    private fun añadirPregunta() {
        alert {
            title="¿Quieres añadir tu pregunta?"
            customView {
                verticalLayout{
                    lparams(width = wrapContent, height = wrapContent)
                    val pregunta = editText{
                        padding = dip(16)
                        hint = "Titulo"
                    }
                    val desc = editText{
                        padding = dip(16)
                        inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        maxLines = 6
                        maxHeight = 10

                        singleLine = false
                        setHorizontallyScrolling(false)
                        hint = "Añade una descripcion..."
                    }
                    positiveButton("Añadir"){
                        if(pregunta.text.toString().isEmpty() || desc.text.toString().isEmpty()){
                            toast("No se ha añadido nada, faltan campos")
                        }else{
                            databasequestion(pregunta.text.toString(), desc.text.toString())
                        }
                    }
                }
            }
        }.show()
    }

    private fun databasequestion(pregunta: String, desc: String) {
        val thispregunta: MutableMap<String, Any> = HashMap() // diccionario key value
        thispregunta["IDCategoria"] = thiscategoria.id
        thispregunta["Titulo"] = pregunta
        thispregunta["Owner"] = mAuth.currentUser!!.displayName.toString()
        thispregunta["IDOwner"] = mAuth.currentUser!!.uid
        thispregunta["Desc"] = desc
        db.collection("preguntas")
            .add(thispregunta)
            .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->
                Log.d("Pregunta","DocumentSnapshot added with ID: " + documentReference.id)
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w("Pregunta","Error adding document", e)
            })
    }

    private fun initRV() {
        adapter = CustomAdapterPreguntas(this, R.layout.rowpreguntas)
        rvPreguntas.adapter = adapter
        rvPreguntas.layoutManager = LinearLayoutManager(this)
    }
    private fun setListener() {
        val docRef = db.collection("preguntas")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(MainActivity.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToList(snapshot.documents)

                adapter.setPreguntas(preguntas)
            } else {
                Log.d(MainActivity.TAG, "Current data: null")
            }
        }
    }
    private fun documentToList(documents: List<DocumentSnapshot>) {
        preguntas.clear()
        documents.forEach { d ->
            val titulo = d["Titulo"] as String
            val id = d.id
            val idcategoria = d["IDCategoria"] as String
            val owner = d["Owner"] as String
            val desc = d["Desc"] as String
            val idowner = d["IDOwner"] as String
            if(idcategoria == thiscategoria.id){
                //preguntas.add(Preguntas(id = id, idCategoria = idcategoria,titulo = titulo, owner = owner))
                preguntas.add(Preguntas(id = id, idCategoria = idcategoria,titulo = titulo,desc = desc, idowner = idowner, owner = owner))
            }
        }

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_pregunta, menu)
        // ************* <Filtro> ************
        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem.actionView as SearchView
        searchView.setQueryHint("Buscar...")
        searchView.setOnQueryTextListener(this)
        // ************* </Filtro> ************
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    // ************* <Filtro> ************
    override fun onQueryTextChange(query: String): Boolean {
        val original = ArrayList<Preguntas>(preguntas)
        adapter.setPreguntas(original.filter { pregunta -> pregunta.titulo.contains(query) })
        return false
    }

    override fun onQueryTextSubmit(text: String): Boolean {
        return false
    }
    // ************* </Filtro> ************


}
