package wily.legacy.mixin;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wily.legacy.Legacy4J;
import wily.legacy.Legacy4JClient;
import wily.legacy.client.CommonColor;
import wily.legacy.client.ControlType;
import wily.legacy.client.LegacyOptions;
import wily.legacy.client.LegacyWorldSettings;
import wily.legacy.client.controller.ControllerBinding;
import wily.legacy.client.screen.*;
import wily.legacy.init.LegacyRegistries;
import wily.legacy.util.ScreenUtil;

import java.util.*;

import static wily.legacy.client.screen.ControlTooltip.*;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen implements ControlTooltip.Event{
    @Shadow @Final private WorldCreationUiState uiState;
    @Shadow @Final private static Component GAME_MODEL_LABEL;
    @Shadow @Final private static Component NAME_LABEL;

    @Shadow protected abstract void createNewWorld(PrimaryLevelData.SpecialWorldProperty arg, LayeredRegistryAccess<RegistryLayer> arg2, Lifecycle lifecycle);

    @Shadow public abstract void popScreen();

    @Shadow protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guiEventListener);

    protected boolean trustPlayers;
    protected Panel panel;
    protected PublishScreen publishScreen;
    protected PackSelector resourcePackSelector;

    protected CreateWorldScreenMixin(Component component) {
        super(component);
    }
    private CreateWorldScreen self(){
        return (CreateWorldScreen) (Object) this;
    }

    @Inject(method = "<init>",at = @At("RETURN"))
    public void initReturn(Minecraft minecraft, Screen screen, WorldCreationContext worldCreationContext, Optional optional, OptionalLong optionalLong, CallbackInfo ci){
        uiState.setDifficulty(((LegacyOptions)minecraft.options).createWorldDifficulty().get());
        panel = new Panel(p-> p.centeredLeftPos(this), p-> (height - p.height) / 2 + 30,225,219);
        resourcePackSelector = PackSelector.resources(panel.x + 13, panel.y + 106, 220,45, !ScreenUtil.hasTooltipBoxes());
        publishScreen = new PublishScreen(this, uiState.getGameMode().gameType);
    }

    @Override
    public void added() {
        super.added();
        ControlTooltip.Renderer.of(this).add(()-> ControlType.getActiveType().isKbm() ? COMPOUND_ICON_FUNCTION.apply(new Icon[]{getKeyIcon(InputConstants.KEY_LSHIFT), PLUS_ICON,getKeyIcon(InputConstants.MOUSE_BUTTON_LEFT)}) : ControllerBinding.LEFT_BUTTON.bindingState.getIcon(), ()-> getFocused() == resourcePackSelector ? getAction("legacy.action.resource_packs_screen") : null);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void init(CallbackInfo ci) {
        ci.cancel();
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
        Runnable create = ()-> confirmWorldCreation(this.minecraft, self(), lifecycle3, () -> this.createNewWorld(complete.specialWorldProperty(), layeredRegistryAccess, lifecycle3), bl);
        resourcePackSelector.applyChanges(true, ()->minecraft.reloadResourcePacks().thenRun(create), create);
        onLoad();

    }
    private void onLoad() {
        Legacy4JClient.startServerConsumer = s->{
            if (ScreenUtil.getLegacyOptions().autoSaveInterval().get() == 0) Legacy4JClient.deleteLevelWhenExitWithoutSaving = true;
            ((LegacyWorldSettings)minecraft.getSingleplayerServer().getWorldData()).setTrustPlayers(trustPlayers);
            publishScreen.publish(s);
            if (resourcePackSelector.hasChanged()) ((LegacyWorldSettings)minecraft.getSingleplayerServer().getWorldData()).setSelectedResourcePacks(resourcePackSelector.getSelectedIds());
        };
    }
    
    private static void confirmWorldCreation(Minecraft minecraft, CreateWorldScreen createWorldScreen, Lifecycle lifecycle, Runnable runnable, boolean bl2) {
        runnable.run();
    }
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        ScreenUtil.renderDefaultBackground(guiGraphics,true);
    }
    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (resourcePackSelector.scrollableRenderer.mouseScrolled(g)) return true;
        return super.mouseScrolled(d, e, f, g);
    }
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics, i, j, f);
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
