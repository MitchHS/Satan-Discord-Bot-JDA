
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.List;

public class Main extends ListenerAdapter {

    String token = "NzE2NTMwMDI1MzQzNzQ2MTI5.XtTk3Q.JHgRm1rw98rvA8Ls_QrqleElyWI";
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


        JDA jda = new JDABuilder("NzE2NTMwMDI1MzQzNzQ2MTI5.XtTk3Q.JHgRm1rw98rvA8Ls_QrqleElyWI").build();


        jda.addEventListener(new MessageListener());
        jda.addEventListener(new EventListener());



    }



}
