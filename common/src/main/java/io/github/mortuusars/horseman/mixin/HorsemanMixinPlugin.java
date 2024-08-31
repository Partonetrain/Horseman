package io.github.mortuusars.horseman.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import io.github.mortuusars.horseman.PlatformHelper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class HorsemanMixinPlugin implements IMixinConfigPlugin {

    private static final Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
            "io.github.mortuusars.horseman.mixin.ServerGamePacketListenerImplMixin",
                () -> !PlatformHelper.isModLoading("horsebuff")
    );

    @Override
    public void onLoad(String mixinPackage) {
        MixinExtrasBootstrap.init();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.toLowerCase().contains("mythicmounts")) {
            return PlatformHelper.isModLoading("mythicmounts");
        }
        return CONDITIONS.getOrDefault(mixinClassName, () -> true).get();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
