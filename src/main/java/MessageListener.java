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
            Member member = event.getMember();
            String name = member.getUser().getId();
            TextChannel defaultText = event.getGuild().getDefaultChannel();
//            defaultText.sendMessage("U fucking wot " +"<@" + name).
            defaultText.sendMessage("U fucking wot " +"<@" + name+">").addFile(new File("/home/pi/watchu.gif")).queue();
        }

//        if(content.contains("!mute")){
//            String msg = content;
//            msg = msg.replace("!mute ", "");
//
//          //  Member member = event.getGuild().getMember();
//
//        }




        // Text event for !choose. Randomly choose item. Syntax: !choose item item2 item3 ... cont ...
        if (content.contains("!choose")) {
            String msg = content;
            Random random = new Random();

            msg = msg.replace("!choose ", "");
            String[] splited = msg.split("\\s+");

            int select = random.nextInt(splited.length);
            channel.sendMessage("Gaben Says: " + splited[select]).queue();
        }

        if(content.contains("!"))

        if(content.contains("!commands")){
            MessageBuilder mb = new MessageBuilder();
            String first  ="                                   |     \n";
            String second = ",---.,---.,-.-.,-.-.,---.,---.,---|,---.\n";
            String third  = "|    |   || | || | |,---||   ||   |`---.\n";
            String fourth = "`---'`---'` ' '` ' '`---^`   '`---'`---'\n";
            String command =  "!ping - Smokescreen cmd\n"
                    + "!music - Displays music commands\n" +
                    "!choose item item2 ... itemXX - Randomly chooses item\n" +
                    "!dicksize @user - Accurate dicksize in inches\n" +
                    "!invite - Generates invite link";

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Commands");
            eb.setColor(Color.RED);
            eb.addField("", command, true);


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