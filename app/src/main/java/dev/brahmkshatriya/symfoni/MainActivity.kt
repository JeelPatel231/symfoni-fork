
package dev.brahmkshatriya.symfoni

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import xyz.gianlu.librespot.audio.decoders.AudioQuality
import xyz.gianlu.librespot.audio.decoders.VorbisOnlyAudioQuality
import xyz.gianlu.librespot.core.SearchManager
import xyz.gianlu.librespot.core.Session
import xyz.gianlu.librespot.metadata.TrackId
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val aage = findViewById<Button>(R.id.forward)
        val piche = findViewById<Button>(R.id.backward)

        val player = ExoPlayer.Builder(this).build()

        val customDataSource = MyCustomDataSource()
        val customDataSourceFactory = DataSource.Factory { customDataSource }

        val mediaSource = ProgressiveMediaSource.Factory(customDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.EMPTY))

        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()

        aage.setOnClickListener {
            player.seekForward()
        }

        piche.setOnClickListener {
            player.seekBack()
        }

    }
}

const val userName = ""
const val password = ""

@OptIn(UnstableApi::class)
class MyCustomDataSource : BaseDataSource(true) {

    private var inputStream: InputStream? = null

    override fun open(dataSpec: DataSpec): Long {
        val sessionConfig = Session.Configuration.Builder()
            .setCacheEnabled(false)
            .setStoreCredentials(false)
            .build()

        val session = Session.Builder(sessionConfig)
            .userPass(userName, password) // See other authentication methods
            .create()

        val searches = session.search().request(SearchManager.SearchRequest("unicorn blood chime"))

        val track = searches.getAsJsonObject("results")
            .getAsJsonObject("tracks")
            .getAsJsonArray("hits")
            .first().asJsonObject
            .get("uri").asString

        val audioQualityPicker = VorbisOnlyAudioQuality(AudioQuality.NORMAL)

        val contentFeeder = session.contentFeeder().load(
            TrackId.fromUri(track),
            audioQualityPicker,
            false,
            null,
        )
        val data = contentFeeder.`in`

        println("Vapas")

        inputStream = data.stream()
        return C.LENGTH_UNSET.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        return inputStream!!.read(buffer, offset, readLength)
    }

    override fun getUri(): Uri {
        return Uri.EMPTY
    }

    override fun close() {
        inputStream?.close()
        inputStream = null
    }
}
