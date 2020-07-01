import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Scanner;

public class VoiceEventListener extends ListenerAdapter {

    ArrayList<String> voiceBlacklist = new ArrayList<>();
    ArrayList<String> whitelist = new ArrayList<>();
    ArrayList<String> interceptList = new ArrayList<>();
    String commandUser = "SonicLiquid";


//    public VoiceEventListener(ArrayList<String> whitelist, ArrayList<String> voiceBlacklist, ArrayList<String> interceptList){
//        this.whitelist = whitelist;
//        this.voiceBlacklist = voiceBlacklist;
//        this.interceptList = interceptList;
//    }


    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        String[] command = event.getMessage().getContentRaw().split(" ", 10);
        if(command[0].contains("!mute") && event.getMember().getUser().getName().contains(commandUser)){
            voiceBlacklist.add(command[1]);
            event.getChannel().sendMessage("Yeah shut the fuck up " + command[1]).queue();
        }

        if(command[0].contains("!unmute") && event.getMember().getUser().getName().contains(commandUser)){
           for(int x = 0; x < voiceBlacklist.size(); x++){
               if(voiceBlacklist.get(x).equals(command[1])){
                   voiceBlacklist.remove(x);
                   event.getChannel().sendMessage("Ok removed :(").queue();
               }
           }
        }

        if(command[0].contains("!intercept") && event.getMember().getUser().getName().contains(commandUser)){
            interceptList.add(command[1]);
            event.getChannel().sendMessage("Fucking good luck " + command[1]).queue();
        }

        if(command[0].contains("!unintercept") && event.getMember().getUser().getName().contains(commandUser)){
            for(int x = 0; x < interceptList.size(); x++){
                if(interceptList.get(x).equals(command[1])){
                    interceptList.remove(x);
                    event.getChannel().sendMessage("Ok removed :(").queue();
                }
            }
        }


        if(command[0].contains("!unwhitelist") && event.getMember().getUser().getName().contains(commandUser)){
            for(int x = 0; x < whitelist.size(); x++){
                if(whitelist.get(x).equals(command[1])){
                    whitelist.remove(x);
                    event.getChannel().sendMessage("Ok removed :(").queue();
                }
            }
        }

        if(command[0].contains("!whitelist") && event.getMember().getUser().getName().contains(commandUser)){
            whitelist.add(command[1]);
            event.getChannel().sendMessage(command[1] + " is Godlike").queue();
        }


    }

    @Override
    public void onGuildVoiceMute(@Nonnull GuildVoiceMuteEvent event) {
        super.onGuildVoiceMute(event);
        Member member = event.getMember();


        // Check for blacklisted users server unmute. If unmuted, toggle mute.
        if(!voiceBlacklist.isEmpty())
        {
            for(int x = 0; x< voiceBlacklist.size(); x++)
            {
                if (member.getUser().getName().contains(voiceBlacklist.get(x))) {
                    if (!member.getVoiceState().isGuildMuted()) {
                        event.getGuild().mute(member, true).queue();
                    }
                }

            }
        }
        // If user is whitelisted, make them servermutable.
        if(!whitelist.isEmpty())
        {
            for(int x =0; x < whitelist.size(); x ++)
            {
                if (member.getUser().getName().contains(whitelist.get(x))) {
                    if (member.getVoiceState().isGuildMuted()) {
                        event.getGuild().mute(member, false).queue();
                    }

                }
            }
        }
    }
// Connecting to any voice channel
    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        super.onGuildVoiceJoin(event);
        Member member = event.getMember();
        if(!interceptList.isEmpty()){
            for(int x=0; x<interceptList.size(); x++){
                if(member.getUser().getName().contains(interceptList.get(x))){
                    VoiceChannel afk = event.getGuild().getAfkChannel();
                    event.getGuild().moveVoiceMember(member, afk).queue();
                }
            }
        }
    }

    // Intercepts them joining any channel, moves them to guild afk channel.
    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {
        super.onGuildVoiceMove(event);
        Member member = event.getMember();
        System.out.println("Joined voice channel: " + event.getMember().toString());
        if(!interceptList.isEmpty() && event.getChannelJoined()!=event.getGuild().getAfkChannel()){
            for(int x=0; x<interceptList.size(); x++){
                if(member.getUser().getName().contains(interceptList.get(x))){
                    VoiceChannel afk = event.getGuild().getAfkChannel();
                    event.getGuild().moveVoiceMember(member, afk).queue();
                }
            }
        }

        if(!whitelist.isEmpty()){
           VoiceChannel oldVC = event.getChannelLeft();
           VoiceChannel newVC = event.getChannelJoined();

           if(oldVC!=newVC) {
               System.out.println("Not equal");
               for (int x = 0; x < whitelist.size(); x++) {
                   if (member.getUser().getName().contains(whitelist.get(x)) && member.getVoiceState().inVoiceChannel()) {
                       event.getGuild().moveVoiceMember(member, oldVC).queue();
                   }
               }
           }
        }
    }



    public ArrayList<String> getInterceptList() {
        return this.interceptList;
    }

    public ArrayList<String> getVoiceBlacklist() {
        return this.voiceBlacklist;
    }

    public ArrayList<String> getWhitelist() {
        return this.whitelist;
    }

    public void purgeBlackList(){
        this.getVoiceBlacklist().clear();
    }

    public void purgeInterceptList(){
        this.getInterceptList().clear();
    }

    public void setInterceptList() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter username/s: ");
        String tmpUser = in.next();
        if (!tmpUser.isEmpty()) {
            String[] users = tmpUser.split("\\s+");
            for(int x =0; x< users.length; x++)
            {
                interceptList.add(users[x]);
                System.out.println("Added user to intercept list: " + users[x]);
            }
//            for (String user : users) {
//                interceptList.add(user);
//                System.out.println("Added user to intercept list: " + user);
//            }
        } else {System.out.println("Add valid user");}
    }

    public void setBlacklist() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter username/s: ");
        String tmpUser = in.next();
        if (!tmpUser.isEmpty()) {
            String[] users = tmpUser.split("\\s+");
            for (String user : users) {
                voiceBlacklist.add(user);
                System.out.println("Added user to troll list: " + user);
            }
        } else {System.out.println("Add valid user");}

    }

    public void setWhitelist() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter username/s: ");
        String tmpUser = in.next();
        if (!tmpUser.isEmpty()) {
            String[] users = tmpUser.split("\\s+");
            for (String user : users) {
                whitelist.add(user);
                System.out.println("Added user to whitelist: " + user);
            }
        } else {System.out.println("Add valid user");}
    }



}
