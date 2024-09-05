import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class Card {
    private static final String[] cardStyleList = {"normal", "ritual", "effect", "fusion", "link", "shiny","spell","synchro","trap","xyz"};
    private static final String[] attributeList = {"Light", "Dark", "Earth", "Fire", "Wind", "Spell","Trap","Divine"};
    private static final String[] typeList = {"Gooner", "Warlock", "Wizard", "Mage", "Paladin", "Demon","Gunk Lord","Retard"};
    private static final Random random = new Random();

    public static void addCard(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();
            Random RANDOM = new Random();
            String atk = String.valueOf((int)(Math.pow(RANDOM.nextDouble(), 2) * 15000));
            String def = String.valueOf((int)(Math.pow(RANDOM.nextDouble(), 2) * 15000));
            String level = String.valueOf(RANDOM.nextInt(11)+1);
            String cardStyle = cardStyleList[RANDOM.nextInt(cardStyleList.length-1)];
            String attribute = attributeList[RANDOM.nextInt(attributeList.length-1)];
            String type = typeList[RANDOM.nextInt(typeList.length-1)];

            var params = event.getMessage().getContentDisplay().substring(1).split("<");
            System.out.println("cardStyle="+cardStyle+"  Title="+params[1]+"   attribute="+ attribute + "  level=" +level + "   type="+type+ "   desc=" +params[2]+"  atk=-"+ atk+"  def="+ def);
            int rowCount = DBTools.insertCard(cardStyle, params[1], attribute, level, type, params[2], atk, def, 0, params[3], true);
            event.getMessage().reply(rowCount > 0 ? "nice. latest card id is " + DBTools.getLatestCardId() : "nope").queue();

            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void viewCard(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();

            var messageText = event.getMessage().getContentDisplay();
            var params = event.getMessage().getContentDisplay().split(" ");

            Integer card_id = null;
            try {
                card_id = Integer.parseInt(params[1]);
            } catch (Exception ex) {

            }
            if (card_id == null) {
                String cardName = params[1];
                Set<String> allCardTitles = DBTools.getCardTitles();
                var cardNameSelected = "";
                if (cardName.equalsIgnoreCase("random")){
                    int randomIndex = random.nextInt(allCardTitles.size());
                    var it = allCardTitles.iterator();
                    for (int i = 0; i < randomIndex; i++) {
                        it.next();
                    }
                    cardNameSelected = it.next();
                } else {
                    var searchRes = me.xdrop.fuzzywuzzy.FuzzySearch.extractOne(cardName, allCardTitles);
                    cardNameSelected = searchRes.getString();
                }
                card_id = DBTools.getCardIdForTitle(cardNameSelected);
            }
            var isGif = params.length >= 3 && (params[2]).equals("gif");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Config.CARD_URL())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            CardService service = retrofit.create(CardService.class);

            int retryCount = 0;
            retrofit2.Response<ResponseBody> res = null;
            do {
                res = isGif ? service.getSummon(card_id).execute() : service.getCard(card_id).execute();
                retryCount++;
            } while (res.body() == null && retryCount <=3);

            ResponseBody body = res.body();
            InputStream imageStream = body.byteStream();
            event.getMessage().reply("Ebic card !!!").addFiles(FileUpload.fromData(imageStream, isGif ? "card.gif" : "card.png")).queue();

            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void addImage(MessageReceivedEvent event) {
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
            int retryCount = 0;
            retrofit2.Response<String> res = null;
            do {
                res = service.uploadImage(RequestBody.create(image_name, MediaType.parse("text/plain")), part).execute();
                retryCount++;
            } while (res.body() == null && retryCount <=3);
            event.getMessage().reply(res.body() == null ? "nope": res.body()).queue();

            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
