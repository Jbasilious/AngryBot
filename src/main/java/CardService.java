import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface CardService {
    @Multipart
    @POST("image")
    Call<String> uploadImage(@Part("label") RequestBody label, @Part MultipartBody.Part image);

    @GET("card")
    Call<ResponseBody> getCard(@Query("id") int id);

    @GET("summon")
    Call<ResponseBody> getSummon(@Query("id") int id);
}