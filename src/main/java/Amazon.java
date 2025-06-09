import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Amazon {

    public static void run(MessageReceivedEvent event){
        event.getMessage().reply("https://amzn.to/45ODItv").queue();
    }
}
