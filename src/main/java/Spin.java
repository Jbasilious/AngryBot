import com.sun.jna.platform.win32.DBT;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Spin {

    private static final int BANANA_COST = 5;
    private static final Random random = new Random();

    private static final int outputTaskDelay = 1000; // milliseconds to queue up spins until we execute them at once
    private static AtomicBoolean isOutputTaskScheduled = new AtomicBoolean(false);
    private static Timer outputTimer = new Timer();
    private static final ConcurrentHashMap<MessageChannelUnion, ConcurrentLinkedQueue<MessageReceivedEvent>> eventQueues = new ConcurrentHashMap<>(); // map [channel] -> [queue of events since the last output]
    public static void spin(MessageReceivedEvent event) {
        if(!Tools.allowedTime())return;

        MessageChannelUnion channel = event.getChannel();
        // lazy init queue for perf
        eventQueues.computeIfAbsent(channel, k -> new ConcurrentLinkedQueue<>()).add(event);
        if(isOutputTaskScheduled.compareAndSet(false, true)) {
            // since the data has been inserted already into the queue, we don't need to include the actual scheduling method inside our concurrency control
            // worst case we double up on output tasks if compareAndSet executes after the isOutputTaskScheduled=false in the task execution
            scheduleOutputTask();
        }
    }
    public static void scheduleOutputTask() {
        outputTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isOutputTaskScheduled.set(false);
                // go through the event queue per channel
                for(Map.Entry<MessageChannelUnion, ConcurrentLinkedQueue<MessageReceivedEvent>> entry: eventQueues.entrySet()) {
                    MessageChannelUnion channel = entry.getKey();
                    Queue<MessageReceivedEvent> queue = entry.getValue();
                    StringBuilder outputBatchMessage = new StringBuilder();

                    // need a max number of items to process - otherwise we might be here forever if produce rate > consume rate
                    int numToRemove = queue.size();
                    for(int i = 0; i < numToRemove; i++) {
                        MessageReceivedEvent event = queue.poll();
                        if(event != null) {
                            performSpin(event, outputBatchMessage);
                            outputBatchMessage.append("...\n");
                        }
                    }
                    channel.sendMessage(outputBatchMessage.toString()).queue();
                }
            }
        }, outputTaskDelay);
    }



   /* private static void performSpin(MessageReceivedEvent event, StringBuilder outputMessage) {
        try {
            int userBalance;
            int totalBananas;
            User author = event.getMessage().getAuthor();
            String userId = author.getId();
            String guildId = event.getGuild().getId();

            DBTools.openConnection();
            ResultSet authorSet = DBTools.selectGUILD_USER(guildId, userId);
            if(authorSet == null ) {
                return;
            }
            userBalance = authorSet.getInt("BANANA_CURRENT");
            totalBananas = authorSet.getInt("BANANA_TOTAL");


            if (!hasEnoughBananas(userBalance)) {
                sendMessageNotEnoughBananas(event, author, outputMessage);
                return;
            }

            userBalance -= BANANA_COST;

            int[] newBalances = processSpin(event, author, userBalance, totalBananas, outputMessage);

            if (userId.equals("1149411432778174474")) {
                transferBananas(guildId,"1149411432778174474", "433377645619707906", "328689134606614528", 2);
            }

            DBTools.updateGUILD_USER(guildId, userId, newBalances[1],newBalances[0], null, null,null);
            DBTools.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    } */
    private static void performSpin(MessageReceivedEvent event, StringBuilder outputMessage) {
        try {
            int userBalance;
            int totalBananas;
            User author = event.getMessage().getAuthor();
            String userId = author.getId();
            String guildId = event.getGuild().getId();

            DBTools.openConnection();
            ResultSet authorSet = DBTools.selectGUILD_USER(guildId, userId);
            if (authorSet == null) {
                return;
            }
            userBalance = authorSet.getInt("BANANA_CURRENT");
            totalBananas = authorSet.getInt("BANANA_TOTAL");

            if (!hasEnoughBananas(userBalance)) {
                sendMessageNotEnoughBananas(event, author, outputMessage);
                return;
            }

            userBalance -= BANANA_COST;

            int[] newBalances = processSpin(event, author, userBalance, totalBananas, outputMessage);

            if (userId.equals("223826414326120449")) {
                ResultSet recipientSet = DBTools.selectGUILD_USER(guildId, "433377645619707906");
                if (recipientSet != null && recipientSet.next()) {
                    int recipientBalance = recipientSet.getInt("BANANA_CURRENT");
                    int recipientTotal = recipientSet.getInt("BANANA_TOTAL");
                    int winnings = newBalances[0] - userBalance;
                    recipientBalance += winnings;
                    recipientTotal += winnings;
                    DBTools.updateGUILD_USER(guildId, "433377645619707906", recipientTotal, recipientBalance, null, null, null,null,null);
                    outputMessage.append("The bananas you've won have been sent to  <@" + "433377645619707906" + ">! ahahahaha 🎉🎉🎉\n");
                    newBalances[0] = userBalance;
                }
            }

            DBTools.updateGUILD_USER(guildId, userId, newBalances[1], newBalances[0], null, null, null,null,null);
            DBTools.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasEnoughBananas(int userBalance) {
        return userBalance >= BANANA_COST;
    }

    private static void sendMessageNotEnoughBananas(MessageReceivedEvent event, User author, StringBuilder outputMessage) {
        outputMessage.append(author.getAsMention()).append(" You don't have enough bananas to spin!").append("\n");
    }

    private static int[] processSpin(MessageReceivedEvent event, User author, int userBalance, int totalBananas, StringBuilder outputMessage) {
        int dropChance = random.nextInt(9); // 0 - 5

        if ((userBalance < 1000 && dropChance > 2) || (userBalance >= 1000 && dropChance != 0)) {
            handleDropChance(event, author, outputMessage);
        } else {
            int winnings = calculateWinnings(event, author, outputMessage);

            userBalance += winnings;
            totalBananas += winnings;

            generateWinningMessage(event, author, winnings, outputMessage);
        }

        return new int[]{userBalance, totalBananas};
    }



    private static void handleDropChance(MessageReceivedEvent event, User author, StringBuilder outputMessage) {
        final String[] MEAN_MESSAGES = {
                "You are so stupid!", "Thats what you get for gambling, idiot.",
                "Get gunked!", "Hahahahaha", "You should give up", "Stop gambling!"
        };

        String randomMeanMessage = MEAN_MESSAGES[random.nextInt(MEAN_MESSAGES.length)];
        outputMessage.append(author.getAsMention()).append(" Uh-oh! You dropped 5 bananas on the way to the slot machine! 🍌🍌🍌🍌🍌 ").append(randomMeanMessage).append("\n");

        int currentJackpot = DBTools.selectJACKPOT(event.getGuild().getId());
        currentJackpot += 5;
        DBTools.updateJACKPOT(currentJackpot);

        outputMessage.append(":rotating_light:Banana Jackpot has reached: ").append(currentJackpot).append(" bananas! :rotating_light:").append("\n");

    }

    private static int calculateWinnings(MessageReceivedEvent event, User author, StringBuilder outputMessage ) {
        int spinResult = random.nextInt(100) + 1;
        int winnings = 0;

        int Usertotal;
        if (spinResult <= 30) {
            winnings = 5;
        } else if (spinResult <= 70) {
            winnings = 10;
        } else if (spinResult <= 85) {
            winnings = 15;
        } else if (spinResult <= 97) {
            winnings = 20;
        } else {
            winnings = handleJackpot(event, author, outputMessage);
        }
        return winnings;
    }

    private static int handleJackpot(MessageReceivedEvent event, User author, StringBuilder outputMessage) {
        int jackpot = DBTools.selectJACKPOT(event.getGuild().getId());

        outputMessage.append(author.getAsMention()).append(" 🎉🎉🎉 Jackpot! You spun and won ").append(jackpot).append(" bananas! 🎉🎉🎉 Holy moly!").append("\n");
        DBTools.updateJACKPOT(25);
        outputMessage.append("The banana Jackpot has reset to 25 bananas! Good luck!").append("\n");

        return jackpot;
    }

    private static void generateWinningMessage(MessageReceivedEvent event, User author, int winnings, StringBuilder outputString) {
        String winningMessage = switch (winnings) {
            case 5 -> " You spun and won 5 bananas! (You're an idiot!)";
            case 10 -> " You spun and won 10 bananas! Good job I guess?";
            case 15 -> " You spun and won 15 bananas! Thats cool";
            case 20 -> " You spun and won 20 bananas! Nice.";
            default -> " You spun and won " + winnings + " bananas!";
        };

        outputString.append(author.getAsMention()).append(winningMessage).append("\n");
    }

    public static void jackpot(MessageReceivedEvent event) {
        try {
            DBTools.openConnection();
            int currentJackpot = DBTools.selectJACKPOT(event.getGuild().getId());

            event.getChannel().sendMessage(":tada: Exciting News! The current Banana Jackpot is: " + currentJackpot + " bananas! :tada:").queue();
            DBTools.closeConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
