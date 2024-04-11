package com.example.indianarmyrkpuram

//import com.google.android.gms.vision.CameraSource
//import com.google.android.gms.vision.Detector
//import com.google.android.gms.vision.barcode.Barcode
//import com.google.android.gms.vision.barcode.BarcodeDetector

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.indianarmyrkpuram.databinding.ActivityMainBinding
import com.example.indianarmyrkpuram.utils.sharePreference.SharePref
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener


class MainActivity : AppCompatActivity() {
    private lateinit var snackbar: Snackbar
    lateinit var binding:ActivityMainBinding
    private val requestCodeCameraPermission = 1001
//    private lateinit var cameraSource: CameraSource
//    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    lateinit var iuhfService: IUHFService
    private var soundPool: SoundPool? = null
    private var soundId = 0
    private var soundId1 = 0
    var handler: Handler? = null
    var isSearchingStart = false
    var isCameraStart = false
    var compactor = ""
    var exCompactor = ""
    var exShelf = ""
    var exRoom = ""

    private var backPressedTime: Long = 0
    private val delayMillis = 2000 // 2 seconds


    lateinit var sharePref: SharePref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharePref = SharePref()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        try{
            iuhfService = UHFManager.getUHFService(this)
        } catch (e:Exception){
            e.printStackTrace()
        }
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setPrompt("Scan a barcode or QR Code")
        intentIntegrator.setOrientationLocked(false)
        binding.btnQrScanner.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                askForCameraPermission()
            } else {
               // binding.cameraSurfaceView.isVisible = false
                //binding.barcodeLine.isVisible = false
                //setupControls()

                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                intentIntegrator.initiateScan()

            }

            startAnimation()

            binding.tvLocateFile.text = "Align QR Code in visible area"
        }

        binding.imExit.setOnClickListener {
            OnExitButton()
        }




//        binding.btnSearch.setOnClickListener {
//            if (binding.etSearchRfidNo.text.toString().isNotEmpty()) {
//                if (isSearchingStart == false) {
//                    startSearchingByRFIDNO()
//
//
//                } else {
//                    stopSearching()
//                }
//            } else{
//                Snackbar.make(binding.root,"No Data for search", Snackbar.LENGTH_SHORT).show()
//            }
//
//        }








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
//                                    Log.d("rssiSound1",rssi.toString())
//                                    val redColor = Color.RED
//                                    val greenColor = Color.GREEN
//                                    val blendedColor = ColorUtils.blendARGB(redColor, greenColor, 0.5f)
//                                    binding.lottieAnimationView.setBackgroundColor(blendedColor)
                                    soundPool!!.play(soundId1, 1f, 1f, 0, 0, 3f)
                                } else {
//                                    Log.d("rssiSound2",rssi.toString())
//                                    val yellowColor = Color.YELLOW
//                                    val redColor = Color.RED
//                                    val blendedColor = ColorUtils.blendARGB(yellowColor, redColor, 0.5f)
//                                    binding.lottieAnimationView.setBackgroundColor(blendedColor)
                                    soundPool!!.play(soundId1, 0.6f, 0.6f, 0, 0, 2f)
                                }
                            } else {
                               // Log.d("rssiSound3",rssi.toString())
//                                val baseColor = Color.parseColor("#ff9a4e") // Your base color
//                                val yellowColor = Color.YELLOW // Yellow color
//
//                                // Mix the colors with a weight of 50% for each color
//                                val blendedColor = ColorUtils.blendARGB(baseColor, yellowColor, 0.5f)
                                //binding.lottieAnimationView.setBackgroundColor(blendedColor)
                                soundPool!!.play(soundId1, 0.3f, 0.3f, 0, 0, 1f)
                            }
                        } else{
//                        binding.gifImage.isVisible = true
//                        binding.mCardView.isVisible = false
                            val backgroundColor = ContextCompat.getColor(applicationContext, R.color.black)
                          binding.lottieAnimationView.setBackgroundColor(backgroundColor)
                        }
                    }
                }

            }
        }

    }





    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )

    }


    @SuppressLint("LongLogTag")
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
//                messageText.setText(intentResult.contents)
//                messageFormat.setText(intentResult.formatName)

                scannedValue = intentResult.contents
                runOnUiThread {

                    if (scannedValue.toString().isNotEmpty()){
                      //  binding.cameraSurfaceView.isVisible = false
                        //binding.barcodeLine.isVisible = false
//                           Input(scannedValue)
                        // getScannedValue(scannedValue)

                        val str = scannedValue
                        Log.d("scannedValue",scannedValue)
                        val values = str.splitToSequence('$').filter { it.isNotEmpty() }.toList()

                        if (values.size >= 4) {
                            val rfidNo = values[0]
                            binding.etSearchRfidNo.setText(rfidNo.toUpperCase())
                            exCompactor = values[2]
                            exRoom = values[1]
                            exShelf = values[3]
                            compactor = exCompactor

//                            if (sharePref.getData(Cons.RFIDNO)?.isNotEmpty() == true){
                                sharePref.clearAll()
//                            } else{
                                sharePref.saveData(Cons.RFIDNO,rfidNo)
                                sharePref.saveData(Cons.COMPACTOR,exCompactor)
                                sharePref.saveData(Cons.ROOM,exRoom)
                                sharePref.saveData(Cons.Shelf,exShelf)
                           // }
                            val intent = Intent(this, SearchingActivity::class.java)

//                            val bundle = Bundle()
//                            bundle.putString(Cons.RFIDNO, rfidNo.uppercase())
//                            bundle.putString(Cons.COMPACTOR, exCompactor)
//                            bundle.putString(Cons.ROOM, exRoom)
//                            bundle.putString(Cons.Shelf, exShelf)
//                            intent.putExtras(bundle)
                            startActivity(intent)

                        } else {
                            // binding.etSearchRfidNo.setText(scannedValue)
                            Snackbar.make(binding.root,scannedValue,Snackbar.LENGTH_SHORT).show()
                        }



                        binding.tvLocateFile.text = "Press trigger to start scan"
                       // stopAnimation()
                      //  isCameraStart=false

                    } else{
                        binding.tvLocateFile.text = "Scan QR again"

                        Toast.makeText(this@MainActivity, "value- $scannedValue", Toast.LENGTH_SHORT).show()


                    }

                }




                Log.d("inetentResult",intentResult.contents)
                Log.d("inetentResultFormateName",intentResult.formatName)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }



