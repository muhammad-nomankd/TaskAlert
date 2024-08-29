package com.example.alarmmanager.repositories

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.alarmmanager.dataclasses.location
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SaveLocationRespository {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun saveLocation(loc: location, context: Context) {

        val locationMap = mapOf(
            "location" to loc.location,
            "locationId" to loc.locationId,
            "country" to loc.country
        )
        if (currentUser != null) {
            val locationId = loc.locationId
            val locationRef =
                firestore.collection("User").document(currentUser.uid).collection("location")
                    .document(locationId)
            locationRef.get()
                .addOnSuccessListener { DocumentSnapshot ->
                    if (DocumentSnapshot.exists()) {
                        locationRef.update(locationMap)
                    } else {
                        locationRef.set(loc)
                            .addOnSuccessListener {
                                Log.d("location", "Location saved successfully")
                            }
                    }
                }

        }
    }
}