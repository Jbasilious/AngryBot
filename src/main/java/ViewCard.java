import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.InputStream;

public class ViewCard {
    public static void run(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();

            var messageText = event.getMessage().getContentDisplay();
            var params = event.getMessage().getContentDisplay().split(" ");
            var card_id = Integer.parseInt(params[1]);
            var isGif = params.length >= 3 && (params[2]).equals("gif");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Config.CARD_URL())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            CardService service = retrofit.create(CardService.class);

            var res = isGif ? service.getSummon(card_id).execute() : service.getCard(card_id).execute();
            ResponseBody body = res.body();
            InputStream imageStream = body.byteStream();
            event.getMessage().reply("Ebic card !!!").addFiles(FileUpload.fromData(imageStream, isGif ? "card.gif" : "card.png")).queue();

            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
