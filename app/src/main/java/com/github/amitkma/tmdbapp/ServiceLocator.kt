package com.github.amitkma.tmdbapp

import android.app.Application
import android.content.Context
import com.github.amitkma.tmdbapp.data.TmdbDataRepository
import com.github.amitkma.tmdbapp.data.api.TmdbApi
import com.github.amitkma.tmdbapp.data.config.ImageUrlProvider
import com.github.amitkma.tmdbapp.ui.features.details.DetailStore
import com.github.amitkma.tmdbapp.ui.features.trending.TrendingStore
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(context: Context): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator(
                        app = context.applicationContext as Application
                    )
                }
                return instance!!
            }
        }
    }

    fun getRepository(): TmdbDataRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getTmdbApi(): TmdbApi

    val imageUrlProvider: ImageUrlProvider

    val trendingStore: TrendingStore

    val detailStore: DetailStore
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class DefaultServiceLocator(val app: Application) :
    ServiceLocator {
    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    /* private val db by lazy {
         MovieApi.create(app, useInMemoryDb)
     }*/

    private val api by lazy { TmdbApi.create() }

    override val trendingStore by lazy { TrendingStore(app) }

    override val detailStore by lazy { DetailStore(app) }

    override val imageUrlProvider by lazy { ImageUrlProvider() }

    override fun getRepository(): TmdbDataRepository = TmdbDataRepository(app, api)

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getTmdbApi(): TmdbApi = api
}