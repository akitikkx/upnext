# Upnext: TV Series Manager

### Database Migration

#### Setup of migrations
- Ensure that the following has been added to the application build.gradle

        android {
            ...
            defaultConfig {
                ...

                javaCompileOptions {
                    annotationProcessorOptions {
                        arguments = ["room.schemaLocation": "$projectDir/schemas".toString()]
                    }
                }
            }
        
            sourceSets {
                androidTest.assets.srcDirs += files("$projectDir/schemas".toString())
            }

- Add the following dependency:

        androidTestImplementation "android.arch.persistence.room:testing:$room_version"

#### Adding a table
- Prepare the current schema for the migration,

        Build > Rebuild Project 

    this will generate a *{version_number}.json* file in *app/schemas/com.theupnextapp.database.UpnextDatabase*
- increase the most recent version by 1
- In *app/src/main/java/com/theupnextapp/database/Room.kt* change the version as well in the @Database section

      Build > Rebuild Project
- Now add the new table/`@Entity` in the *com.theupnextapp.database* package
- Add the new entity to the *app/src/main/java/com/theupnextapp/database/Room.kt* @Database section

      Build > Rebuild Project
- Look inside *{latest_version_number}.json* for the new table's createSql and copy this
- Create/Update *com.theupnextapp.database.DatabaseMigration.kt* and define the new migration

            val MIGRATION_1_2: Migration = object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `trakt_watchlist` (`id` INTEGER, `listed_at` TEXT, `rank` INTEGER, `title` TEXT, `imdbID` TEXT, `slug` TEXT, `tmdbID` INTEGER, `traktID` INTEGER, `tvdbID` INTEGER, `tvrageID` INTEGER, `tvMazeID` INTEGER, PRIMARY KEY(`id`))")
                }
            }
- ensure that the new migration is added to the getDatabase in *Room.kt*

        fun getDatabase(context: Context): UpnextDatabase {
            ...
                    .addMigrations(MIGRATION_1_2)
                    .build()
                }
            }
            return INSTANCE
        }
- deploy app to device

#### Typography
- All font styles are defined in *res/values/type.xml*
- The name of the style is of the format
 `TextAppearance.Upnext.<qualifer>` e.g `TextAppearance.Upnext.Headline6` with the parent
  matching the Material Design `TextAppearance.MaterialComponents.Headline6`
