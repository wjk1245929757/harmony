package com.example.toolkit.util;

import ohos.aafwk.ability.AbilitySlice;
import ohos.agp.components.*;

import java.util.ArrayList;
import java.util.List;

public class PageProvider extends PageSliderProvider {
    private List<Component> layoutFilesIds;//存放布局文件的ID值

    public PageProvider(List<Component> pageslist) {
        this.layoutFilesIds = pageslist;
    }

    @Override
    public int getCount() {
        return layoutFilesIds.size();
    }

    @Override
    public Object createPageInContainer(ComponentContainer componentContainer, int i) {
        componentContainer.addComponent(layoutFilesIds.get(i));
        return layoutFilesIds.get(i);
    }

    @Override
    public void destroyPageFromContainer(ComponentContainer componentContainer, int i, Object o) {
        componentContainer.removeComponent((Component) o);
    }

    @Override
    public boolean isPageMatchToObject(Component component, Object o) {
        return component == o;
    }
}
