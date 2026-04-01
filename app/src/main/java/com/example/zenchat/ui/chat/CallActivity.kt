package com.example.zenchat.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.zenchat.databinding.ActivityCallBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

class CallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var roomRef: DatabaseReference

    private var peerConnection: PeerConnection? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var localVideoSource: VideoSource? = null
    private var localAudioSource: AudioSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null

    private lateinit var eglBase: EglBase

    private var roomId: String = ""
    private var myUid: String = ""
    private var otherUid: String = ""
    private var isInitiator: Boolean = false
    private var didSendOffer = false
    private var isRemoteDescriptionSet = false

    private var offerListener: ValueEventListener? = null
    private var answerListener: ValueEventListener? = null
    private var candidateListener: ChildEventListener? = null

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_CODE = 919
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        otherUid = intent.getStringExtra("uid").orEmpty()
        if (myUid.isBlank() || otherUid.isBlank()) {
            Toast.makeText(this, "Unable to start call", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        roomId = if (myUid < otherUid) "${myUid}_$otherUid" else "${otherUid}_$myUid"
        isInitiator = myUid < otherUid

        dbRef = FirebaseDatabase.getInstance().reference
        roomRef = dbRef.child("calls").child(roomId)

        binding.endCallButton.setOnClickListener { finish() }

        if (!hasMediaPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                CAMERA_AUDIO_PERMISSION_CODE
            )
        } else {
            startCall()
        }
    }

    private fun startCall() {
        initWebRtc()
        createPeerConnection()
        startLocalMedia()
        observeSignaling()
        maybeCreateOffer()
    }

    private fun hasMediaPermissions(): Boolean {
        val cam = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val mic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        return cam && mic
    }

    private fun initWebRtc() {
        eglBase = EglBase.create()
        binding.localView.init(eglBase.eglBaseContext, null)
        binding.remoteView.init(eglBase.eglBaseContext, null)
        binding.localView.setZOrderMediaOverlay(true)
        binding.localView.setMirror(true)
        binding.remoteView.setMirror(false)

        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions()
        )
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    private fun createPeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState) = Unit
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) = Unit
            override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) = Unit
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) = Unit
            override fun onRemoveStream(stream: org.webrtc.MediaStream) = Unit
            override fun onDataChannel(dataChannel: org.webrtc.DataChannel) = Unit
            override fun onRenegotiationNeeded() = Unit
            override fun onAddStream(stream: org.webrtc.MediaStream) = Unit
            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) = Unit
            override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState) = Unit
            override fun onTrack(transceiver: org.webrtc.RtpTransceiver?) {
                val track = transceiver?.receiver?.track() as? VideoTrack ?: return
                runOnUiThread { track.addSink(binding.remoteView) }
            }

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out org.webrtc.MediaStream>?) = Unit

            override fun onIceCandidate(candidate: IceCandidate) {
                val data = mapOf(
                    "sdpMid" to candidate.sdpMid,
                    "sdpMLineIndex" to candidate.sdpMLineIndex,
                    "candidate" to candidate.sdp
                )
                roomRef.child("candidates").child(myUid).push().setValue(data)
            }
        })
    }

    private fun startLocalMedia() {
        val factory = peerConnectionFactory ?: return

        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        videoCapturer = createCameraCapturer()
        localVideoSource = factory.createVideoSource(false)
        videoCapturer?.initialize(surfaceTextureHelper, this, localVideoSource?.capturerObserver)
        videoCapturer?.startCapture(640, 480, 30)

        localVideoTrack = factory.createVideoTrack("LOCAL_VIDEO_TRACK", localVideoSource)
        localVideoTrack?.addSink(binding.localView)

        localAudioSource = factory.createAudioSource(MediaConstraints())
        localAudioTrack = factory.createAudioTrack("LOCAL_AUDIO_TRACK", localAudioSource)

        peerConnection?.addTrack(localVideoTrack, listOf("ZENCHAT_STREAM"))
        peerConnection?.addTrack(localAudioTrack, listOf("ZENCHAT_STREAM"))
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(this)
        val deviceNames = enumerator.deviceNames
        deviceNames.firstOrNull { enumerator.isFrontFacing(it) }?.let {
            return enumerator.createCapturer(it, null)
        }
        deviceNames.firstOrNull()?.let {
            return enumerator.createCapturer(it, null)
        }
        return null
    }

    private fun observeSignaling() {
        offerListener = roomRef.child("offer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isInitiator || isRemoteDescriptionSet) return
                val sdp = snapshot.child("sdp").getValue(String::class.java).orEmpty()
                if (sdp.isBlank()) return
                val remoteOffer = SessionDescription(SessionDescription.Type.OFFER, sdp)
                peerConnection?.setRemoteDescription(SimpleSdpObserver {
                    isRemoteDescriptionSet = true
                    createAndSendAnswer()
                }, remoteOffer)
            }

            override fun onCancelled(error: DatabaseError) = Unit
        })

        answerListener = roomRef.child("answer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isInitiator || isRemoteDescriptionSet) return
                val sdp = snapshot.child("sdp").getValue(String::class.java).orEmpty()
                if (sdp.isBlank()) return
                val remoteAnswer = SessionDescription(SessionDescription.Type.ANSWER, sdp)
                peerConnection?.setRemoteDescription(SimpleSdpObserver {
                    isRemoteDescriptionSet = true
                }, remoteAnswer)
            }

            override fun onCancelled(error: DatabaseError) = Unit
        })

        candidateListener = roomRef.child("candidates").child(otherUid)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val sdp = snapshot.child("candidate").getValue(String::class.java).orEmpty()
                    val sdpMid = snapshot.child("sdpMid").getValue(String::class.java)
                    val sdpMLineIndex = snapshot.child("sdpMLineIndex").getValue(Int::class.java) ?: 0
                    if (sdp.isBlank()) return
                    peerConnection?.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, sdp))
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = Unit
                override fun onChildRemoved(snapshot: DataSnapshot) = Unit
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
                override fun onCancelled(error: DatabaseError) = Unit
            })
    }

    private fun maybeCreateOffer() {
        if (!isInitiator || didSendOffer) return
        didSendOffer = true
        peerConnection?.createOffer(object : SimpleSdpObserver({
            peerConnection?.setLocalDescription(SimpleSdpObserver(), it)
            roomRef.child("offer").setValue(mapOf("type" to "offer", "sdp" to it.description))
        }) {}, MediaConstraints())
    }

    private fun createAndSendAnswer() {
        peerConnection?.createAnswer(object : SimpleSdpObserver({
            peerConnection?.setLocalDescription(SimpleSdpObserver(), it)
            roomRef.child("answer").setValue(mapOf("type" to "answer", "sdp" to it.description))
        }) {}, MediaConstraints())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_AUDIO_PERMISSION_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startCall()
        } else {
            Toast.makeText(this, "Camera and microphone permissions are required", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        offerListener?.let { roomRef.child("offer").removeEventListener(it) }
        answerListener?.let { roomRef.child("answer").removeEventListener(it) }
        candidateListener?.let { roomRef.child("candidates").child(otherUid).removeEventListener(it) }
        if (isFinishing) {
            roomRef.removeValue()
        }

        videoCapturer?.stopCaptureSafely()
        videoCapturer?.dispose()
        localVideoTrack?.dispose()
        localAudioTrack?.dispose()
        localVideoSource?.dispose()
        localAudioSource?.dispose()
        peerConnection?.close()
        peerConnection?.dispose()
        binding.localView.release()
        binding.remoteView.release()
        eglBase.release()
    }
}

private open class SimpleSdpObserver(
    private val onCreateSuccess: ((SessionDescription) -> Unit)? = null,
) : org.webrtc.SdpObserver {
    override fun onCreateSuccess(sessionDescription: SessionDescription?) {
        sessionDescription?.let { onCreateSuccess?.invoke(it) }
    }

    override fun onSetSuccess() = Unit
    override fun onCreateFailure(error: String?) = Unit
    override fun onSetFailure(error: String?) = Unit
}

private fun CameraVideoCapturer.stopCaptureSafely() {
    try {
        stopCapture()
    } catch (_: InterruptedException) {
    }
}
