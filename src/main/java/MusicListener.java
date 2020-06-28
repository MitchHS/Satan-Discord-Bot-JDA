import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MusicListener extends ListenerAdapter {


    public final AudioPlayerManager playerManager;
    public final Map<Long, GuildMusicManager> musicManagers;
    public long guildID;
    public ArrayList<Playlist> musicPlaylist = new ArrayList<>();


    public MusicListener() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);
        this.guildID = guildId;


        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ", 2);

        switch (command[0]){
            case "!volume":
                Message msg = event.getMessage();
                String content = msg.getContentRaw();
                content = content.replace("!volume ","");
                TextChannel channel = event.getChannel();
                GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

                try {
                    setVolume(musicManagers.get(event.getGuild().getIdLong()), Integer.valueOf(content));
                } catch (Exception e) {
                    event.getChannel().sendMessage("Error, enter valid number");
                }
                event.getChannel().sendMessage("Volumed changed to: " + content).queue();
                break;

            case "!music":
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Color.RED);
                eb.setTitle("Music Commands");

                String play  ="\n          !play youtube.xxxx - Join voice channel & add music to queue." ;
                String skip=  "\n          !skip - Skips current song in queue.";
                String pause= "\n          !pause - Pause current song in queue.";
                String resume= "\n          !resume - Resume current song in queue.";
                String stop = "\n          !stop - Stops current song in queue.";
                String cmd = play + skip + pause + resume + stop;
                eb.addField("", cmd, true);
                event.getChannel().sendMessage(eb.build()).queue();
                break;

            case "!newPlaylist":
                Playlist playlist = new Playlist(command[1], new ArrayList<String>());
                musicPlaylist.add(playlist);
                event.getChannel().sendMessage("Added " + playlist.getName() + " to playlists").queue();
               // System.out.println(musicPlaylist.get(0).getName());
                break;

            case "!Playlists":
                EmbedBuilder playEb = new EmbedBuilder();
                playEb.setColor(Color.RED);
                playEb.setTitle("Existing playlists: ");
                String list = "";
                if(!musicPlaylist.isEmpty()) {
                    for (Playlist p : musicPlaylist) {
                        list = list + p.toString() + "\n";
                    }
                }
                playEb.addField("", list, true );
                event.getChannel().sendMessage(playEb.build()).queue();
                break;


            case "!resume":
                resume(event.getChannel());
                break;

            case "!stop":
                stop(event.getChannel());
                break;

            case "!pause":
                pause(event.getChannel());
                break;

            case "!skip":
                skipTrack(event.getChannel());
                break;

            case "!play":
                if (command.length == 2) {
            VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
            if(voiceChannel == null){event.getChannel().sendMessage("Join a voice channel before attempting to play.. ").queue();
               return;
            }
            loadAndPlay(event.getChannel(), command[1], voiceChannel);
          }
                break;

            default:
                break;
        }

    //    if ("!play".equals(command[0]) && command.length == 2) {
//            VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
//            if(voiceChannel == null){event.getChannel().sendMessage("Join a voice channel before attempting to play.. ").queue();
//            return;
//            }
//            loadAndPlay(event.getChannel(), command[1], voiceChannel);
//        } else if ("!skip".equals(command[0])) {
//            skipTrack(event.getChannel());
//        } else if ("!pause".equals(command[0])) {
//            pause(event.getChannel());
//        } else if ("!stop".equals(command[0])) {
//            stop(event.getChannel());
//        } else if ("!resume".equals(command[0])) {
//            resume(event.getChannel());
//        } else if (command[0].contains("!newPlaylist")) {
//            Message message = event.getMessage();
//            String content = message.getContentRaw();
//            String msg = content;
//            Random random = new Random();
//
//            msg = msg.replace("!newPlaylist ", "");
//            String[] splitString = msg.split("\\s+");
//            Playlist playlist = new Playlist(command[1], new ArrayList<String>());
//            musicPlaylist.add(playlist);
//            event.getChannel().sendMessage("Added " + playlist.getName() + " to playlists");
//            System.out.println(musicPlaylist.get(0).getName());


//        } else if ("!music".equals(command[0])){
//            EmbedBuilder eb = new EmbedBuilder();
//            eb.setColor(Color.RED);
//            eb.setTitle("Music Commands");
//
//            String play  ="\n          !play youtube.xxxx - Join voice channel & add music to queue." ;
//            String skip=  "\n          !skip - Skips current song in queue.";
//            String pause= "\n          !pause - Pause current song in queue.";
//            String resume= "\n          !resume - Resume current song in queue.";
//            String stop = "\n          !stop - Stops current song in queue.";
//            String cmd = play + skip + pause + resume + stop;
//            eb.addField("", cmd, true);
//            event.getChannel().sendMessage(eb.build()).queue();

//        } else if ("!v".equals(command[0])) {
//            Message msg = event.getMessage();
//            String content = msg.getContentRaw();
//            content = content.replace("!volume ","");
//            TextChannel channel = event.getChannel();
//            GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
//
//            try {
//                setVolume(musicManagers.get(event.getGuild().getIdLong()), Integer.valueOf(content));
//            } catch (Exception e) {
//                event.getChannel().sendMessage("Error, enter valid number");
//            }
//            event.getChannel().sendMessage("Volumed changed to: " + content).queue();
//
//        }


            super.onGuildMessageReceived(event);
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl, VoiceChannel vc) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track, vc);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack, vc);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();

            }
        });
    }



    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, VoiceChannel vc) {
        connectToFirstVoiceChannel(guild.getAudioManager(),vc);

        musicManager.scheduler.queue(track);

    }

    private void setVolume(GuildMusicManager musicManager, int volume){
        musicManager.player.setVolume(volume);
    }

    private void stop(TextChannel channel){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.player.stopTrack();
        channel.sendMessage("Stopping").queue();
    }

    private void pause(TextChannel channel){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        channel.sendMessage("Pausing " + musicManager.player.getPlayingTrack().getInfo().title).queue();
        musicManager.player.setPaused(true);

    }

    private void resume(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.player.setPaused(false);
        channel.sendMessage("Resuming " + musicManager.player.getPlayingTrack().getInfo()).queue();
    }

    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track: " +  musicManager.player.getPlayingTrack().getInfo().title).queue();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager, VoiceChannel vc) {
//        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
//            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
//                audioManager.openAudioConnection(vc);
//                break;
//            }
//        }
        audioManager.openAudioConnection(vc);

    }

    // Inner class for game type playlists. Will be reset on bot shutdown. Could save the playlists to file if neccessary.
    class Playlist {
        String name;
        ArrayList<String> urlList;

        public Playlist(String name, ArrayList<String> urlList){
            this.name = name;
            this.urlList = urlList;
        }

        public String getName() {
            return this.name;
        }

        public ArrayList<String> getPlaylist() {
            return this.urlList;
        }

        @Override
        public String toString() {
           return this.name;
        }
    }
}