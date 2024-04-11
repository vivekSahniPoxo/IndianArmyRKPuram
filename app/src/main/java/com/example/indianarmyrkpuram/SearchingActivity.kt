package com.example.indianarmyrkpuram

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.indianarmyrkpuram.databinding.ActivitySearchingBinding
import com.example.indianarmyrkpuram.utils.sharePreference.SharePref
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener

class SearchingActivity : AppCompatActivity() {

    companion object {
        private const val PRESS_BACK_INTERVAL = 2000 // Interval in milliseconds
    }

    private var lastBackPressTime: Long = 0
    lateinit var binding:ActivitySearchingBinding
    lateinit var iuhfService: IUHFService
    var isSearchingStart = false
    private var soundPool: SoundPool? = null
    private var soundId = 0
    private var soundId1 = 0
    var handler: Handler? = null

    var rfidNo = ""
    var compactor = ""
    var shelf = ""
    var room = ""

    private var backPressedTime: Long = 0
    private val delayMillis = 2000 // 2 seconds
    var isCommingFrom = false
    lateinit var sharePref: SharePref
    private lateinit var snackbar: Snackbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharePref = SharePref()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        try {

            rfidNo = sharePref.getData(Cons.RFIDNO).toString()
            binding.etSearchRfidNo.setText(rfidNo.uppercase())
             compactor =  sharePref?.getData(Cons.COMPACTOR).toString()
             room =  sharePref?.getData(Cons.ROOM).toString()
            shelf =  sharePref?.getData(Cons.Shelf).toString()
            val bundle = intent.extras
//             rfidNo = bundle?.getString(Cons.RFIDNO).toString()
//             binding.etSearchRfidNo.setText(rfidNo.uppercase())
//             compactor =  bundle?.getString(Cons.COMPACTOR).toString()
//             room =  bundle?.getString(Cons.ROOM).toString()
//            shelf =  bundle?.getString(Cons.Shelf).toString()
            isCommingFrom = bundle?.getBoolean(Cons.isCommingFrom) == true
        } catch (e:Exception){
            Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()
        }

        if (rfidNo.isNotEmpty() && isCommingFrom){
            startSearchingByRFIDNO()
        }


        try{
            iuhfService = UHFManager.getUHFService(this)
        } catch (e:Exception){
            e.printStackTrace()
        }




        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == 1 && isSearchingStart==true) {
                    if (!TextUtils.isEmpty(binding.etSearchRfidNo.text.toString().trim())) {
                        val spdInventoryData = msg.obj as SpdInventoryData
                        val epc = spdInventoryData.getEpc()
                        //Log.d("wkfpkp",epc)
                        if (epc == binding.etSearchRfidNo.text.toString().toUpperCase().trim()) {
                            // binding.mCardView.setBackgroundColor(ContextCompat.getColor(this@SearchActivity, R.color.green2))
                            val rssi = spdInventoryData.getRssi().toInt()
                            Log.d("rssi",rssi.toString())
                            val i = -60
                            val j = -40
                            if (rssi > i) {
                                if (rssi > j) {
                                    soundPool!!.play(soundId1, 1f, 1f, 0, 0, 3f)
                                } else {
                                    soundPool!!.play(soundId1, 0.6f, 0.6f, 0, 0, 2f)
                                }
                            } else {
                                // Log.d("rssiSound3",rssi.toString())

                                soundPool!!.play(soundId1, 0.3f, 0.3f, 0, 0, 1f)
                            }
                        } else{
                            val backgroundColor = ContextCompat.getColor(applicationContext, R.color.black)
                            binding.lottieAnimationView.setBackgroundColor(backgroundColor)
                        }
                    }
                }

            }
        }

    }



    fun startSearchingByRFIDNO(){
        startSearching()
        iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
            override fun getInventoryData(var1: SpdInventoryData) {
                handler?.sendMessage(handler!!.obtainMessage(1, var1))
                Log.d("as3992_6C", "id is $soundId")
            }

            override fun onInventoryStatus(status: Int) {

            }
        })
    }



    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isSearchingStart = true
        try {
            initSoundPool()
        } catch (e:Exception){
            Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()
        }
        runOnUiThread(kotlinx.coroutines.Runnable {
            showCustomSnackbar(binding.etSearchRfidNo.text.toString().toUpperCase(),compactor,room,shelf)

            binding.imLocate.isVisible = false
            binding.btnQrScanner.isVisible =  false
            binding.etSearchRfidNo.isVisible = false
            binding.tvLocateFile.isVisible = false
            binding.tvTitle.text = "Searching in progress"

            binding.lottieAnimationView.isVisible = true
            val animationView = binding.lottieAnimationView
            animationView.addAnimatorUpdateListener { animation: ValueAnimator? -> }
            animationView.playAnimation()

        })

        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30
            iuhfService.inventoryStart()

        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }


    }

    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        try {
            soundPool!!.release()

        } catch (e:Exception){
            e.printStackTrace()
        }finally {
            iuhfService.inventoryStop()
            iuhfService.closeDev()
            hideSnackbar()
            isSearchingStart = false
        }



        runOnUiThread(kotlinx.coroutines.Runnable {
            binding.btnQrScanner.isVisible =  true
            binding.etSearchRfidNo.isVisible = true
            binding.imLocate.isVisible = true
            binding.lottieAnimationView.isVisible = false
            binding.tvLocateFile.isVisible = true
            binding.tvTitle.setText(R.string.file_tracking_system)
            binding.tvLocateFile.text = "Locate your file"
            //binding.btnSearch.text = "Search"

            //binding.btnSearch.setBackgroundColor(ContextCompat.getColor(this,R.color.green2))

           // finish()
        })


    }

    @SuppressLint("ResourceAsColor")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_BUTTON_R2) {
            if (binding.etSearchRfidNo.text.toString().isNotEmpty()) {
                if (isSearchingStart == false) {
                    startSearchingByRFIDNO()


                } else {
                    stopSearching()
                }
            } else{
                Snackbar.make(binding.root,"Please scan QR or type RFID number to proceed", Snackbar.LENGTH_SHORT).show()
            }

            return true
        }
        else{
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish()
//                val currentTime = System.currentTimeMillis()
//                if (currentTime - lastBackPressTime < PRESS_BACK_INTERVAL) {
//                    super.onBackPressed() // Close the app directly
//                } else {
//                    Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show() // Prompt to press again to exit
//                    lastBackPressTime = currentTime
//                }
//                if (System.currentTimeMillis() - backPressedTime < delayMillis) {
//                    Log.d("onKeyDown", "Exiting the app")
//                    finish()
//                } else {
//                    Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
//                    backPressedTime = System.currentTimeMillis()
//                    Log.d("onKeyDown", "Press again to exit")
//                }
                return true
            }
        }


        return super.onKeyUp(keyCode, event)
    }


