package com.example.clapapp.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.example.clapapp.R
import com.example.clapapp.adapters.issend
import com.example.clapapp.count
import com.example.clapapp.databinding.ActivityMainBinding
import com.example.clapapp.isactive
import com.example.clapapp.ispurchased
import com.example.clapapp.key1
import com.example.clapapp.service.SoundBlinkService
import com.fondesa.kpermissions.PermissionStatus
import com.fondesa.kpermissions.allDenied
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.anyPermanentlyDenied
import com.fondesa.kpermissions.anyShouldShowRationale
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.PermissionRequest
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.water.soak.SteamHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

var isBlinking = false
var MP3_RESOURCE_ID = 0

class MainActivity : AppCompatActivity(), PermissionRequest.Listener {
    private val request by lazy {
        permissionsBuilder(
            Manifest.permission.RECORD_AUDIO,
        ).build()
    }
    private var mInterstitialAd: InterstitialAd? = null

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var cameraManager: CameraManager
    private var billingClient: BillingClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseAnalytics.getInstance(this)

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)



        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { billingResult: BillingResult, list: List<Purchase?>? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        Log.d(ContentValues.TAG, "Response Ok")
                        if (purchase != null) {
                            HnadlePurchased(purchase)
                        }
                    }
                } else {
                    Log.d(ContentValues.TAG, "Not Response Ok")
                }
            }.build()
        isConnection()

        binding.inAppPurchase.setOnClickListener {
            GetSingleConsumeable()
        }
        if (issend) {
            val data = intent.getIntExtra("dataKey", 0)
            MP3_RESOURCE_ID = data
            issend = false
        } else {
            MP3_RESOURCE_ID = R.raw.ringtone1
        }

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        if (isactive) {
            binding.ivApply.setImageResource(R.drawable.deactive)
        } else {
            binding.ivApply.setImageResource(R.drawable.active)
        }

        binding.ivApply.setOnClickListener {
            showInterstitialAd()
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                request.addListener(this)
                request.send()
            } else {

                if (isBlinking) {
                    if (MP3_RESOURCE_ID == 0) {
                        Toast.makeText(this, "Select ringtone first!", Toast.LENGTH_SHORT).show()
                    } else {
                        if (!isactive) {
                            startService()
                            isactive = true
                            binding.ivApply.setImageResource(R.drawable.deactive)
                            binding.textView.text = "DEACTIVE"
                            Toast.makeText(this, "Activated", Toast.LENGTH_SHORT).show()
                        } else {
                            stopService()
                            binding.ivApply.setImageResource(R.drawable.active)
                            binding.textView.text = "ACTIVE"
                            Toast.makeText(this, "Disabled", Toast.LENGTH_SHORT).show()
                            isactive = false
                        }
                    }
                } else {
                    if (MP3_RESOURCE_ID == 0) {
                        Toast.makeText(this, "Select ringtone first!", Toast.LENGTH_SHORT).show()
                    } else {
                        if (!isactive) {
                            startService()
                            isactive = true
                            binding.ivApply.setImageResource(R.drawable.deactive)
                            binding.textView.text = "DEACTIVE"
                            Toast.makeText(this, "Activated", Toast.LENGTH_SHORT).show()
                        } else {
                            stopService()
                            binding.ivApply.setImageResource(R.drawable.active)
                            binding.textView.text = "ACTIVE"
                            Toast.makeText(this, "Disabled", Toast.LENGTH_SHORT).show()
                            isactive = false

                        }
                    }
                }

            }

        }


        initExitDialog()

        binding.ivFlashlight.setOnClickListener {
            showInterstitialAd()
            showBottomSheet()
        }
        binding.ivRingtones.setOnClickListener {
            count++
            startActivity(Intent(this, RingtoneActivity::class.java))
        }
        binding.toggle1.setOnClickListener {
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) binding.drawerLayout.openDrawer(
                GravityCompat.START
            ) else binding.drawerLayout.closeDrawer(
                GravityCompat.END
            )
        }

        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, R.string.open, R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.layoutAd.setOnClickListener {
            if (SteamHelper.urlApp.isBlank()) {
                return@setOnClickListener
            }
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(SteamHelper.urlApp))
            startActivity(i)
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.rate -> {
                    val rateIntent = Intent(
                        Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.packageName)
                    )
                    startActivity(rateIntent)
                }

                R.id.share -> {
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        "https://play.google.com/store/apps/details?id=" + this.packageName
                    )
                    sendIntent.type = "text/plain"
                    startActivity(sendIntent)
                }

                R.id.moreapps -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("https://play.google.com/store/apps/developer?id=Dax+Apps")
                    startActivity(intent)
                }

                R.id.exit -> {
                    finishAffinity()
                }
            }
            true
        }
    }

    private var num = 0

    fun showInterstitialAd() {
        if (num > 5) return
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading Ads...")
        progressDialog.setCancelable(false)

        val adRequest2: AdRequest = AdRequest.Builder().build()

        progressDialog.show()
        InterstitialAd.load(this,
            resources.getString(R.string.interstitial),
            adRequest2,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    progressDialog.dismiss()
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    progressDialog.dismiss()
                    if (!ispurchased) {
                        num++
                        interstitialAd.show(this@MainActivity)
                    } else {
                        Log.d("BANNER_LC_2", "Can not show ad.")
                    }
                }
            })
    }

    private fun startService() {
        val serviceIntent = Intent(this, SoundBlinkService::class.java)
        startService(serviceIntent)
    }

    private fun stopService() {
        val serviceIntent = Intent(this, SoundBlinkService::class.java)
        stopService(serviceIntent)
    }

    private fun showBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layou, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        val radioGroup: RadioGroup = bottomSheetView.findViewById(R.id.radioGroup)
        val radioNormalFlash: RadioButton = bottomSheetView.findViewById(R.id.radioNormalFlash)
        val radioBlinkingFlash: RadioButton = bottomSheetView.findViewById(R.id.radioBlinkingFlash)
        val buttonApply: Button = bottomSheetView.findViewById(R.id.buttonApply)

        // Set up the click listener for the "Apply" button in the BottomSheet
        buttonApply.setOnClickListener {
            isBlinking = radioBlinkingFlash.isChecked
            bottomSheetDialog.dismiss()
        }

        // Show the BottomSheet
        bottomSheetDialog.show()
    }

    var dialog: BottomSheetDialog? = null

    private fun initExitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_exit, null)
        dialog = BottomSheetDialog(this)
        dialog!!.setContentView(dialogView)
        val exitButton = dialogView.findViewById<TextView>(R.id.exitButton)
        exitButton.setOnClickListener {
            dialog!!.dismiss()
            finishAffinity()
        }
    }

    override fun onBackPressed() {
        dialog?.show()
    }

    private fun HnadlePurchased(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            billingClient?.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) { billingResult ->
                if (billingResult.getResponseCode() === BillingClient.BillingResponseCode.OK) {
                    for (purchased in purchase.products) {
                        if (purchased.equals(
                                "remove_ads", ignoreCase = true
                            )
                        ) {
                            ispurchased = true
                            saveBooleanToSharedPreferences1(this, key1, ispurchased)
                            Log.d(ContentValues.TAG, "Purchased Successful")
                            ConsumePurchased(purchase)
                            Non_consumed(purchase)
                        }
                    }
                }
            }
        }
    }

    private fun saveBooleanToSharedPreferences1(context: Context, key: String, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences("MyAppPurchase", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    private fun Non_consumed(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val purchaseParams =
                    AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                        .build()
                billingClient?.acknowledgePurchase(purchaseParams) { billingResult ->
                    Log.d(
                        ContentValues.TAG, "Consumed Successful$billingResult"
                    )
                }
            }
        }
    }

    private fun ConsumePurchased(purchase: Purchase) {
        val consumeParams =
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        val listener = ConsumeResponseListener { billingResult: BillingResult?, s: String ->
            Log.d(
                ContentValues.TAG, "Consumed Successful$s"
            )
        }
        billingClient?.consumeAsync(consumeParams, listener)

    }

    private fun GetSingleConsumeable() {
        val productArrayList = ArrayList<QueryProductDetailsParams.Product>()
        //Set your In App Product ID in setProductId()
        productArrayList.add(
            QueryProductDetailsParams.Product.newBuilder().setProductId("remove_ads")
                .setProductType(BillingClient.ProductType.INAPP).build()
        )
        val productDetailsParams =
            QueryProductDetailsParams.newBuilder().setProductList(productArrayList).build()
        billingClient?.queryProductDetailsAsync(productDetailsParams) { billingResult, list ->
            LaunchPurchaseFlow(list.get(0))
        }
    }

    private fun LaunchPurchaseFlow(productDetails: ProductDetails) {
        val paramsArrayList = ArrayList<BillingFlowParams.ProductDetailsParams>()
        paramsArrayList.add(
            BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams =
            BillingFlowParams.newBuilder().setProductDetailsParamsList(paramsArrayList).build()
        billingClient?.launchBillingFlow(this, billingFlowParams)
    }


    private fun isConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.d(ContentValues.TAG, "Connection Not Granted")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    Log.d(ContentValues.TAG, "Connection Granted")
                }
            }
        })
    }

    override fun onPermissionsResult(result: List<PermissionStatus>) {
        when {
            result.anyPermanentlyDenied() -> showPermanentlyDeniedDialog()
            result.anyShouldShowRationale() -> showRationaleDialog(request)
            result.allGranted() -> {
            }

            result.allDenied() -> {
            }
        }
    }

    private fun showRationaleDialog(request: PermissionRequest) {
        android.app.AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("Permission is required to proceed")
            .setPositiveButton("Request Again") { _, _ ->
                // Send the request again.
                request.send()
            }.setNegativeButton(android.R.string.cancel) { _, _ ->
            }.show()
    }

    private fun showPermanentlyDeniedDialog() {
        android.app.AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("permission is required to proceed")
            .setPositiveButton("Open Settings") { _, _ ->
                // Open the app's settings.
                val intent = createAppSettingsIntent()
                startActivity(intent)
            }.setNegativeButton(android.R.string.cancel) { _, _ ->
            }.show()
    }

    private fun createAppSettingsIntent() = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", baseContext.packageName, null)
    }

    private var job: Job? = null
    override fun onResume() {
        super.onResume()
        job?.cancel()
        job = lifecycleScope.launch {
            while (SteamHelper.urlApp.isBlank()) {
                delay(1800)
            }
            binding.layoutAd.visibility = View.VISIBLE
        }
    }
}