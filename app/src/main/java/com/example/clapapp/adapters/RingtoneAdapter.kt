package com.example.clapapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.clapapp.R
import com.example.clapapp.activities.MainActivity
import com.example.clapapp.count
import com.example.clapapp.dataclass.DataClass
import com.google.android.material.bottomsheet.BottomSheetDialog


var mediaPlayer: MediaPlayer? = null
var issend = false

class RingtoneAdapter(private val context: Context, private val songs: List<DataClass>) :
    RecyclerView.Adapter<RingtoneAdapter.ViewHolder>() {
    private var playingPosition: Int = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val ivplay: ImageView = view.findViewById(R.id.iv_play)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_songs, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val song = songs[position]
        holder.titleTextView.text = song.title
        val isPlaying = position == playingPosition
        val iconRes = if (isPlaying) R.drawable.smallpause else R.drawable.smallplay
        holder.ivplay.setImageResource(iconRes)
        holder.ivplay.setOnClickListener {
            count++
            if (isPlaying) {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer?.release()
                mediaPlayer = null
                playingPosition = -1
            } else {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(holder.itemView.context, song.artist)
                mediaPlayer?.setOnCompletionListener {
                    mediaPlayer?.stop()
                    mediaPlayer?.reset()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    playingPosition = -1
                    notifyDataSetChanged()
                }
                mediaPlayer?.start()
                playingPosition = position
            }
            notifyDataSetChanged()
        }
        holder.itemView.setOnClickListener {
            if (mediaPlayer != null) {
                mediaPlayer!!.stop()
                holder.ivplay.setImageResource(R.drawable.smallplay)
            }
            showBottomSheetDialog(song.artist)
        }
    }
    override fun getItemCount(): Int {
        return songs.size
    }
    private fun showBottomSheetDialog(audio :Int) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val bottomSheetView = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_layout, null)
        val confirmButton = bottomSheetView.findViewById<TextView>(R.id.applyButton)
        confirmButton.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("dataKey", audio)
            context.startActivity(intent)
            bottomSheetDialog.dismiss()
            Toast.makeText(context,"Applied", Toast.LENGTH_SHORT).show()
            issend = true
        }
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }
}