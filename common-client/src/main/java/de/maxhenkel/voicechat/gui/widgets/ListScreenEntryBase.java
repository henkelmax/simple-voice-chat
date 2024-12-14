package de.maxhenkel.voicechat.gui.widgets;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.list.AbstractOptionList;

import java.util.List;

public abstract class ListScreenEntryBase<T extends AbstractOptionList.Entry<T>> extends AbstractOptionList.Entry<T> {

    protected final List<IGuiEventListener> children;

    public ListScreenEntryBase() {
        this.children = Lists.newArrayList();
    }

    @Override
    public List<? extends IGuiEventListener> children() {
        return children;
    }

}
