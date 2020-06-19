import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.File;

public class ServerConnectionListener extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
        Member member = event.getMember();
        String name = member.getUser().getId();
        TextChannel defaultText = event.getGuild().getDefaultChannel();


        //defaultText.sendMessage("U fucking wot " +"@\" + name").addFile(new File("C:/Users/Mitchell/Downloads/watchu.gif")).queue();
        defaultText.sendMessage("U fucking wot " +"<@" + name+">").addFile(new File("/home/pi/watchu.gif")).queue();
    }
}
