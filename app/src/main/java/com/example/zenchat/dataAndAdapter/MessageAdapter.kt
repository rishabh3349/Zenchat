package com.example.zenchat.dataAndAdapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.zenchat.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(val context: Context,val messageList:ArrayList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ITEM_RECEIVE=1
    val ITEM_SENT=2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if(viewType==1){
            //receive
            val view:View=LayoutInflater.from(context).inflate(R.layout.receive,parent,false)
            return ReceiveViewHolder(view)
        }
        else{
            //sent
            val view:View=LayoutInflater.from(context).inflate(R.layout.sent,parent,false)
            return SentViewHolder(view)
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
            } else {
                viewHolder.sentImage.visibility = View.GONE
                viewHolder.sentMessage.visibility = View.VISIBLE
                viewHolder.sentMessage.text = currentMessage.message
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
            } else {
                viewHolder.receiveImage.visibility = View.GONE
                viewHolder.receiveMessage.visibility = View.VISIBLE
                viewHolder.receiveMessage.text = currentMessage.message
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

    class SentViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val sentMessage=itemView.findViewById<TextView>(R.id.text_message_sent)
        val sentImage=itemView.findViewById<ImageView>(R.id.image_message_sent)
    }
    class ReceiveViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val receiveMessage=itemView.findViewById<TextView>(R.id.text_message_receive)
        val receiveImage=itemView.findViewById<ImageView>(R.id.image_message_receive)
    }
}