//    private fun setupControls() {
//        barcodeDetector =
//            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
//
//        cameraSource = CameraSource.Builder(this, barcodeDetector)
//            .setRequestedPreviewSize(1920, 1080)
//            .setAutoFocusEnabled(true) //you should add this feature
//            .build()
//
//        binding.cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
//            @SuppressLint("MissingPermission")
//            override fun surfaceCreated(holder: SurfaceHolder) {
//                try {
//                    //Start preview after 1s delay
//                    if (!isCameraStart) {
//                        isCameraStart =true
//                        cameraSource.start(holder)
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//
//            @SuppressLint("MissingPermission")
//            override fun surfaceChanged(
//                holder: SurfaceHolder,
//                format: Int,
//                width: Int,
//                height: Int
//            ) {
//                try {
//                    cameraSource.start(holder)
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//
//            override fun surfaceDestroyed(holder: SurfaceHolder) {
//                cameraSource.stop()
//                binding.cameraSurfaceView.isVisible = false
//                binding.barcodeLine.isVisible = false
//                stopAnimation()
//            }
//        })
//
//
//        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
//            override fun release() {
//               // showToast("Scanner has been closed")
//               // Log.d("ScannerClosed","Scanner has been closed")
//                Toast.makeText(applicationContext, "Scanner has been closed", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
//                var barcodes = detections.detectedItems
//                if (barcodes.size() == 1) {
//                    scannedValue = barcodes.valueAt(0).rawValue
//                    //Log.d("scannedValue",scannedValue)
//
//                   runOnUiThread {
//                        cameraSource.stop()
//
//                       if (scannedValue.toString().isNotEmpty()){
//                           binding.cameraSurfaceView.isVisible = false
//                           binding.barcodeLine.isVisible = false
////                           Input(scannedValue)
//                          // getScannedValue(scannedValue)
//
//                           val str = scannedValue
//                           val values = str.splitToSequence('$').filter { it.isNotEmpty() }.toList()
//
//                           if (values.size >= 4) {
//                               val rfidNo = values[0]
//                               binding.etSearchRfidNo.setText(rfidNo.toUpperCase())
//                                exCompactor = values[1]
//                                exRoom = values[2]
//                                exShelf = values[3]
//
//                           } else {
//                              // binding.etSearchRfidNo.setText(scannedValue)
//                               Snackbar.make(binding.root,scannedValue,Snackbar.LENGTH_SHORT).show()
//                           }
//
//
//
////                            exCompactor = extracte dValue.second
////                             exRoom = extractedValue.third
////                             exShelf = extractedValue.fourth
////                           showCustomSnackbar(extractedValue.first,exCompactor,exRoom,exShelf)
//                           binding.tvLocateFile.text = "Press trigger to start scan"
//                           stopAnimation()
//                           isCameraStart=false
//
//                       } else{
//                           binding.tvLocateFile.text = "Scan QR again"
//                          // barcodes = detections.detectedItems
//                           //setupControls()
//                           //startAnimation()
//                           Toast.makeText(this@MainActivity, "value- $scannedValue", Toast.LENGTH_SHORT).show()
//
//
//                       }
//                       // Toast.makeText(this@MainActivity, "value- $extractedValue", Toast.LENGTH_SHORT).show()
//                        //finish()
//                    }
//                }else
//                {
//                    //detections.detectedItems.clear()
//                    //Log.e("value- else","value- else")
//                   // showToast("value- else")
//                   // Toast.makeText(this@MainActivity, "value- else", Toast.LENGTH_SHORT).show()
//
//                }
//            }
//        })
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
              //  setupControls()
            } else {
                //showToast("Permission Denied")

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
       // cameraSource.stop()
    }



    fun startAnimation() {
        val aniSlide: Animation =
            AnimationUtils.loadAnimation(this@MainActivity, R.anim.scanner_animation)
       // binding.barcodeLine.startAnimation(aniSlide)
    }

    fun stopAnimation() {
       // binding.barcodeLine.clearAnimation()

    }


    @SuppressLint("ResourceAsColor", "SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_BUTTON_R2) {
            if (binding.etSearchRfidNo.text.toString().isNotEmpty()) {
                if (sharePref.getData(Cons.COMPACTOR)?.isNotEmpty() == true) {

                    val intent = Intent(this, SearchingActivity::class.java)
                   val bundle = Bundle()
                    bundle.putString(Cons.RFIDNO, binding.etSearchRfidNo.text.toString().uppercase())
                    bundle.putString(Cons.COMPACTOR, exCompactor)
                    bundle.putString(Cons.ROOM, exRoom)
                    bundle.putString(Cons.Shelf, exShelf)
                    bundle.putBoolean(Cons.isCommingFrom,true)
                    intent.putExtras(bundle)
                    startActivity(intent)
                } else{
                    Snackbar.make(binding.root,"Please scan QR or type RFID number to proceed", Snackbar.LENGTH_SHORT).show()
                }
//                if (isSearchingStart == false) {
//                   // startSearchingByRFIDNO()
//
//
//
//
//                } else {
//                    stopSearching()
//                }
            } else{
                Snackbar.make(binding.root,"Please scan QR or type RFID number to proceed", Snackbar.LENGTH_SHORT).show()
            }

            return true
        } else{
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - backPressedTime < delayMillis) {
                Log.d("onKeyDown", "Exiting the app")
                sharePref.clearAll()
                finish()
            } else {
                Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
                Log.d("onKeyDown", "Press again to exit")
            }
            return true
        }
        }


        return super.onKeyUp(keyCode, event)
    }


     fun OnExitButton() {


        val dialogView = layoutInflater.inflate(R.layout.quite_dialog, null)
        val builder = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val dialog = builder.create()
        val btnYEs = dialog.findViewById<Button>(R.id.btn_yes)
        val btnNo = dialog.findViewById<Button>(R.id.btn_no)
        btnYEs.setOnClickListener {
            if (isSearchingStart){
                stopSearching()
            }
            finish()

        }

        btnNo.setOnClickListener {
            dialog.dismiss()
            // Handle any other action you want
        }

        dialog.show()
    }



    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isSearchingStart = true
        initSoundPool()
        runOnUiThread(kotlinx.coroutines.Runnable {
            showCustomSnackbar(binding.etSearchRfidNo.text.toString().toUpperCase(),exCompactor,exRoom,exShelf)

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
            isSearchingStart = false


        } catch (e:Exception){
            e.printStackTrace()
        }finally {
            iuhfService.inventoryStop()
            iuhfService.closeDev()
            hideSnackbar()
        }


        runOnUiThread(kotlinx.coroutines.Runnable {
//            binding.gifImage.isVisible = false
//            binding.imBook.isVisible = true
            binding.btnQrScanner.isVisible =  true
            binding.etSearchRfidNo.isVisible = true
            binding.imLocate.isVisible = true
            binding.lottieAnimationView.isVisible = false
            binding.tvLocateFile.isVisible = true
            binding.tvTitle.setText(R.string.file_tracking_system)
            binding.tvLocateFile.text = "Locate your file"
            //binding.btnSearch.text = "Search"

            //binding.btnSearch.setBackgroundColor(ContextCompat.getColor(this,R.color.green2))
        })


    }


