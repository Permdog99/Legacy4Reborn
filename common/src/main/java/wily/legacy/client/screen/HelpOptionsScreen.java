package wily.legacy.client.screen;

import com.google.common.collect.Streams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import wily.legacy.Legacy4J;
import wily.legacy.client.LegacyOptions;
import wily.legacy.client.LegacyTip;
import wily.legacy.client.screen.tu25.SettingsScreenTU25;
import wily.legacy.init.LegacyRegistries;
import wily.legacy.util.ScreenUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

public class HelpOptionsScreen extends RenderableVListScreen {
    protected Minecraft minecraft = Minecraft.getInstance();
    public HelpOptionsScreen(Screen parent) {
        super(parent,Component.translatable("options.title"), r-> {});
        renderableVList.addRenderable(Button.builder(Component.translatable("legacy.menu.change_skin"),(b)-> minecraft.getToasts().addToast(new LegacyTip(Component.literal("Work in Progress!!"), 80, 40).disappearTime(960))).build());
        renderableVList.addRenderable(Button.builder(Component.translatable("legacy.menu.how_to_play"),(b)-> minecraft.getToasts().addToast(new LegacyTip(Component.literal("Work in Progress!!"), 80, 40).disappearTime(960))).build());
        renderableVList.addRenderable(openScreenButton(Component.translatable("controls.title"),()-> new RenderableVListScreen(this,Component.translatable("controls.title"),r->r.addRenderables(Button.builder(Component.translatable("options.mouse_settings.title"), button -> this.minecraft.setScreen(new PanelVListScreen(r.screen,250,110,Component.translatable("options.mouse_settings.title"), minecraft.options.invertYMouse(), minecraft.options.mouseWheelSensitivity(), minecraft.options.discreteMouseScroll(), minecraft.options.touchscreen()))).build(),Button.builder(Component.translatable("controls.keybinds.title"), button -> this.minecraft.setScreen(new LegacyKeyBindsScreen(r.screen,minecraft.options))).build(),Button.builder(Component.translatable("legacy.controls.controller"), button -> this.minecraft.setScreen(new ControllerMappingScreen(r.screen,minecraft.options))).build()))).build());
        renderableVList.addRenderable(openScreenButton(Component.translatable("legacy.menu.settings"),()-> Legacy4J.isTU25() ? new SettingsScreenTU25(this) : new SettingsScreen(this)).build());
        renderableVList.addRenderable(openScreenButton(Component.translatable("credits_and_attribution.button.credits"),()->new RenderableVListScreen(this,Component.translatable("credits_and_attribution.screen.title"),r-> r.addRenderables(openScreenButton(Component.translatable("credits_and_attribution.button.credits"),()->new WinScreen(false, () -> this.minecraft.setScreen(r.screen))).build(),Button.builder(Component.translatable("credits_and_attribution.button.attribution"), ConfirmLinkScreen.confirmLink(this,"https://aka.ms/MinecraftJavaAttribution")).build(),Button.builder(Component.translatable("credits_and_attribution.button.licenses"), ConfirmLinkScreen.confirmLink(this,"https://aka.ms/MinecraftJavaLicenses")).build()))).build());
        assert minecraft != null;
        if (((LegacyOptions) minecraft.options).developerMode().get()) {
            renderableVList.addRenderable(openScreenButton(Component.translatable("legacy.menu.developersettings"),()->prepareDeveloperSettings(new PanelVListScreenNoTitle(this,180,248,Component.translatable("legacy.menu.developersettings"), ((LegacyOptions)minecraft.options).unfocusedInputs(), ((LegacyOptions)minecraft.options).autoSaveWhenPaused(), minecraft.options.autoJump(), ((LegacyOptions)minecraft.options).flyingViewRolling(),minecraft.options.toggleSprint(),((LegacyOptions)minecraft.options).displayNameTagBorder(),((LegacyOptions)minecraft.options).legacyCreativeBlockPlacing(),minecraft.options.realmsNotifications(), minecraft.options.allowServerListing(), minecraft.options.biomeBlendRadius(), minecraft.options.prioritizeChunkUpdates(), minecraft.options.simulationDistance(),((LegacyOptions)minecraft.options).overrideTerrainFogStart(),((LegacyOptions)minecraft.options).terrainFogStart(),((LegacyOptions)minecraft.options).terrainFogEnd(), minecraft.options.bobView(), minecraft.options.gamma(),minecraft.options.cloudStatus(), minecraft.options.particles(), minecraft.options.mipmapLevels(), minecraft.options.entityShadows(), minecraft.options.screenEffectScale(), minecraft.options.entityDistanceScaling(), minecraft.options.fov(),minecraft.options.fovEffectScale(),minecraft.options.darknessEffectScale(),minecraft.options.glintSpeed(), minecraft.options.glintStrength(), ((LegacyOptions)minecraft.options).displayHUD(),((LegacyOptions)minecraft.options).displayHand(),minecraft.options.showAutosaveIndicator(),((LegacyOptions)minecraft.options).showVanillaRecipeBook(),minecraft.options.attackIndicator(),((LegacyOptions)minecraft.options).hudDistance(),((LegacyOptions)minecraft.options).inGameTooltips(),((LegacyOptions)minecraft.options).legacyItemTooltips(),((LegacyOptions)minecraft.options).vignette(),((LegacyOptions)minecraft.options).animatedCharacter(),((LegacyOptions)minecraft.options).smoothAnimatedCharacter(), ((LegacyOptions)minecraft.options).legacyCreativeTab(),((LegacyOptions)minecraft.options).vanillaTabs(), minecraft.options.operatorItemsTab(), minecraft.options.narrator(), minecraft.options.highContrast(), minecraft.options.notificationDisplayTime(), minecraft.options.damageTiltStrength(), minecraft.options.glintSpeed(), minecraft.options.glintStrength(), minecraft.options.hideLightningFlash(), minecraft.options.darkMojangStudiosBackground(), minecraft.options.panoramaSpeed(), minecraft.options.narratorHotkey(),minecraft.options.chatVisibility(), minecraft.options.chatColors(), minecraft.options.chatLinks(), minecraft.options.chatLinksPrompt(),  minecraft.options.backgroundForChatOnly(), minecraft.options.chatOpacity(), minecraft.options.textBackgroundOpacity(), minecraft.options.chatScale(), minecraft.options.chatLineSpacing(), minecraft.options.chatDelay(), minecraft.options.chatWidth(), minecraft.options.chatHeightFocused(), minecraft.options.chatHeightUnfocused(), minecraft.options.narrator(), minecraft.options.autoSuggestions(), minecraft.options.hideMatchedNames(), minecraft.options.reducedDebugInfo(), minecraft.options.onlyShowSecureChat(), ((LegacyOptions) minecraft.options).titleUpdateMode()))).build());
        }
        this.parent = parent;
    }

    public PanelVListScreenNoTitle prepareDeveloperSettings(PanelVListScreenNoTitle screen){
        screen.renderableVList.addRenderable(openScreenButton(Component.translatable("options.language"), () -> new LegacyLanguageScreen(screen, minecraft.getLanguageManager())).build());
        screen.renderableVList.addRenderable(openScreenButton(Component.translatable("legacy.menu.mods"), () -> new ModsScreen(screen)).build());
        assert minecraft != null;
        if (minecraft.level == null && !minecraft.hasSingleplayerServer()) screen.renderableVList.addOptions(((LegacyOptions)minecraft.options).createWorldDifficulty());
        screen.renderableVList.addLinkedOptions(14,((LegacyOptions)minecraft.options).displayLegacyGamma(), OptionInstance::get, ((LegacyOptions)minecraft.options).legacyGamma());
        screen.renderableVList.addLinkedOptions(9,((LegacyOptions)minecraft.options).autoResolution(), b-> !b.get(),((LegacyOptions)minecraft.options).interfaceResolution());
        return screen;
    }
}
