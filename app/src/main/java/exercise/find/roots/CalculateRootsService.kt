package exercise.find.roots

        import android.app.IntentService
                import android.content.Intent
                import android.util.Log
                import kotlin.math.ceil
                import kotlin.math.sqrt

        class CalculateRootsService : IntentService("CalculateRootsService") {
            private fun sendSuccessBroadcast(original_num: Long, root1: Int, root2: Long) {
                val broadcastIntent = Intent("found_roots")
                broadcastIntent.putExtra("original_number", original_num)
                broadcastIntent.putExtra("root1", root1)
                broadcastIntent.putExtra("root2", root2)
        sendBroadcast(broadcastIntent)
    }

    private fun sendFailureBroadcast(original_num: Long, timeDifference: Long) {
        val broadcastIntent = Intent("stopped_calculations")
        broadcastIntent.putExtra("original_number", original_num)
        broadcastIntent.putExtra("time_until_give_up_seconds", timeDifference / 1000)
        sendBroadcast(broadcastIntent)
    }

    public override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        val timeStartMs = System.currentTimeMillis()
        val numberToCalculateRootsFor = intent.getLongExtra("number_for_service", 0)
        if (numberToCalculateRootsFor <= 0) {
            Log.e("CalculateRootsService", "can't calculate roots for non-positive input$numberToCalculateRootsFor")
            return
        } else {
            val sqrt = ceil(sqrt(numberToCalculateRootsFor.toDouble()))
            for (i in 2..sqrt.toInt()) {
                if(numberToCalculateRootsFor % i == 0L){
                    sendSuccessBroadcast(numberToCalculateRootsFor, i, numberToCalculateRootsFor / i)
                    return
                }
                if(System.currentTimeMillis() - timeStartMs > 20000) {
                    sendFailureBroadcast(numberToCalculateRootsFor, System.currentTimeMillis()-timeStartMs)
                    return
                }
            }
            sendSuccessBroadcast(numberToCalculateRootsFor, 1, numberToCalculateRootsFor)  // in case number is prime
        }
    }
}