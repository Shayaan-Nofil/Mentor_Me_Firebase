package com.ShayaanNofil.i210450

import Chats
import Mentors
import Messages
import User
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat_recycle_adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random


private lateinit var mAuth: FirebaseAuth
private lateinit var database: DatabaseReference
class individual_chat : AppCompatActivity() {
    private var messages: Messages? = null
    private var messageimg: Uri? = null
    private var uploadimgbt : ImageButton? = null
    private var chat : Chats? = null
    private var screenshot: Boolean = false
    private var recording : Boolean = false
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null


    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_chat)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        chat = intent.getSerializableExtra("object") as Chats

        //Screenshots
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentResolver.registerContentObserver(uri, true, screenshotObserver)

        var chattername : TextView = findViewById(R.id.chatter_name)
        mAuth = Firebase.auth

        //Get the appropriate name of the chatter
        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.w("TAG", "User is a user")
                    database = FirebaseDatabase.getInstance().getReference("Mentor")

                    database.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (data in snapshot.children) {
                                    val mentor = data.getValue(Mentors::class.java)
                                    if (mentor != null) {
                                        FirebaseDatabase.getInstance().getReference("Mentor").child(mentor.id!!).child("Chats").addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    for (data in snapshot.children) {
                                                        val chats = data.getValue(String::class.java)
                                                        if (chats != null) {
                                                            if (chats == chat!!.id) {
                                                                chattername.text = mentor.name
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
                else{
                    Log.w("TAG", "User is a mentor")

                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (data in snapshot.children) {
                                    val mentor = data.getValue(User::class.java)
                                    if (mentor != null) {
                                        FirebaseDatabase.getInstance().getReference("User").child(mentor.id!!).child("Chats").addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                Log.w("TAG", "Mentor | Got chats")
                                                if (snapshot.exists()) {
                                                    for (data in snapshot.children) {
                                                        val chats = data.getValue(String::class.java)
                                                        if (chats != null) {
                                                            if (chats == chat!!.id) {
                                                                chattername.text = mentor.name
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })


        val recycle_topmentor: RecyclerView = findViewById(R.id.messages_recycler)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        recycle_topmentor.layoutManager = layoutManager


        val messagearray: MutableList<Messages> = mutableListOf()

        FirebaseDatabase.getInstance().getReference("Chat").child(chat!!.id).child("Messages").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messagearray.clear()
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val myclass = data.getValue(Messages::class.java)
                        if (myclass != null) {
                            messagearray.add(myclass)
                        }
                    }
                    val adapter = chat_recycle_adapter(messagearray)
                    recycle_topmentor.adapter = adapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        val btsend: android.widget.Button = findViewById(R.id.btsend)
        btsend.setOnClickListener(View.OnClickListener {
            uploadmessage()
        })

        val backbutton: android.widget.Button = findViewById(R.id.back_button)
        backbutton.setOnClickListener(View.OnClickListener {
            finish()
        })

        val voicebutton=findViewById<View>(R.id.voicecall_button)
        voicebutton.setOnClickListener(View.OnClickListener {
            val temp = Intent(this, voice_call::class.java )
            startActivity(temp)
            finish()
        })

        val videobutton=findViewById<View>(R.id.videocall_button)
        videobutton.setOnClickListener(View.OnClickListener {
            val temp = Intent(this, call_screen_simple::class.java )
            startActivity(temp)
        })

        val uppic=findViewById<View>(R.id.btcamera)
        uppic.setOnClickListener(View.OnClickListener {
            val temp = Intent(this, camera_picture_mode::class.java )
            val bundle = Bundle()
            bundle.putSerializable("chatdata", chat)
            temp.putExtras(bundle)
            startActivity(temp)
        })

        val vcrecbutton: ImageButton = findViewById(R.id.btvcrec)
        vcrecbutton.setOnClickListener() {
            if (!recording){
                recording = true
                vcrecbutton.setBackgroundResource(R.drawable.voice_icon_red)
                startRecording()
            }
            else {
                recording = false
                vcrecbutton.setBackgroundResource(R.drawable.mic_icon_white)
                stopRecording()
                uploadAudio()
            }
        }


        val task_homebutton=findViewById<View>(R.id.bthome)
        task_homebutton.setOnClickListener(View.OnClickListener {
            val temp = Intent(this, home_page::class.java )
            startActivity(temp)
        })

        val task_searchbutton=findViewById<View>(R.id.btsearch)
        task_searchbutton.setOnClickListener(View.OnClickListener {
            val temp = Intent(this, Search::class.java )
            startActivity(temp)
        })

        val task_chatbutton=findViewById<View>(R.id.btchat)
        task_chatbutton.setOnClickListener(View.OnClickListener {
            val temp = Intent(this, chats_page::class.java )
            startActivity(temp)
        })

        val task_profilebutton=findViewById<View>(R.id.btprofile)
        task_profilebutton.setOnClickListener(View.OnClickListener {
            val temp = Intent(this, profile_page::class.java )
            startActivity(temp)
        })

        uploadimgbt=findViewById(R.id.btattcontent)
        uploadimgbt!!.setOnClickListener(View.OnClickListener {
            messages = Messages()
            openGalleryForImage()
        })
    }

    private val galleryprofileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null){
            messages!!.tag = "image"
            messageimg = uri

            Glide.with(this)
                .asBitmap()
                .load(messageimg)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val bitmapDrawable = BitmapDrawable(this@individual_chat.resources, resource)
                        uploadimgbt!!.background = bitmapDrawable
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle cleanup here
                    }
                })
        }
    }
    private fun openGalleryForImage() {
        galleryprofileLauncher.launch("image/*")
    }
    private fun uploadmessage(){
        var message: Messages = Messages()
        mAuth = Firebase.auth
        if (messages == null){ //Text Messages
            if (screenshot == true){
                message.content = "**User took a screenshot of chat**"
            }
            else{
                val messagecontent : EditText = findViewById(R.id.message_text)
                message.content = messagecontent.text.toString()
                messagecontent.setText("")
                screenshot = false
            }
            message.time = SimpleDateFormat("HH:mm").format(Date())
            message.senderid = mAuth.uid.toString()
            message.tag = "text"

            FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(Mentors::class.java)
                        if (user != null) {
                            Log.w("TAG", "in user, getting url")
                            Log.w("TAG", user.profilepic.toString())
                            message.senderpic = user.profilepic.toString()

                            FirebaseDatabase.getInstance().getReference("Chat").child(chat!!.id).child("Messages").push().setValue(message)
                        }
                    }
                    else{
                        FirebaseDatabase.getInstance().getReference("Mentor").child(mAuth.uid.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val user = snapshot.getValue(Mentors::class.java)
                                    if (user != null) {
                                        Log.w("TAG", "in mentor, getting url")
                                        Log.w("TAG", user.profilepic.toString())
                                        message.senderpic = user.profilepic.toString()

                                        FirebaseDatabase.getInstance().getReference("Chat").child(chat!!.id).child("Messages").push().setValue(message)
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
        else{ //Other messages
            screenshot = false
            messages!!.senderid = mAuth.uid.toString()
            messages!!.time = SimpleDateFormat("HH:mm").format(Date())
            message = messages!!
            messages = null
            uploadimgbt!!.background = ContextCompat.getDrawable(this, R.drawable.gallary_icon_white)

            val storageref = FirebaseStorage.getInstance().reference

            storageref.child("Chats").child(chat!!.id + Random.nextInt(0,100000).toString()).putFile(messageimg!!).addOnSuccessListener {
                it.metadata!!.reference!!.downloadUrl.addOnSuccessListener {task ->
                    message.content = task.toString()
                    Log.w("TAG", "Upload Success")

                    FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val user = snapshot.getValue(User::class.java)
                                if (user != null) {
                                    Log.w("TAG", "in user, getting url")
                                    message.senderpic = user.profilepic.toString()

                                    FirebaseDatabase.getInstance().getReference("Chat").child(chat!!.id).child("Messages").push().setValue(message)
                                }
                            }
                            else {
                                FirebaseDatabase.getInstance().getReference("Mentor").child(mAuth.uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val user = snapshot.getValue(Mentors::class.java)
                                            if (user != null) {
                                                Log.w("TAG", "in mentor, getting url")
                                                message.senderpic = user.profilepic.toString()

                                                FirebaseDatabase.getInstance().getReference("Chat").child(chat!!.id).child("Messages").push().setValue(message)
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }.addOnFailureListener{
                Log.w("TAG", "Upload failed")
            }
        }
        Log.w("Message uRI", message.content)
    }

    private val screenshotObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?, userId: Int) {
            super.onChange(selfChange, uri, userId)
            if (!screenshot) {
                screenshot = true
                Log.d("ContentObserver", "Calling uploadmessage()")
                uploadmessage()
            }
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            audioFile = File.createTempFile("audio", ".mp3", externalCacheDir)
            setOutputFile(audioFile!!.absolutePath)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("TAG", "startRecording: ", e)
            }
        }
    }
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    private fun uploadAudio() {
        val storageRef = FirebaseStorage.getInstance().reference.child("Chats").child("voice").child(chat!!.id + Random.nextInt(0,100000).toString())
        val uploadTask = audioFile?.let { storageRef.putFile(Uri.fromFile(it)) }

        uploadTask?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val message = Messages().apply {
                    content = uri.toString()
                    senderid = mAuth.uid.toString()
                    time = SimpleDateFormat("HH:mm").format(Date())
                    tag = "audio"
                }
                FirebaseDatabase.getInstance().getReference("Chat").child(chat!!.id).child("Messages").push().setValue(message)
            }
        }?.addOnFailureListener {
            Log.e("TAG", "uploadAudio: ", it)
        }
    }
}