package net.azarquiel.firebase2020prueba.model

import java.io.Serializable

data class Categorias(var id: String,var nombre:String=""): Serializable
data class Preguntas(var id:String,var idCategoria:String, var titulo:String="",var desc: String="",var idowner: String="", var owner:String=""): Serializable
data class Respuestas(var idPregunta:String, var idowner: String="", var owner:String="", var content:String=""): Serializable
