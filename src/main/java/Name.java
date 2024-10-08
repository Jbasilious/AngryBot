import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class Name {

    public static void run(MessageReceivedEvent event) {

        if (Tools.modCheck(event,true)) {

            String mentionedUser = "";
            String newName = "";
            String content = event.getMessage().getContentRaw();

                mentionedUser = content.split(" ")[1].trim();
                    newName = content.substring(content.indexOf(content.split(" ")[2].trim()));

                    System.out.println("content  " + content);
                    System.out.println("UID" + mentionedUser);
                    System.out.println("newname  " + newName);

                    String UID = event.getMessage().getMentions().getUsers().get(0).getId();
                    event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById(UID)), newName).queue();



        }
    }
}
