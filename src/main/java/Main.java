
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main extends ListenerAdapter {


//     public static void main(String[] args) {
//          JDA jda;
//          String token = "NzE2NTMwMDI1MzQzNzQ2MTI5.XtTk3Q.JHgRm1rw98rvA8Ls_QrqleElyWI";
//
//          try{
//                jda = new JDABuilder("token").build();
//          } catch (Exception e) {}
//
//
//     }

    public static void main(String[] args) throws LoginException

    {
        String menuOption;
        String token = "NzE2NTMwMDI1MzQzNzQ2MTI5.XtTk3Q.JHgRm1rw98rvA8Ls_QrqleElyWI";
        JDA jda = null;
        MusicListener musicListener = new MusicListener();
        VoiceEventListener voiceEventListener = new VoiceEventListener();
        MessageListener messageListener = new MessageListener();
        ServerConnectionListener serverConnectionListener = new ServerConnectionListener();

        Scanner in = new Scanner(System.in);
        jda = new JDABuilder(token).build();
                    jda.addEventListener(voiceEventListener);
                    jda.addEventListener(messageListener);
                    jda.addEventListener(musicListener);
                    jda.addEventListener(serverConnectionListener);


        System.out.println("Connected to server");
        musicListener.init();

//        do {
//            System.out.print("\n -- SATAN DISCORD BOT -- \n");
//            System.out.println("NOTE: All users are added via their official discord name not nickname");
//            System.out.print("Select Option:\n");
//            System.out.print("B - Add user/s to voice blacklist. Syntax: user user2 \n");
//            System.out.print("W - Add user to immunity mute whitelist Syntax: user user2\n");
//            System.out.print("I - Add user to voice channel intercept list Syntax: user user2\n");
//            System.out.print("R - Remove all users from troll/intercept list\n");
//            System.out.print("P - Print Users in every list\n");
//            System.out.print("C - Connect bot and add listeners\n");
//            System.out.print("D - Disconnect bot\n");
//            System.out.print("Q - Quit app and disconnect bot\n");
//
//            menuOption = in.next();
//
//
//            switch (menuOption) {
//                case "B":
//                    if(voiceEventListener!=null){
//                        voiceEventListener.setBlacklist();
//                    }
//                    break;
//                case "W":
//                    if(voiceEventListener!=null){
//                        voiceEventListener.setWhitelist();
//                    }
////                    if(jda!=null){
////                    if (jda.getStatus().equals(JDA.Status.CONNECTED)) {
////                        jda.shutdownNow();
////                        System.out.println("Restarting bot");
////                        jda = new JDABuilder(token).build();
////                        jda.addEventListener(voiceEventListener);
////                        jda.addEventListener(messageListener);
////                        jda.addEventListener(new MusicListener());
////                        System.out.println("Completed");
////                    }
////                    }
//                    break;
//                case "I":
//                    if(voiceEventListener!=null){
//                        voiceEventListener.setInterceptList();
//                    }
////                    if(jda!=null){
////                        if (jda.getStatus().equals(JDA.Status.CONNECTED)) {
////                            jda.shutdownNow();
////                            System.out.println("Restarting bot");
////                            jda = new JDABuilder(token).build();
////                            jda.addEventListener(voiceEventListener);
////                            jda.addEventListener(messageListener);
////                            jda.addEventListener(new MusicListener());
////                            System.out.println("Completed");
////                        }
////                    }
//                    break;
//                case "D":
//                    // Disconnect
//                    if (jda != null) {
//                       try{ jda.getDirectAudioController().disconnect(jda.getGuildById(musicListener.guildID)); }
//                       catch (Exception e) {}
//                        jda.shutdown();
//                        System.out.println("Shutting down bot");
//                    } else { System.out.println("No bot available: Connect bot first");}
//                    break;
//                case "C":
//                    jda = new JDABuilder(token).build();
//                    jda.addEventListener(voiceEventListener);
//                    jda.addEventListener(messageListener);
//                    jda.addEventListener(new MusicListener());
//                    jda.addEventListener(new ServerConnectionListener());
//                    System.out.println("connected.");
//                    break;
//                case "Q":
//                    System.out.printf("\nQutting and disconnecting bot\n");
//                    if (jda != null) {
//                       // jda.getDirectAudioController().disconnect(jda.getGuildById(musicListener.guildID));
//                        jda.shutdown();
//                        System.out.println("Shutting down bot");
//                    } System.exit(0);
//                    break;
//                case "P":
//                    System.out.println("Blacklisted users: " + voiceEventListener.getVoiceBlacklist().toString());
//                    System.out.println("Whitelisted users: " + voiceEventListener.getWhitelist().toString());
//                    System.out.println("Voice intercept users: " + voiceEventListener.getInterceptList().toString());
//                    break;
//                case "R":
//                    if(voiceEventListener!=null){
//                        voiceEventListener.purgeBlackList();
//                        voiceEventListener.purgeInterceptList();
//                        System.out.println("Purged users from blacklist/intercept list");
//                    } else {System.out.println("Nothing to purge.. ");}
////                    if (jda != null) {
////                        jda.shutdownNow();
////                        System.out.println("Restarting bot");
////                        jda = new JDABuilder(token).build();
////                        jda.addEventListener(voiceEventListener);
////                        jda.addEventListener(messageListener);
////                        jda.addEventListener(musicListener);
////                        System.out.println("Completed");
////                    }
//                   break;
//                default:
//                    System.out.println("Enter valid option");
//                    break;
//            }
//
//
//        } while (menuOption.compareToIgnoreCase("Q") != 0);
//        System.exit(0);
    }







}
