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
import java.io.*;
import java.net.URL;
import java.util.*;

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
                setVolumeCommand(command, event);
                break;

            case "!music":
                musicCommands(command, event);
                break;

            case "!newPlaylist":
                newPlaylist(command, event);
                break;

            case "!removePlaylist":
               removePlaylist(command, event);
                break;

            case "!list":
                listPlaylists(command, event);
                break;

            case "!songList":
               listSongs(command, event);
                break;

            case "!add":
               addSongToPlaylist(command, event);
                break;

            case "!remove":
                remove(command, event);
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
                addPlaylistToQueue(command, event);
                break;

            case "!play":
                if (command.length == 2) {
            VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
            if(voiceChannel == null){event.getChannel().sendMessage("Join a voice channel before attempting to play.. ").queue();

            } else {
                loadAndPlay(event.getChannel(), command[1], voiceChannel);
            }
          }
                break;

            case "!queue":
               getQueueTitles(event);
                break;

            case "!shuffle":
                if(command.length == 2) {
                    for (int x = 0; x < musicPlaylist.size(); x++) {
                        if (command[1].equals(musicPlaylist.get(x).getName())) {
                            shuffle(command, event, musicPlaylist.get(x));
                            event.getChannel().sendMessage("Shuffling " + command[1]).queue();
                            break;
                        }
                    }
                } else {
                    event.getChannel().sendMessage("Invalid syntax: !shuffle playlistName");
                }
                break;

            case "!purge":
                try {
                    musicManagers.get(event.getGuild().getIdLong()).scheduler.purge();
                    event.getChannel().sendMessage("Purged the entire queue").queue();
                } catch (NullPointerException e){
                    event.getChannel().sendMessage("Queue list is empty").queue();
                }

                break;


            default:
                break;
        }

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

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " first track of playlist " + playlist.getName() + ")").queue();

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

    private void loadAndPlayQuiet(final TextChannel channel, final String trackUrl, VoiceChannel vc) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(channel.getGuild(), musicManager, track, vc);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

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


    public void musicCommands(String[] commands, GuildMessageReceivedEvent event){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED);
        eb.setTitle("Music Commands");

        String queue = "\n          Prints list of all songs in queue";
        String play  ="            Join voice channel & add music to queue. Optional: Specify amount to loop playlist." ;
        String skip=  "\n          Skips current song in queue.";
        String pause= "\n          Pause current song in queue.";
        String resume= "\n          Resume current song in queue.";
        String stop = "\n          Stops current song in queue.";
        String purge = "\n          Purges all songs from current queue.";
        String playlistCmd = "\n           Adds all songs in playlist to queue.";
        String newPlaylist = "\n           Creates a new volatile playlist.";
        String removePlaylist = "\n           Deletes playlist completely.";
        String add = "\n           Adds video to existing playlist.";
        String listPlaylist = "\n           Lists all available playlists.";
        String listSonglist = "\n           Lists all songs in specified playlist.";
        String remove = "\n           Removes songtitle from playlist. " +
                "List the name exactly as it appears. Use !listSongs cmd to get titles.";


        eb.addField("!queue", queue, false);
        eb.addField("!play youtube.xxxx", play, false);
        eb.addField("!skip", skip, false);
        eb.addField("!pause", pause, false);
        eb.addField("!resume", resume, false);
        eb.addField("!stop", stop, false);
        eb.addField("!purge", purge, false);
        eb.addField("!playlist playlistName || !playlist playlistName int", playlistCmd, false);
        eb.addField("!newPlaylist playlistName", newPlaylist, false);
        eb.addField("!add playlistName youtube.xx", add, false);
        eb.addField("!list", listPlaylist, false);
        eb.addField("!songList playlistName", listSonglist, false);
        eb.addField("!removePlaylist playlistName", removePlaylist, false);
        eb.addField("!remove playlistName songTitle", remove, false);

        event.getChannel().sendMessage(eb.build()).queue();
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, VoiceChannel vc) {
        connectToFirstVoiceChannel(guild.getAudioManager(),vc);

        musicManager.scheduler.queue(track);

    }

    public void getQueueTitles(GuildMessageReceivedEvent event) {
        try {
            int size = musicManagers.get(event.getGuild().getIdLong()).scheduler.getSize();
            ArrayList<String> titles = musicManagers.get(event.getGuild().getIdLong()).scheduler.getList();
            String tmp = "";
            if(size > 0 && titles.size() > 0 ){
                for(String song : titles){
                    tmp = tmp + song + "\n";
                }

                EmbedBuilder playlistEB = new EmbedBuilder();
                playlistEB.setColor(Color.RED);
                playlistEB.setTitle("Songs in current queue");
                playlistEB.addField("", tmp, true);
                event.getChannel().sendMessage(playlistEB.build()).queue();
            } else {
                event.getChannel().sendMessage("No songs in current queue").queue();
            }
        } catch (Exception e){
            event.getChannel().sendMessage("No songs in current queue").queue();
        }

    }

    private void setVolume(GuildMusicManager musicManager, int volume){
        musicManager.player.setVolume(volume);
    }

    public void setVolumeCommand(String[] command, GuildMessageReceivedEvent event){
        if(command.length == 2){
            TextChannel channel = event.getChannel();
            GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
            try {
                setVolume(musicManagers.get(event.getGuild().getIdLong()), Integer.valueOf(command[1]));
            } catch (Exception e) {
                event.getChannel().sendMessage("Error, enter valid number");
            }
            event.getChannel().sendMessage("Volumed changed to: " + command[1]).queue();
        } else {
            event.getChannel().sendMessage("Invalid syntax: !volume integer");
        }
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

    public void shuffle(String[] command, GuildMessageReceivedEvent event, Playlist playlist){
        ArrayList<String> copy = new ArrayList<>();
        for(String url : playlist.getPlaylist()){
            copy.add(url);
        }
        System.out.println("Old order = " + copy.toString());
        ArrayList<String> shuffled = new ArrayList<>();
        Random random = new Random();
        while (!copy.isEmpty()){
         int rand = random.nextInt(copy.size());
         shuffled.add(copy.get(rand));
         copy.remove(rand);
        }

        System.out.println("Copy playlist size: " + copy.size());
        System.out.println("new Order : " + shuffled.toString());
        VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();

        for(int x = 0; x < shuffled.size(); x++){
            loadAndPlayQuiet(event.getChannel(), shuffled.get(x), voiceChannel);
        }


    }

    // Title of youtube url
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


    public void newPlaylist(String[] command, GuildMessageReceivedEvent event){
        if (command.length == 2) {
            Playlist playlist = new Playlist(command[1], new ArrayList<String>());
            musicPlaylist.add(playlist);
            event.getChannel().sendMessage("Added " + playlist.getName() + " to playlists").queue();

            File playlistFolder = new File(System.getProperty("users.dir") + "playlists");

            if(playlistFolder.exists() && playlistFolder.isDirectory()){
                try{
                    File newPlaylistFile = new File(playlistFolder.getAbsolutePath() + "/" + command[1] + ".txt");
                    newPlaylistFile.createNewFile();
                    System.out.println("Creating new playing to dir: " + newPlaylistFile.getName());
                } catch (IOException e){
                    System.out.println("Error creating file: " + e);
                }
            }

        } else {
            event.getChannel().sendMessage("Syntax error:  !newPlaylist playlistName").queue();
        }
    }


    public void removePlaylist(String[] command, GuildMessageReceivedEvent event){
        if(command.length == 2){
            for(int x = 0; x < musicPlaylist.size(); x ++){
                if(command[1].equals(musicPlaylist.get(x).getName())){
                    musicPlaylist.remove(x);

                    // If found in memory arraylist it must exist as a text file.
                    File playlistFolder = new File(System.getProperty("users.dir") + "playlists");
                    File playlistFile = new File(playlistFolder.getAbsolutePath() + "/" + command[1] + ".txt");
                    playlistFile.delete();

                    if(!playlistFile.exists()) {
                        event.getChannel().sendMessage("Successfully removed " + command[1]).queue();
                    }
                    break;
                } else if (x == musicPlaylist.size() -1){
                    event.getChannel().sendMessage("Playlist doesn't exist: " + command[1]).queue();
                }
            }
        } else {  event.getChannel().sendMessage("Syntax error:  !removePlaylist playlistName").queue();}
    }


    public void listSongs(String[] command, GuildMessageReceivedEvent event) {
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

                } else {
                    if( x == musicPlaylist.size()-1 && !musicPlaylist.get(x).getName().equals(listName)){
                        event.getChannel().sendMessage("Playlist does not exist").queue();
                    }
                }
            }
        }
    }

    public void listPlaylists(String[] command, GuildMessageReceivedEvent event) {
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
    }

    public void addSongToPlaylist(String[] command, GuildMessageReceivedEvent event){
        String name = null;
        String url = null;

        try{
            name = command[1];
            url = command[2];

        } catch (Exception e){
            event.getChannel().sendMessage("Invalid Sytax:  !add playlistName youtube.xx").queue();
        }
        // Error check
        if(name!=null && url !=null && url.contains("youtube")) {
            if (name.isEmpty()) {
                event.getChannel().sendMessage("Error, please provide a playlist").queue();
            } else if (url.isEmpty()) {
                event.getChannel().sendMessage("Error, please provide a YouTube URL").queue();
            } else {

                // Add into current memory playlist
                for (int x = 0; x < musicPlaylist.size(); x++) {
                    if (name.equals(musicPlaylist.get(x).getName())) {
                        musicPlaylist.get(x).addURL(url);
                        event.getChannel().sendMessage("Adding " + getTitleQuietly(url) + " to " + musicPlaylist.get(x).getName()).queue();
                        break;
                    } else if (x == musicPlaylist.size() - 1 && !name.equals(musicPlaylist.get(x).getName())) {
                        event.getChannel().sendMessage("Cannot add to playlist " + command[1].toString() + ". Playlist does not exist").queue();
                    }
                }

                // Add into txt file for persistence
                try {
                    File playlistFolder = new File(System.getProperty("users.dir") + "playlists");
                    File files[] = playlistFolder.listFiles();

                    for(int x = 0; x < files.length; x++){
                        String fName = files[x].getName();
                        fName = fName.replace(".txt", "");

                        if(fName.equals(name)){
                            if(files[x].length() < 1) {
                                System.out.println("Writing to file: " + name + " with data : " + url);
                                FileWriter myWriter = new FileWriter(files[x], true);
                                myWriter.write(url);

                                myWriter.close();
                            } else {
                                System.out.println("Writing to file: " + name + " with data : " + url);
                                FileWriter myWriter = new FileWriter(files[x], true);
                                myWriter.write("\n" + url);

                                myWriter.close();
                            }
                            break;
                        }
                    }

                }catch (IOException e){

                }

            }
        } else {
            event.getChannel().sendMessage("Invalid syntax: !add playlistName youtube.xxx").queue();
        }
    }

    public void remove(String[] command, GuildMessageReceivedEvent event){
        if(command.length > 3 ){
            Message message = event.getMessage();
            String msgContent = message.getContentRaw();
            System.out.println("Command > 3");

            msgContent = msgContent.replace("!remove ", "");
            String[] split = msgContent.split("\\s+");


            if(split.length > 1){
                System.out.println("split length > 1");
                for(int x = 0; x < musicPlaylist.size(); x ++){

                    if(split[0].equals(musicPlaylist.get(x).getName())){
                        // Remove playlist name from msg content. Can be achieved by calling commands[2]
                        // need to redo this section later.
                        msgContent = msgContent.replaceAll(split[0]+" ", "");
                        boolean ret = musicPlaylist.get(x).removeSongByTitle(msgContent);

                        // Removing line from txt file
                        File playlistFolder = new File(System.getProperty("users.dir") + "playlists");
                        File playlistFile = new File(playlistFolder.getAbsolutePath() + "/" + musicPlaylist.get(x).getName() + ".txt");

                        boolean removed = removeUrlFromFile(playlistFile, msgContent);
                        if(removed){ event.getChannel().sendMessage("Removed : " + ret).queue();}
                        break;
                    } else if (x == musicPlaylist.size()-1){
                        event.getChannel().sendMessage("Cannot find song").queue();
                    }
                }
            }
        } else {
            event.getChannel().sendMessage("Invalid syntax: !remove playListName SongTitle");
        }
    }

    public void addPlaylistToQueue(String[] command, GuildMessageReceivedEvent event){
        // If loading playlist once
        if(command.length == 2) {
            String playName = null;
            try {
                playName = command[1];
            } catch (ArrayIndexOutOfBoundsException e) {

            }
            if (playName == null) {
                event.getChannel().sendMessage("Specify Playlist.. ").queue();
            } else {
                boolean exists = false;
                boolean hasLoaded = false;
                for (Playlist p : musicPlaylist) {
                    if (p.getName().equals(command[1])) {
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
                                loadAndPlayQuiet(event.getChannel(), s, voiceChannel);
                                hasLoaded = true;
                            }
                        }
                    }
                }
                if(hasLoaded){
                    event.getChannel().sendMessage("Playlist " + playName + " added to queue").queue();
                } 
                if (!exists) {
                    event.getChannel().sendMessage("Playlist not found, try again").queue();
                }
            }
            // if attempting to load playlist X times to loop.
        } else if(command.length == 3){

            String playName = null;
            int repeat = 0;
            try{
                repeat = Integer.parseInt(command[2]);
            } catch (Exception e){
                event.getChannel().sendMessage("Syntax error: Enter valid number, e.g !playlist playListName 4");
            }
            try {
                playName = command[1];
            } catch (ArrayIndexOutOfBoundsException e) {

            }
            if (playName == null || repeat == 0) {
                event.getChannel().sendMessage("Syntax error: !playlist playlistName int").queue();
            } else {
                boolean exists = false;

                for (Playlist p : musicPlaylist) {
                    if (p.getName().equals(command[1])) {
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
                                for(int x = 0; x < repeat; x++) {
                                    loadAndPlayQuiet(event.getChannel(), s, voiceChannel);
                                }
                            }
                            event.getChannel().sendMessage("Loading " + playName + " " + repeat + " times").queue();
                        }
                    }
                }
                if (!exists) {
                    event.getChannel().sendMessage("Playlist not found, try again").queue();
                }
            }
        }

    }

    public int getQueueSize(GuildMusicManager manager){
        return manager.scheduler.getSize();
    }

    public boolean removeUrlFromFile(File playlist, String title){
        File inputFile = playlist;
        String tmp = playlist.getAbsolutePath().replace(".txt","") + "tmp" + ".txt";
        File tempFile = new File(tmp);
        if(inputFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                String lineToRemove = title;
                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    // trim newline when comparing with lineToRemove
                    String trimmedLine = currentLine.trim();
                    //  if (trimmedLine.equals(lineToRemove)) continue;
                    if (getTitleQuietly(trimmedLine).equals(lineToRemove)) {
                        System.out.println("txt file lines mathc");
                        continue;
                    }

                    writer.write(currentLine + System.getProperty("line.separator"));
                }
                writer.close();
                reader.close();
            } catch (Exception e) {
                System.out.println("Error while removing url: " + e);
            }
            playlist.delete();
            return tempFile.renameTo(playlist);

        } else {System.out.println("Playlist file not found"); return  false;}
    }

    // init Directories for playlists, read existing playlists.
    public Thread init(){
        Thread t = new Thread(){
            public void run() {
                String workingDir = System.getProperty("user.dir");
                File playlists = new File(System.getProperty("users.dir") + "playlists");

                if(playlists.exists() && playlists.isDirectory()){
                    System.out.println("Environment existing - Ready..");
                    File[] listFiles = playlists.listFiles();

                    if(listFiles.length <=0){
                        System.out.println("No playlists to read.");
                    } else {
                        for(int x = 0; x < listFiles.length; x++){
                            try {
                                ArrayList<String> urlList = new ArrayList<>();
                                Scanner myReader = new Scanner(listFiles[x]);
                                while (myReader.hasNextLine()){
                                    String url = myReader.nextLine();
                                    urlList.add(url);
                                }
                                musicPlaylist.add(new Playlist(listFiles[x].getName().replace(".txt", ""), urlList));
                                System.out.println("Loaded playlist: " + listFiles[x].getName());
                                myReader.close();
                            }catch (FileNotFoundException e){

                            }
                        }
                    }
                } else {
                    System.out.println("Creating environment directories..");
                    playlists.mkdir();
                    System.out.println("Complete: " + playlists.exists());
                }

                for(Playlist p : musicPlaylist){
                    System.out.println("Playlist in memory: " + p.getName());

                }
            }
            } ;
        return  t;
        }





    // Inner class for game type playlists. Arraylist gets populated from text files.
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

            for(int x = 0; x < urlList.size(); x ++){
                if(getTitleQuietly(urlList.get(x)).equals(title)){
                    urlList.remove(x);
                    System.out.println("MATCHED TITLE");
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