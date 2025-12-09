package com.omnitools.omniTools.core;

/**
 * 定义 omni 工具的所有可能模式
 * 后续可以轻松扩展添加新模式
 */
public enum ToolMode {
    WRENCH("wrench", "toolmode.omnitools.wrench"),
    LINK("link", "toolmode.omnitools.link"),
    RENAME("rename", "toolmode.omnitools.rename");

    private final String id;
    private final String translationKey;

    ToolMode(String id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static ToolMode fromId(String id) {
        for (ToolMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }
        return WRENCH; // 默认回到普通模式
    }
}
