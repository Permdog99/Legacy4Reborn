package wily.legacy.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import wily.legacy.Legacy4J;
import wily.legacy.Legacy4JClient;
import wily.legacy.client.CommonColor;
import wily.legacy.client.ControlType;
import wily.legacy.client.LegacyWorldSettings;
import wily.legacy.client.controller.ControllerBinding;
import wily.legacy.util.LegacySprites;
import wily.legacy.util.ScreenUtil;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiConsumer;

import static wily.legacy.client.screen.ControlTooltip.*;

public class LoadSaveScreen extends PanelBackgroundScreen {
    public static final Component GAME_MODEL_LABEL = Component.translatable("selectWorld.gameMode");
    private final boolean deleteOnClose;
    public BiConsumer<GameRules, MinecraftServer> applyGameRules = (r,s)-> {};
    protected final LevelStorageSource.LevelStorageAccess access;
    protected final LegacySliderButton<GameType> gameTypeSlider;
    public boolean trustPlayers;
    public boolean allowCommands;
    public static final List<ResourceKey<Level>> RESETTABLE_DIMENSIONS = new ArrayList<>(List.of(Level.NETHER));
    public final List<ResourceKey<Level>> dimensionsToReset = new ArrayList<>();
    public Difficulty difficulty;
    protected boolean changedGameType = false;
    public final LevelSummary summary;
    protected final PackSelector resourcePackSelector;
    protected Collection<String> originalSelectedPacks = Collections.emptyList();
    protected final TickBox onlineTickBox;
    protected final PublishScreen publishScreen;
    public static final List<GameType> GAME_TYPES = Arrays.stream(GameType.values()).toList();

    public LoadSaveScreen(Screen screen, LevelSummary summary, LevelStorageSource.LevelStorageAccess access, boolean deleteOnClose) {
        super(s-> new Panel(p-> (screen.width - 225) / 2, p-> (s.height - p.height) / 2 + 30,225,177), Component.translatable("legacy.menu.load_save.load"));
        this.deleteOnClose = deleteOnClose;
        this.parent = screen;
        this.summary = summary;
        this.access = access;
        difficulty = summary.getSettings().difficulty();
        gameTypeSlider = new LegacySliderButton<>(0,0, 220,16, b -> b.getDefaultMessage(GAME_MODEL_LABEL,b.getObjectValue().getLongDisplayName()),b->Tooltip.create(Component.translatable("selectWorld.gameMode."+b.getObjectValue().getName()+ ".info")),summary.getSettings().gameType(),()->GAME_TYPES, b-> changedGameType = true);
        gameTypeSlider.active = !summary.isHardcore();
        publishScreen = new PublishScreen(this, gameTypeSlider.getObjectValue());
        onlineTickBox = new TickBox(0,0,220,publishScreen.publish, b-> PublishScreen.PUBLISH, b->null, button -> {
            if (button.selected) minecraft.setScreen(publishScreen);
            button.selected = publishScreen.publish = false;
        });
        allowCommands = summary.hasCheats();
        trustPlayers = ((LegacyWorldSettings)(Object)summary.getSettings()).trustPlayers();
        List<String> packs = ((LegacyWorldSettings)(Object)summary.getSettings()).getSelectedResourcePacks();
        if (!packs.isEmpty()){
            originalSelectedPacks = Minecraft.getInstance().getResourcePackRepository().getSelectedIds();
            Minecraft.getInstance().getResourcePackRepository().setSelected(packs);
        }
        resourcePackSelector = PackSelector.resources(panel.x + 13, panel.y + 112, 220,45, !ScreenUtil.hasTooltipBoxes());
        if (!originalSelectedPacks.isEmpty()) Minecraft.getInstance().getResourcePackRepository().setSelected(originalSelectedPacks);

    }
    public LoadSaveScreen(Screen screen, LevelSummary summary) {
        this(screen,summary, getSummaryAccess(summary),false);
    }

    @Override
    public void addControlTooltips(Renderer renderer) {
        super.addControlTooltips(renderer);
        renderer.add(()-> ControlType.getActiveType().isKbm() ? COMPOUND_ICON_FUNCTION.apply(new Icon[]{getKeyIcon(InputConstants.KEY_LSHIFT), PLUS_ICON,getKeyIcon(InputConstants.MOUSE_BUTTON_LEFT)}) : ControllerBinding.LEFT_BUTTON.bindingState.getIcon(), ()-> getFocused() == resourcePackSelector ? getAction("legacy.action.resource_packs_screen") : null);
    }

