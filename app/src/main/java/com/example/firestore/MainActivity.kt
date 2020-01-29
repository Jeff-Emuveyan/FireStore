package com.example.firestore

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
typealias fileUploadCallback = () -> Unit

class MainActivity : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    val fileUploadChoosen = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLog.movementMethod = ScrollingMovementMethod()

        sendButton.setOnClickListener{

            validateInputFields {
                progressBar.visibility = View.VISIBLE
                sendButton.visibility = View.INVISIBLE
                tvLog.text = ""
                tvLog.visibility = View.INVISIBLE

                //send user to db:
                db.collection("user")
                    .add(it)
                    .addOnSuccessListener {
                        progressBar.visibility = View.INVISIBLE
                        sendButton.visibility = View.VISIBLE
                        tvLog.text = "id: ${it.id}"
                        tvLog.visibility = View.VISIBLE
                    }
                    .addOnFailureListener{
                        progressBar.visibility = View.INVISIBLE
                        sendButton.visibility = View.VISIBLE
                        tvLog.text = "Failed to sync"
                        tvLog.visibility = View.INVISIBLE
                    }
            }

        }

        fetchButton.setOnClickListener{
            progressBar.visibility = View.VISIBLE
            fetchButton.visibility = View.INVISIBLE

            db.collection("user").orderBy("age", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener{
                    progressBar.visibility = View.INVISIBLE
                    fetchButton.visibility = View.VISIBLE

                    it?.let {
                        if(it.isSuccessful){
                            var resultString: String = ""
                            for (document in it.result!!) {
                                Log.e("Result:: ", document.id + " => " + document.data)
                                resultString = "$resultString \nResult::  ${document.id} =>> ${document.data}"
                            }
                            tvLog.text = resultString
                        }else{
                            tvLog.text = "Unsuccessful!"
                        }
                    }
                }
        }


        fetchOne.setOnClickListener{
            progressBar.visibility = View.VISIBLE
            fetchOne.visibility = View.INVISIBLE

            db.document("user/0hZEGo6EHkEvUsoRQby3")
                .get()
                .addOnCompleteListener{
                    progressBar.visibility = View.INVISIBLE
                    fetchOne.visibility = View.VISIBLE

                    it?.let {
                        if(it.isSuccessful){
                            tvLog.text = "Result:: ${it.result?.id} =>> ${it.result?.data}"
                            Log.e("Result::", it.result?.id + " => " + it.result?.data)

                        }else{
                            tvLog.text = "Unsuccessful!"
                        }
                    }
                }
        }


        uploadFileButton.setOnClickListener{
            //choose file to upload:
            chooseFile("Select an image file", fileUploadChoosen)
        }
    }


    private fun validateInputFields(onValidated: (User) -> Unit){

        val name = editTextName.text.toString()
        val age = editTextAge.text.toString()

        if(name.isEmpty() || age.isEmpty()){
            Toast.makeText(this@MainActivity, "Missing input fields", Toast.LENGTH_LONG).show()
        }else{
            onValidated.invoke(User(name, age))
        }
    }


    private fun chooseFile(title: String, expectedResultCode: Int) = MaterialFilePicker()
        .withActivity(this)
        .withRequestCode(expectedResultCode)
        .withHiddenFiles(false) // Show hidden files and folders
        .withTitle(title)
        .start()


    private fun uploadFIle(folderName: String, filePath: String){

        if(filePath == null || filePath.equals("")){
            return
        }
        val uri: Uri = Uri.fromFile(File(filePath))
        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("myFiles/${uri.lastPathSegment}")

        fileRef.putFile(uri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    tvLog.text = downloadUri.toString()
                } else {
                    tvLog.text = "Upload failed ${task.exception?.message}"
                }
            }.addOnProgressListener {
            tvLog.text = "File size: ${it.totalByteCount}, /nFIle sent: ${it.bytesTransferred}"
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == fileUploadChoosen && resultCode == RESULT_OK) {
            val filePath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
            filePath?.let {
                uploadFIle("randomFiles", it)
            }


        }
    }
}