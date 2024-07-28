package wily.legacy.client.screen;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import wily.legacy.Legacy4JClient;
import wily.legacy.client.ControlType;
import wily.legacy.client.LegacyOptions;
import wily.legacy.client.controller.ControllerBinding;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static wily.legacy.client.screen.ControlTooltip.*;

public class SettingsScreen extends PanelVListScreen {

    protected SettingsScreen(Screen parent) {
        super(parent, s-> Panel.centered(s,170,128,0,10),Component.translatable("legacy.menu.settings"));
        renderableVList.addRenderable(openScreenButton(Component.translatable("legacy.menu.game_options"), ()-> prepareGameOptionsScreen(new PanelVListScreen(this,180,168, 0, 10, Component.translatable("legacy.menu.game_options"),minecraft.options.toggleCrouch(),((LegacyOptions)minecraft.options).hints(),((LegacyOptions)minecraft.options).autoSaveInterval(),((LegacyOptions)minecraft.options).directSaveLoad(),((LegacyOptions)minecraft.options).cursorMode(),minecraft.options.sensitivity(), ((LegacyOptions)minecraft.options).developerMode()),minecraft)).build());
        renderableVList.addRenderable(openScreenButton(Component.translatable("legacy.menu.audio"),()->prepareAudioScreen(new PanelVListScreenNoTitle(this,180,248,Component.translatable("legacy.menu.audio"), Streams.concat(Arrays.stream(SoundSource.values()).sorted(Comparator.comparingInt(s->s == SoundSource.MUSIC ? 0 : 1)).map(minecraft.options::getSoundSourceOptionInstance), Stream.of(((LegacyOptions)minecraft.options).caveSounds(),((LegacyOptions)minecraft.options).minecartSounds())).map(s-> s.createButton(minecraft.options,0,0,0)).toArray(AbstractWidget[]::new)))).build());
        renderableVList.addRenderable(openScreenButton(Component.translatable("legacy.menu.graphics"),()-> prepareGraphicsScreen(new PanelVListScreen(this,180,168, 0, 10,Component.translatable("legacy.menu.graphics"),minecraft.options.graphicsMode(), minecraft.options.renderDistance(), minecraft.options.framerateLimit(), ((LegacyOptions)minecraft.options).legacyGamma(),minecraft.options.fullscreen(),minecraft.options.ambientOcclusion(), minecraft.options.enableVsync()){
            @Override
            public void addControlTooltips(Renderer renderer) {
                super.addControlTooltips(renderer);
            }
        })).build());
        renderableVList.addRenderable(openScreenButton(Component.translatable("legacy.menu.user_interface"),()-> prepareUserInterfaceScreen(new PanelVListScreen(this,180,148, 0, 10,Component.translatable("legacy.menu.user_interface"),((LegacyOptions)minecraft.options).tooltipBoxes(),((LegacyOptions)minecraft.options).selectedControlIcons(),((LegacyOptions)minecraft.options).hudScale(),((LegacyOptions)minecraft.options).hudOpacity(),((LegacyOptions)minecraft.options).interfaceSensitivity(),((LegacyOptions)minecraft.options).classicCrafting(), minecraft.options.showSubtitles()))).build());
        renderableVList.addRenderable(openScreenButton(Component.translatable("legacy.menu.reset_defaults"),()->new ConfirmationScreen(this,Component.translatable("legacy.menu.reset_settings"),Component.translatable("legacy.menu.reset_message"), b1->{
            Legacy4JClient.resetVanillaOptions(minecraft);
            minecraft.setScreen(this);
        })).build());
    }
    public PanelVListScreen prepareGameOptionsScreen(PanelVListScreen s, Minecraft minecraft){
        s.renderableVList.addRenderable(openScreenButton(Component.translatable("options.language"), () -> new LegacyLanguageScreen(s, minecraft.getLanguageManager())).build());
        return s;
    }
    public PanelVListScreenNoTitle prepareAudioScreen(PanelVListScreenNoTitle screen){
        return screen;
    }
    public PanelVListScreen prepareGraphicsScreen(PanelVListScreen screen){
        Monitor monitor = minecraft.getWindow().findBestMonitor();
        int j = monitor == null ? -1: minecraft.getWindow().getPreferredFullscreenVideoMode().map(monitor::getVideoModeIndex).orElse(-1);
        screen.renderableVList.addOptions(0,new OptionInstance<>("options.fullscreen.resolution", OptionInstance.noTooltip(), (component, integer) -> {
            if (monitor == null)
                return Component.translatable("options.fullscreen.unavailable");
            if (integer == -1) {
                return Options.genericValueLabel(component, Component.translatable("options.fullscreen.current"));
            }
            VideoMode videoMode = monitor.getMode(integer);
            return Options.genericValueLabel(component, Component.translatable("options.fullscreen.entry", videoMode.getWidth(), videoMode.getHeight(), videoMode.getRefreshRate(), videoMode.getRedBits() + videoMode.getGreenBits() + videoMode.getBlueBits()));
        }, new OptionInstance.IntRange(-1, monitor != null ? monitor.getModeCount() - 1 : -1), j, integer -> {
            if (monitor == null)
                return;
            minecraft.getWindow().setPreferredFullscreenVideoMode(integer == -1 ? Optional.empty() : Optional.of(monitor.getMode(integer)));
        }));
        return screen;
    }
    public PanelVListScreen prepareUserInterfaceScreen(PanelVListScreen screen){
        return screen;
    }

