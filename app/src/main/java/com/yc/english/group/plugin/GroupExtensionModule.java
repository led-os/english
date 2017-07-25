package com.yc.english.group.plugin;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

/**
 * Created by wanglin  on 2017/7/17 18:31.
 */

public class GroupExtensionModule extends DefaultExtensionModule {

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        super.getPluginModules(conversationType);
        List<IPluginModule> pluginModules = new ArrayList<>();

        pluginModules.add(new TaskPlugin());
        pluginModules.add(new PicturePlugin());
        pluginModules.add(new VoicePlugin());
        pluginModules.add(new FilePlugin());
        return pluginModules;
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        return super.getEmoticonTabs();
    }
}