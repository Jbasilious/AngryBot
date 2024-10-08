import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Hooker {
    private static final int cost = 10;
    private static final Random random = new Random();
    static int bananaCost;
    static List stdList = new ArrayList<String>();
    public static void run(MessageReceivedEvent event) {
        bananaCost = cost;
        try {
            DBTools.openConnection();
            ResultSet authorSet = DBTools.selectGUILD_USER(event.getGuild().getId(), event.getAuthor().getId());

            List<Member> mentionedUsers = new ArrayList<Member>();
            if (event.getMessage().getMentions().mentionsEveryone()) {
                mentionedUsers = event.getGuild().getMembers();
            }else mentionedUsers = event.getMessage().getMentions().getMembers();  //list of tagged users


            if (!mentionedUsers.isEmpty()) bananaCost = cost * mentionedUsers.size();
            assert authorSet != null;
            if (bananaCost > authorSet.getInt("BANANA_CURRENT")) return;
            bananaCost = authorSet.getInt("BANANA_CURRENT") - bananaCost;
            DBTools.updateGUILD_USER(event.getGuild().getId(), event.getAuthor().getId(), null, bananaCost, null, null, null, null, null);
            if (mentionedUsers.isEmpty()) hooker(event, Objects.requireNonNull(event.getMember()));
            else for (Member m : mentionedUsers) hooker(event, m);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void hooker(MessageReceivedEvent event, Member m) {
        String currentName = m.getEffectiveName();

        if (m.getId().equals(Objects.requireNonNull(event.getMember()).getId()))
            event.getChannel().sendMessage(currentName + " has rented themselves a hooker for " + cost + " bananas!").queue();
        else
            event.getChannel().sendMessage(event.getMember().getEffectiveName() + " has rented " + currentName + " a hooker for " + cost + " bananas!").queue();

        try {
            ResultSet authorSet = DBTools.selectGUILD_USER(event.getGuild().getId(), m.getUser().getId());
            assert authorSet != null;
            int hookerCount = 1 + authorSet.getInt("HOOKER");
            String std = authorSet.getString("STD");


            if (random.nextInt(100) < 33) {
                String disease = (String) stdList.toArray()[random.nextInt(stdList.toArray().length)];
                std = stdList(disease, authorSet.getString("STD"));

                    if(!Tools.modCheck(m,false)) event.getGuild().modifyNickname(m, buildName(m, disease)).queue();

                event.getChannel().sendMessage("Uh oh! " + currentName + " caught " + disease + ".").queue();
            }
            DBTools.updateGUILD_USER(event.getGuild().getId(), m.getUser().getId(), null, null, null, null, null, hookerCount, std);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String buildName(Member m, String disease) {
        String nickname;
        if (m.getNickname().equals("null")) nickname = m.getUser().getName();
        else nickname = m.getNickname();
        String newName = "*" + disease + "* " + nickname;
        if (newName.length() > 32) newName = newName.substring(0, 32);

        return newName;
    }

    public static String stdList(String disease, String std) {
        if (std.isEmpty() || std.isBlank()) std = disease;
        else if (!std.contains(disease)) std += ", " + disease;
        return std;
    }



    public static void std(MessageReceivedEvent mostRecentEvent) {
        String commandContent = "";
        String argument = "";
        String content = mostRecentEvent.getMessage().getContentRaw().toLowerCase();
        if (content.contains(" ")) {
            commandContent = content.split(" ")[1].toLowerCase().trim();
            if (content.substring(content.indexOf(content.split(" ")[1].toLowerCase().trim())).contains(" ")) {
                argument = content.substring(content.indexOf(content.split(" ")[2].toLowerCase().trim()));
            }
        }
        System.out.println("commandContent: " + commandContent);
        System.out.println("argument" + argument);
        switch (commandContent) {
            case "list":
                list(mostRecentEvent);
                break;
            case "add":
                try {
                    addStd(argument, mostRecentEvent);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "remove":
            case "rm":
                try {
                    removeStd(argument, mostRecentEvent);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "h":
            case "help":
            case "?":
            case "man":
            default:
                help(mostRecentEvent);
                break;

        }
    }

    public static void initializeList() throws SQLException {
        DBTools.openConnection();
        ResultSet result = DBTools.getCOMMAND_KEYWORD("stdList");
        DBTools.closeConnection();
        assert result != null;
        result.next();
        stdList = Tools.csvToList(result.getString("KEYWORD"));
        System.out.println("Std list initialized: " + result.getString("KEYWORD"));

    }


    public static void removeStd(String input, MessageReceivedEvent event) throws SQLException {
        input = Tools.camelCase(input);
        System.out.println("std remove method run");
        if(!Tools.modCheck(event.getMember(),true)) return;
        if (stdList.contains(input)) {
            stdList.remove(input);
            String update = Tools.listToCsv(stdList);
            DBTools.openConnection();
            DBTools.updateCOMMAND_KEYWORD("stdList", update);
            DBTools.closeConnection();
        }
    }

    public static void addStd(String input, MessageReceivedEvent event) throws SQLException {
        System.out.println("std add method run");
        if(!Tools.modCheck(event.getMember(),true)) return;
            if (!input.contains(";") && !stdList.contains(Tools.camelCase(input))) {
            stdList.add(Tools.camelCase(input));
            String update = Tools.listToCsv(stdList);
            System.out.println(update);

            try {
                DBTools.openConnection();
                DBTools.updateCOMMAND_KEYWORD("stdList", update);
                DBTools.closeConnection();
                event.getGuildChannel().sendMessage(update + " has been added").queue();

            }catch (SQLException e){}
        }                event.getGuildChannel().sendMessage("This STD already exists or contains an invalid character").queue();

    }


    public static void help(MessageReceivedEvent event) {
        System.out.println("STD help method run");
        AngryBot.eb.clear();
        AngryBot.eb.setTitle("STD Commands");
        AngryBot.eb.addField("STD list","Lists the current STDs",false);
        AngryBot.eb.addField("STD add <arg>","adds a new STD",false);
        AngryBot.eb.addField("STD remove <arg>","removes a STD",false);

        event.getGuildChannel().sendMessageEmbeds(AngryBot.eb.build()).queue();

    }

    private static void list(MessageReceivedEvent event) {
        AngryBot.eb.clear();
        AngryBot.eb.setTitle("STDs: ");
        AngryBot.eb.addField(new MessageEmbed.Field(stdList.toString(),"",false));

        event.getGuildChannel().sendMessageEmbeds(AngryBot.eb.build()).queue();
    }

}