    @Override
    protected void init() {
        super.init();
        renderableVList.init(this,panel.x + 10,panel.y + 6,panel.width - 20, 200);
    }

    @Override
    public void onClose() {
        super.onClose();
        onClose.accept(this);
    }



    //    public Button.Builder openScreenButton(Component component, Supplier<Screen> supplier) {
//        return Button.builder(component, button -> this.minecraft.setScreen(supplier.get()));
//    }
//
//    protected SettingsScreen(Screen screen) {
//        super(s-> new Panel(p-> (screen.width - 170) / 2, p-> (s.height - p.height) / 2 + 15,170,135), Component.translatable("legacy.menu.settings"));


//        renderableVList.init(this,((screen.width - 160) / 2) + 10,((screen.height - panel.height) / 2 + 30) + 6,panel.width - 20,panel.height);
    }

//    @Override
//    protected void init() {
//        panel.init();
//        addRenderableWidget(openScreenButton(Component.translatable("legacy.menu.game_options"), ()-> prepareGameOptionsScreen(new PanelVListScreen(this,180,152,Component.translatable("legacy.menu.game_options"),minecraft.options.toggleCrouch(),((LegacyOptions)minecraft.options).hints(),((LegacyOptions)minecraft.options).autoSaveInterval(),((LegacyOptions)minecraft.options).directSaveLoad(),((LegacyOptions)minecraft.options).cursorMode(),minecraft.options.sensitivity()),minecraft)).bounds(panel.x + 10, panel.y + 6,panel.width - 20,20).build());
//        addRenderableWidget(openScreenButton(Component.translatable("legacy.menu.audio"),()->prepareAudioScreen(new PanelVListScreen(this,180,218,Component.translatable("legacy.menu.audio"), Streams.concat(Arrays.stream(SoundSource.values()).sorted(Comparator.comparingInt(s->s == SoundSource.MUSIC ? 0 : 1)).map(minecraft.options::getSoundSourceOptionInstance), Stream.of(((LegacyOptions)minecraft.options).caveSounds(),((LegacyOptions)minecraft.options).minecartSounds())).map(s-> s.createButton(minecraft.options,0,0,0)).toArray(AbstractWidget[]::new)))).bounds(panel.x + 10, panel.y + 31,panel.width - 20,20).build());
//        addRenderableWidget(openScreenButton(Component.translatable("legacy.menu.graphics"),()-> prepareGraphicsScreen(new PanelVListScreen(this,180,168,Component.translatable("legacy.menu.graphics"),minecraft.options.graphicsMode(), minecraft.options.renderDistance(), minecraft.options.framerateLimit(), ((LegacyOptions)minecraft.options).legacyGamma(),minecraft.options.fullscreen(),minecraft.options.ambientOcclusion(), minecraft.options.enableVsync()){
//            @Override
//            public void addControlTooltips(Renderer renderer) {
//                super.addControlTooltips(renderer);
//            }
//        })).bounds(panel.x + 10, panel.y + 56,panel.width - 20,20).build());
//        addRenderableWidget(openScreenButton(Component.translatable("legacy.menu.user_interface"),()-> prepareUserInterfaceScreen(new PanelVListScreen(this,180,148,Component.translatable("legacy.menu.user_interface"),((LegacyOptions)minecraft.options).tooltipBoxes(),((LegacyOptions)minecraft.options).selectedControlIcons(),((LegacyOptions)minecraft.options).hudScale(),((LegacyOptions)minecraft.options).hudOpacity(),((LegacyOptions)minecraft.options).interfaceSensitivity(),((LegacyOptions)minecraft.options).classicCrafting(), minecraft.options.showSubtitles()))).bounds(panel.x + 10, panel.y + 81,panel.width - 20,20).build());
//        addRenderableWidget(openScreenButton(Component.translatable("legacy.menu.reset_defaults"),()->new ConfirmationScreen(this,Component.translatable("legacy.menu.reset_settings"),Component.translatable("legacy.menu.reset_message"), b1->{
//            Legacy4JClient.resetVanillaOptions(minecraft);
//            minecraft.setScreen(this);
//        })).bounds(panel.x + 10, panel.y + 106,panel.width - 20,20).build());
//    }
//
//    @Override
//    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
//        super.renderBackground(guiGraphics, i, j, f);
//        panel.render(guiGraphics,i,j,f);
//    }
//
//    public PanelVListScreen prepareGameOptionsScreen(PanelVListScreen s, Minecraft minecraft){
//        s.renderableVList.addRenderable(openScreenButton(Component.translatable("options.language"), () -> new LegacyLanguageScreen(s, minecraft.getLanguageManager())).build());
//        return s;
//    }
//    public PanelVListScreen prepareAudioScreen(PanelVListScreen screen){
//        return screen;
//    }
//    public PanelVListScreen prepareGraphicsScreen(PanelVListScreen screen){
//        Monitor monitor = minecraft.getWindow().findBestMonitor();
//        int j = monitor == null ? -1: minecraft.getWindow().getPreferredFullscreenVideoMode().map(monitor::getVideoModeIndex).orElse(-1);
//        screen.renderableVList.addOptions(0,new OptionInstance<>("options.fullscreen.resolution", OptionInstance.noTooltip(), (component, integer) -> {
//            if (monitor == null)
//                return Component.translatable("options.fullscreen.unavailable");
//            if (integer == -1) {
//                return Options.genericValueLabel(component, Component.translatable("options.fullscreen.current"));
//            }
//            VideoMode videoMode = monitor.getMode(integer);
//            return Options.genericValueLabel(component, Component.translatable("options.fullscreen.entry", videoMode.getWidth(), videoMode.getHeight(), videoMode.getRefreshRate(), videoMode.getRedBits() + videoMode.getGreenBits() + videoMode.getBlueBits()));
//        }, new OptionInstance.IntRange(-1, monitor != null ? monitor.getModeCount() - 1 : -1), j, integer -> {
//            if (monitor == null)
//                return;
//            minecraft.getWindow().setPreferredFullscreenVideoMode(integer == -1 ? Optional.empty() : Optional.of(monitor.getMode(integer)));
//        }));
//        return screen;
//    }
//    public PanelVListScreen prepareUserInterfaceScreen(PanelVListScreen screen){
//        return screen;
//    }
