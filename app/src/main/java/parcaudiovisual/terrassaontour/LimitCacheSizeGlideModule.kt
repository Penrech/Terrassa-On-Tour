package parcaudiovisual.terrassaontour

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.module.GlideModule


class LimitCacheSizeGlideModule : GlideModule {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    }


    override fun applyOptions(context: Context, builder: GlideBuilder) {
        /*if (MyApp.from(context).isTest())
            return  // NOTE: StatFs will crash on robolectric.*/
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, 15000))

    }


}