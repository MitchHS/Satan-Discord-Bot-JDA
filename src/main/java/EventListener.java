import com.sun.jdi.event.Event;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;

import javax.annotation.Nonnull;

public class EventListener implements net.dv8tion.jda.api.hooks.EventListener {
    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if(event instanceof GuildVoiceGuildMuteEvent){
            Member member = ((GuildVoiceGuildMuteEvent) event).getMember();
            System.out.println(member.getEffectiveName() + member.getVoiceState().isGuildMuted());

            if(member.getEffectiveName().contains("SonicLiquid")){
                if(!member.getVoiceState().isGuildMuted()){
                    System.out.println("Event > unmuted");
                    ((GuildVoiceGuildMuteEvent) event).getGuild().mute(member, true).queue();

                }
            }


    }
    }
}
