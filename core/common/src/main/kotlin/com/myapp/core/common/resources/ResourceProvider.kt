package com.myapp.core.common.resources

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface ResourceProvider {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg args: Any): String
    fun getColor(@ColorRes resId: Int): Int
    fun getDimension(@DimenRes resId: Int): Float
}

class AndroidResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : ResourceProvider {

    override fun getString(@StringRes resId: Int): String =
        context.getString(resId)

    override fun getString(@StringRes resId: Int, vararg args: Any): String =
        context.getString(resId, *args)

    override fun getColor(@ColorRes resId: Int): Int =
        ContextCompat.getColor(context, resId)

    override fun getDimension(@DimenRes resId: Int): Float =
        context.resources.getDimension(resId)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ResourceProviderModule {

    @Binds
    @Singleton
    abstract fun bindResourceProvider(impl: AndroidResourceProvider): ResourceProvider
}
