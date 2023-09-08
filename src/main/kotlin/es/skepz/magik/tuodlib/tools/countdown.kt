package es.skepz.magik.tuodlib.tools

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/***
 * A more lightweight version of the Countdown class, with only the features needed for a simple countdown
 * @param plugin: the plugin to run the task on
 * @param count: the function to be run every second
 * @param time: the amount of time to run
 ***/
class LightCountdown(private val plugin: JavaPlugin, private val count: (x: Int) -> Unit, private var time: Int) {
    private var task: BukkitTask? = null // the task
    private var timer = 0 // the timer that decrements

    /***
     * run the countdown
     ***/
    fun start() {
        timer = time // reset the timer (for multiple uses)
        task = object : BukkitRunnable() { // make the runnable
            override fun run() { // run function
                count(timer) // run the count function
                if (timer-- <= 0) cancel() // if the timer is done, stop the timer
            }
        }.runTaskTimer(plugin, 0L, 20L) // schedule the repeating task with a 1 second delay
    }

    /***
     * cancel the countdown
     ***/
    fun cancel() {
        if (task != null) task!!.cancel()
    }

}

/***
 * Schedule a simple countdown timer (LEGACY)
 * @param plugin: the plugin to run the task from
 * @param count: The function that will be run every second
 * @param time: how many seconds to count down from
 ***/
fun simpleCountdown(plugin: JavaPlugin, count: (x: Int) -> Unit, time: Int): LightCountdown {
    val scd = LightCountdown(plugin, count, time)
    scd.start()
    return scd
}

/***
 * create a countdown for a specified amount of time, executing a task every second
 * @param plugin: the plugin to run the task from
 * @param count: the function that will be run every second (takes an integer of what the current seconds left are)
 * @param time: how many seconds the countdown should run for
 * @return returns the Countdown object which can be canceled with .cancel() and restarted with .start()
 ***/
fun countdown(plugin: JavaPlugin, count: (secondsLeft: Int) -> Unit, time: Int): Countdown {
    val cd = Countdown(plugin, count, time)
    cd.start()
    return cd
}

/***
 * create a countdown for a specified amount of time, executing a task every second
 * @param plugin: the plugin to run the task from
 * @param count: the function that will be run every second (takes an integer of what the current seconds left are)
 * @param beforeTimer: the function that will run before the timer starts
 * @param afterTimer: the function to run after the countdown is complete
 * @param time: how many seconds the countdown should run for
 * @return returns the Countdown object which can be canceled with .cancel() and restarted with .start()
 ***/
fun countdown(plugin: JavaPlugin, count: (secondsLeft: Int) -> Unit, beforeTimer: () -> Unit, afterTimer: () -> Unit, time: Int): Countdown {
    val cd = Countdown(plugin, count, beforeTimer, afterTimer, time)
    cd.start()
    return cd
}

/***
 * create a countdown for a specified amount of time, executing a task every second
 * @param plugin: the plugin to run the task from
 * @param count: the function that will be run every second (takes an integer of what the current seconds left are)
 * @param beforeTimer: the function that will run before the timer starts
 * @param beforeTimerDelay: the delay after the beginning function until the countdown starts
 * @param afterTimer: the function to run after the countdown is complete
 * @param time: how many seconds the countdown should run for
 * @return returns the Countdown object which can be canceled with .cancel() and restarted with .start()
 ***/
fun countdown(plugin: JavaPlugin, count: (secondsLeft: Int) -> Unit, beforeTimer: () -> Unit, beforeTimerDelay: Int, afterTimer: () -> Unit, time: Int): Countdown {
    val cd = Countdown(plugin, count, beforeTimer, beforeTimerDelay, afterTimer, time)
    cd.start()
    return cd
}

/***
 * create a countdown for a specified amount of time, executing a task every second
 * @param plugin: the plugin to run the task from
 * @param count: the function that will be run every second (takes an integer of what the current seconds left are)
 * @param beforeTimer: the function that will run before the timer starts
 * @param beforeTimerDelay: the delay after the beginning function until the countdown starts
 * @param time: how many seconds the countdown should run for
 * @return returns the Countdown object which can be canceled with .cancel() and restarted with .start()
 ***/
fun countdown(plugin: JavaPlugin, count: (secondsLeft: Int) -> Unit, beforeTimer: () -> Unit, beforeTimerDelay: Int, time: Int): Countdown {
    val cd = Countdown(plugin, count, beforeTimer, beforeTimerDelay, time)
    cd.start()
    return cd
}

