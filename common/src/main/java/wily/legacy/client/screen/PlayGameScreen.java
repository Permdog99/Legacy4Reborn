package wily.legacy.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.compress.utils.FileNameUtils;
import wily.legacy.Legacy4J;
import wily.legacy.Legacy4JClient;
import wily.legacy.client.CommonColor;
import wily.legacy.client.ControlType;
import wily.legacy.client.LegacyWorldTemplate;
import wily.legacy.client.screen.compat.FriendsServerRenderableList;
import wily.legacy.client.controller.ControllerBinding;
import wily.legacy.util.LegacySprites;
import wily.legacy.util.ScreenUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static wily.legacy.client.screen.ControlTooltip.getAction;

public class PlayGameScreen extends PanelVListScreen implements ControlTooltip.Event,RenderableVList.Access{
    private static final Component SAFETY_TITLE = Component.translatable("multiplayerWarning.header").withStyle(ChatFormatting.BOLD);
    private static final Component SAFETY_CONTENT = Component.translatable("multiplayerWarning.message");
    private static final Component SAFETY_CHECK = Component.translatable("multiplayerWarning.check");
    public static final Component DIRECT_CONNECTION = Component.translatable("selectServer.direct");
    public boolean isLoading = false;
    private PlayGameScreen screen;
    protected final TabList tabList = new TabList().add(30,0,Component.translatable("legacy.menu.load"), b-> repositionElements());
    private final ServerStatusPinger pinger = new ServerStatusPinger();
    public final SaveRenderableList saveRenderableList = new SaveRenderableList(this);
    protected final ServerRenderableList serverRenderableList = PublishScreen.hasWorldHost() ? new FriendsServerRenderableList() : new ServerRenderableList();
    private final CreationList creationList = new CreationList();
    @Override
    public void addControlTooltips(ControlTooltip.Renderer renderer) {
        super.addControlTooltips(renderer);
        Supplier<Boolean> saveOptions = ()-> saveRenderableList.renderables.stream().anyMatch(r-> saveRenderableList.renderables.indexOf(r) > 1 && r instanceof GuiEventListener l && l.isFocused());
        renderer.add(()-> ControlType.getActiveType().isKbm() ? ControlTooltip.getKeyIcon(InputConstants.KEY_O) : ControllerBinding.UP_BUTTON.bindingState.getIcon(),()->saveOptions.get() || serverRenderableList.renderables.stream().anyMatch(r-> serverRenderableList.renderables.indexOf(r) > 0 && r instanceof GuiEventListener l && l.isFocused()) ? getAction(saveOptions.get() ? "legacy.menu.save_options" : "legacy.menu.server_options") : null);
        renderer.add(()-> serverRenderableList.renderables.stream().anyMatch(r-> serverRenderableList.renderables.indexOf(r) > 0 && r instanceof GuiEventListener l && l.isFocused()) ? ControlType.getActiveType().isKbm() ? ControlTooltip.getKeyIcon(InputConstants.KEY_X) : ControllerBinding.LEFT_BUTTON.bindingState.getIcon(): null, ()->DIRECT_CONNECTION);
    }
    public PlayGameScreen(Screen parent, int initialTab) {
        super(s-> Panel.centered(s,520,214,0,30),Component.translatable("legacy.menu.play_game"));
        this.parent = parent;
        tabList.selectedTab = initialTab;
    }
    public PlayGameScreen(Screen parent) {
        this(parent,0);
    }
    protected boolean canNotifyOnlineFriends(){
        return serverRenderableList.hasOnlineFriends() && Util.getMillis() % 1000 < 500;
    }
    @Override
    protected void init() {
        panel.init();
        saveRenderableList.init(this,panel.x + 19,panel.y + 31,(panel.width/2) - 18 - 11, 30 * 6);
        serverRenderableList.init(this,panel.x + 9 + (panel.width/2 - 4) + 6,panel.y + 31,(panel.width/2) - 18 - 11, 30 * 6);
    }

