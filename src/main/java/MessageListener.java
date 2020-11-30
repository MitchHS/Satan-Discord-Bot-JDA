import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.InviteAction;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MessageListener extends ListenerAdapter
{
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        String[] command = event.getMessage().getContentRaw().split(" ", 10);
        if (event.getAuthor().isBot()) return;

        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = event.getChannel();
        event.getChannel();

        if(content.contains("!invite")){
            Invite invite = event.getTextChannel().createInvite().complete();
            event.getChannel().sendMessage(invite.getUrl()).queue();
        }

        if (content.equals("!ping"))
        {
            System.out.println("PING CATCH");
            Member member = event.getMember();
            String name = member.getUser().getId();
            TextChannel textChannel = event.getTextChannel();
            textChannel.sendMessage("U fucking wot " +"<@" + name+">").addFile(new File("/home/pi/satanBot/watchu.gif")).queue();
        }


        // Text event for !choose. Randomly choose item. Syntax: !choose item item2 item3 ... cont ...
        if (content.contains("!choose")) {
            String msg = content;
            Random random = new Random();

            msg = msg.replace("!choose ", "");
            String[] splited = msg.split("\\s+");

            int select = random.nextInt(splited.length);
            channel.sendMessage("Gaben Says: " + splited[select]).queue();
        }



        if(content.contains("!commands")){

            String ping = "Smokescreen cmd";
            String music = "Displays music commands";
            String choose = "Randomly chooses item";
            String dickSize = "Accurate dicksize in inches";
            String invite = "Generates invite link";

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Commands");
            eb.setColor(Color.RED);
            eb.addField("!ping", ping, false);
            eb.addField("!music", music, false);
            eb.addField("!choose item item2 .... itemX", choose, false);
            eb.addField("!dicksize @user", dickSize, false);
            eb.addField("!invite", invite, false);

            event.getChannel().sendMessage(eb.build()).queue();
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