package wily.legacy.client.screen;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PanelVListScreen extends PanelBackgroundScreen implements RenderableVList.Access{
    public final RenderableVList renderableVList = new RenderableVList();
    public Consumer<PanelVListScreen> onClose = s->{};
    public PanelVListScreen(Function<Screen,Panel> panelConstructor, Component component) {
        super(panelConstructor, component);
    }
    public PanelVListScreen(Screen parent, Function<Screen,Panel> panelConstructor, Component component) {
        super(panelConstructor, component);
        this.parent = parent;
    }
    public PanelVListScreen(Screen parent, int imageWidth, int imageHeight, Component component) {
        super(imageWidth, imageHeight, component);
        this.parent = parent;
    }
    public PanelVListScreen(Screen parent, int imageWidth, int imageHeight, Component component, Renderable... renderables) {
        this(parent,imageWidth, imageHeight, component);
        renderableVList.addRenderables(renderables);
    }
    public PanelVListScreen(Screen parent, int imageWidth, int imageHeight, Component component, OptionInstance<?>... optionInstances) {
        this(parent,imageWidth, imageHeight, component);
        renderableVList.addOptions(optionInstances);
    }
    public PanelVListScreen(Screen parent, int imageWidth, int imageHeight, int xOffset, int yOffset, Component component, OptionInstance<?>... optionInstances) {
        this(parent, s-> Panel.centered(s,imageWidth,imageHeight, xOffset, yOffset),component);
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
