import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TTSManager(private val context: Context, private val onInitCallback: () -> Unit) :
    TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.KOREAN)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSManager", "Language is not supported or missing data")
            } else {
                // TTS 초기화 성공
                Log.d("TTSManager", "TextToSpeech initialization successful")
                onInitCallback.invoke()
            }
        } else {
            Log.e("TTSManager", "TextToSpeech initialization failed")
        }
    }

    fun speak(text: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "UniqueID")
        } else {
            // LOLLIPOP 이하의 버전에서는 UtteranceId를 지원하지 않음
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    fun shutdown() {
        textToSpeech?.let {
            if (it.isSpeaking) {
                it.stop()
            }
            it.shutdown()
        }
    }
}
