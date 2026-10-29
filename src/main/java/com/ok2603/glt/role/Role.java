package com.ok2603.glt.role;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Who a player is playing as. NONE means they haven't picked yet. */
public enum Role implements StringRepresentable {
    NONE("none"),
    CHITO("chito"),
    YUURI("yuuri");

    public static final Codec<Role> CODEC = StringRepresentable.fromEnum(Role::values);

    private final String name;

    Role(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static Role byId(int id) {
        Role[] roles = values();
        return id >= 0 && id < roles.length ? roles[id] : NONE;
    }
}
