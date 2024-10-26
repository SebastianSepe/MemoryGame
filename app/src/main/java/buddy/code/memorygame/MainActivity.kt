package buddy.code.memorygame

import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import buddy.code.memorygame.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private var score = 0
    private var result: String = ""
    private var userAnswer: String = ""
    private var bestScore = 0


    // MediaPlayer variables
    private var buttonClickSound: MediaPlayer? = null
    private var winSound: MediaPlayer? = null
    private var loseSound: MediaPlayer? = null
    private var backgroundMusic: MediaPlayer? = null
    private var panelSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        bestScore = getBestScore()
        binding.tvBestScore.text = "Best Score: $bestScore"

        // Initialize MediaPlayer objects
        buttonClickSound = MediaPlayer.create(this, R.raw.button_click)
        winSound = MediaPlayer.create(this, R.raw.win_sound)
        loseSound = MediaPlayer.create(this, R.raw.lose_sound)
        backgroundMusic = MediaPlayer.create(this, R.raw.background_music)
        panelSound = MediaPlayer.create(this, R.raw.panel_sound)

        // Set background music to loop and start playing
        backgroundMusic?.isLooping = true
        backgroundMusic?.setVolume(0.2f, 0.2f)  // Ajusta el volumen a un nivel moderado
        backgroundMusic?.start()

        binding.apply {
            panel1.setOnClickListener(this@MainActivity)
            panel2.setOnClickListener(this@MainActivity)
            panel3.setOnClickListener(this@MainActivity)
            panel4.setOnClickListener(this@MainActivity)
            startGame()
        }
    }



    private fun disableButtons() {
        binding.root.forEach { view ->
            if (view is Button) {
                view.isEnabled = false
            }
        }
    }

    private fun enableButtons() {
        binding.root.forEach { view ->
            if (view is Button) {
                view.isEnabled = true
            }
        }
    }

    private fun startGame() {
        result = ""
        userAnswer = ""
        disableButtons()
        lifecycleScope.launch {
            val round = (3..5).random()
            repeat(round) {
                delay(400)
                val randomPanel = (1..4).random()
                result += randomPanel
                val panel = when (randomPanel) {
                    1 -> binding.panel1
                    2 -> binding.panel2
                    3 -> binding.panel3
                    else -> binding.panel4
                }

                // Play the panel sound and change panel color
                panelSound?.let {
                    it.seekTo(0)
                    it.start()  // Play sound when panel is shown
                }
                val drawableYellow = ActivityCompat.getDrawable(this@MainActivity, R.drawable.btn_yellow)
                val drawableDefault = ActivityCompat.getDrawable(this@MainActivity, R.drawable.btn_state)
                panel.background = drawableYellow
                delay(1000)
                panel.background = drawableDefault
            }
            enableButtons()
        }
    }

    private fun loseAnimation() {
        loseSound?.start()  // Play lose sound
        binding.apply {
            score = 0
            tvScore.text = "0"
            disableButtons()
            val drawableLose = ActivityCompat.getDrawable(this@MainActivity, R.drawable.btn_lose)
            val drawableDefault = ActivityCompat.getDrawable(this@MainActivity, R.drawable.btn_state)
            lifecycleScope.launch {
                binding.root.forEach { view ->
                    if (view is Button) {
                        view.background = drawableLose
                        delay(300)
                        view.background = drawableDefault
                    }
                }
                Toast.makeText(this@MainActivity, "L O S E", Toast.LENGTH_SHORT).show()
                delay(2000)
                startGame()
            }
        }
    }

    override fun onClick(view: View?) {
        view?.let {
            buttonClickSound?.let {
                it.seekTo(0)  // Reiniciar el sonido al inicio
                it.start()    // Reproducir sonido de clic de botón
            }

            userAnswer += when(it.id){
                R.id.panel1 -> "1"
                R.id.panel2 -> "2"
                R.id.panel3 -> "3"
                R.id.panel4 -> "4"
                else -> "4"
            }

            if(userAnswer == result){
                Toast.makeText(this, "W I N", Toast.LENGTH_SHORT).show()
                winSound?.let {
                    it.seekTo(0)
                    it.start()  // Reproducir sonido de victoria
                }
                score++
                binding.tvScore.text = score.toString()
                if (score > bestScore) {
                    bestScore = score
                    saveBestScore(bestScore)
                    binding.tvBestScore.text = "Best Score: $bestScore"
                }
                startGame()
            } else if(userAnswer.length >= result.length){
                loseAnimation()
            }
        }
    }

    private fun saveBestScore(score: Int) {
        val sharedPreferences = getSharedPreferences("memory_game_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("best_score", score)
        editor.apply()
    }

    private fun getBestScore(): Int {
        val sharedPreferences = getSharedPreferences("memory_game_prefs", MODE_PRIVATE)
        return sharedPreferences.getInt("best_score", 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release MediaPlayer resources
        buttonClickSound?.release()
        winSound?.release()
        loseSound?.release()
        backgroundMusic?.release()
        panelSound?.release()
    }
}
