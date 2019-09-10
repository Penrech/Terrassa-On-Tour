package parcaudiovisual.terrassaontour.utils

class VideoUtility {

    fun formatSecondsToString(inMilis: Int): String{

        val inSeconds = inMilis / 1000
        val minutes: Int = inSeconds / 60
        val seconds: Int = inSeconds % 60

        val minutesString: String
        val seconsString: String

        minutesString = if (minutes < 10){
            "0$minutes"
        } else {
            "$minutes"
        }
        seconsString = if (seconds < 10){
            "0$seconds"
        } else {
            "$seconds"
        }

        return "$minutesString:$seconsString"
    }

    fun getProgressPercentage(currentDuration: Int, totalDuration: Int): Int{
        val percentage: Double?

        val currentSeconds = (currentDuration / 1000)
        val totalSeconds = (totalDuration / 1000)

        // calculating percentage
        percentage = currentSeconds.toDouble() / totalSeconds * 100

        // return percentage
        return percentage.toInt()
    }

    fun progressToTimer(progress: Int, totalDuration: Int): Int {
        var totalDuration = totalDuration
        var currentDuration = 0
        totalDuration /= 1000
        currentDuration = (progress.toDouble() / 100 * totalDuration).toInt()

        // return current duration in milliseconds
        return currentDuration * 1000
    }
}