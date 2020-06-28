import com.fasterxml.jackson.databind.util.JSONPObject;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.net.URL;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;


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
        String[] command = event.getMessage().getContentRaw().split(" ", 10);

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

                String play  ="            Join voice channel & add music to queue." ;
                String skip=  "\n          !skip - Skips current song in queue.";
                String pause= "\n          !pause - Pause current song in queue.";
                String resume= "\n          !resume - Resume current song in queue.";
                String stop = "\n          !stop - Stops current song in queue.";
                String playlistCmd = "\n           Adds all songs in playlist to queue.";
                String newPlaylist = "\n           Creates a new volatile playlist";
                String removePlaylist = "\n           Deletes playlist completely";
                String add = "\n           Adds video to existing playlist";
                String listPlaylist = "\n           Lists all available playlists";
                String listSonglist = "\n           Lists all songs in specified playlist";
                String remove = "\n           Removes songtitle from playlist. " +
                        "List the name exactly as it appears. Use !listSongs cmd to get titles.";


                eb.addField("!play youtube.xxxx", play, false);
                eb.addField("!skip", skip, false);
                eb.addField("!pause", pause, false);
                eb.addField("!resume", resume, false);
                eb.addField("!stop", stop, false);
                eb.addField("!playlist playlistName", playlistCmd, false);
                eb.addField("!newPlaylist playlistName", newPlaylist, false);
                eb.addField("!add playlistName youtube.xx", add, false);
                eb.addField("!playlistList", listPlaylist, false);
                eb.addField("!listSongs playlistName", listSonglist, false);
                eb.addField("!removePlaylist playlistName", removePlaylist, false);
                eb.addField("!remove playlistName songTitle", remove, false);




                event.getChannel().sendMessage(eb.build()).queue();
                break;

            case "!newPlaylist":
                if (command.length == 2) {
                    Playlist playlist = new Playlist(command[1], new ArrayList<String>());
                    musicPlaylist.add(playlist);
                    event.getChannel().sendMessage("Added " + playlist.getName() + " to playlists").queue();
                    // System.out.println(musicPlaylist.get(0).getName());
                } else {
                    event.getChannel().sendMessage("Syntax error:  !newPlaylist playlistName").queue();
                }

                break;

            case "!removePlaylist":
                if(command.length == 2){
                    for(int x = 0; x < musicPlaylist.size(); x ++){
                        if(command[1].equals(musicPlaylist.get(x).getName())){
                            musicPlaylist.remove(x);
                            event.getChannel().sendMessage("Successfully removed " + command[1]).queue();
                            break;
                        } else if (x == musicPlaylist.size() -1){
                            event.getChannel().sendMessage("Playlist doesn't exist: " + command[1]).queue();
                        }
                    }
                } else {  event.getChannel().sendMessage("Syntax error:  !removePlaylist playlistName").queue();}
                break;

            case "!playlistList":
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

            case "!songList":
                String listName = null;
                ArrayList<String> songlist = null;
                try
                {listName = command[1];
                } catch (ArrayIndexOutOfBoundsException e){
                    event.getChannel().sendMessage("Syntax error:  !songList playlist").queue();
                }

                if(listName!=null){
                    for(int x =0; x<musicPlaylist.size(); x++){
                        if(musicPlaylist.get(x).getName().equals(listName)){
                            songlist = musicPlaylist.get(x).getPlaylist();
                            String s = "";
                            for(String song : songlist){
                                s = s + getTitleQuietly(song) + "\n";
                            }
                            EmbedBuilder playlistEB = new EmbedBuilder();
                            playlistEB.setColor(Color.RED);
                            playlistEB.setTitle(musicPlaylist.get(x).getName() + " songs");
                            playlistEB.addField("", s, true);
                            event.getChannel().sendMessage(playlistEB.build()).queue();

                        }
                    }
                }

                break;

            case "!add":
                String name = null;
                String url = null;

                try{
                     name = command[1];
                     url = command[2];
                } catch (IndexOutOfBoundsException e){
                    event.getChannel().sendMessage("Invalid Sytax:  !add playlistName youtube.xx").queue();
                }
                if(name!=null && url !=null) {
                    if (name.isEmpty()) {
                        event.getChannel().sendMessage("Error, please provide a playlist").queue();
                    } else if (url.isEmpty()) {
                        event.getChannel().sendMessage("Error, please provide a YouTube URL").queue();
                    } else {

                        for (int x = 0; x < musicPlaylist.size(); x++) {
                            if (name.equals(musicPlaylist.get(x).getName())) {
                                musicPlaylist.get(x).addURL(url);
                                //   System.out.println(musicPlaylist.get(x).getPlaylist());
                                //  event.getChannel().sendMessage("Added url to " + musicPlaylist.get(x).getName()).queue();
                                event.getChannel().sendMessage("Adding " + getTitleQuietly(url) + " to " + musicPlaylist.get(x).getName()).queue();
                                break;
                            } else if (x == musicPlaylist.size() - 1 && !name.equals(musicPlaylist.get(x).getName())) {
                                event.getChannel().sendMessage("Cannot add to playlist " + command[1].toString() + ". Playlist does not exist").queue();
                            }
                        }

                    }
                } else {

                }

                break;

            case "!remove":
                if(command.length > 3){
                    Message message = event.getMessage();
                    String msgContent = message.getContentRaw();
                    System.out.println("Command > 3");

                    msgContent = msgContent.replace("!remove ", "");
                    String[] split = msgContent.split("\\s+");
                    System.out.println("SPLIT 0 = " + split[0]);

                    if(split.length > 1){
                        System.out.println("split length > 1");
                        for(int x = 0; x < musicPlaylist.size(); x ++){
                                if(split[0].equals(musicPlaylist.get(x).getName())){
                                   msgContent = msgContent.replaceAll(split[0]+" ", "");
                                   System.out.println("After remove:" + msgContent);
                                    boolean ret = musicPlaylist.get(x).removeSongByTitle(msgContent);
                                    event.getChannel().sendMessage("Removed : " + ret).queue();
                                    break;
                                } else if (x == musicPlaylist.size()-1){
                                    event.getChannel().sendMessage("Cannot find song").queue();
                                }
                        }
                    }
                }
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

            case "!playlist":
                String playName = null;
                try{
                    playName = command[1];
                } catch (ArrayIndexOutOfBoundsException e){

                }
                if(playName == null){
                    event.getChannel().sendMessage("Specify Playlist.. ").queue();
                } else {
                    boolean exists = false;
                    for(Playlist p : musicPlaylist){
                        if(p.getName().equals(command[1])) {
                            ArrayList<String> tracks = p.getPlaylist();
                            exists = true;
                            if (tracks.isEmpty()) {
                                event.getChannel().sendMessage("Playlist url list is empty... ").queue();
                            } else {
                                for (String s : tracks) {
                                    VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
                                    if (voiceChannel == null) {
                                        event.getChannel().sendMessage("Join a voice channel before attempting to play.. ").queue();
                                        break;
                                    }
                                    silentLoadAndPlay(event.getChannel(), s, voiceChannel);
                                    event.getChannel().sendMessage("Adding playlist to current queue..").queue();
                                }
                            }
                        }
                    }
                    if(!exists){
                        event.getChannel().sendMessage("Playlist not found, try again").queue();
                    }
                }
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

    private void silentLoadAndPlay(final TextChannel channel, final String trackUrl, VoiceChannel vc) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
               // channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track, vc);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

              //  channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

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

    public static String getTitleQuietly(String youtubeUrl) {
        try {
            if (youtubeUrl != null) {
                URL embededURL = new URL("http://www.youtube.com/oembed?url=" +
                        youtubeUrl + "&format=json"
                );

                return new JSONObject(IOUtils.toString(embededURL)).getString("title");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

        public void addURL(String url) {
            urlList.add(url);
        }

        public boolean removeSongByTitle(String title){
            boolean isTrue = false;
            System.out.println("PASSED ARG:" + title);
            for(int x = 0; x < urlList.size(); x ++){
                System.out.println("Retrieved TITLE URL:" + getTitleQuietly(urlList.get(x)));
                if(getTitleQuietly(urlList.get(x)).equals(title)){
                    urlList.remove(x);
                    System.out.println("MATCHED TITLES");
                    isTrue = true;
                } else if(x == urlList.size()){
                    isTrue = false;
                }
            }
            return isTrue;
        }

        @Override
        public String toString() {
           return this.name;
        }
    }
}