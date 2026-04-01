package com.example.zenchat.ui.chat

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.zenchat.data.model.Message
import com.example.zenchat.databinding.ReceivedMessageBinding
import com.example.zenchat.databinding.SentMessageBinding
import com.google.firebase.auth.FirebaseAuth

class ChatMessageAdapter(private val messageList:ArrayList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ITEM_RECEIVE=1
    val ITEM_SENT=2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if(viewType==1){
            val binding = ReceivedMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ReceiveViewHolder(binding)
        }
        else{
            val binding = SentMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SentViewHolder(binding)
        }
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage=messageList[position]
        var image=false
        if(currentMessage.message.toString().split("|")[0]=="imageCheckSent"){
            image=true
        }

        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder

            if (image) {
                val base64Image = currentMessage.message.toString().split("|")[1]
                val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                viewHolder.sentMessage.visibility = View.GONE
                viewHolder.sentImage.visibility = View.VISIBLE
                viewHolder.sentImage.setImageBitmap(bitmap)
                viewHolder.timeStampSent.text = currentMessage.time
            } else {
                viewHolder.sentImage.visibility = View.GONE
                viewHolder.sentMessage.visibility = View.VISIBLE
                viewHolder.sentMessage.text = currentMessage.message
                viewHolder.timeStampSent.text = currentMessage.time
            }

        } else {
            val viewHolder = holder as ReceiveViewHolder

            if (image) {
                val base64Image = currentMessage.message.toString().split("|")[1]
                val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                viewHolder.receiveMessage.visibility = View.GONE
                viewHolder.receiveImage.visibility = View.VISIBLE
                viewHolder.receiveImage.setImageBitmap(bitmap)
                viewHolder.timeStampReceive.text = currentMessage.time
            } else {
                viewHolder.receiveImage.visibility = View.GONE
                viewHolder.receiveMessage.visibility = View.VISIBLE
                viewHolder.receiveMessage.text = currentMessage.message
                viewHolder.timeStampReceive.text = currentMessage.time
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage=messageList[position]
        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SENT
        }
        else {
            return ITEM_RECEIVE
        }
    }
    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentViewHolder(private val binding: SentMessageBinding) :RecyclerView.ViewHolder(binding.root){
        val sentMessage=binding.textMessageSent
        val sentImage=binding.imageMessageSent
        var timeStampSent=binding.textMessageTimeSent
    }
    class ReceiveViewHolder(private val binding: ReceivedMessageBinding) :RecyclerView.ViewHolder(binding.root){
        val receiveMessage=binding.textMessageReceive
        val receiveImage=binding.imageMessageReceive
        val timeStampReceive=binding.textMessageTimeReceive

    }
}