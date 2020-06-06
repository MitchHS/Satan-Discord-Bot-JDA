import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Scanner;

public class VoiceEventListener extends ListenerAdapter {

    ArrayList<String> voiceBlacklist = new ArrayList<>();
    ArrayList<String> whitelist = new ArrayList<>();
    ArrayList<String> interceptList = new ArrayList<>();

//    public VoiceEventListener(ArrayList<String> whitelist, ArrayList<String> voiceBlacklist, ArrayList<String> interceptList){
//        this.whitelist = whitelist;
//        this.voiceBlacklist = voiceBlacklist;
//        this.interceptList = interceptList;
//    }

    @Override
    public void onGuildVoiceMute(@Nonnull GuildVoiceMuteEvent event) {
        super.onGuildVoiceMute(event);
        Member member = event.getMember();
        System.out.println(member.getEffectiveName() + member.getVoiceState().isGuildMuted());

        if(member.getEffectiveName().contains("SonicLiquid")){
            if(!member.getVoiceState().isGuildMuted()){
                System.out.println("Event > unmuted");
                event.getGuild().mute(member, true).queue();

            }
        }

        if(!voiceBlacklist.isEmpty())
        {
            for(int x = 0; x< voiceBlacklist.size(); x++)
            {
                if (member.getEffectiveName().contains(voiceBlacklist.get(x))) {
                    if (!member.getVoiceState().isGuildMuted()) {
                        event.getGuild().mute(member, true).queue();
                    }
                }

            }
        }

        if(!whitelist.isEmpty())
        {
            for(int x =0; x < whitelist.size(); x ++)
            {
                if (member.getEffectiveName().contains(whitelist.get(x))) {
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
      //  System.out.println("Joined voice channel: " + event.getMember().toString());
        if(!interceptList.isEmpty()){
            for(int x=0; x<interceptList.size(); x++){
                if(member.getEffectiveName().contains(interceptList.get(x))){
                    VoiceChannel afk = event.getGuild().getAfkChannel();
                    event.getGuild().moveVoiceMember(member, afk).queue();
                }
            }
        }
    }

    // Move from afk channel
    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {
        super.onGuildVoiceMove(event);
        Member member = event.getMember();
        System.out.println("Joined voice channel: " + event.getMember().toString());
        if(!interceptList.isEmpty() && event.getChannelJoined()!=event.getGuild().getAfkChannel()){
            for(int x=0; x<interceptList.size(); x++){
                if(member.getEffectiveName().contains(interceptList.get(x))){
                    VoiceChannel afk = event.getGuild().getAfkChannel();
                    event.getGuild().moveVoiceMember(member, afk).queue();
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
