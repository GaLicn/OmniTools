package com.omnitools.omniTools.core;

/**
 * 定义 omni 工具的所有可能模式
 * 后续可以轻松扩展添加新模式
 */
public enum ToolMode {
    WRENCH("wrench", "普通扳手模式"),
    ME_BEAM_FORMER("me_beam_former", "ME光束绑定器模式");

    private final String id;
    private final String displayName;

    ToolMode(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
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