//    override fun onBackPressed() {
//        if (System.currentTimeMillis() - backPressedTime < delayMillis) {
//            super.onBackPressed()
//        } else {
//            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
//            backPressedTime = System.currentTimeMillis()
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

    fun Activity.hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
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
                iuhfService.inventoryStart()




            }
        })
    }




    fun extractComponents(input: String): ExtractionResult {
        val regex = """(\d+)(\$[A-Z]\d*)""".toRegex()
        val matchResult = regex.find(input)

        return if (matchResult != null) {
            val (numbers, coordinates) = matchResult.destructured
            val cleanedNumbers = numbers.replace("[^\\d]".toRegex(), "") // Remove non-numeric characters

            // Set default values
            var compactor: String? = null
            var room: String? = null
            var shelf: String? = null

            coordinates.split("\\$").forEach { coordinate ->
                when {
                    coordinate.startsWith("C") -> compactor = coordinate
                    coordinate.startsWith("R") -> room = coordinate
                    coordinate.startsWith("S") -> shelf = coordinate
                    // Add more cases if needed
                }
            }

            ExtractionResult.Success(cleanedNumbers, compactor, room, shelf)
        } else {
            ExtractionResult.Failure("Invalid input format")
        }
    }





//    fun showSnackBar(){
//        val snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
//        val customSnackView: View = layoutInflater.inflate(R.layout.custome_snackbar, null)
//
//        // set the background of the default snackbar as transparent
//        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
//
//        // now change the layout of the snackbar
//        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
//
//        // set padding of the all corners as 0
//        snackbarLayout.setPadding(0, 0, 0, 0)
//    }



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
