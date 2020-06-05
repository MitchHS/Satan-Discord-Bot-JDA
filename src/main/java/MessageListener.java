import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MessageListener extends ListenerAdapter
{
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;

        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = event.getChannel();
        event.getChannel();
        System.out.println(content);
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.equals("!ping"))
        {
            channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
        }

        if(content.contains("!mute")){
            String msg = content;
            msg = msg.replace("!mute ", "");

          //  Member member = event.getGuild().getMember();

        }

        // Text event for !choose. Randomly choose item. Syntax: !choose item item2 item3 ... cont ...
        if (content.contains("!choose")) {
            String msg = content;
            Random random = new Random();

            msg = msg.replace("!choose ", "");
            System.out.println(msg);
            String[] splited = msg.split("\\s+");

            for (int x = 0; x < splited.length; x++) {
                System.out.println(splited[x]);
            }

            System.out.println(splited.length);
            int select = random.nextInt(splited.length);
            channel.sendMessage("Gaben Says: " + splited[select]).queue();
        }

        // Syntax !dicksize @user
        if (content.contains("!dicksize")) {
            String msg = content;
            msg = msg.replace("!dicksize ", "");
            double random = ThreadLocalRandom.current().nextDouble(.05, 10.00);
            DecimalFormat df = new DecimalFormat("#.##");
            random = Double.valueOf(df.format(random));


            if (!msg.contains("@")) {
                channel.sendMessage("Error: Choose a valid user").queue();
            } else {
                channel.sendMessage(msg + " " + random + " Inch").queue();
            }
        }
    }




}