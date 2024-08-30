import com.example.durranitech.models.LocationResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface LocationService {
    @GET("v1/geo/cities")
    suspend fun getCities(
        @Header("x-rapidapi-key") apiKey: String,
        @Header("x-rapidapi-host") host: String = "wft-geo-db.p.rapidapi.com",
        @Query("namePrefix") namePrefix: String
    ): LocationResponse
}