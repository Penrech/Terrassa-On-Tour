package parcaudiovisual.terrassaontour.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_big_image_info_window.view.*

import parcaudiovisual.terrassaontour.R

class BigImageInfoWindow : Fragment() {
   var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_big_image_info_window, container, false)

        val imageUrlString = arguments?.getString("imageUrl")
        if (imageUrlString != null) {
            val imageURI = Uri.parse(imageUrlString)

            Picasso.get()
                .load(imageURI)
                .noFade()
                .placeholder(R.drawable.placeholder_loading_big)
                .into(rootView!!.infoWindowImageDetail)
        }

        return rootView
    }

    companion object {
        fun newInstance(imageUrl: String): BigImageInfoWindow {
           val fragment = BigImageInfoWindow()
            val args = Bundle()

            args.putString("imageUrl",imageUrl)
            fragment.arguments = args
            return fragment
            }
    }
}