    public static LevelStorageSource.LevelStorageAccess getSummaryAccess(LevelSummary summary){
        try {
            return Minecraft.getInstance().getLevelSource().createAccess(summary.getLevelId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void init() {
        panel.init();
        onlineTickBox.selected = publishScreen.publish;
        onlineTickBox.setPosition(panel.x+ 13, panel.y+65);
        addRenderableWidget(new TickBox(panel.x+ 13,panel.y+83,allowCommands, b->Component.translatable("selectWorld.allowCommands"), b->null, b-> allowCommands = b.selected));
        addRenderableWidget(onlineTickBox);
        RESETTABLE_DIMENSIONS.forEach(d-> addRenderableWidget(new TickBox(panel.x+ 13,panel.y+101,false, b-> Component.translatable("legacy.menu.load_save.reset",ScreenUtil.getDimensionName(d)), b-> null, t-> {
            if (t.selected) dimensionsToReset.add(d);
            else dimensionsToReset.remove(d);
        })));
        addRenderableWidget(new LegacySliderButton<>(panel.x + 13, panel.y + 119, 200,16, b -> b.getDefaultMessage(Component.translatable("options.difficulty"),b.getObjectValue().getDisplayName()),b->null, difficulty,()-> Arrays.asList(Difficulty.values()), b-> difficulty = b.getObjectValue())).active = !((LegacyWorldSettings)(Object)summary.getSettings()).isDifficultyLocked() && !summary.isHardcore();
        Button loadButton = addRenderableWidget(Button.builder(Component.translatable("legacy.menu.load_save.load"), button -> {
            try {
                this.onLoad();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).bounds(panel.x + 13, panel.y + 144,200,20).build());

        setInitialFocus(loadButton);
    }

    private void onLoad() throws IOException {
        access.close();
        dimensionsToReset.forEach(l-> {
            if (l == Level.OVERWORLD) return;
            try {
                deleteLevelDimension(access,l);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        resourcePackSelector.applyChanges(true, ()->minecraft.reloadResourcePacks().thenRun(this::completeLoad), this::completeLoad);
        if (!originalSelectedPacks.isEmpty()) Minecraft.getInstance().getResourcePackRepository().setSelected(originalSelectedPacks);
    }
    private void completeLoad(){
        loadWorld(this,minecraft,summary);
        Legacy4JClient.startServerConsumer = s-> {
            if (dimensionsToReset.contains(Level.END)) s.getLevel(Level.END).setDragonFight(new EndDragonFight(minecraft.getSingleplayerServer().getLevel(Level.END),minecraft.getSingleplayerServer().getWorldData().worldGenOptions().seed(), EndDragonFight.Data.DEFAULT));
            s.setDefaultGameType(gameTypeSlider.getObjectValue());
            s.setDifficulty(difficulty, false);
            applyGameRules.accept(s.getGameRules(), minecraft.getSingleplayerServer());
            if (publishScreen != null) {
                publishScreen.publish(s);
            }
            ((LegacyWorldSettings)s.getWorldData()).setAllowCommands(allowCommands);
            if (resourcePackSelector.hasChanged()) ((LegacyWorldSettings)s.getWorldData()).setSelectedResourcePacks(resourcePackSelector.getSelectedIds());
            if (changedGameType && summary.getGameMode() != gameTypeSlider.getObjectValue()) s.getPlayerList().getPlayer(minecraft.player.getUUID()).setGameMode(gameTypeSlider.getObjectValue());
        };
    }

    @Override
    public void onClose() {
        if (deleteOnClose) {
            try {
                access.deleteLevel();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        access.safeClose();
        super.onClose();
    }

    public static void deleteLevelDimension(LevelStorageSource.LevelStorageAccess access, ResourceKey<Level> dimension) throws IOException {
        Path path = access.getDimensionPath(dimension);
        Legacy4J.LOGGER.info("Deleting dimension {}", dimension);
        int i = 1;

        while(i <= 5) {
            Legacy4J.LOGGER.info("Attempt {}...", i);
            try {
                Files.walkFileTree(path,new SimpleFileVisitor<>(){
                    public FileVisitResult visitFile(Path pathx, BasicFileAttributes basicFileAttributes) throws IOException {
                        Legacy4J.LOGGER.debug("Deleting {}", pathx);
                        Files.delete(pathx);
                        return FileVisitResult.CONTINUE;
                    }
                    public FileVisitResult postVisitDirectory(Path pathx, @Nullable IOException iOException) throws IOException {
                        if (iOException != null) {
                            throw iOException;
                        } else {
                            Files.delete(pathx);
                            return FileVisitResult.CONTINUE;
                        }
                    }
                });
                break;
            } catch (IOException var6) {
                if (i >= 5) {
                    throw var6;
                }
                Legacy4J.LOGGER.warn("Failed to delete {}", path, var6);
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException var5) {
                }
                ++i;
            }
        }

    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (resourcePackSelector.scrollableRenderer.mouseScrolled(g)) return true;
        return super.mouseScrolled(d, e, f, g);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        panel.render(guiGraphics,i,j,f);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.5f,0,0);
        guiGraphics.blitSprite(LegacySprites.SQUARE_ENTITY_PANEL,panel.x + 12, panel.y + 9, 32,32);
        guiGraphics.pose().popPose();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0,0.5f,0);
        guiGraphics.blit(SaveRenderableList.iconCache.getUnchecked(summary).textureLocation(),panel.x + 13, panel.y + 10, 0,0,29,29,29,29);
        guiGraphics.drawString(font,summary.getLevelName(),panel.x + 47, panel.y + 12, CommonColor.INVENTORY_GRAY_TEXT.get(),false);
        guiGraphics.drawString(font,Component.translatable("legacy.menu.load_save.created_in", (summary.hasCheats() ? GameType.CREATIVE : GameType.SURVIVAL).getShortDisplayName()),panel.x + 47, panel.y + 29, CommonColor.INVENTORY_GRAY_TEXT.get(),false);
        guiGraphics.pose().popPose();
        guiGraphics.drawString(font,Component.translatable("commands.seed.success",((LegacyWorldSettings)(Object)summary.getSettings()).getDisplaySeed()),panel.x + 13, panel.y + 49, CommonColor.INVENTORY_GRAY_TEXT.get(),false);
    }

    public static void loadWorld(Screen screen, Minecraft minecraft, LevelSummary summary) {
        SaveRenderableList.resetIconCache();
        minecraft.createWorldOpenFlows().checkForBackupAndLoad(summary.getLevelId(), ()-> minecraft.setScreen(screen));
    }
}
