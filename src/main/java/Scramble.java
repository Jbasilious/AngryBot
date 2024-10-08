import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class Scramble {

    public static void run(MessageReceivedEvent event) {
            if (Tools.modCheck(event.getMember(), true)) {

                Message msg = event.getMessage();
                String ID;                                  //unique user ID
                String nickname;

                List<User> users = msg.getMentions().getUsers();  //list of tagged users

                for (User u : users) {
                    ID = u.getId();
                    if (!Tools.modCheck(event.getMember(), true)) {
                        nickname = Tools.stringScramble(Objects.requireNonNull(event.getGuild().getMemberById(u.getId())).getEffectiveName());
                        nickname = nickname.substring(0, 1).toUpperCase() + nickname.substring(1);
                        event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById(ID)), nickname).queue();
                    }
                }
            }

    }
}
