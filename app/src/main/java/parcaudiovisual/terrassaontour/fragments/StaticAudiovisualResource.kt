package parcaudiovisual.terrassaontour.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_static_audiovisual_resource.view.*

import parcaudiovisual.terrassaontour.R

class StaticAudiovisualResource : Fragment() {
    var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_static_audiovisual_resource, container, false)

        val staticImageUrl = arguments?.getString("src")

        Picasso.get().load(staticImageUrl)
            .noFade()
            .placeholder(R.drawable.placeholder_loading_big)
            .into(rootView!!.StaticImageView)

        return rootView
    }


    companion object {
        fun newInstance(imageURL: String?): StaticAudiovisualResource{
            val fragment = StaticAudiovisualResource()
            val args = Bundle()

            args.putString("src",imageURL)
            fragment.arguments = args
            return fragment
        }
    }
}
