package wily.legacy.mixin;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static net.minecraft.world.item.MapItem.getSavedData;

@Mixin(MapItem.class)
public abstract class MapItemMixin {


    @Shadow protected abstract BlockState getCorrectStateForFluidBlock(Level level, BlockState blockState, BlockPos blockPos);

    @Shadow
    private static boolean isBiomeWatery(boolean[] bls, int i, int j) {
        return false;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void update(Level level, Entity entity, MapItemSavedData mapItemSavedData) {
        if (level.dimension() == mapItemSavedData.dimension && entity instanceof Player) {
            int i = 1 << mapItemSavedData.scale;
            int j = mapItemSavedData.centerX;
            int k = mapItemSavedData.centerZ;
            int l = Mth.floor(entity.getX() - (double)j) / i + 64;
            int m = Mth.floor(entity.getZ() - (double)k) / i + 64;
            int n = 128 / i;
            if (level.dimensionType().hasCeiling()) {
                n /= 2;
            }

            MapItemSavedData.HoldingPlayer holdingPlayer = mapItemSavedData.getHoldingPlayer((Player)entity);
            ++holdingPlayer.step;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
            boolean bl = false;

            for(int o = l - n + 1; o < l + n; ++o) {
                if ((o & 15) == (holdingPlayer.step & 15) || bl) {
                    bl = false;
                    double d = 0.0;

                    for(int p = m - n - 1; p < m + n; ++p) {
                        if (o >= 0 && p >= -1 && o < 128 && p < 128) {
                            int q = Mth.square(o - l) + Mth.square(p - m);
                            boolean bl2 = q > (n - 2) * (n - 2);
                            int r = (j / i + o - 64) * i;
                            int s = (k / i + p - 64) * i;
                            Multiset<MapColor> multiset = LinkedHashMultiset.create();
                            LevelChunk levelChunk = level.getChunk(SectionPos.blockToSectionCoord(r), SectionPos.blockToSectionCoord(s));
                            if (!levelChunk.isEmpty()) {
                                int t = 0;
                                double e = 0.0;
                                int u;
                                if (level.dimensionType().hasCeiling()) {
                                    u = r + s * 231871;
                                    u = u * u * 31287121 + u * 11;
                                    if ((u >> 20 & 1) == 0) {
                                        multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
                                    } else {
                                        multiset.add(Blocks.STONE.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
                                    }

                                    e = 100.0;
                                } else {
                                    for(u = 0; u < i; ++u) {
                                        for(int v = 0; v < i; ++v) {
                                            mutableBlockPos.set(r + u, 0, s + v);
                                            int w = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, mutableBlockPos.getX(), mutableBlockPos.getZ()) + 1;
                                            BlockState blockState;
                                            if (w <= level.getMinBuildHeight() + 1) {
                                                blockState = Blocks.BEDROCK.defaultBlockState();
                                            } else {
                                                do {
                                                    --w;
                                                    mutableBlockPos.setY(w);
                                                    blockState = levelChunk.getBlockState(mutableBlockPos);
                                                } while(blockState.getMapColor(level, mutableBlockPos) == MapColor.NONE && w > level.getMinBuildHeight());

                                                if (w > level.getMinBuildHeight() && !blockState.getFluidState().isEmpty()) {
                                                    int x = w - 1;
                                                    mutableBlockPos2.set(mutableBlockPos);

                                                    BlockState blockState2;
                                                    do {
                                                        mutableBlockPos2.setY(x--);
                                                        blockState2 = levelChunk.getBlockState(mutableBlockPos2);
                                                        ++t;
                                                    } while(x > level.getMinBuildHeight() && !blockState2.getFluidState().isEmpty());

                                                    blockState = this.getCorrectStateForFluidBlock(level, blockState, mutableBlockPos);
                                                }
                                            }

                                            mapItemSavedData.checkBanners(level, mutableBlockPos.getX(), mutableBlockPos.getZ());
                                            e += (double)w / (double)(i * i);
                                            multiset.add(blockState.getMapColor(level, mutableBlockPos));
                                        }
                                    }
                                }

                                t /= i * i;
                                MapColor mapColor = (MapColor)Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.NONE);
                                MapColor.Brightness brightness;
                                double f;
                                if (mapColor == MapColor.WATER) {
                                    f = (double)t * 0.1 + (double)(o + p & 1) * 0.2;
                                    if (f < 0.5) {
                                        brightness = MapColor.Brightness.HIGH;
                                    } else if (f > 0.9) {
                                        brightness = MapColor.Brightness.LOW;
                                    } else {
                                        brightness = MapColor.Brightness.NORMAL;
                                    }
                                } else {
                                    f = (e - d) * 4.0 / (double)(i + 4) + ((double)(o + p & 1) - 0.5) * 0.4;
                                    if (f > 0.6) {
                                        brightness = MapColor.Brightness.HIGH;
                                    } else if (f < -0.6) {
                                        brightness = MapColor.Brightness.LOW;
                                    } else {
                                        brightness = MapColor.Brightness.NORMAL;
                                    }
                                }

                                d = e;
                                if (p >= 0 && q < n * n && (!bl2 || (o + p & 1) != 0)) {
                                    bl |= mapItemSavedData.updateColor(o, p, mapColor.getPackedId(brightness));
                                }
                            }
                        }
                    }
                }
            }

        }
    }


    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void renderBiomePreviewMap(ServerLevel serverLevel, ItemStack itemStack) {
        MapItemSavedData mapItemSavedData = getSavedData((ItemStack)itemStack, serverLevel);
        if (mapItemSavedData != null) {
            if (serverLevel.dimension() == mapItemSavedData.dimension) {
                int i = 1 << mapItemSavedData.scale;
                int j = mapItemSavedData.centerX;
                int k = mapItemSavedData.centerZ;
                boolean[] bls = new boolean[16384];
                int l = j / i - 64;
                int m = k / i - 64;
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

                int n;
                int o;
                for(n = 0; n < 128; ++n) {
                    for(o = 0; o < 128; ++o) {
                        Holder<Biome> holder = serverLevel.getBiome(mutableBlockPos.set((l + o) * i, 0, (m + n) * i));
                        bls[n * 128 + o] = holder.is(BiomeTags.WATER_ON_MAP_OUTLINES);
                    }
                }

                for(n = 1; n < 127; ++n) {
                    for(o = 1; o < 127; ++o) {
                        int p = 0;

                        for(int q = -1; q < 2; ++q) {
                            for(int r = -1; r < 2; ++r) {
                                if ((q != 0 || r != 0) && isBiomeWatery(bls, n + q, o + r)) {
                                    ++p;
                                }
                            }
                        }

                        MapColor.Brightness brightness = MapColor.Brightness.LOWEST;
                        MapColor mapColor = MapColor.NONE;
                        if (isBiomeWatery(bls, n, o)) {
                            mapColor = MapColor.COLOR_ORANGE;
                            if (p > 7 && o % 2 == 0) {
                                switch ((n + (int)(Mth.sin((float)o + 0.0F) * 7.0F)) / 8 % 5) {
                                    case 0:
                                    case 4:
                                        brightness = MapColor.Brightness.LOW;
                                        break;
                                    case 1:
                                    case 3:
                                        brightness = MapColor.Brightness.NORMAL;
                                        break;
                                    case 2:
                                        brightness = MapColor.Brightness.HIGH;
                                }
                            } else if (p > 7) {
                                mapColor = MapColor.NONE;
                            } else if (p > 5) {
                                brightness = MapColor.Brightness.NORMAL;
                            } else if (p > 3) {
                                brightness = MapColor.Brightness.LOW;
                            } else if (p > 1) {
                                brightness = MapColor.Brightness.LOW;
                            }
                        } else if (p > 0) {
                            mapColor = MapColor.COLOR_BROWN;
                            if (p > 3) {
                                brightness = MapColor.Brightness.NORMAL;
                            } else {
                                brightness = MapColor.Brightness.LOWEST;
                            }
                        }

                        if (mapColor != MapColor.NONE) {
                            mapItemSavedData.setColor(n, o, mapColor.getPackedId(brightness));
                        }
                    }
                }

            }
        }
    }
}
