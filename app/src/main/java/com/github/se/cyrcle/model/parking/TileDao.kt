package com.github.se.cyrcle.model.parking

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface TileDao {

  @Upsert suspend fun upsert(tile: Tile)

  @Delete suspend fun delete(tile: Tile)

  @Query("SELECT * FROM tiles") suspend fun getAllTiles(): List<Tile>

  @Query("SELECT * FROM tiles WHERE uid = :uid") suspend fun getTile(uid: String): Tile
}
