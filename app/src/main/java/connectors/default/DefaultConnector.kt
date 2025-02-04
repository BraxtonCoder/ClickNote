package connectors.default

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultConnector @Inject constructor(
    private val firebaseApp: FirebaseApp,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val json: Json
) {
    suspend fun <T> executeQuery(
        collection: String,
        queryParams: Map<String, Any>? = null,
        deserializer: DeserializationStrategy<T>
    ): T {
        val collectionRef = firestore.collection(collection)
        var query: Query = collectionRef
        
        queryParams?.forEach { (field, value) ->
            query = query.whereEqualTo(field, value)
        }
        
        val snapshot = query.get().await()
        val jsonString = snapshot.documents.firstOrNull()?.data?.toString()
            ?: throw NoSuchElementException("No document found")
            
        return json.decodeFromString(deserializer, jsonString)
    }

    suspend fun <T> executeMutation(
        collection: String,
        document: String? = null,
        data: T,
        serializer: SerializationStrategy<T>
    ) {
        val jsonString = json.encodeToString(serializer, data)
        val docRef = if (document != null) {
            firestore.collection(collection).document(document)
        } else {
            firestore.collection(collection).document()
        }
        
        docRef.set(json.parseToJsonElement(jsonString).toString()).await()
    }

    suspend fun deleteDocument(collection: String, document: String) {
        firestore.collection(collection).document(document).delete().await()
    }

    suspend fun uploadFile(path: String, bytes: ByteArray): String {
        val ref = storage.reference.child(path)
        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteFile(path: String) {
        storage.reference.child(path).delete().await()
    }

    fun cleanup() {
        // Cleanup resources if needed
    }
}
