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

class MainActivity : AppCompatActivity() {
    private var broadcastReceiverForSuccess: BroadcastReceiver? = null
    private var broadcastReceiverForFailure: BroadcastReceiver? = null

    /**
     * this function enables user-input and the calculate button and hides the progress bar
     */
    fun finishCalculation(userInput: View, calcButton: View, progressBar: View){
        userInput.isEnabled = true
        calcButton.isEnabled = true
        progressBar.visibility = View.GONE
    }

    // TODO: add any other fields to the activity as you want
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
                val newText = editTextUserInput.text.toString()
                buttonCalculateRoots.isEnabled = true
                // todo: check conditions to decide if button should be enabled/disabled (see spec below)
            }
        })

        // set click-listener to the button
        buttonCalculateRoots.setOnClickListener { v: View? ->
            val intentToOpenService = Intent(this@MainActivity, CalculateRootsService::class.java)
            val userInputString = editTextUserInput.text.toString()
            // todo: check that `userInputString` is a number. handle bad input. convert `userInputString` to long
            val userInputLong: Long = userInputString.toLong()
            intentToOpenService.putExtra("number_for_service", userInputLong)
            startService(intentToOpenService)
            buttonCalculateRoots.isEnabled = false
            editTextUserInput.isEnabled = false
            progressBar.visibility = View.VISIBLE
        }

        // register a broadcast-receiver to handle action "found_roots"
        val myActivity = this
        broadcastReceiverForSuccess = object : BroadcastReceiver() {
            override fun onReceive(context: Context, incomingIntent: Intent) {
                if (incomingIntent.action != "found_roots") return
                // success finding roots!
                val originalNum = incomingIntent.getLongExtra("original_number", 0)
                val root1 = incomingIntent.getIntExtra("root1", 0)
                val root2 = incomingIntent.getLongExtra("root2", 0)
                // check if number is prime or not and return the appropriate message
                val msg = if(root1==1) String.format("%d is a prime number", originalNum) else
                    String.format("The roots found for the number %d are: %d, %d",originalNum, root1, root2)
                Toast.makeText(myActivity, msg, Toast.LENGTH_LONG).show()
                finishCalculation(editTextUserInput, buttonCalculateRoots, progressBar)
            }
        }
        // register a broadcast-receiver to handle action "stopped_calculations"
        broadcastReceiverForFailure = object : BroadcastReceiver() {
            override fun onReceive(context: Context, incomingIntent: Intent) {
                if (incomingIntent.action != "stopped_calculations") return
                // success finding roots!
                val originalNum = incomingIntent.getLongExtra("original_number", 0)
                val secondsElapsed = incomingIntent.getLongExtra("time_until_give_up_seconds",
                        0)
                val msg = String.format("Couldn't find roots for the number %d," +
                        " calculation aborted after %d sec", originalNum, secondsElapsed)
                Toast.makeText(myActivity, msg, Toast.LENGTH_LONG).show()
                finishCalculation(editTextUserInput, buttonCalculateRoots, progressBar)
            }
        }
        registerReceiver(broadcastReceiverForSuccess, IntentFilter("found_roots"))
        registerReceiver(broadcastReceiverForFailure, IntentFilter("stopped_calculations"))

        /*
    todo:
     add a broadcast-receiver to listen for abort-calculating as defined in the spec (below)
     to show a Toast, use this code:
     `Toast.makeText(this, "text goes here", Toast.LENGTH_SHORT).show()`
     */
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(broadcastReceiverForSuccess)
        this.unregisterReceiver(broadcastReceiverForFailure)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // TODO: put relevant data into bundle as you see fit
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // TODO: load data from bundle and set screen state (see spec below)
    }
}