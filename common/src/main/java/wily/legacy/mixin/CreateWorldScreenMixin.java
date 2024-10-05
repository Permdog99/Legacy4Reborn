package wily.legacy.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wily.legacy.Legacy4J;
import wily.legacy.Legacy4JClient;
import wily.legacy.client.CommonColor;
import wily.legacy.client.LegacyOptions;
import wily.legacy.client.LegacyClientWorldSettings;
import wily.legacy.client.screen.*;
import wily.legacy.init.LegacyRegistries;
import wily.legacy.util.ScreenUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen implements ControlTooltip.Event{
    @Shadow @Final private WorldCreationUiState uiState;
    @Shadow @Final private static Component GAME_MODEL_LABEL;
    @Shadow @Final private static Component NAME_LABEL;

    @Shadow protected abstract void createNewWorld(PrimaryLevelData.SpecialWorldProperty arg, LayeredRegistryAccess<RegistryLayer> arg2, Lifecycle lifecycle);

    @Shadow public abstract void popScreen();

    @Shadow protected abstract Optional<LevelStorageSource.LevelStorageAccess> createNewWorldDirectory();

    @Shadow protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guiEventListener);

    protected boolean trustPlayers;
    protected Panel panel;
    protected PublishScreen publishScreen;
    protected Assort.Selector resourceAssortSelector;

    protected CreateWorldScreenMixin(Component component) {
        super(component);
    }
    private CreateWorldScreen self(){
        return (CreateWorldScreen) (Object) this;
    }

    @Inject(method = "<init>",at = @At("RETURN"))
    public void initReturn(Minecraft minecraft, Screen screen, WorldCreationContext worldCreationContext, Optional optional, OptionalLong optionalLong, CallbackInfo ci){
        uiState.setDifficulty(((LegacyOptions)minecraft.options).createWorldDifficulty().get());
        panel = Legacy4J.isTU25() ? new Panel(p-> (width - (p.width + (ScreenUtil.hasTooltipBoxes() ? 160 : 0))) / 2, p-> (height - p.height) / 2,245,228) : (Legacy4J.isTU5() ? new Panel(p-> p.centeredLeftPos(this), p-> (height - p.height) / 2,225,225) : new Panel(p-> p.centeredLeftPos(this), p-> (height - p.height) / 2 + 30,225,219));
        resourceAssortSelector = Assort.Selector.resources(panel.x + 13, panel.y + 106, 220,45, !ScreenUtil.hasTooltipBoxes());
        publishScreen = new PublishScreen(this, uiState.getGameMode().gameType);
    }

    @Override
    public void added() {
        super.added();
        resourceAssortSelector.addControlTooltips(this,ControlTooltip.Renderer.of(this));
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void init(CallbackInfo ci) {
        ci.cancel();
        if (Legacy4J.isTU25()) {
            legacy4J$initTU25();
        } else if (Legacy4J.isTU5()) {
            legacy4J$initTU5();
        } else {
            legacy4J$initTU1();
        }
    }

    @Unique
    public void legacy4J$initTU1() {
        panel.init();
        addRenderableOnly(panel);
        TickBox hostPrivilleges = new TickBox(panel.x + 13, panel.y + 11,uiState.isAllowCheats(),b->Component.translatable("selectWorld.allowCommands"),b->null,b->uiState.setAllowCheats(b.selected));
        addRenderableWidget(new TickBox(panel.x+ 13, panel.y+29,publishScreen.publish, b-> PublishScreen.PUBLISH, b->null, button -> {
            if (button.selected) minecraft.setScreen(publishScreen);
            button.selected = publishScreen.publish = false;
        }));
        uiState.addListener(s-> hostPrivilleges.active = !s.isDebug() && !s.isHardcore());
        addRenderableWidget(hostPrivilleges);
        EditBox nameEdit = new EditBox(font, panel.x + 13, panel.y + 56,200, 20, Component.translatable("selectWorld.enterName"));
        nameEdit.setValue(uiState.getName());
        nameEdit.setResponder(uiState::setName);
        setInitialFocus(nameEdit);
        addRenderableWidget(nameEdit);
        EditBox editBox = new EditBox(font, panel.x + 13, panel.y + 96, 200, 20, Component.translatable("selectWorld.enterSeed"));
        editBox.setValue(uiState.getSeed());
        editBox.setResponder(string -> uiState.setSeed(editBox.getValue()));
        addRenderableWidget(editBox);
//        LegacySliderButton<WorldCreationUiState.SelectedGameMode> gameModeButton = addRenderableWidget(new LegacySliderButton<>(panel.x + 13, panel.y + 118, 195,16, b -> b.getDefaultMessage(GAME_MODEL_LABEL,b.getObjectValue().displayName),b->Tooltip.create(uiState.getGameMode().getInfo()),uiState.getGameMode(),()-> List.of(WorldCreationUiState.SelectedGameMode.SURVIVAL, WorldCreationUiState.SelectedGameMode.HARDCORE, WorldCreationUiState.SelectedGameMode.CREATIVE), b->uiState.setGameMode(b.getObjectValue())));
//        uiState.addListener(worldCreationUiState -> gameModeButton.active = !worldCreationUiState.isDebug());
        LegacySliderButton<Difficulty> difficultyButton = addRenderableWidget(new LegacySliderButton<>(panel.x + 13, panel.y + 136, 200,16, b -> b.getDefaultMessage(Component.translatable("options.difficulty"),b.getObjectValue().getDisplayName()),b->null,uiState.getDifficulty(),()-> Arrays.asList(Difficulty.values()), b->uiState.setDifficulty(b.getObjectValue())));
        uiState.addListener(worldCreationUiState -> {
            difficultyButton.setObjectValue(uiState.getDifficulty());
            difficultyButton.active = !uiState.isHardcore();
        });

        addRenderableWidget(new LegacySliderButton<>(panel.x + 13, panel.y + 161, 200,16, s -> s.getDefaultMessage(Component.translatable("selectWorld.mapType"), uiState.getWorldType().describePreset()), b -> uiState.getWorldType().isAmplified() ? Tooltip.create(Component.translatable("generator.minecraft.amplified.info")) : null, uiState.getWorldType(), () -> hasAltDown() ? uiState.getAltPresetList() : uiState.getNormalPresetList(), b -> uiState.setWorldType(b.getObjectValue())));

        addRenderableWidget(Button.builder(Component.translatable("selectWorld.create"), button -> this.onCreate()).bounds(panel.x + 13, panel.y + 186,200,20).build());


        this.uiState.onChanged();
    }

    @Unique
    public void legacy4J$initTU5() {
        panel.init();
        addRenderableOnly(panel);
        TickBox hostPrivilleges = new TickBox(panel.x + 13, panel.y + 11,uiState.isAllowCheats(),b->Component.translatable("selectWorld.allowCommands"),b->null,b->uiState.setAllowCheats(b.selected));
        addRenderableWidget(new TickBox(panel.x+ 13, panel.y+29,publishScreen.publish, b-> PublishScreen.PUBLISH, b->null, button -> {
            if (button.selected) minecraft.setScreen(publishScreen);
            button.selected = publishScreen.publish = false;
        }));
        uiState.addListener(s-> hostPrivilleges.active = !s.isDebug() && !s.isHardcore());
        addRenderableWidget(hostPrivilleges);
        EditBox nameEdit = new EditBox(font, panel.x + 13, panel.y + 56,200, 20, Component.translatable("selectWorld.enterName"));
        nameEdit.setValue(uiState.getName());
        nameEdit.setResponder(uiState::setName);
        setInitialFocus(nameEdit);
        addRenderableWidget(nameEdit);
        EditBox editBox = new EditBox(font, panel.x + 13, panel.y + 96, 200, 20, Component.translatable("selectWorld.enterSeed"));
        editBox.setValue(uiState.getSeed());
        editBox.setResponder(string -> uiState.setSeed(editBox.getValue()));
        addRenderableWidget(editBox);
        LegacySliderButton<WorldCreationUiState.SelectedGameMode> gameModeButton = addRenderableWidget(new LegacySliderButton<>(panel.x + 13, panel.y + 132, 200,16, b -> b.getDefaultMessage(GAME_MODEL_LABEL,b.getObjectValue().displayName),b->Tooltip.create(uiState.getGameMode().getInfo()),uiState.getGameMode(),()-> List.of(WorldCreationUiState.SelectedGameMode.SURVIVAL, WorldCreationUiState.SelectedGameMode.CREATIVE), b->uiState.setGameMode(b.getObjectValue())));
        uiState.addListener(worldCreationUiState -> gameModeButton.active = !worldCreationUiState.isDebug());
        LegacySliderButton<Difficulty> difficultyButton = addRenderableWidget(new LegacySliderButton<>(panel.x + 13, panel.y + 152, 200,16, b -> b.getDefaultMessage(Component.translatable("options.difficulty"),b.getObjectValue().getDisplayName()),b->null,uiState.getDifficulty(),()-> Arrays.asList(Difficulty.values()), b->uiState.setDifficulty(b.getObjectValue())));
        uiState.addListener(worldCreationUiState -> {
            difficultyButton.setObjectValue(uiState.getDifficulty());
            difficultyButton.active = !uiState.isHardcore();
        });

        // addRenderableWidget(new LegacySliderButton<>(panel.x + 13, panel.y + 161, 200,16, s -> s.getDefaultMessage(Component.translatable("selectWorld.mapType"), uiState.getWorldType().describePreset()), b -> uiState.getWorldType().isAmplified() ? Tooltip.create(Component.translatable("generator.minecraft.amplified.info")) : null, uiState.getWorldType(), () -> hasAltDown() ? uiState.getAltPresetList() : uiState.getNormalPresetList(), b -> uiState.setWorldType(b.getObjectValue())));
        addRenderableWidget(Button.builder(Component.translatable( "createWorld.tab.more.title"), button -> minecraft.setScreen(new WorldMoreOptionsScreen(self(), b-> trustPlayers = b))).bounds(panel.x + 13, panel.y + 172,200,20).build());
        addRenderableWidget(Button.builder(Component.translatable("selectWorld.create"), button -> this.onCreate()).bounds(panel.x + 13, panel.y + 196,200,20).build());
        this.uiState.onChanged();
    }

    @Unique
    public void legacy4J$initTU25() {
        panel.init();
        addRenderableOnly(panel);
        EditBox nameEdit = new EditBox(font, panel.x + 13, panel.y + 25,220, 20, Component.translatable("selectWorld.enterName"));
        nameEdit.setValue(uiState.getName());
        nameEdit.setResponder(uiState::setName);
        uiState.addListener(worldCreationUiState -> nameEdit.setTooltip(Tooltip.create(Component.translatable("selectWorld.targetFolder", Component.literal(worldCreationUiState.getTargetFolder()).withStyle(ChatFormatting.ITALIC)))));
        setInitialFocus(nameEdit);
        addRenderableWidget(nameEdit);
        LegacySliderButton<WorldCreationUiState.SelectedGameMode> gameModeButton = addRenderableWidget(new LegacySliderButton<>(panel.x + 13, panel.y + 51, 220,16, b -> b.getDefaultMessage(GAME_MODEL_LABEL,b.getObjectValue().displayName),b->Tooltip.create(uiState.getGameMode().getInfo()),uiState.getGameMode(),()-> List.of(WorldCreationUiState.SelectedGameMode.SURVIVAL, WorldCreationUiState.SelectedGameMode.HARDCORE, WorldCreationUiState.SelectedGameMode.CREATIVE), b->uiState.setGameMode(b.getObjectValue())));
        uiState.addListener(worldCreationUiState -> gameModeButton.active = !worldCreationUiState.isDebug());
        LegacySliderButton<Difficulty> difficultyButton = addRenderableWidget(new LegacySliderButton<>(panel.x + 13, panel.y + 77, 220,16, b -> b.getDefaultMessage(Component.translatable("options.difficulty"),b.getObjectValue().getDisplayName()),b->Tooltip.create(uiState.getDifficulty().getInfo()),uiState.getDifficulty(),()-> Arrays.asList(Difficulty.values()), b->uiState.setDifficulty(b.getObjectValue())));
        uiState.addListener(worldCreationUiState -> {
            difficultyButton.setObjectValue(uiState.getDifficulty());
            difficultyButton.active = !uiState.isHardcore();
        });

        addRenderableWidget(Button.builder(Component.translatable( "createWorld.tab.more.title"), button -> minecraft.setScreen(new WorldMoreOptionsScreenTU25(self(), b-> trustPlayers = b))).bounds(panel.x + 13, panel.y + 172,220,20).build());
        addRenderableWidget(Button.builder(Component.translatable("selectWorld.create"), button -> this.onCreate()).bounds(panel.x + 13, panel.y + 197,220,20).build());
        addRenderableWidget(new TickBox(panel.x+ 14, panel.y+155,220,publishScreen.publish, b-> PublishScreen.PUBLISH, b->null, button -> {
            if (button.selected) minecraft.setScreen(publishScreen);
            button.selected = publishScreen.publish = false;
        }));
        resourceAssortSelector.setX(panel.x + 13);
        resourceAssortSelector.setY(panel.y + 106);
        addRenderableWidget(resourceAssortSelector);
        this.uiState.onChanged();
    }

    @Override
    public void repositionElements() {
        rebuildWidgets();
    }

    private void onCreate() {
        WorldCreationContext worldCreationContext = this.uiState.getSettings();
        WorldDimensions.Complete complete = worldCreationContext.selectedDimensions().bake(worldCreationContext.datapackDimensions());
        LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = worldCreationContext.worldgenRegistries().replaceFrom(RegistryLayer.DIMENSIONS, complete.dimensionsRegistryAccess());
        Lifecycle lifecycle = FeatureFlags.isExperimental(worldCreationContext.dataConfiguration().enabledFeatures()) ? Lifecycle.experimental() : Lifecycle.stable();
        Lifecycle lifecycle2 = layeredRegistryAccess.compositeAccess().allRegistriesLifecycle();
        Lifecycle lifecycle3 = lifecycle2.add(lifecycle);
        boolean bl = lifecycle2 == Lifecycle.stable();
        confirmWorldCreation(this.minecraft, self(), lifecycle3, ()-> {
            this.createNewWorld(complete.specialWorldProperty(), layeredRegistryAccess, lifecycle3);
            resourceAssortSelector.applyResourceChanges(this::onLoad);
        }, bl);
        onLoad();

    }
    private void onLoad() {
        Legacy4JClient.serverPlayerJoinConsumer = s->{
            ((LegacyClientWorldSettings)s.server.getWorldData()).setTrustPlayers(trustPlayers);
            s.server.getPlayerList().sendPlayerPermissionLevel(s);
            publishScreen.publish((IntegratedServer) s.server);
            ((LegacyClientWorldSettings)minecraft.getSingleplayerServer().getWorldData()).setSelectedResourceAssort(resourceAssortSelector.getSelectedAssort());
        };
    }

    @Redirect(method = "createNewWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createWorldOpenFlows()Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;"))
    private WorldOpenFlows createNewWorld(Minecraft instance) {
        return new WorldOpenFlows(minecraft,Legacy4JClient.currentWorldSource);
    }
    @Redirect(method = "createNewWorldDirectory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource;createAccess(Ljava/lang/String;)Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;"))
    private LevelStorageSource.LevelStorageAccess createNewWorldDirectory(LevelStorageSource instance, String path) throws IOException {
        LevelStorageSource.LevelStorageAccess access = instance.createAccess(path);
        if (Files.exists(access.getLevelDirectory().path())) {
            try {
                FileUtils.deleteDirectory(access.getLevelDirectory().path().toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return access;
    }
    @Redirect(method = "createNewWorldDirectory", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getLevelSource()Lnet/minecraft/world/level/storage/LevelStorageSource;"))
    private LevelStorageSource createNewWorldDirectory(Minecraft instance) {
        return Legacy4JClient.currentWorldSource;
    }
    
    private static void confirmWorldCreation(Minecraft minecraft, CreateWorldScreen createWorldScreen, Lifecycle lifecycle, Runnable runnable, boolean bl2) {
        runnable.run();
    }
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        if (!Legacy4J.isTU25()) {
            ScreenUtil.renderDefaultBackground(guiGraphics, true);
        } else {
            ScreenUtil.renderDefaultBackground(guiGraphics,false);
            resourceAssortSelector.renderTooltipBox(guiGraphics,panel);
        }
    }
    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (resourceAssortSelector.scrollableRenderer.mouseScrolled(g)) return true;
        return super.mouseScrolled(d, e, f, g);
    }
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics, i, j, f);
        if (Legacy4J.isTU25()) {
            legacy4J$renderTU25(guiGraphics, i, j ,f);
        } else {
            legacy4J$renderTU1(guiGraphics, i, j ,f);
        }
    }

    @Unique
    public void legacy4J$renderTU25(GuiGraphics guiGraphics, int i, int j, float f) {
        for (Renderable renderable : this.renderables)
            renderable.render(guiGraphics, i, j, f);
        guiGraphics.drawString(font,NAME_LABEL, panel.x + 14, panel.y + 15, CommonColor.INVENTORY_GRAY_TEXT.get(),false);
    }

    @Unique
    public void legacy4J$renderTU1(GuiGraphics guiGraphics, int i, int j, float f) {
        for (Renderable renderable : this.renderables)
            renderable.render(guiGraphics, i, j, f);
        guiGraphics.drawString(font,NAME_LABEL, panel.x + 13, panel.y + 46, CommonColor.INVENTORY_GRAY_TEXT.get(),false);
        guiGraphics.drawString(font, "Seed for the World Generator", panel.x + 13, panel.y + 86, CommonColor.INVENTORY_GRAY_TEXT.get(),false);
        guiGraphics.drawString(font, "Leave blank for a random seed", panel.x + 13, panel.y + 119, CommonColor.INVENTORY_GRAY_TEXT.get(),false);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        return super.keyPressed(i,j,k);
    }



    @Override
    public void onClose() {
        ScreenUtil.playSimpleUISound(LegacyRegistries.BACK.get(),1.0f);
        popScreen();
    }
}
