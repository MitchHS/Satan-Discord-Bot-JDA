

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
  private final AudioPlayer player;
  public final BlockingQueue<AudioTrack> queue;
  public MusicListener musicListener;
  public boolean loop;
  public MusicListener.Playlist playlist;
  public TextChannel tc;
  public VoiceChannel vc;

  /**
   * @param player The audio player this scheduler uses
   */
  public TrackScheduler(AudioPlayer player) {
    this.player = player;
    this.queue = new LinkedBlockingQueue<>();
  }

  /**
   * Add the next track to queue or play right away if nothing is in the queue.
   *
   * @param track The track to play or add to queue.
   */
  public void queue(AudioTrack track) {
    // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
    // something is playing, it returns false and does nothing. In that case the player was already playing so this
    // track goes to the queue instead.
    if (!player.startTrack(track, true)) {
      queue.offer(track);
    }
  }

  /**
   * Start the next track, stopping the current one if it is playing.
   */
  public void nextTrack() {
    // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
    // giving null to startTrack, which is a valid argument and will simply stop the player.
    player.startTrack(queue.poll(), false);
  }

  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
    if (endReason.mayStartNext) {
      if(loop){
        if(getSize() <= 1){
          LoopSongs(playlist.getPlaylist(), vc, tc);
        }
        nextTrack();
      } else {
        nextTrack();
      }
    }
  }

  public int getSize(){
    return queue.size();
  }

  public ArrayList<String> getList() {
    ArrayList<String> titles = new ArrayList<>();
    Iterator iter = this.queue.iterator();
    while (iter.hasNext()){
      for(int x = 0; x< this.queue.size(); x++){
        AudioTrack obj = (AudioTrack) iter.next();
        AudioTrackInfo info = (AudioTrackInfo) obj.getInfo();
        titles.add(info.title);
      }
    }
    return titles;

    // this.queue.stream.map(AudioTrack::getInfo).map(AudioTrackInfo::title).collect();

  }

  public void LoopSongs(ArrayList<String> songlist, VoiceChannel vc, TextChannel tc){
    if(this.musicListener== null){
      throw new NullPointerException("MusicListener cannot be null");
    } else {
      for(int x = 0; x < songlist.size(); x ++ ){
        musicListener.loadAndPlayQuiet(tc, songlist.get(x), vc);
      }

    }
  }

  public boolean isLooping(boolean bool){
    this.loop = bool;
    return true;
  }

  public void setMusicListener(MusicListener m, VoiceChannel vc, TextChannel tc, MusicListener.Playlist playlist){
    this.musicListener = m;
    this.vc = vc;
    this.tc = tc;
    this.playlist = playlist;
  }



  public void purge() {
    this.player.stopTrack();
    this.queue.clear();

  }
}
