package parcaudiovisual.terrassaontour

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONException
import org.json.JSONObject

open class Audiovisual : RealmObject() {

    //Propiedades
    @PrimaryKey var id: String? = null
    var id_punto_audiovisual: String? = null
    var title: String? = null
    var description: String? = null
    var img_cabecera: String? = null
    var img_cabecera_thumbnail: String? = null
    var src: String? = null
    var year: String? = null
    var tipo_medio: String? = null
    var formato: String? = null
    var actores = RealmList<String>()
    var directores = RealmList<String>()
    var productoras = RealmList<ClienteProductora>()
    var clientes = RealmList<ClienteProductora>()
    var rutas_audiovisual = RealmList<String>()

    fun getEnum(type: String): TipoMedioAudiovisual {
        return TipoMedioAudiovisual.valueOf(type)
    }

    companion object {
        fun jsonAReferencia(referencia: JSONObject) : Audiovisual? {
            try {
                val resultado = Audiovisual()

                val actores = referencia.getJSONArray("actor")
                for (i: Int in 0 until actores.length()){
                    resultado.actores.add(actores[i] as String)
                }

                val directores = referencia.getJSONArray("director")
                for (i: Int in 0 until directores.length()){
                    resultado.directores.add(directores[i] as String)
                }

                val productoras = referencia.getJSONArray("productora")
                for (i: Int in 0 until productoras.length()){
                    val productoraInfo = productoras.getJSONObject(i)
                    val productora = ClienteProductora(productoraInfo.getString("nombre_productora"),productoraInfo.getString("link_productora"))
                    resultado.productoras.add(productora)
                }

                val clientes = referencia.getJSONArray("cliente")
                for (i: Int in 0 until clientes.length()) {
                    val clienteInfo = clientes.getJSONObject(i)
                    val cliente = ClienteProductora(clienteInfo.getString("nombre_cliente"), clienteInfo.getString("link_cliente"))
                    resultado.clientes.add(cliente)
                }

                val rutas = referencia.getJSONArray("rutas")
                for (i: Int in 0 until rutas.length()){
                    resultado.rutas_audiovisual.add(rutas[i] as String)
                }

                resultado.id = referencia.getString("id_audiovisual")
                resultado.id_punto_audiovisual = referencia.getString("id_punto_audiovisual")
                resultado.title = referencia.getString("title")
                resultado.description = referencia.getString("descripcion")
                resultado.img_cabecera = referencia.getString("img_cabecera")
                resultado.img_cabecera_thumbnail = referencia.getString("img_cabecera_thumbnail")
                resultado.src = referencia.getString("src")
                resultado.year = referencia.getString("year")
                resultado.tipo_medio = referencia.getString("tipo_medio")
                resultado.formato = referencia.getString("formato")

                return resultado
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }
    }
}
enum class TipoMedioAudiovisual(val type: String){
    VIDEO("1"),
    IMAGEN("2")
}
open class ClienteProductora() : RealmObject() {

    //Propiedades
    var nombre: String? = null
    var link: String? = null

    constructor( nombre: String, link: String): this() {
        this.nombre = nombre
        this.link = link
    }
}
class ClienteProductoraParcelable constructor(val nombre: String, val link: String): Parcelable{

    constructor(source: Parcel): this (
        source.readString()!!,
        source.readString()!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(nombre)
        dest.writeString(link)
    }

    override fun describeContents(): Int {
       return 0
    }

    companion object CREATOR: Parcelable.Creator<ClienteProductoraParcelable>{
        override fun createFromParcel(source: Parcel?): ClienteProductoraParcelable {
            return ClienteProductoraParcelable(source!!)
        }

        override fun newArray(size: Int): Array<ClienteProductoraParcelable?> {
            return arrayOfNulls(size)
        }
    }
}
class AudiovisualParcelable constructor(
    val id: String?,
    val id_punto_audiovisual: String?,
    val title: String?,
    val description: String?,
    val img_cabecera: String?,
    val img_cabecera_thumbnail: String?,
    val src: String?,
    val year: String?,
    val tipo_medio: String?,
    val formato: String?,
    val actores : ArrayList<String>,
    val directores : ArrayList<String> ,
    val productoras : ArrayList<ClienteProductoraParcelable>,
    val clientes : ArrayList<ClienteProductoraParcelable>,
    val rutas_audiovisual: List<String>
    ): Parcelable {

    constructor(source: Parcel): this (
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.createStringArrayList(),
        source.createStringArrayList(),
        source.createTypedArrayList(ClienteProductoraParcelable.CREATOR),
        source.createTypedArrayList(ClienteProductoraParcelable.CREATOR),
        source.createStringArrayList()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(id_punto_audiovisual)
        dest.writeString(title)
        dest.writeString(description)
        dest.writeString(img_cabecera)
        dest.writeString(img_cabecera_thumbnail)
        dest.writeString(src)
        dest.writeString(year)
        dest.writeString(tipo_medio)
        dest.writeString(formato)
        dest.writeStringList(actores)
        dest.writeStringList(directores)
        dest.writeTypedList(productoras)
        dest.writeTypedList(clientes)
        dest.writeStringList(rutas_audiovisual)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR: Parcelable.Creator<AudiovisualParcelable>{
        override fun createFromParcel(source: Parcel?): AudiovisualParcelable {
            return AudiovisualParcelable(source!!)
        }

        override fun newArray(size: Int): Array<AudiovisualParcelable?> {
           return arrayOfNulls(size)
        }
    }
}