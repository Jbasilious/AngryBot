import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class BananaScore {

    public static void run(MessageReceivedEvent event) {

        try {
            DBTools.openConnection();
            ResultSet result = DBTools.selectGUILD_USER(event.getGuild().getId(), event.getAuthor().getId());
            DBTools.closeConnection();
            int total = result.getInt("BANANA_TOTAL");
            int count = result.getInt("BANANA_CURRENT");
            int gunked = result.getInt("GUNKED");
            int gunks = result.getInt("GUNKS");
            int timeout = result.getInt("TIMEOUT");
            int hooker = result.getInt("HOOKER");
            String std = result.getString("STD");
            User author = event.getAuthor();            // author object
            String nickname = Objects.requireNonNull(event.getMember()).getEffectiveName();

            AngryBot.eb.clear();
            AngryBot.eb.setAuthor(nickname, null, author.getEffectiveAvatarUrl());
            AngryBot.eb.setTitle("Your Score", null);
            AngryBot.eb.setColor(new Color(0xF7FF00));
            AngryBot.eb.addField("Total Bananas", Integer.toString(total), false);
            AngryBot.eb.addField("Current Bananas", Integer.toString(count), false);
            AngryBot.eb.addField("Gunked", Integer.toString(gunked), false);
            AngryBot.eb.addField("Gunks", Integer.toString(gunks), false);
            AngryBot.eb.addField("Timeouts", Integer.toString(timeout), false);
            AngryBot.eb.addField("Hookers", Integer.toString(hooker), false);
            AngryBot.eb.addField("STD's caught", std, false);

            event.getMessage().replyEmbeds(AngryBot.eb.build()).queue();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
