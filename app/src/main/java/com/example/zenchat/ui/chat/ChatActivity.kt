package com.example.zenchat.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zenchat.R
import com.example.zenchat.data.model.Message
import com.example.zenchat.databinding.ActivityChatBinding
import com.example.zenchat.ui.BottomNavActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: ChatMessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private var isListening = false

    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    companion object {
        private const val IMAGE_PICK_CODE = 1001
        private const val AUDIO_PERMISSION_CODE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val isDarkMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        binding.root.setBackgroundResource(
            if (isDarkMode) R.drawable.dark_chat_background else R.drawable.chat_background
        )
        supportActionBar?.show()

        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    if (isDarkMode) R.color.surface else R.color.blue
                )
            )
        )

        messageList = ArrayList()
        messageAdapter = ChatMessageAdapter(messageList)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        requestAudioPermission()
        setupSpeechRecognizer()

        binding.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.sendButton.setImageResource(
                    if (s.isNullOrEmpty()) R.drawable.ic_mic else R.drawable.ic_send
                )
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.imageButton.setOnClickListener {
            val pickImage = Intent(Intent.ACTION_PICK)
            pickImage.type = "image/*"
            startActivityForResult(pickImage, IMAGE_PICK_CODE)
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageBox.text.trim().toString()
            if (message.isEmpty()) {
                if (!isListening) {
                    isListening = true
                    speechRecognizer.startListening(speechIntent)
                    binding.sendButton.setImageResource(R.drawable.ic_pause)
                } else {
                    speechRecognizer.stopListening()
                    isListening = false
                    binding.sendButton.setImageResource(R.drawable.ic_mic)
                }
            } else {
                val currentTime = Calendar.getInstance().time
                val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val formattedTime = formatter.format(currentTime)
                val messageObject = Message(message = message, time = formattedTime , senderId = senderUid)
                sendMessageToFirebase(messageObject)
                binding.messageBox.setText("")
            }
        }

        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        message?.let { messageList.add(it) }
                    }
                    messageAdapter.notifyDataSetChanged()
                    binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
                    binding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    private fun sendMessageToFirebase(message: Message) {
        mDbRef.child("chats").child(senderRoom!!).child("messages").push()
            .setValue(message).addOnSuccessListener {
                if (senderRoom != receiverRoom) {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(message)
                }
            }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, BottomNavActivity::class.java)
                startActivity(intent)
                onBackPressed()
                true
            }
            R.id.action_call -> {
                val receiverUid = intent.getStringExtra("uid")
                if (!receiverUid.isNullOrBlank()) {
                    startActivity(
                        Intent(this, CallActivity::class.java).apply {
                            putExtra("uid", receiverUid)
                        }
                    )
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                isListening = false
                binding.sendButton.setImageResource(R.drawable.ic_mic)
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                spokenText?.let {
                    val currentTime = Calendar.getInstance().time
                    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val formattedTime = formatter.format(currentTime)
                    val messageObject = Message(it, formattedTime ,FirebaseAuth.getInstance().currentUser?.uid)
                    sendMessageToFirebase(messageObject)
                }
                binding.sendButton.setImageResource(R.drawable.ic_mic)
                binding.sendButton.setImageResource(R.drawable.ic_mic)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            val inputStream = contentResolver.openInputStream(imageUri!!)
            val imageBytes = inputStream?.readBytes()
            val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
            val currentTime = Calendar.getInstance().time
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedTime = formatter.format(currentTime)
            val imageMessage = Message("imageCheckSent|" + base64Image, formattedTime ,FirebaseAuth.getInstance().currentUser?.uid)
            sendMessageToFirebase(imageMessage)
        }
    }
}
