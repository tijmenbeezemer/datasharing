package nl.tijmen.articlereader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "read_links")
data class ReadLinkEntity(
    @PrimaryKey val link: String
)
