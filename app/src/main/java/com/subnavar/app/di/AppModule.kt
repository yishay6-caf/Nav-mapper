package com.subnavar.app.di

import android.content.Context
import androidx.room.Room
import com.subnavar.app.data.local.db.SubNavDatabase
import com.subnavar.app.data.local.db.dao.BuildingDao
import com.subnavar.app.data.local.db.dao.EdgeDao
import com.subnavar.app.data.local.db.dao.FloorDao
import com.subnavar.app.data.local.db.dao.WaypointDao
import com.subnavar.app.data.repository.BuildingRepositoryImpl
import com.subnavar.app.domain.repository.BuildingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SubNavDatabase =
        Room.databaseBuilder(
            context,
            SubNavDatabase::class.java,
            "subnav_database"
        ).build()

    @Provides
    fun provideBuildingDao(db: SubNavDatabase): BuildingDao = db.buildingDao()

    @Provides
    fun provideFloorDao(db: SubNavDatabase): FloorDao = db.floorDao()

    @Provides
    fun provideWaypointDao(db: SubNavDatabase): WaypointDao = db.waypointDao()

    @Provides
    fun provideEdgeDao(db: SubNavDatabase): EdgeDao = db.edgeDao()

    @Provides
    @Singleton
    fun provideBuildingRepository(impl: BuildingRepositoryImpl): BuildingRepository = impl
}
