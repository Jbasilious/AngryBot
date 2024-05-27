import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class AddCard {
    public static void run(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();

            var params = event.getMessage().getContentDisplay().substring(1).split("<");
            int rowCount = DBTools.insertCard(params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], 0, params[9], true);
            event.getMessage().reply(rowCount > 0 ? "nice. latest card id is " + DBTools.getLatestCardId() : "nope").queue();

            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
