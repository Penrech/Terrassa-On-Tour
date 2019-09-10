package parcaudiovisual.terrassaontour

import android.os.Parcel
import android.os.Parcelable

class DetailInfoImages constructor(val imgPrincipal: String, val imgSecundary: String, val day: Int, val interior: Int): Parcelable {

    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readInt(),
        source.readInt()
        )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(imgPrincipal)
        dest.writeString(imgSecundary)
        dest.writeInt(day)
        dest.writeInt(interior)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR: Parcelable.Creator<DetailInfoImages> {
        override fun createFromParcel(source: Parcel?): DetailInfoImages {
            return DetailInfoImages(source!!)
        }

        override fun newArray(size: Int): Array<DetailInfoImages?> {
            return arrayOfNulls(size)
        }
    }
}