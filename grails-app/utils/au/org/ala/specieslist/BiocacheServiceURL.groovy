package au.org.ala.specieslist

import com.fasterxml.jackson.databind.JsonNode
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BiocacheServiceURL {
    @GET("occurrences/taxaCount")
    Call<JsonNode> occurrenceCounts(
            @Query("separator") String separator,
            @Query("guids") String guids);
}