    @Override
    public void renderDefaultBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        ScreenUtil.renderDefaultBackground(guiGraphics,true);
        panel.render(guiGraphics,i,j,f);
        guiGraphics.blitSprite(LegacySprites.PANEL_RECESS, panel.x + 9, panel.y + 11, panel.width/2 - 10, panel.height - 23);
        guiGraphics.blitSprite(LegacySprites.PANEL_RECESS, panel.x + 9 + (panel.width/2 - 9 - 4) + 6, panel.y + 11, panel.width/2 - 10, panel.height - 23);
        guiGraphics.drawString(this.font, "Start Game", panel.width/4 + (font.width("Start Game") - font.width("Start Game")/2) + 9,panel.y + 19, CommonColor.INVENTORY_GRAY_TEXT.get(), false);
        guiGraphics.drawString(this.font, "Join Game", (panel.width/2 + panel.width/4) + (font.width("Join Game") - font.width("Join Game")/2) + 9,panel.y + 19, CommonColor.INVENTORY_GRAY_TEXT.get(), false);
    }

    @Override
    public RenderableVList getRenderableVList() {
        if (tabList.selectedTab == 2) return serverRenderableList;
        else if (tabList.selectedTab == 1) return creationList;
        return saveRenderableList;
    }

    @Override
    public void removed() {
        if (this.saveRenderableList != null) {
            SaveRenderableList.resetIconCache();
        }
        if (serverRenderableList.lanServerDetector != null) {
            serverRenderableList.lanServerDetector.interrupt();
            serverRenderableList.lanServerDetector = null;
        }
        this.pinger.removeAll();
    }
    @Override
    public void tick() {
        super.tick();
        List<LevelSummary> summaries = saveRenderableList.pollLevelsIgnoreErrors();
        if (summaries != saveRenderableList.currentlyDisplayedLevels) {
            saveRenderableList.fillLevels("",summaries);
            repositionElements();

        }

        List<LanServer> list = serverRenderableList.lanServerList.takeDirtyServers();
        if (list != null) {
            if (serverRenderableList.lanServers == null || !new HashSet<>(serverRenderableList.lanServers).containsAll(list)) {
                serverRenderableList.lanServers = list;
                serverRenderableList.updateServers();
                rebuildWidgets();
            }
        }
        this.pinger.tick();

    }




    @Override
    public boolean keyPressed(int i, int j, int k) {
        tabList.controlTab(i);
        tabList.directionalControlTab(i);
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        if (i == 294) {
            if (tabList.selectedTab == 0) {
                saveRenderableList.reloadSaveList();
            } else if (tabList.selectedTab == 2) {
                serverRenderableList.updateServers();
            }
            this.rebuildWidgets();
            return true;
        }
        if (i == InputConstants.KEY_X && tabList.selectedTab == 2){
            EditBox serverBox = new EditBox(Minecraft.getInstance().font, 0,0,200,20,DIRECT_CONNECTION);
            minecraft.setScreen(new ConfirmationScreen(this, serverBox.getMessage(),Component.translatable("addServer.enterIp"), b1->  ConnectScreen.startConnecting(this, minecraft, ServerAddress.parseString(serverBox.getValue()), new ServerData("","", ServerData.Type.OTHER), false)){
                boolean released = false;
                @Override
                protected void addButtons() {
                    super.addButtons();
                    okButton.active = false;
                }
                @Override
                public boolean charTyped(char c, int i) {
                    if (!released) return false;
                    return super.charTyped(c, i);
                }
                @Override
                public boolean keyReleased(int i2, int j, int k) {
                    if (i2 == i) released = true;
                    return super.keyReleased(i2, j, k);
                }
                @Override
                protected void init() {
                    super.init();
                    serverBox.setPosition(panel.getX() + 15, panel.getY() + 45);
                    serverBox.setResponder(s-> okButton.active = ServerAddress.isValidAddress(s));
                    addRenderableWidget(serverBox);
                }
            });
            return true;
        }
        return false;
    }

    public ServerStatusPinger getPinger() {
        return this.pinger;
    }

    public ServerList getServers() {
        return serverRenderableList.servers;
    }
    public void onFilesDrop(List<Path> list) {
        if (tabList.selectedTab == 0) {
            for (Path path : list) {
                if (!path.getFileName().toString().endsWith(".mcsave") && !path.getFileName().toString().endsWith(".zip")) return;
            }
            String string = list.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
            minecraft.setScreen(new ConfirmationScreen(this, Component.translatable("legacy.menu.import_save"), Component.translatable("legacy.menu.import_save_message", string), (b) -> {
                list.forEach(p -> {
                    try {
                        Legacy4JClient.importSaveFile(new FileInputStream(p.toFile()),minecraft.getLevelSource(), FileNameUtils.getBaseName(p.getFileName().toString()));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                });
                minecraft.setScreen(this);
                saveRenderableList.reloadSaveList();
            }));
        }
    }
}