/***
 * create a countdown for a specified amount of time, executing a task every second
 * @param plugin: the plugin to run the task from
 * @param count: the function that will be run every second (takes an integer of what the current seconds left are)
 * @param afterTimer: the function to run after the countdown is complete
 * @param time: how many seconds the countdown should run for
 * @return returns the Countdown object which can be canceled with .cancel() and restarted with .start()
 ***/
fun countdown(plugin: JavaPlugin, count: (secondsLeft: Int) -> Unit, afterTimer: () -> Unit, time: Int): Countdown {
    val cd = Countdown(plugin, count, afterTimer, time)
    cd.start()
    return cd
}

/***
 * @param plugin: The plugin
 * @param count: the function that will be run every second
 * @param seconds: how long the timer should be in seconds
 ***/
class Countdown(private val plugin: JavaPlugin, val count: (timeLeft: Int) -> Unit, private val seconds: Int) : Runnable {

    private var bt: () -> Unit = {} // before timer function
    private var at: () -> Unit = {} // after timer function
    private var btd = 0L

    /***
     * @param plugin: The plugin
     * @param beforeTimer: the runnable that will execute at the start of the timer
     * @param beforeTimerDelay: the delay after beforeTimer() is run until the countdown starts
     * @param count: the function that will be run every second
     * @param seconds: how long the timer should be in seconds
     ***/
    constructor(plugin: JavaPlugin, count: (timeLeft: Int) -> Unit, beforeTimer: () -> Unit, beforeTimerDelay: Int, seconds: Int) : this(plugin, count, seconds) {
        bt = beforeTimer
        btd = (beforeTimerDelay * 20).toLong()
    }
    /***
     * @param plugin: The plugin
     * @param count: the function that will be run every second
     * @param afterTimer: the runnable that will execute when the timer finishes
     * @param seconds: how long the timer should be in seconds
     ***/
    constructor(plugin: JavaPlugin, count: (timeLeft: Int) -> Unit, afterTimer: () -> Unit, seconds: Int) : this(plugin, count, seconds) {
        at = afterTimer
    }
    /***
     * @param plugin: The plugin
     * @param beforeTimer: the runnable that will execute at the start of the timer
     * @param count: the function that will be run every second
     * @param afterTimer: the runnable that will execute when the timer finishes
     * @param seconds: how long the timer should be in seconds
     ***/
    constructor(plugin: JavaPlugin, count: (timeLeft: Int) -> Unit, beforeTimer: () -> Unit, afterTimer: () -> Unit, seconds: Int) : this(plugin, count, seconds) {
        bt = beforeTimer
        at = afterTimer
    }
    /***
     * @param plugin: The plugin
     * @param beforeTimer: the runnable that will execute at the start of the timer
     * @param beforeTimerDelay: the delay after beforeTimer() is run until the countdown starts
     * @param count: the function that will be run every second
     * @param afterTimer: the runnable that will execute when the timer finishes
     * @param seconds: how long the timer should be in seconds
     ***/
    constructor(plugin: JavaPlugin, count: (timeLeft: Int) -> Unit, beforeTimer: () -> Unit, beforeTimerDelay: Int, afterTimer: () -> Unit, seconds: Int) : this(plugin, count, seconds) {
        bt = beforeTimer
        at = afterTimer
        btd = (beforeTimerDelay * 20).toLong()
    }

    private var taskID: Int? = -1 // task id
    private var secondsLeft = seconds // how many seconds are left
    var isRunning = false // is the task running

    private var shouldCancel = false // should the task be canceled

    /***
     * the task run function
     ***/
    override fun run() {
        isRunning = true // set the status to running
        if (shouldCancel) { // if a cancel request has been sent
            cancel() // force a stop
            shouldCancel = false // reset variables
            return
        }
        if (secondsLeft < 1) { // should the task end?
            at() // run the after function
            cancel() // force a stop
            shouldCancel = false // reset variables
            isRunning = false // reset variables
            return // return
        }
        // run the count function passed by the user
        count(secondsLeft--) // run the main function and decrement the timer
    }

    /***
     * begin the countdown
     ***/
    fun start() {
        // ensure that variables are reset
        secondsLeft = seconds
        isRunning = false
        shouldCancel = false
        bt()
        // run the countdown
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, { taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 20L) }, btd)
    }

    /***
     * Cancel the current countdown and end the task
     ***/
    fun cancel() {
        if (!isRunning) return
        if (taskID != null && taskID != -1) Bukkit.getScheduler().cancelTask(taskID!!) // cancel the task
        taskID = -1
        isRunning = false
    }

}