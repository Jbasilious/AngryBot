import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class AddImage {
    public static void run(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();

            var messageText = event.getMessage().getContentDisplay();
            var params = event.getMessage().getContentDisplay().split(" ");
            var image_name = params[1];

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Config.CARD_URL())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            CardService service = retrofit.create(CardService.class);

            var attachment = event.getMessage().getAttachments().get(0);
            var stream = attachment.getProxy().download().get();

            var part = MultipartBody.Part.createFormData(
                    "image",
                    "myPic",
                    RequestBody.create(stream.readAllBytes(), MediaType.parse("image/*"))
            );
            var res = service.uploadImage(RequestBody.create(image_name, MediaType.parse("text/plain")), part).execute();

            event.getMessage().reply(res.body() == null ? "nope": res.body()).queue();

            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