//    override fun onBackPressed() {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastBackPressTime < PRESS_BACK_INTERVAL) {
//            super.onBackPressed() // Close the app directly
//        } else {
//            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show() // Prompt to press again to exit
//            lastBackPressTime = currentTime
//        }
//    }


    fun initSoundPool() {
        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        soundId = soundPool!!.load("/system/media/audio/ui/VideoRecord.ogg", 0)
        Log.w("as3992_6C", "id is $soundId")
        soundId1 = soundPool!!.load(this, R.raw.scankey, 0)

    }
    override fun onPause() {
        super.onPause()
        if (isSearchingStart==true){
            stopSearching()
        }
        iuhfService.closeDev()
    }
    override fun onStop() {
        super.onStop()
        Log.w("stop", "im stopping")
        try {
            soundPool!!.release()
            iuhfService.inventoryStop()
            // super.onStop()
        } catch (e:Exception){
            Log.d("eee",e.toString())
        }
    }




    @SuppressLint("MissingInflatedId")
    private fun showCustomSnackbar(rfidNo: String, compactor: String,room:String,shelf:String) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(this)
        val customLayout = inflater.inflate(R.layout.custome_snackbar, null)

        // Find TextViews in the custom layout
        val textView1: TextView = customLayout.findViewById(R.id.tv_rfid_Number)
        val textView2: TextView = customLayout.findViewById(R.id.tv_compactor)

        val roomTXt: TextView = customLayout.findViewById(R.id.tv_room_no)
        val shelfTxt: TextView = customLayout.findViewById(R.id.tv_shelfNo)

        // Set text for TextViews
        textView1.text = rfidNo
        textView2.text = compactor
        roomTXt.text = room
        shelfTxt.text = shelf


        // Create the custom Snackbar with the custom layout
        snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            "",
            Snackbar.LENGTH_INDEFINITE
        )

        // Customize your Snackbar here if needed
        // For example, change the background color
        snackbar.view.setBackgroundColor(resources.getColor(R.color.blue))

        // Add a "Hide" action to the Snackbar
        snackbar.setAction("Hide") {
            hideSnackbar()
        }

        // Set the custom layout to the Snackbar
        val snackbarView = snackbar.view as Snackbar.SnackbarLayout
        snackbarView.removeAllViews() // Remove default layout
        snackbarView.addView(customLayout)

        // Show the Snackbar
        snackbar.show()
    }

    private fun hideSnackbar() {
        // Check if the Snackbar is showing before trying to dismiss it
        if (snackbar.isShownOrQueued) {
            snackbar.dismiss()
        }
    }

}