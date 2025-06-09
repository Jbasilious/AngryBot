import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class AngryBot extends ListenerAdapter {

    static final String command = "<";
    private static final Map<String, List<Long>> userMessageTimes = new HashMap<>();
    private static final int SPAM_MESSAGE_THRESHOLD = 40; // Adjust this to the number of messages to consider as spam
    private static final int SPAM_TIME_WINDOW_SECONDS = 60; // Adjust this to the time window in seconds
    private static final int SPAM_TIMEOUT_DURATION = 300; // 300 seconds (5 minutes)
    static MessageReceivedEvent mostRecentEvent;
    static EmbedBuilder eb = new EmbedBuilder();
    static Map<String, Runnable> commands = new HashMap<>();
    private static JDA jda;
    private static int charlesCounter = 0; //count charles messages
    private final Set<String> spamTimeoutUsers = new HashSet<>();
    InputStream in;

    public AngryBot() {
        Tools.initializeParser();
    }


    public static void main(String[] args) throws SQLException {
        commands.put("sherpa", () -> Sherpa.run(mostRecentEvent));
        commands.put("scramble", () -> Scramble.run(mostRecentEvent));
        commands.put("gunk", () -> Gunk.gunk(mostRecentEvent, true));
        commands.put("ungunk", () -> Gunk.gunk(mostRecentEvent, false));
        commands.put("score", () -> BananaScore.run(mostRecentEvent));
        commands.put("spin", () -> Spin.spin(mostRecentEvent));
        commands.put("jackpot", () -> Spin.jackpot(mostRecentEvent));
        commands.put("name", () -> Name.run(mostRecentEvent));
        commands.put("addimage", () -> Card.addImage(mostRecentEvent));
        commands.put("addcard", () -> Card.addCard(mostRecentEvent));
        commands.put("card", () -> Card.viewCard(mostRecentEvent));
        commands.put("hooker", () -> Hooker.run(mostRecentEvent));
        commands.put("std", () -> Hooker.std(mostRecentEvent));
        commands.put("amazon", () -> Amazon.run(mostRecentEvent));
        Sherpa.initializeList();
        Hooker.initializeList();


        jda = JDABuilder.createDefault(Config.BOT_TOKEN())
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new AngryBot())
                .setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
                .setMemberCachePolicy(MemberCachePolicy.ALL) // ignored if chunking enabled
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .build();
    }

    /*
        @Override
        public void onReady(@NonNull ReadyEvent event) {
            // Your code to initialize guild user database

                            Guild guild = event.getJDA().getGuildById("801894633650782238");
                // Assuming you want to perform this for all guilds the bot is in
                String guildId = guild.getId();
                    guild.loadMembers().onSuccess(members -> {
                        members.forEach(member -> {
                            try {

                                if(!Tools.modCheck(member)) member.modifyNickname("Brunkz").queue();}
                            catch (HierarchyException e) {
                                e.printStackTrace();
                            }
                        });
                    }).onError(error -> {
                        error.printStackTrace();
                    });
            }


    @Override
    public void onReady(@NonNull ReadyEvent event) {
        try {
            DBTools.openConnection();
            for (Guild g : event.getJDA().getGuilds()) {
                List<Member> members = g.getMembers();
                String GID = g.getId();
                for (Member m : members) {
                    DBTools.insertGUILD_USER(GID, m.getId());
                }

            }
            DBTools.closeConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
 */
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        try {
            DBTools.openConnection();
            DBTools.insertJACKPOT(event.getGuild().getId());

            List<Member> members = event.getGuild().getMembers();
            String GID = event.getGuild().getId();
            for (Member m : members) {
                DBTools.insertGUILD_USER(GID, m.getId());
            }
            DBTools.closeConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        try {
            DBTools.openConnection();
            DBTools.insertGUILD_USER(event.getGuild().getId(), event.getMember().getId());
            DBTools.closeConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent event) {
        User brendan = jda.getUserById("708499135770394634");
        PrivateChannel brendansDM = brendan.openPrivateChannel().complete();
        OffsetDateTime timeoutEnd = event.getNewTimeOutEnd();
        if (timeoutEnd != null) {
            OffsetDateTime now = OffsetDateTime.now();
            long remainingSeconds = Duration.between(now, timeoutEnd).getSeconds();
            String formattedDuration = Tools.formatDuration(remainingSeconds);
            brendansDM.sendMessage(event.getUser().getEffectiveName() + " has been timed out for " + formattedDuration).queue();
        } else {
            System.out.println("Timeout end time is null for user " + event.getUser().getEffectiveName());
        }
        if (event.getUser().getId().equals("328689134606614528") || event.getUser().getId().equals("976597595831033916")) {
            System.out.println("Timeout User: " + event.getUser().getId() + "  " + event.getNewTimeOutEnd());
            event.getMember().removeTimeout().queue();
        }
        try {
            DBTools.openConnection();
            int timeout = 1 + DBTools.selectGUILD_USER(event.getGuild().getId(), event.getUser().getId()).getInt("TIMEOUT");
            DBTools.updateGUILD_USER(event.getGuild().getId(), event.getUser().getId(), null, null, null, null, timeout, null, null);
            DBTools.closeConnection();
        } catch (SQLException e) {

        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        //   event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById("1149411432778174474")), "No").queue();


        User user = event.getAuthor();
        String userId = user.getId();
        String content = event.getMessage().getContentRaw();

        if (user.isBot() || spamTimeoutUsers.contains(userId))
            return; // Ignore messages sent by the bot itself & Check if the user is in spam timeout

        // Check if the user is spamming
        if (isSpamming(user, content)) {
            sendFunnyJoke(event);
            return;
        }
        // event.getChannel().sendMessage("I have a boner and it won't go away until i get unbanned").queue();

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //~~~~~~~~~ BANANA DROP HANDLING ~~~~~~~~~~~~~~~
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        int randomNum = ThreadLocalRandom.current().nextInt(0, 100001);

        // Customizable drop rates
        double regularBananaRate = 1.0 / 20;      // 1/50    chance
        double rareBananaRate = 1.0 / 500;        // 1/500   chance
        double epicBananaRate = 1.0 / 2000;       // 1/2000  chance
        double uniqueBananaRate = 1.0 / 5000;     // 1/5000  chance
        double legendaryBananaRate = 1.0 / 20000; // 1/20000 chance
        if (randomNum < regularBananaRate * 100000) {
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F34C")).queue(); // Regular banana
            handleBananaEvent(event, 1);
        } else if (randomNum < (regularBananaRate + rareBananaRate) * 100000) {
            sendBananaImage(event, "rare_banana.png", 5);
        } else if (randomNum < (regularBananaRate + rareBananaRate + epicBananaRate) * 100000) {
            sendBananaImage(event, "epic_banana.png", 10);
        } else if (randomNum < (regularBananaRate + rareBananaRate + epicBananaRate + uniqueBananaRate) * 100000) {
            sendBananaImage(event, "unique_banana.png", 25);
        } else if (randomNum < (regularBananaRate + rareBananaRate + epicBananaRate + uniqueBananaRate + legendaryBananaRate) * 100000) {
            sendBananaImage(event, "legendary_banana.png", 100);
        }


        updateUserMessageTime(userId);
        mostRecentEvent = event;

        ListIterator<Role> roles = Objects.requireNonNull(event.getMember()).getRoles().listIterator();

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //~~~~~~~~~ COMMAND HANDLING ~~~~~~~~~~~~~~~
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if (event.getAuthor().getId().equals("1149411432778174474")) {  //reply NO / NO U to lleters
            if (content.equalsIgnoreCase("No")) {

                try {
                    event.getGuildChannel().sendMessage("yea").queue();
                } catch (NullPointerException E) {
                    E.printStackTrace();
                }
                return;
            }
            if (content.equalsIgnoreCase("No u")) {

                try {
                    event.getGuildChannel().sendMessage("No u").queue();
                } catch (NullPointerException E) {
                    E.printStackTrace();
                }
                return;
            }
        } else if (event.getAuthor().getId().equals("764537809045815322")) {  //reply no u / boot fucker to turd fergeson
            if (content.equalsIgnoreCase("boot fucker")) {

                try {
                    event.getGuildChannel().sendMessage("No u").queue();
                } catch (NullPointerException E) {
                    E.printStackTrace();
                }
                return;
            }
            if (content.equalsIgnoreCase("No u")) {

                try {
                    event.getGuildChannel().sendMessage("No u").queue();
                } catch (NullPointerException E) {
                    E.printStackTrace();
                }
                return;
            }
        } /*else if (event.getAuthor().getId().equals("992590293327159326")) { //NOT NOW CHARLES FUCK
            if (charlesCounter % 2 == 0) {
                charlesCounter += 1;
                event.getMessage().reply("# NOT NOW CHARLES FUCK").queue();
            } else {
                charlesCounter -= 1;
            }
        }*/
        // Check if the message is a command
        if (content.startsWith(command)) {
            String[] commandParts = content.substring(command.length()).split(" ");
            String commandContent = commandParts[0].toLowerCase();

            if (commands.containsKey(commandContent)) {
                commands.get(commandContent).run();
            } else {
                // Handle unknown command
                //event.getChannel().sendMessage("Unknown command: " + content).queue();
            }
            return;
        }

        ListIterator<String> triggers = Sherpa.TriggerWords.listIterator();
        while (triggers.hasNext()) {
            if (content.contains(triggers.next()) && Tools.containsParserTypes(content)) {
                Sherpa.replySherpa(event);
                return;
            }
        }

    }

    private void sendBananaImage(MessageReceivedEvent event, String imageName, int points) {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("bananas/" + imageName);
            if (in == null) {
                throw new IllegalArgumentException("Image resource not found: " + imageName);
            }
            File tempFile = File.createTempFile("image", ".png");
            tempFile.deleteOnExit();
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            event.getMessage().replyFiles(FileUpload.fromData(tempFile)).queue();
            handleBananaEvent(event, points);                                     //remember to change this
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the error appropriately
        }
    }

    private boolean isSpamming(User user, String content) {
        String userId = user.getId();

        // Check if the user has exceeded the spam message threshold within the time window
        if (userMessageTimes.containsKey(userId)) {
            List<Long> messageTimes = userMessageTimes.get(userId);
            long currentTime = System.currentTimeMillis();

            // Remove old message times
            messageTimes.removeIf(time -> currentTime - time > SPAM_TIME_WINDOW_SECONDS * 1000);

            // If the command is <spin, don't count it towards spamming
            if (content.startsWith("<spin")) {
                return false;
            }

            // Add the current message time
            messageTimes.add(currentTime);

            // Check if the user has sent too many messages in the time window
            if (messageTimes.size() > SPAM_MESSAGE_THRESHOLD) {
                // Put the user in spam timeout
                spamTimeoutUsers.add(userId);
                scheduleRemoveFromTimeout(userId);
                return true;
            }
        } else {
            // Initialize the user's message times list
            userMessageTimes.put(userId, new ArrayList<>(Collections.singletonList(System.currentTimeMillis())));
        }

        return false;
    }

    private void sendFunnyJoke(MessageReceivedEvent event) {
        User user = event.getAuthor();
        event.getChannel().sendMessage(user.getAsMention() + ", whoops! You tripped over a banana peel and can't find any bananas for a while. 🍌").queue();
    }

    private void updateUserMessageTime(String userId) {
        // Update the user's message time
        if (userMessageTimes.containsKey(userId)) {
            userMessageTimes.get(userId).add(System.currentTimeMillis());
        } else {
            // Initialize the user's message times list
            userMessageTimes.put(userId, new ArrayList<>(Collections.singletonList(System.currentTimeMillis())));
        }
    }

    private void scheduleRemoveFromTimeout(String userId) {
        // Schedule a task to remove the user from spam timeout after the specified duration
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            spamTimeoutUsers.remove(userId);
            System.out.println("User " + userId + " removed from spam timeout");
        }, SPAM_TIMEOUT_DURATION, TimeUnit.SECONDS);
        scheduler.shutdown();
    }

    private void handleBananaEvent(MessageReceivedEvent event, int bananaValue) {
        String tier;
        String message;
        if (bananaValue == 1) {
            tier = "Normal";
            message = null;
        } else if (bananaValue == 5) {
            tier = "Rare";
            message = event.getAuthor().getName() + " uncovered a rare banana! 🌟 It's worth 5 regular bananas!";
        } else if (bananaValue == 10) {
            tier = "Epic";
            message = event.getAuthor().getName() + " stumbled upon an epic banana! 🎉 It's worth 10 regular bananas!";
        } else if (bananaValue == 25) {
            tier = "Unique";
            message = "Amazing discovery! " + event.getAuthor().getName() + " found a unique banana! 🚀 It's worth 25 regular bananas!";
        } else if (bananaValue == 100) {
            tier = "Legendary";
            message = "HOLY FUCKING SHIT!!! UNBELIEVABLE! " + event.getAuthor().getName() + " found a LEGENDARY banana! 🌈 It's worth 100 regular bananas!";
        } else {
            tier = "Unknown";
            message = event.getAuthor().getName() + " found an UNKNOWN banana! 🍌 That shouldnt be possible! What a stupid idiot!";
        }

        // Your logic to print the message goes here
        System.out.println(message);
        if (message != null) {
            event.getChannel().sendMessage(message).queue();
        }
        // Your existing logic to update the user's banana total in the database
        try {
            DBTools.openConnection();
            ResultSet set = DBTools.selectGUILD_USER(event.getGuild().getId(), event.getAuthor().getId());
            int total = bananaValue + set.getInt("BANANA_TOTAL");
            int current = bananaValue + set.getInt("BANANA_CURRENT");
            DBTools.updateGUILD_USER(event.getGuild().getId(), event.getAuthor().getId(), total, current, null, null, null, null, null);
            DBTools.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
