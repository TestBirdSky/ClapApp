package com.example.clapapp.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clapapp.R
import com.example.clapapp.adapters.RingtoneAdapter
import com.example.clapapp.adapters.mediaPlayer
import com.example.clapapp.count
import com.example.clapapp.databinding.ActivityRingtoneBinding
import com.example.clapapp.dataclass.DataClass
import com.example.clapapp.ispurchased
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class RingtoneActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRingtoneBinding
    val list = listOf(
        DataClass(1, "Ringtone # 1 ", R.raw.ringtone1, false),
        DataClass(2, "Ringtone # 2 ", R.raw.ringtone2, false),
        DataClass(3, "Ringtone # 3 ", R.raw.ringtone3, false),
        DataClass(4, "Ringtone # 4 ", R.raw.ringtone4, false),
        DataClass(5, "Ringtone # 5 ", R.raw.ringtone5, false),

    )
    private var mInterstitialAd2: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ringtone)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ringtone)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = RingtoneAdapter(this,list)

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading Ads...")
        progressDialog.setCancelable(false)

        val adRequest2: AdRequest = AdRequest.Builder().build()
            progressDialog.show()
//            InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712",
            InterstitialAd.load(this, resources.getString(R.string.interstitial),
                adRequest2, object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        progressDialog.dismiss()
                        mInterstitialAd2 = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd2 = interstitialAd
                        progressDialog.dismiss()
                        if (!ispurchased) {
                            interstitialAd.show(this@RingtoneActivity)
                        } else {
                            Log.d("BANNER_LC_2", "Can not show ad.")
                        }


                    }
                })

        if (!ispurchased) {
            val adRequest: AdRequest = AdRequest.Builder().build()
            binding.adView2.loadAd(adRequest)

        } else {
            Log.d("BANNER_LC_2", "Can not show ad.")

        }
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
        }
    }
}