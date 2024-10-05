package wily.legacy.client.screen.tu25;

import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import wily.legacy.Legacy4J;
import wily.legacy.Legacy4JClient;
import wily.legacy.client.screen.ConfirmationScreen;

import java.io.IOException;

public class SaveOptionsScreenTU25 extends ConfirmationScreen {
    protected final PlayGameScreenTU25 parent = (PlayGameScreenTU25) super.parent;
    private LevelSummary summary;

    public SaveOptionsScreenTU25(PlayGameScreenTU25 parent, LevelSummary summary) {
        super(parent, 230, 165, Component.translatable("legacy.menu.save_options"), Component.translatable("legacy.menu.save_options_message"), (b)->{});
        this.summary = summary;
    }

    @Override
    protected void addButtons() {
        renderableVList.addRenderable(Button.builder(Component.translatable("gui.cancel"), b-> minecraft.setScreen(parent)).bounds(panel.x + 15, panel.y + panel.height - 96,200,20).build());
        EditBox renameBox = new EditBox(font, width / 2 - 100,0,200, 20, Component.translatable("selectWorld.enterName"));
        renameBox.setValue(summary.getLevelName());
        renderableVList.addRenderable(Button.builder(Component.translatable("legacy.menu.rename_save"),b-> minecraft.setScreen(new ConfirmationScreen(parent,Component.translatable("legacy.menu.rename_save_title"),Component.translatable("legacy.menu.rename_save_message"), p->{
            String id = summary.getLevelId();
            try {
                LevelStorageSource.LevelStorageAccess levelStorageAccess = minecraft.getLevelSource().validateAndCreateAccess(id);
                levelStorageAccess.renameLevel(renameBox.getValue());
                levelStorageAccess.close();
                parent.saveRenderableList.reloadSaveList();
                minecraft.setScreen(parent);
            } catch (IOException iOException) {
                SystemToast.onWorldAccessFailure(minecraft, id);
                parent.saveRenderableList.reloadSaveList();
            } catch (ContentValidationException contentValidationException) {
                Legacy4J.LOGGER.warn("{}", contentValidationException.getMessage());
                minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(()-> minecraft.setScreen(parent)));
            }
        }){

            @Override
            protected void init() {
                super.init();
                renameBox.setResponder(s-> okButton.active = !Util.isBlank(s));
                renameBox.setPosition(panel.x + 15,panel.y + 45);
                addRenderableWidget(renameBox);
            }
        })).bounds(panel.x + 15, panel.getRectangle().bottom() - 74,200,20).build());
        renderableVList.addRenderable(Button.builder(Component.translatable("selectWorld.delete"),b-> minecraft.setScreen(new ConfirmationScreen(parent,230,120, Component.translatable("selectWorld.delete"), Component.translatable("selectWorld.deleteQuestion"), b1->parent.saveRenderableList.deleteSave(summary)))).bounds(panel.x + 15, panel.getRectangle().bottom() - 52,200,20).build());

        renderableVList.addRenderable(Button.builder(Component.translatable("legacy.menu.copySave"),b-> minecraft.setScreen(new ConfirmationScreen(parent,230,120, Component.translatable("legacy.menu.copySave"), Component.translatable("legacy.menu.copySaveMessage"), b1->{
            String id = summary.getLevelId();
            Legacy4JClient.copySaveFile(minecraft.getLevelSource().getLevelPath(id),minecraft.getLevelSource(),id);
            parent.saveRenderableList.reloadSaveList();
            minecraft.setScreen(parent);

        }))).bounds(panel.x + 15, panel.getRectangle().bottom() - 30,200,20).build());
    }
}