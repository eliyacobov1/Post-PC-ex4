package exercise.find.roots
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.NumberFormatException
import java.math.BigInteger

class MainActivity : AppCompatActivity() {
    private var broadcastReceiverForSuccess: BroadcastReceiver? = null
    private var broadcastReceiverForFailure: BroadcastReceiver? = null
    private var isCalculating = false
    private var currInput = ""

    /**
     * this function enables user-input and the calculate button and hides the progress bar
     */
    fun finishCalculation(userInput: View, calcButton: View, progressBar: View){
        userInput.isEnabled = true
        calcButton.isEnabled = true
        progressBar.visibility = View.GONE
        isCalculating = false
    }

    /**
     * this function enables user-input and the calculate button and hides the progress bar
     */
    private fun startCalculation(userInput: View, calcButton: View, progressBar: View){
        userInput.isEnabled = false
        calcButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        isCalculating = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val editTextUserInput = findViewById<EditText>(R.id.editTextInputNumber)
        val buttonCalculateRoots = findViewById<Button>(R.id.buttonCalculateRoots)

        // set initial UI:
        progressBar.visibility = View.GONE // hide progress
        editTextUserInput.setText("") // cleanup text in edit-text
        editTextUserInput.isEnabled = true // set edit-text as enabled (user can input text)
        buttonCalculateRoots.isEnabled = false // set button as disabled (user can't click)

        // set listener on the input written by the keyboard to the edit-text
        editTextUserInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                // text did change
                currInput = editTextUserInput.text.toString()
                try {
                    currInput.toLong()
                } catch (e: NumberFormatException){
                    buttonCalculateRoots.isEnabled = false
                }
                buttonCalculateRoots.isEnabled = currInput != ""
            }
        })

        // set click-listener to the button
        buttonCalculateRoots.setOnClickListener { v: View? ->
            val intentToOpenService = Intent(this@MainActivity, CalculateRootsService::class.java)
            val userInputString = editTextUserInput.text.toString()
            val userInputLong: BigInteger = userInputString.toBigInteger()
            intentToOpenService.putExtra("number_for_service", userInputLong.toLong())
            startService(intentToOpenService)
            startCalculation(editTextUserInput, buttonCalculateRoots, progressBar)
        }

        // register a broadcast-receiver to handle action "found_roots"
        val myActivity = this
        broadcastReceiverForSuccess = object : BroadcastReceiver() {
            override fun onReceive(context: Context, incomingIntent: Intent) {
                if (incomingIntent.action != "found_roots") return
                // success finding roots!
                val originalNum = incomingIntent.getLongExtra("original_number", 0)
                val root1 = incomingIntent.getLongExtra("root1", 0)
                val root2 = incomingIntent.getLongExtra("root2", 0)
                // check if number is prime or not and return the appropriate message
                val msg = String.format("%d*%d=%d", root1, root2, originalNum)
                val successIntent = Intent(this@MainActivity, SuccessActivity::class.java)
                successIntent.putExtra("message", msg)
                startActivity(successIntent)
                finishCalculation(editTextUserInput, buttonCalculateRoots, progressBar)
            }
        }
        // register a broadcast-receiver to handle action "stopped_calculations"
        broadcastReceiverForFailure = object : BroadcastReceiver() {
            override fun onReceive(context: Context, incomingIntent: Intent) {
                if (incomingIntent.action != "stopped_calculations") return
                val secondsElapsed = incomingIntent.getLongExtra("time_until_give_up_seconds",
                        0)
                val msg = String.format("calculation aborted after %d sec", secondsElapsed)
                Toast.makeText(myActivity, msg, Toast.LENGTH_LONG).show()
                finishCalculation(editTextUserInput, buttonCalculateRoots, progressBar)
            }
        }
        registerReceiver(broadcastReceiverForSuccess, IntentFilter("found_roots"))
        registerReceiver(broadcastReceiverForFailure, IntentFilter("stopped_calculations"))
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(broadcastReceiverForSuccess)
        this.unregisterReceiver(broadcastReceiverForFailure)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentInput", currInput);
        outState.putBoolean("isCalculating", isCalculating);
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setContentView(R.layout.activity_main)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val editTextUserInput = findViewById<EditText>(R.id.editTextInputNumber)
        val buttonCalculateRoots = findViewById<Button>(R.id.buttonCalculateRoots)

        isCalculating = savedInstanceState.getBoolean("isCalculating", false)
        currInput = savedInstanceState.getString("currentInput", "")
        editTextUserInput.setText(currInput)

        if (isCalculating) startCalculation(editTextUserInput, buttonCalculateRoots, progressBar)
        else finishCalculation(editTextUserInput, buttonCalculateRoots, progressBar)
    }
}