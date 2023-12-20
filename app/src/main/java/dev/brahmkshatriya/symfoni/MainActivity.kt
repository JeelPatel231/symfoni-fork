package dev.brahmkshatriya.symfoni

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dev.brahmkshatriya.symfoni.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.gianlu.librespot.audio.AbsChunkedInputStream
import xyz.gianlu.librespot.audio.DecodedAudioStream
import xyz.gianlu.librespot.audio.PlayableContentFeeder
import xyz.gianlu.librespot.audio.PlayableContentFeeder.LoadedStream
import xyz.gianlu.librespot.audio.decoders.AudioQuality
import xyz.gianlu.librespot.audio.decoders.VorbisOnlyAudioQuality
import xyz.gianlu.librespot.core.SearchManager
import xyz.gianlu.librespot.core.Session
import xyz.gianlu.librespot.metadata.TrackId
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    var _binding: ActivityMainBinding? = null
    val binding
        get() = _binding!!

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)


        val exoPlayer = ExoPlayer.Builder(this).build()



        binding.playerView.player = exoPlayer

        lifecycleScope.launch(Dispatchers.IO) {
            val session = SpotifySession()

            val myDataSource = MyCustomDataSource( { session.getTestTrack } )

            val customDataSourceFactory = DataSource.Factory { myDataSource }

            val mediaSource = ProgressiveMediaSource.Factory(customDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.EMPTY))

            runOnUiThread {
                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()
                exoPlayer.play()
            }
        }

    }
}

class SpotifySession {
    fun getTrack(trackUri: String): LoadedStream {
        return session.contentFeeder().load(
            TrackId.fromUri(trackUri),
            audioQualityPicker,
            false,
            null,
        )
    }

    val getTestTrack
        get() = getTrack(track)

    companion object {
        private val sessionConfig = Session.Configuration.Builder()
            .setCacheEnabled(false)
            .setStoreCredentials(false)
            .build()

        val session = Session.Builder(sessionConfig)
            .userPass(userName, password) // See other authentication methods
            .create()


        private val searches = session.search().request(SearchManager.SearchRequest("unicorn blood chime"))

        private val track = searches.getAsJsonObject("results")
            .getAsJsonObject("tracks")
            .getAsJsonArray("hits")
            .first().asJsonObject
            .get("uri").asString

        val audioQualityPicker = VorbisOnlyAudioQuality(AudioQuality.NORMAL)
    }
}

const val userName = ""
const val password = ""

@OptIn(UnstableApi::class)
class MyCustomDataSource(
//    private val decodedAudioStream: DecodedAudioStream
    private val streamGetter: () -> LoadedStream
) : BaseDataSource(true) {

    private lateinit var inputStream: InputStream

    override fun open(dataSpec: DataSpec): Long {

//        val sessionConfig = Session.Configuration.Builder()
//            .setCacheEnabled(false)
//            .setStoreCredentials(false)
//            .build()
//
//        val session = Session.Builder(sessionConfig)
//            .userPass(userName, password) // See other authentication methods
//            .create()
//
//        val searches = session.search().request(SearchManager.SearchRequest("unicorn blood chime"))
//
//        val track = searches.getAsJsonObject("results")
//            .getAsJsonObject("tracks")
//            .getAsJsonArray("hits")
//            .first().asJsonObject
//            .get("uri").asString
//
//        val audioQualityPicker = VorbisOnlyAudioQuality(AudioQuality.NORMAL)
//
//        val contentFeeder = session.contentFeeder().load(
//            TrackId.fromUri(track),
//            audioQualityPicker,
//            false,
//            null,
//        )

        Log.d("DEBUG", "OPENED")

        val stream = streamGetter()
        inputStream = stream.`in`.stream()

//        val intermediate = decodedAudioStream.stream().readBytes() //.buffered(1024 * 4)

//        inputStream = intermediate.inputStream()
//        return C.LENGTH_UNSET.toLong()
//        return data.stream().size()
//        return data.decryptTimeMs().toLong()
//        Log.d("DEBUG", totalBuffered!!.size.toString())

//        return C.LENGTH_UNSET.toLong()

//        val k = data.stream().size().toLong()
//        Log.d("DEBUG", "VAPAS")
//        return intermediate.size.toLong() //totalBuffered!!.size.toLong()
        return stream.`in`.stream().size().toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        return inputStream.read(buffer, offset, readLength) //.read(buffer, offset, readLength)
    }

    override fun getUri(): Uri {
        return Uri.EMPTY
    }

    override fun close() {
        inputStream.close()
    }
}
