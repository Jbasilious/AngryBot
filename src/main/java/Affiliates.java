import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;


public class Affiliates {

    public static void initializeList() throws SQLException {
        DBTools.openConnection();
        ResultSet result = DBTools.getAffiliates();
        DBTools.closeConnection();
        assert result != null;
        System.out.println(" Populating Affiliate Links:");
        while (result.next()) {
            AngryBot.AffiliateMap.put(result.getString(1).toLowerCase(), result.getString(2));
            System.out.println(result.getString(1) + "  " + result.getString(2));
        }
    }

    public static void links(MessageReceivedEvent event) {
        AngryBot.eb.clear();
        AngryBot.eb.setTitle("Links");
        AngryBot.AffiliateMap.forEach((s, s2) -> AngryBot.eb.addField(s, s2, false));
        AngryBot.eb.build();
        event.getMessage().replyEmbeds(AngryBot.eb.build()).queue();
    }


    public static void singleLink(MessageReceivedEvent event) {
        String message = event.getMessage().getContentStripped().toLowerCase().substring(1).split(" ")[0];
        try {
            event.getMessage().reply(AngryBot.AffiliateMap.get(message)).queue();
        } catch (IllegalArgumentException exception) {
            System.out.println("arg: " + message);
            exception.printStackTrace();
        }
    }
}