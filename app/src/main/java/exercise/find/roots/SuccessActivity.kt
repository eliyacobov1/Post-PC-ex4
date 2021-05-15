package exercise.find.roots

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class SuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)
        val textView = findViewById<TextView>(R.id.msg)
        val intent = intent
        textView.text = intent.getStringExtra("message")
    }
}