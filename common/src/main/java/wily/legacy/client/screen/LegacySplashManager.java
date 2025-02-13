package wily.legacy.client.screen;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class LegacySplashManager extends SimplePreparableReloadListener<List<String>> {
    private static final ResourceLocation SPLASHES_LOCATION = new ResourceLocation("texts/splashes.txt");
    private static final RandomSource RANDOM = RandomSource.create();
    private final List<String> splashes = Lists.newArrayList();
    private final User user;

    public LegacySplashManager(User user) {
        this.user = user;
    }

    protected List<String> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        try {
            BufferedReader bufferedReader = Minecraft.getInstance().getResourceManager().openAsReader(SPLASHES_LOCATION);

            List var4;
            try {
                var4 = (List)bufferedReader.lines().map(String::trim).filter((string) -> {
                    return string.hashCode() != 125780783;
                }).collect(Collectors.toList());
            } catch (Throwable var7) {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                    }
                }

                throw var7;
            }

            if (bufferedReader != null) {
                bufferedReader.close();
            }

            return var4;
        } catch (IOException var8) {
            return Collections.emptyList();
        }
    }

    protected void apply(List<String> list, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.splashes.clear();
        this.splashes.addAll(list);
    }

    @Nullable
    public LegacySplashRenderer getSplash() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
            return LegacySplashRenderer.CHRISTMAS;
        } else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
            return LegacySplashRenderer.NEW_YEAR;
        } else if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {
            return LegacySplashRenderer.HALLOWEEN;
        } else if (this.splashes.isEmpty()) {
            return null;
        } else if (this.user != null && RANDOM.nextInt(this.splashes.size()) == 42) {
            String var10002 = this.user.getName();
            return new LegacySplashRenderer(var10002.toUpperCase(Locale.ROOT) + " IS YOU");
        } else {
            return new LegacySplashRenderer((String)this.splashes.get(RANDOM.nextInt(this.splashes.size())));
        }
    }
}
