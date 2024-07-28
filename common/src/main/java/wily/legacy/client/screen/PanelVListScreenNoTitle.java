package wily.legacy.client.screen;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import wily.legacy.util.ScreenUtil;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PanelVListScreenNoTitle extends PanelBackgroundScreen{
    protected final RenderableVList renderableVList = new RenderableVList();
    public Consumer<PanelVListScreenNoTitle> onClose = s->{};
    public PanelVListScreenNoTitle(Function<Screen,Panel> panelConstructor, Component component) {
        super(panelConstructor, component);
    }
    public PanelVListScreenNoTitle(Screen parent, Function<Screen,Panel> panelConstructor, Component component) {
        super(panelConstructor, component);
        this.parent = parent;
    }
    public PanelVListScreenNoTitle(Screen parent, int imageWidth, int imageHeight, Component component) {
        super(imageWidth, imageHeight, component);
        this.parent = parent;
    }
    public PanelVListScreenNoTitle(Screen parent, int imageWidth, int imageHeight, Component component, Renderable... renderables) {
        this(parent,imageWidth, imageHeight, component);
        renderableVList.addRenderables(renderables);
    }
    public PanelVListScreenNoTitle(Screen parent, int imageWidth, int imageHeight, Component component, OptionInstance<?>... optionInstances) {
        this(parent,imageWidth, imageHeight, component);
        renderableVList.addOptions(optionInstances);
    }


    @Override
    protected void init() {
        super.init();
        renderableVListInit();

    }

    public boolean isFocused(ScreenDirection direction){
        ComponentPath path = parent.nextFocusPath(new FocusNavigationEvent.ArrowNavigation(direction));
        return renderables.contains(parent.getFocused()) && (path == null || path.component() == parent.getFocused());
    }

    public Button.Builder openScreenButton(Component component, Supplier<Screen> supplier) {
        return Button.builder(component, button -> this.minecraft.setScreen(supplier.get()));
    }

    public void renderableVListInit(){
        getRenderableVList().init(this,panel.x + 10,panel.y + 10,panel.width - 20,panel.height);
    }
    @Override
    public void onClose() {
        super.onClose();
        onClose.accept(this);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        ScreenUtil.renderDefaultBackground(guiGraphics,false);
    }

    public RenderableVList getRenderableVList() {
        return renderableVList;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        getRenderableVList().mouseScrolled(g);
        return super.mouseScrolled(d, e, f, g);
    }
    public boolean renderableKeyPressed(int i){
        return getRenderableVList().keyPressed(i, true);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (renderableKeyPressed(i)) return true;
        return super.keyPressed(i, j, k);
    }
}
