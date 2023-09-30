package com.github.may2beez.farmhelperv2.feature;

import com.github.may2beez.farmhelperv2.util.LogUtils;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class FeatureManager {
    private static FeatureManager instance;
    public static FeatureManager getInstance() {
        if (instance == null) {
            instance = new FeatureManager();
        }
        return instance;
    }

    private final ArrayList<IFeature> features = new ArrayList<>();

    public void fillFeatures() {
        features.addAll(new Reflections().getSubTypesOf(IFeature.class).stream()
                .map(c -> {
                    try {
                        return ((IFeature) c.getMethod("getInstance").invoke(null));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public boolean shouldPauseMacroExecution() {
        return features.stream().anyMatch(feature -> {
            if (feature.isEnabled()) {
                return feature.shouldPauseMacroExecution();
            }
            return false;
        });
    }

    public void disableAll() {
        features.forEach(feature -> {
            if (feature.isActivated() && feature.isEnabled()) {
                feature.stop();
                LogUtils.sendDebug("Disabled feature: " + feature.getName());
            }
        });
    }
}