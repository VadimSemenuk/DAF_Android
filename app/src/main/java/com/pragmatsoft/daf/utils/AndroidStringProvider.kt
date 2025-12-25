package com.pragmatsoft.daf.utils

import android.content.Context
import com.pragmatsoft.daf.data.interfaces.StringProvider

class AndroidStringProvider(private val context: Context) : StringProvider {

    override fun getString(resourceId: Int, vararg formatArgs: Any): String {
        return context.getString(resourceId, *formatArgs)
    }

    override fun getQuantityString(resourceId: Int, quality: Int, vararg formatArgs: Any): String {
        return context.resources.getQuantityString(resourceId, quality, *formatArgs)
    }
}