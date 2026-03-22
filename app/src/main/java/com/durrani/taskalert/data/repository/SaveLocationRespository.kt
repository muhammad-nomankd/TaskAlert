package com.durrani.taskalert.data.repository

import android.content.Context
import android.util.Log
import com.durrani.taskalert.domain.models.location
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SaveLocationRespository {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun saveLocation(loc: location, context: Context, onSuccess: (() -> Unit)? = null, onFailure: ((Exception) -> Unit)? = null) {
        if (currentUser == null) {
            Log.e("SaveLocationRepository", "No authenticated user found")
            onFailure?.invoke(Exception("User not authenticated"))
            return
        }

        val locationMap = mapOf(
            "location" to loc.location,
            "locationId" to loc.locationId,
            "country" to loc.country
        )

        Log.d("SaveLocationRepository", "Attempting to save location: ${loc.location}, ${loc.country}")

        // Get reference to user's location collection
        val userLocationCollection = firestore.collection("User")
            .document(currentUser.uid)
            .collection("location")

        // First, delete all existing locations (assuming one location per user)
        userLocationCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firestore.batch()

                // Delete all existing location documents
                for (document in querySnapshot.documents) {
                    batch.delete(document.reference)
                }

                // Add the new location document
                val newLocationRef = userLocationCollection.document(loc.locationId)
                batch.set(newLocationRef, locationMap)

                // Commit the batch
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("SaveLocationRepository", "Location saved successfully: ${loc.location}")
                        onSuccess?.invoke()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("SaveLocationRepository", "Failed to save location", exception)
                        onFailure?.invoke(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("SaveLocationRepository", "Failed to query existing locations", exception)
                // If query fails, try to save directly
                userLocationCollection.document(loc.locationId)
                    .set(locationMap)
                    .addOnSuccessListener {
                        Log.d("SaveLocationRepository", "Location saved successfully (direct): ${loc.location}")
                        onSuccess?.invoke()
                    }
                    .addOnFailureListener { saveException ->
                        Log.e("SaveLocationRepository", "Failed to save location directly", saveException)
                        onFailure?.invoke(saveException)
                    }
            }
    }
}
